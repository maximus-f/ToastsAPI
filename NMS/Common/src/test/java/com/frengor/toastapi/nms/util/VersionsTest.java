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
                Map.entry("1.21.4", "v1_21_R3"), Map.entry("1.21.5", "v1_21_R4"));
        revisions.forEach((release, revision) -> {
            assertEquals(release, Optional.of(revision), Versions.getNMSVersion(release));
            assertEquals(release, Optional.of(revision), Versions.getNMSVersion(release + "-R0.1-SNAPSHOT"));
        });
    }

    @Test
    public void rejectsUnbundledVersionsFutureReleasesAndMalformedInput() {
        for (String release : new String[]{null, "", "unknown", "1", "1.20.x", "1.20.0", "1.20.4.1",
                "1.15.2", "1.18.2", "1.19.3", "1.19.5", "1.20.7", "1.21.6", "1.21.99", "1.22",
                "26.1.2", "1.21.5-pre1", "1.21.5-rc1", "1.21.5-pre1-R0.1-SNAPSHOT", "1.21.5-custom"}) {
            assertTrue(String.valueOf(release), Versions.getNMSVersion(release).isEmpty());
        }
    }
}
