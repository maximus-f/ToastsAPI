# Changelog

## 1.2.0

- Restore Minecraft 1.15–1.15.2, 1.16–1.16.5, 1.17–1.17.1 and 1.18–1.18.2 adapters from UAA 2.8.2 (`dfbcbb4`).
- Support Java 8 for the shared library, test plugin and 1.15/1.16 adapters; Java 16 for 1.17; and Java 17 for 1.18.
- Include the restored adapters in both NMS distributions and explicitly select their supported releases.
- Add shared packet serialization tests for all seven restored adapters, including rich text and partial progress.
- Fix BuildTools and isolated-build handling for the plain server jars used before 1.17.
- Retain existing 1.19.4–26.3 support. Minecraft 1.19–1.19.3 remain unsupported.

## 1.1.0

- Add Minecraft 1.21.6–1.21.11, 26.1–26.1.2, 26.2 and 26.3 adapters.
- Centralize the release version and verify the library's published Maven metadata.

## 1.0.1

- Repair the multi-module build and separate the shadeable library from the test plugin.

Upstream UAA changes are tracked separately in the [UAA changelog](https://modrinth.com/plugin/ultimateadvancementapi/changelog).
