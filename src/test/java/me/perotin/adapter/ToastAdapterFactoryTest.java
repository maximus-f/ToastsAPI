package me.perotin.adapter;

import org.junit.jupiter.api.Test;
import java.util.logging.Logger;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the ToastAdapterFactory class.
 */
public class ToastAdapterFactoryTest {

    /**
     * Test that the factory returns a valid adapter for a supported version.
     */
    @Test
    public void testCreateAdapter_SupportedVersion() {
        // Create a factory that returns a supported version
        ToastAdapterFactory factory = new TestToastAdapterFactory("v1_20_R3");
        
        // Create an adapter
        ToastAdapter adapter = factory.createAdapter();
        
        // Verify that the adapter is not null
        assertNotNull(adapter, "Adapter should not be null for supported version");
        
        // Verify that the adapter is of the correct type
        assertEquals("me.perotin.adapter.v1_20_R3.ToastAdapter_v1_20_R3", adapter.getClass().getName(),
                "Adapter should be of the correct type");
    }

    /**
     * Test that the factory returns null for an unsupported version.
     */
    @Test
    public void testCreateAdapter_UnsupportedVersion() {
        // Create a factory that returns an unsupported version
        ToastAdapterFactory factory = new TestToastAdapterFactory("v1_15_R1");
        
        // Create an adapter
        ToastAdapter adapter = factory.createAdapter();
        
        // Verify that the adapter is null
        assertNull(adapter, "Adapter should be null for unsupported version");
    }

    /**
     * A test implementation of ToastAdapterFactory that returns a specific version.
     */
    private static class TestToastAdapterFactory extends ToastAdapterFactory {
        private final String version;

        /**
         * Creates a new TestToastAdapterFactory.
         *
         * @param version The version to return
         */
        public TestToastAdapterFactory(String version) {
            super(Logger.getLogger("ToastAdapterFactoryTest"));
            this.version = version;
        }

        @Override
        String getServerVersion() {
            return version;
        }
    }
}