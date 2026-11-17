# ToastsAPI

A small Minecraft toast notification library extracted from [UltimateAdvancementAPI](https://github.com/frengor/UltimateAdvancementAPI). Shade it into a Bukkit/Spigot plugin to display a toast with an item icon, title, and task/goal/challenge frame. It does not require the UAA plugin, a database, or an advancement progression system.

The public Java package and Maven coordinates remain `com.frengor.toastapi` and `com.frengor:toastapi` for compatibility.

## Artifacts

| Module | Output | Purpose |
| --- | --- | --- |
| `ToastAPI` | `ToastAPI/target/toastapi-1.3.0.jar` | Shadeable library with all supported adapters. No `plugin.yml`, commands, or plugin entry point. |
| `TestPlugin` | `TestPlugin/target/toastapi-test-plugin-1.3.0.jar` | Standalone `/toast` test plugin that embeds and relocates the library. |
| `NMS` | Internal adapter and distribution jars | Build inputs; do not install these in a server's plugins folder. |

The library JAR is approximately **297 KiB** with all 24 adapters (about 17% smaller than the previous 358 KiB build). Its contents are the public toast API, shared runtime helpers, version adapters, its own Maven metadata and manifest, and the `LICENSE`, `LGPL`, and `NOTICE` files. The `/toast` command and `JavaPlugin` entry point belong only to `TestPlugin`.

Packaging removes internal adapter Maven metadata and redundant ZIP directory entries, then compresses the final library and test-plugin JARs at level 9. This runs automatically during `package`, including IntelliJ's **Build JARs** configuration. Adapter classes are retained explicitly; do not enable Shade's `minimizeJar`, because adapters load through reflection. Packaging tests enforce a 320 KiB library budget and check all 24 adapters and the required resources.

Unused prepared-advancement wrappers, reset-all-advancements packets, and internal parent/display accessors have been removed. The public `ToastAPI` and `AdvancementFrameType` API is unchanged; code that directly used the internal `com.frengor.toastapi.nms` wrappers may need updating. No telemetry is bundled.

## Supported releases

Only these release versions are accepted. Future patches and prereleases are rejected rather than assigned an older adapter.

| Minecraft | Adapter | Runtime Java minimum |
| --- | --- | --- |
| 1.15, 1.15.1, 1.15.2 | `v1_15_R1` | 8 |
| 1.16, 1.16.1 | `v1_16_R1` | 8 |
| 1.16.2, 1.16.3 | `v1_16_R2` | 8 |
| 1.16.4, 1.16.5 | `v1_16_R3` | 8 |
| 1.17, 1.17.1 | `v1_17_R1` | 16 |
| 1.18, 1.18.1 | `v1_18_R1` | 17 |
| 1.18.2 | `v1_18_R2` | 17 |
| 1.19, 1.19.1, 1.19.2 | `v1_19_R1` | 17 |
| 1.19.3 | `v1_19_R2` | 17 |
| 1.19.4 | `v1_19_R3` | 17 |
| 1.20, 1.20.1 | `v1_20_R1` | 17 |
| 1.20.2 | `v1_20_R2` | 17 |
| 1.20.3, 1.20.4 | `v1_20_R3` | 17 |
| 1.20.5, 1.20.6 | `v1_20_R4` | 21 |
| 1.21, 1.21.1 | `v1_21_R1` | 21 |
| 1.21.2, 1.21.3 | `v1_21_R2` | 21 |
| 1.21.4 | `v1_21_R3` | 21 |
| 1.21.5 | `v1_21_R4` | 21 |
| 1.21.6, 1.21.7, 1.21.8 | `v1_21_R5` | 21 |
| 1.21.9, 1.21.10 | `v1_21_R6` | 21 |
| 1.21.11 | `v1_21_R7` | 21 |
| 26.1, 26.1.1, 26.1.2 | `v26_1_R2` | 25 |
| 26.2 | `v26_2_R1` | 25 |
| 26.3 | `v26_3_R1` | 25 |

These are adapter targets, not a claim that every server/client combination has been tested. All listed adapters are built and packaged, including 1.19–1.19.3, which closes the previous gap. Server forks may require a newer Java version than the minimum shown.

Version detection accepts Spigot's `-R0.1-SNAPSHOT` suffix and Paper's newer `.build.<number>-<channel>` / `.local-SNAPSHOT` suffixes. Minecraft prereleases and snapshots remain unsupported.

The public library is intended for a conventional Bukkit plugin with `plugin.yml`. Its adapters through 1.21.11 use **Spigot mappings**; Paper 1.20.5–1.21.11 remaps those Bukkit plugins when loading them. Minecraft 26.x ships unobfuscated, so its adapters use the native names and skip remapping. See [Paper's mapping documentation](https://docs.papermc.io/paper/dev/userdev/).

For a host plugin that also supports releases before 26.1, do not mark the host as Mojang-mapped when embedding this public jar. The internal `NMS/DistributionMojangMapped` artifact is not a complete public library. Native Mojang-mapped hosts on older releases and Folia are not covered by this integration. Future 26.x releases and snapshots are rejected until an adapter has been checked and explicitly added.

## Build

Requirements:

- Maven 3.9 or newer.
- JDK 25 or newer **running Maven** (`mvn -version` shows the selected JDK). Shared library classes and the test plugin target Java 8. The 1.15/1.16 adapters target Java 8, 1.17 targets Java 16, 1.18–1.20.4 target Java 17, and newer adapters target Java 21, including the 26.x adapters. Keeping their bytecode at Java 21 lets older supported Paper remappers read the combined jar. The 26.x adapters still require a Java 25 server at runtime.
- Local Spigot BuildTools outputs for **1.15.2, 1.16.1, 1.16.3, 1.16.5, 1.17.1, 1.18.1, 1.18.2, 1.19.2, 1.19.3, 1.19.4, 1.20.1, 1.20.2, 1.20.4, 1.20.6, 1.21.1, 1.21.3, 1.21.4, 1.21.5, 1.21.8, 1.21.10, 1.21.11, 26.1.2, 26.2, and 26.3**. The full Spigot server jars cannot be downloaded from a public Maven repository.

Run [Spigot BuildTools](https://www.spigotmc.org/wiki/buildtools/) with Java 8 for 1.15/1.16, Java 16 for 1.17.1, Java 17 for 1.18–1.20.4, Java 21 for 1.20.6 and 1.21.x, and Java 25 for 26.x. The helper uses `JAVA_HOME/bin/java` when set, otherwise `java` on your PATH. It refreshes BuildTools and requests `--remapped` only for 1.17–1.21.x; 1.15/1.16 use plain versioned server jars, and 26.x is unobfuscated:

```sh
# With Java 8 selected:
bash install_spigot_jars.sh 1.15.2 1.16.1 1.16.3 1.16.5

# With Java 16 selected:
bash install_spigot_jars.sh 1.17.1

# With Java 17 selected:
bash install_spigot_jars.sh 1.18.1 1.18.2 1.19.2 1.19.3 1.19.4 1.20.1 1.20.2 1.20.4

# With Java 21 selected:
bash install_spigot_jars.sh 1.20.6 1.21.1 1.21.3 1.21.4 1.21.5 1.21.8 1.21.10 1.21.11

# With Java 25 selected:
bash install_spigot_jars.sh 26.1.2 26.2 26.3
```

BuildTools installs server jars, API and parent POMs, plus remapped Mojang/obfuscated jars and mapping tables for 1.17–1.21.x. The 1.15/1.16 builds do not produce separate mapping artifacts. Preserve those POMs; manually installing only a jar is insufficient. Set `TOASTS_BUILD_DIR` to change the helper's working directory (default: `~/spigot-builds`).

Then, with Maven running on JDK 25 or newer:

```sh
mvn clean verify
```

`verify` runs version-selection tests, constructs and encodes/decodes real advancement packets for all three toast styles against **all 24 adapter targets**, and checks the final library and test-plugin jars, including reflective adapter discovery after relocation. The adapter tests bootstrap Minecraft registries without starting a server or opening network ports. They do not verify client rendering or every patch release in each adapter group.

To install the library for another local Maven project:

```sh
mvn clean install
```

The release version is defined once as `<revision>1.3.0</revision>` in the root `pom.xml`. Every module inherits it through `${revision}`; dependencies between modules use `${project.version}`. To bump the release, change that one property. For a temporary build version:

```sh
mvn -Drevision=1.3.1-SNAPSHOT clean install
```

The [Flatten Maven Plugin](https://www.mojohaus.org/flatten-maven-plugin/) resolves the version before packaging, so installed/published POMs contain concrete parent versions and work in other projects. Generated `.flattened-pom.xml` files are ignored by Git and removed by `mvn clean`. This follows [Maven's CI-friendly version setup](https://maven.apache.org/guides/mini/guide-maven-ci-friendly.html).

The separate `mc-version` property in each adapter selects its Spigot compile target; it is independent of the ToastsAPI release version. Maven Enforcer checks that module versions and parent references agree. No machine-specific `toolchains.xml` is needed.

### Verify without old ToastsAPI artifacts

After preparing the BuildTools prerequisites:

```sh
python3 scripts/verify_clean_build.py
```

This creates an isolated Maven repository, copies **only** the required Spigot server/mapping/parent outputs, and runs `mvn clean verify`. It copies no ToastsAPI or UAA artifacts, no Maven plugins, and no other dependency caches; public dependencies are downloaded again. The repository and its seed manifest remain available for inspection.

Optional arguments:

```sh
python3 scripts/verify_clean_build.py \
  --buildtools-repository /path/to/buildtools/maven-repository \
  --repository /path/to/empty/verification-repository
```

### Automated server and bot tests

In IntelliJ's run-configuration selector:

- **Build JARs** runs the ordinary `clean verify` build and all adapter tests.
- **Test Servers** builds and verifies the JARs, then tests Paper **1.19.4, 1.20.6, and 1.21.11**, sequentially. This is the shorter daily check across different protocol and plugin-remapping generations.
- **Test All Servers** attempts one Paper release per adapter (the 24 compile targets). This is the slower release check. It **fails overall if any target fails or is unsupported**, so incomplete coverage cannot appear green.

These shared configurations live in `.run/`. Reopen the project if IntelliJ has not loaded them. They use IntelliJ's bundled Java (25+ required). The server-tests profile runs only in the final `TestPlugin` module, after the normal packaging checks. Ordinary builds never launch a server.

The runner downloads official Paper builds through the [Paper downloads service](https://docs.papermc.io/misc/downloads-service/), verifies their SHA-256 checksums, and caches the selected build metadata. It prefers stable builds; if a release has only development builds (currently 1.21.5), it uses the latest published build for the disposable test server and records its channel in the report. Each test uses a fresh world, a local-only port, offline authentication, and an operator bot. It checks plugin enablement, runs `/toast` for task/goal/challenge, and asserts the received title, frame, item icon/count, toast flag, parent, completed criteria, and subsequent removal packets. Startup failures, bot timeouts/disconnects, assertion failures, and server error logs fail the run. The runner stops each server before starting the next.

The bot uses a pinned [minecraft-protocol](https://github.com/PrismarineJS/node-minecraft-protocol) dependency and an included npm lockfile. **26.2 and 26.3 currently lack protocol data** in that dependency and are reported as `UNSUPPORTED`; their Java packet tests still run. Patch-version aliases are accepted only when the protocol data explicitly records the same protocol number. A missing Paper download or Java installation is reported as a failure, not silently skipped. This matrix tests Paper; it does not claim coverage of every Spigot/fork/patch combination.

Results are under `target/server-tests/<timestamp>/`:

- `summary.md`: readable pass/fail table.
- `summary.json`: exact server build and channel, server/plugin hashes, assertions, and failure details.
- `junit.xml`: CI test results, with unsupported targets marked skipped.
- `<version>/server.log`, `bot.log`, and `bot.json`: per-version evidence.

The report survives ordinary `verify` builds; `clean` removes `target/`. Downloaded servers, local toolchains, and consent settings live in the Git-ignored `.test-tools/` directory. Delete a cached version's `servers/paper/<version>/build.json` to deliberately select a newer Paper build next time.

#### First-time setup on another machine

Python 3.9+, Node.js 22+ with npm, and the Java versions listed in the Build section are required. On macOS/Linux, the runner can download a checksum-verified Node 22.14.0 runtime into `.test-tools/node` if Node is absent. Java is selected from `JAVA_8_HOME`, `JAVA_16_HOME`, `JAVA_17_HOME`, `JAVA_21_HOME`, and `JAVA_25_HOME`, or the local settings below. Java 25 can also come from Maven's runtime for 26.x servers.

Explicitly accept the [Minecraft EULA](https://aka.ms/MinecraftEULA) before launching test servers. Create `.test-tools/server-tests-local.json` locally (do not commit consent or machine paths):

```json
{
  "acceptEula": true,
  "javaHomes": {
    "8": "/path/to/jdk8",
    "16": "/path/to/jdk16",
    "17": "/path/to/jdk17",
    "21": "/path/to/jdk21"
  }
}
```

Only set `acceptEula` to `true` if you accept it. For the daily smoke suite, only the Java 17 and 21 server toolchains are needed, in addition to Java 25 running Maven. The runner's Python executable can be overridden with `-DserverTests.python=/path/to/python3` if IntelliJ cannot find it.

Equivalent Maven commands, or values for a custom IntelliJ Maven configuration:

```sh
mvn verify -Pserver-tests
mvn verify -Pserver-tests -DserverTests.versions=1.19.4,1.20.4
mvn verify -Pserver-tests -DserverTests.versions=all
```

To test executable Spigot or other server JARs you have prepared yourself, run the Python runner with `--server-dir /path/to/jars`; it expects filenames such as `1.19.4.jar`. These must be runnable distributions with their dependencies, not the thin Maven compile artifacts:

```sh
python3 scripts/server-tests/run.py \
  --plugin TestPlugin/target/toastapi-test-plugin-1.3.0.jar \
  --versions 1.19.4 --server-dir /path/to/jars
```

Runner failure-handling tests: `python3 scripts/server-tests/test_runner.py`.

**This verifies delivery and packet contents, not the rendered toast.** Use a real matching Minecraft client for visual checks of icons, formatting, animation, and timing.

## Embed the library

After `mvn clean install`, add this dependency to your plugin:

```xml
<dependency>
    <groupId>com.frengor</groupId>
    <artifactId>toastapi</artifactId>
    <version>1.3.0</version>
</dependency>
```

Shade and relocate it into your plugin. Keep your own Bukkit/Paper API dependency `provided` and replace the example destination package:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-shade-plugin</artifactId>
    <version>3.6.2</version>
    <executions>
        <execution>
            <phase>package</phase>
            <goals><goal>shade</goal></goals>
        </execution>
    </executions>
    <configuration>
        <createDependencyReducedPom>false</createDependencyReducedPom>
        <!-- Adapters are loaded reflectively; minimization can remove them. -->
        <minimizeJar>false</minimizeJar>
        <artifactSet>
            <includes><include>com.frengor:toastapi</include></includes>
        </artifactSet>
        <relocations>
            <relocation>
                <pattern>com.frengor.toastapi</pattern>
                <shadedPattern>your.plugin.libs.toastapi</shadedPattern>
            </relocation>
        </relocations>
    </configuration>
</plugin>
```

If you shade additional libraries, extend `artifactSet` to include them too. Do not add `depend: [ToastAPI]`: your plugin contains the library. The test plugin is not a runtime dependency.

### Java usage

Call from your plugin after the server has initialized, on the server thread:

```java
import com.frengor.toastapi.AdvancementFrameType;
import com.frengor.toastapi.ToastAPI;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

if (ToastAPI.isSupported()) {
    ToastAPI.displayCustomToast(
        player,
        new ItemStack(Material.DIAMOND),
        "Achievement unlocked!",
        AdvancementFrameType.GOAL
    );
}
```

`isSupported()` checks whether an adapter is included for the server release. `displayCustomToast` rejects unsupported releases and invalid arguments; reflection/linkage failures are reported to the caller. ItemStack icons retain their item metadata. No `getInstance()` call is required.

## Test plugin

Copy **only** `TestPlugin/target/toastapi-test-plugin-1.3.0.jar` into a supported server's `plugins/` directory and restart it. In game:

```text
/toast task Quest completed!
/toast goal Achievement unlocked!
/toast challenge Boss defeated!
```

The command accepts players only. Permission `toastapi.toast` defaults to operators; `toastapi.*` includes it. No join notification is registered. The test plugin disables itself on unsupported releases.

## Attribution and license

The restored adapters for 1.15–1.19.3 and the adapters for 1.21.6–26.3 are adapted from UAA 2.8.2, commit [`dfbcbb4`](https://github.com/frengor/UltimateAdvancementAPI/tree/dfbcbb4c6988fe570eb63454833b65f0ea524ffa). Only the identifier, display, advancement, frame, packet and supporting utility classes are included. The restored 1.15/1.16 code is adjusted for Java 8. Version selection uses the CraftBukkit package revisions in actual Spigot jars: 1.16.2/1.16.3 use R2, and 1.16.4/1.16.5 use R3; this corrects the boundaries in that upstream version table.

Derived from [UltimateAdvancementAPI](https://github.com/frengor/UltimateAdvancementAPI) by fren_gor and EscanorTargaryen, particularly its NMS wrappers and toast implementation. This project declares **LGPL-3.0-or-later** in its Maven metadata. See [LICENSE](LICENSE), [LGPL](LGPL), and [NOTICE](NOTICE); these notices are also included in the distributed library.
