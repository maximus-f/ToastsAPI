// Real protocol client: validates packets, not Minecraft's visual rendering.
const assert = require('node:assert/strict');
const fs = require('node:fs');
const mc = require('minecraft-protocol');
const data = require('minecraft-data');
const nbt = require('prismarine-nbt');

if (process.argv[2] === '--supports') {
  const version = process.argv[3];
  const d = data(version);
  // Patch aliases are valid only when the data explicitly records the same protocol.
  const release = data.versions.pc.find(v => v.minecraftVersion === version);
  if (!d || !release || release.version !== d.version.version) process.exit(2);
  try { mc.createDeserializer({ state: 'play', isServer: false, version }); }
  catch (_) { process.exit(2); }
  process.exit(0);
}

const [version, port, output] = process.argv.slice(2);
const d = data(version);
const root = 'com.frengor:root';
const notification = 'com.frengor:notification';
const cases = [
  { style: 'task', frame: 0, icon: 'grass_block' },
  { style: 'goal', frame: 2, icon: 'diamond' },
  { style: 'challenge', frame: 1, icon: 'dragon_head' }
];
// Vanilla frame enum order is TASK, CHALLENGE, GOAL.
let index = 0;
let added = false;
let finished = false;
let started = false;
let timeout;
const results = [];
const client = mc.createClient({ host: '127.0.0.1', port: Number(port),
  username: 'ToastTestBot', auth: 'offline', version, hideErrors: true });

function finish(error) {
  if (finished) return;
  finished = true;
  clearTimeout(timeout);
  fs.writeFileSync(output, JSON.stringify({ version, status: error ? 'FAIL' : 'PASS',
    cases: results, error: error?.stack }, null, 2));
  if (error) console.error(error.stack || error);
  client.end();
  setTimeout(() => process.exit(error ? 1 : 0), 100);
}

function text(component) {
  if (component && typeof component === 'object' && component.type) component = nbt.simplify(component);
  if (typeof component === 'string') {
    try { return text(JSON.parse(component)); } catch (_) { return component; }
  }
  if (Array.isArray(component)) return component.map(text).join('');
  return (component?.text || '') + (component?.extra || []).map(text).join('');
}

function next() {
  if (index === cases.length) return finish();
  added = false;
  const c = cases[index];
  c.title = `ToastsAPI_${c.style}_integration`;
  clearTimeout(timeout);
  timeout = setTimeout(() => finish(new Error(`Timed out waiting for ${c.style} add/remove packets`)), 20000);
  client.chat(`/toast ${c.style} ${c.title}`);
}

client.on('error', finish);
client.on('end', reason => { if (!finished) finish(new Error(`Disconnected: ${reason}`)); });
client.on('disconnect', packet => finish(new Error(`Kicked: ${JSON.stringify(packet)}`)));
client.on('kick_disconnect', packet => finish(new Error(`Kicked: ${JSON.stringify(packet)}`)));
client.on('position', packet => {
  if (packet.teleportId !== undefined) client.write('teleport_confirm', { teleportId: packet.teleportId });
  if (!started) {
    started = true;
    setTimeout(next, 1500);
  }
});
timeout = setTimeout(() => finish(new Error('Bot did not spawn within 45 seconds')), 45000);

client.on('advancements', packet => {
  if (finished || !started || index >= cases.length) return;
  try {
    const entry = packet.advancementMapping.find(x => x.key === notification);
    if (entry) {
      const c = cases[index];
      assert.equal(added, false, 'Unexpected duplicate toast packet');
      assert.equal(packet.reset, false, 'Toast must not reset existing advancements');
      assert.ok(packet.advancementMapping.some(x => x.key === root), 'Root is missing');
      assert.equal(entry.value.parentId, root);
      const display = entry.value.displayData;
      assert.ok(display, 'Display is missing');
      assert.equal(text(display.title).replace(/§./g, ''), c.title);
      assert.equal(display.frameType, c.frame, 'Wrong frame');
      assert.equal(display.flags.show_toast, 1, 'Toast flag is off');
      assert.equal(display.flags.hidden, 0);
      assert.equal(display.icon.itemId, d.itemsByName[c.icon].id, 'Wrong item icon');
      const count = display.icon.itemCount ?? display.icon.count;
      assert.equal(count, 1, 'Wrong icon count');
      for (const key of [root, notification]) {
        const progress = packet.progressMapping.find(x => x.key === key);
        assert.ok(progress, `Missing progress for ${key}`);
        assert.ok(progress.value.some(x => x.criterionIdentifier === '0' && x.criterionProgress != null),
          `Criterion not completed for ${key}`);
      }
      assert.deepEqual(entry.value.requirements, [['0']]);
      assert.equal(packet.identifiers.length, 0);
      added = true;
    }
    if (packet.identifiers.includes(notification)) {
      assert.ok(added, 'Removal arrived without a validated toast');
      assert.equal(packet.reset, false);
      assert.ok(packet.identifiers.includes(root), 'Root was not removed');
      assert.equal(packet.advancementMapping.length, 0);
      assert.equal(packet.progressMapping.length, 0);
      results.push({ ...cases[index], status: 'PASS' });
      index++;
      clearTimeout(timeout);
      setTimeout(next, 250);
    }
  } catch (error) { finish(error); }
});
