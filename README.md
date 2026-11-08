# ToastsAPI

A small Minecraft toast notification library extracted from [UltimateAdvancementAPI](https://github.com/frengor/UltimateAdvancementAPI). Shade it into a Bukkit/Spigot plugin to display a toast with an item icon, title, and task/goal/challenge frame. It does not require the UAA plugin, a database, or an advancement progression system.

The public Java package and Maven coordinates remain `com.frengor.toastapi` and `com.frengor:toastapi` for compatibility.

## Artifacts

| Module | Output | Purpose |
| --- | --- | --- |
| `ToastAPI` | `ToastAPI/target/toastapi-1.0.1.jar` | Shadeable library with all supported adapters. No `plugin.yml`, commands, or plugin entry point. |
| `TestPlugin` | `TestPlugin/target/toastapi-test-plugin-1.0.1.jar` | Standalone `/toast` test plugin that embeds and relocates the library. |
| `NMS` | Internal adapter and distribution jars | Build inputs; do not install these in a server's plugins folder. |

## Supported releases

Only these release versions are accepted. Future patches and prereleases are rejected rather than assigned an older adapter.

| Minecraft | Adapter | Runtime Java minimum |
| --- | --- | --- |
| 1.19.4 | `v1_19_R3` | 17 |
| 1.20, 1.20.1 | `v1_20_R1` | 17 |
| 1.20.2 | `v1_20_R2` | 17 |
| 1.20.3, 1.20.4 | `v1_20_R3` | 17 |
| 1.20.5, 1.20.6 | `v1_20_R4` | 21 |
| 1.21, 1.21.1 | `v1_21_R1` | 21 |
| 1.21.2, 1.21.3 | `v1_21_R2` | 21 |
| 1.21.4 | `v1_21_R3` | 21 |
| 1.21.5 | `v1_21_R4` | 21 |

These are adapter targets, not a claim that every server/client combination has been tested. Historical 1.15–1.19.3 sources remain in the repository but are excluded from the build and distribution.

The public library uses **Spigot mappings** and is intended for a conventional Bukkit plugin with `plugin.yml`. On supported Paper versions from 1.20.5, Paper normally remaps Spigot-mapped Bukkit plugins when loading them; see [Paper's mapping documentation](https://docs.papermc.io/paper/dev/userdev/#default-mappings-assumption). Do not combine this jar with a host plugin marked as Mojang-mapped. The internal `NMS/DistributionMojangMapped` artifact is not a complete public library. Native Mojang-mapped host plugins and Folia are not covered by this integration.

## Build

Requirements:

- Maven 3.9 or newer.
- JDK 21 or newer **running Maven** (`mvn -version` shows the selected JDK). Shared classes target Java 17; newer adapters target Java 21.
- Local Spigot BuildTools outputs for **1.19.4, 1.20.1, 1.20.2, 1.20.4, 1.20.6, 1.21.1, 1.21.3, 1.21.4, and 1.21.5**. The full Spigot server jars cannot be downloaded from a public Maven repository.

Run [Spigot BuildTools](https://www.spigotmc.org/wiki/buildtools/) with `--remapped` for each revision. Use a JDK supported by that Minecraft revision: Java 17 through 1.20.4, and Java 21 for 1.20.6 and 1.21.x. The helper uses `java` on your PATH:

```sh
# With Java 17 selected:
bash install_spigot_jars.sh 1.19.4 1.20.1 1.20.2 1.20.4

# With Java 21 selected:
bash install_spigot_jars.sh 1.20.6 1.21.1 1.21.3 1.21.4 1.21.5
```

BuildTools installs the remapped Mojang/obfuscated jars, API, parent POMs and mapping tables. Preserve those POMs; manually installing only a jar is insufficient. Set `TOASTS_BUILD_DIR` to change the helper's working directory (default: `~/spigot-builds`).

Then, with Maven running on JDK 21 or newer:

```sh
mvn clean verify
```

`verify` runs version-selection tests and checks the final library and test-plugin jars, including reflective adapter discovery after relocation. It does not start a Minecraft server or verify client rendering.

To install the library for another local Maven project:

```sh
mvn clean install
```

All project modules use `1.0.1`. Maven Enforcer checks that module versions and parent references agree. No machine-specific `toolchains.xml` is needed.

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
    <version>1.0.1</version>
</dependency>
```

Shade and relocate it into your plugin. Keep your own Bukkit/Paper API dependency `provided` and replace the example destination package:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-shade-plugin</artifactId>
    <version>3.6.1</version>
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

Copy **only** `TestPlugin/target/toastapi-test-plugin-1.0.1.jar` into a supported server's `plugins/` directory and restart it. In game:

```text
/toast task Quest completed!
/toast goal Achievement unlocked!
/toast challenge Boss defeated!
```

The command accepts players only. Permission `toastapi.toast` defaults to operators; `toastapi.*` includes it. No join notification is registered. The test plugin disables itself on unsupported releases.

## Attribution and license

Derived from [UltimateAdvancementAPI](https://github.com/frengor/UltimateAdvancementAPI) by fren_gor and EscanorTargaryen, particularly its NMS wrappers and toast implementation. This project declares **LGPL-3.0-or-later** in its Maven metadata. See [LICENSE](LICENSE), [LGPL](LGPL), and [NOTICE](NOTICE); these notices are also included in the distributed library.
