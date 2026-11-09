package com.frengor.toastapi.nms.util;

import org.junit.Test;

import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class VersionsTest {
    @Test
    public void resolvesEverySupportedReleaseAndBukkitSuffix() {
        Map<String, String> revisions = Map.ofEntries(
                Map.entry("1.19.4", "v1_19_R3"),
                Map.entry("1.20", "v1_20_R1"), Map.entry("1.20.1", "v1_20_R1"),
                Map.entry("1.20.2", "v1_20_R2"),
                Map.entry("1.20.3", "v1_20_R3"), Map.entry("1.20.4", "v1_20_R3"),
                Map.entry("1.20.5", "v1_20_R4"), Map.entry("1.20.6", "v1_20_R4"),
                Map.entry("1.21", "v1_21_R1"), Map.entry("1.21.1", "v1_21_R1"),
                Map.entry("1.21.2", "v1_21_R2"), Map.entry("1.21.3", "v1_21_R2"),
                Map.entry("1.21.4", "v1_21_R3"), Map.entry("1.21.5", "v1_21_R4"),
                Map.entry("1.21.6", "v1_21_R5"), Map.entry("1.21.7", "v1_21_R5"), Map.entry("1.21.8", "v1_21_R5"),
                Map.entry("1.21.9", "v1_21_R6"), Map.entry("1.21.10", "v1_21_R6"), Map.entry("1.21.11", "v1_21_R7"),
                Map.entry("26.1", "v26_1_R2"), Map.entry("26.1.1", "v26_1_R2"), Map.entry("26.1.2", "v26_1_R2"),
                Map.entry("26.2", "v26_2_R1"), Map.entry("26.3", "v26_3_R1"));
        revisions.forEach((release, revision) -> {
            assertEquals(release, Optional.of(revision), Versions.getNMSVersion(release));
            assertEquals(release, Optional.of(revision), Versions.getNMSVersion(release + "-R0.1-SNAPSHOT"));
            assertEquals(release, Optional.of(revision), Versions.getNMSVersion(release + "-R0.2-SNAPSHOT"));
            assertEquals(release, Optional.of(revision), Versions.getNMSVersion(release + ".build.10-stable"));
            assertEquals(release, Optional.of(revision), Versions.getNMSVersion(release + ".build.1-beta"));
            assertEquals(release, Optional.of(revision), Versions.getNMSVersion(release + ".local-SNAPSHOT"));
        });
    }

    @Test
    public void rejectsUnbundledVersionsFutureReleasesAndMalformedInput() {
        for (String release : new String[]{null, "", "unknown", "1", "1.20.x", "1.20.0", "1.20.4.1",
                "1.15.2", "1.18.2", "1.19.3", "1.19.5", "1.20.7", "1.21.12", "1.21.99", "1.22",
                "26", "26.0", "26.1.0", "26.1.3", "26.2.1", "26.3.1", "26.4", "27.1",
                "1.21.5-pre1", "1.21.5-rc1", "1.21.5-pre1-R0.1-SNAPSHOT", "1.21.5-custom",
                "26.3-pre1", "26.3-rc1", "26.3-pre1-R0.1-SNAPSHOT", "26.4-snapshot-7",
                "26.3-pre1.build.1-alpha", "26.3-rc1.local-SNAPSHOT", "26.4.build.1-stable",
                "26.3.build.bad-stable", "26.3.build.1", "26.3.build.1-stable-extra"}) {
            assertTrue(String.valueOf(release), Versions.getNMSVersion(release).isEmpty());
        }
    }
}
