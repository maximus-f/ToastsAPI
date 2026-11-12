# ToastsAPI

A small Minecraft toast notification library extracted from [UltimateAdvancementAPI](https://github.com/frengor/UltimateAdvancementAPI). Shade it into a Bukkit/Spigot plugin to display a toast with an item icon, title, and task/goal/challenge frame. It does not require the UAA plugin, a database, or an advancement progression system.

The public Java package and Maven coordinates remain `com.frengor.toastapi` and `com.frengor:toastapi` for compatibility.

## Artifacts

| Module | Output | Purpose |
| --- | --- | --- |
| `ToastAPI` | `ToastAPI/target/toastapi-1.2.0.jar` | Shadeable library with all supported adapters. No `plugin.yml`, commands, or plugin entry point. |
| `TestPlugin` | `TestPlugin/target/toastapi-test-plugin-1.2.0.jar` | Standalone `/toast` test plugin that embeds and relocates the library. |
| `NMS` | Internal adapter and distribution jars | Build inputs; do not install these in a server's plugins folder. |

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

These are adapter targets, not a claim that every server/client combination has been tested. Minecraft **1.19–1.19.3 remain unsupported**: their historical sources are excluded from the build and distribution. All seven 1.15–1.18 adapter folders are now built and packaged. Server forks may require a newer Java version than the minimum shown.

Version detection accepts Spigot's `-R0.1-SNAPSHOT` suffix and Paper's newer `.build.<number>-<channel>` / `.local-SNAPSHOT` suffixes. Minecraft prereleases and snapshots remain unsupported.

The public library is intended for a conventional Bukkit plugin with `plugin.yml`. Its adapters through 1.21.11 use **Spigot mappings**; Paper 1.20.5–1.21.11 remaps those Bukkit plugins when loading them. Minecraft 26.x ships unobfuscated, so its adapters use the native names and skip remapping. See [Paper's mapping documentation](https://docs.papermc.io/paper/dev/userdev/).

For a host plugin that also supports releases before 26.1, do not mark the host as Mojang-mapped when embedding this public jar. The internal `NMS/DistributionMojangMapped` artifact is not a complete public library. Native Mojang-mapped hosts on older releases and Folia are not covered by this integration. Future 26.x releases and snapshots are rejected until an adapter has been checked and explicitly added.

## Build

Requirements:

- Maven 3.9 or newer.
- JDK 25 or newer **running Maven** (`mvn -version` shows the selected JDK). Shared library classes and the test plugin target Java 8. The 1.15/1.16 adapters target Java 8, 1.17 targets Java 16, 1.18–1.20.4 target Java 17, and newer adapters target Java 21, including the 26.x adapters. Keeping their bytecode at Java 21 lets older supported Paper remappers read the combined jar. The 26.x adapters still require a Java 25 server at runtime.
- Local Spigot BuildTools outputs for **1.15.2, 1.16.1, 1.16.3, 1.16.5, 1.17.1, 1.18.1, 1.18.2, 1.19.4, 1.20.1, 1.20.2, 1.20.4, 1.20.6, 1.21.1, 1.21.3, 1.21.4, 1.21.5, 1.21.8, 1.21.10, 1.21.11, 26.1.2, 26.2, and 26.3**. The full Spigot server jars cannot be downloaded from a public Maven repository.

Run [Spigot BuildTools](https://www.spigotmc.org/wiki/buildtools/) with Java 8 for 1.15/1.16, Java 16 for 1.17.1, Java 17 for 1.18–1.20.4, Java 21 for 1.20.6 and 1.21.x, and Java 25 for 26.x. The helper uses `JAVA_HOME/bin/java` when set, otherwise `java` on your PATH. It refreshes BuildTools and requests `--remapped` only for 1.17–1.21.x; 1.15/1.16 use plain versioned server jars, and 26.x is unobfuscated:

```sh
# With Java 8 selected:
bash install_spigot_jars.sh 1.15.2 1.16.1 1.16.3 1.16.5

# With Java 16 selected:
bash install_spigot_jars.sh 1.17.1

# With Java 17 selected:
bash install_spigot_jars.sh 1.18.1 1.18.2 1.19.4 1.20.1 1.20.2 1.20.4

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

`verify` runs version-selection tests, constructs and encodes/decodes real advancement packets for all three toast styles against all seven 1.15–1.18 adapter targets and the six 1.21.6–26.3 adapter targets, and checks the final library and test-plugin jars, including reflective adapter discovery after relocation. The adapter tests bootstrap Minecraft registries without starting a server or opening network ports. They do not verify client rendering or every patch release in each adapter group.

To install the library for another local Maven project:

```sh
mvn clean install
```

The release version is defined once as `<revision>1.2.0</revision>` in the root `pom.xml`. Every module inherits it through `${revision}`; dependencies between modules use `${project.version}`. To bump the release, change that one property. For a temporary build version:

```sh
mvn -Drevision=1.2.1-SNAPSHOT clean install
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

## Embed the library

After `mvn clean install`, add this dependency to your plugin:

```xml
<dependency>
    <groupId>com.frengor</groupId>
    <artifactId>toastapi</artifactId>
    <version>1.2.0</version>
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

Copy **only** `TestPlugin/target/toastapi-test-plugin-1.2.0.jar` into a supported server's `plugins/` directory and restart it. In game:

```text
/toast task Quest completed!
/toast goal Achievement unlocked!
/toast challenge Boss defeated!
```

The command accepts players only. Permission `toastapi.toast` defaults to operators; `toastapi.*` includes it. No join notification is registered. The test plugin disables itself on unsupported releases.

## Attribution and license

The restored adapters for 1.15–1.18.2 and the adapters for 1.21.6–26.3 are adapted from UAA 2.8.2, commit [`dfbcbb4`](https://github.com/frengor/UltimateAdvancementAPI/tree/dfbcbb4c6988fe570eb63454833b65f0ea524ffa). Only the identifier, display, advancement, frame, packet and supporting utility classes are included. The restored 1.15/1.16 code is adjusted for Java 8. Version selection uses the CraftBukkit package revisions in actual Spigot jars: 1.16.2/1.16.3 use R2, and 1.16.4/1.16.5 use R3; this corrects the boundaries in that upstream version table.

Derived from [UltimateAdvancementAPI](https://github.com/frengor/UltimateAdvancementAPI) by fren_gor and EscanorTargaryen, particularly its NMS wrappers and toast implementation. This project declares **LGPL-3.0-or-later** in its Maven metadata. See [LICENSE](LICENSE), [LGPL](LGPL), and [NOTICE](NOTICE); these notices are also included in the distributed library.
