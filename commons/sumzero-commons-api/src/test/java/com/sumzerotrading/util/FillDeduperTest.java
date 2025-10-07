package com.sumzerotrading.util;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FillDeduperTest {

    private FillDeduper deduper;

    @BeforeEach
    public void setUp() {
        // Create deduper with smaller values for testing
        deduper = new FillDeduper(5, Duration.ofSeconds(15)); // Small cache size and short expiration
    }

    @Test
    public void testDefaultConstructor() {
        FillDeduper defaultDeduper = new FillDeduper();

        assertTrue(defaultDeduper.firstTime("test_fill"));
        assertFalse(defaultDeduper.firstTime("test_fill"));

        // Verify default values are set
        assertEquals(200_000, defaultDeduper.maxSize);
        assertEquals(Duration.ofMinutes(30), defaultDeduper.expiration);
    }

    @Test
    public void testFirstTimeReturnsTrueForNewFillId() {
        String fillId = "fill_12345";

        boolean result = deduper.firstTime(fillId);

        assertTrue(result, "First time should return true for new fill ID");
    }

    @Test
    public void testFirstTimeReturnsFalseForDuplicateFillId() {
        String fillId = "fill_12345";

        // First call should return true
        boolean firstCall = deduper.firstTime(fillId);
        assertTrue(firstCall, "First call should return true");

        // Second call should return false
        boolean secondCall = deduper.firstTime(fillId);
        assertFalse(secondCall, "Second call should return false for duplicate fill ID");
    }

    @Test
    public void testMultipleUniqueFillIds() {
        String fillId1 = "fill_12345";
        String fillId2 = "fill_67890";
        String fillId3 = "fill_abcde";

        assertTrue(deduper.firstTime(fillId1), "First unique fill ID should return true");
        assertTrue(deduper.firstTime(fillId2), "Second unique fill ID should return true");
        assertTrue(deduper.firstTime(fillId3), "Third unique fill ID should return true");

        // Verify they're all cached now
        assertFalse(deduper.firstTime(fillId1), "First fill ID should now return false");
        assertFalse(deduper.firstTime(fillId2), "Second fill ID should now return false");
        assertFalse(deduper.firstTime(fillId3), "Third fill ID should now return false");
    }

    @Test
    public void testCacheEvictionWhenMaxSizeExceeded() throws InterruptedException {
        // Fill the cache to max capacity (5 items)
        for (int i = 1; i <= 5; i++) {
            assertTrue(deduper.firstTime("fill_" + i), "Fill " + i + " should be first time");
        }

        // Verify cache is at capacity
        assertTrue(deduper.seen.asMap().size() <= 5, "Cache size should not exceed max size");

        // Add more items beyond capacity
        for (int i = 6; i <= 10; i++) {
            assertTrue(deduper.firstTime("fill_" + i), "Fill " + i + " should be first time");
        }

        // Give cache time to process eviction
        Thread.sleep(200);

        // Cache should still respect size limit (with some tolerance for async
        // eviction)
        assertTrue(deduper.seen.asMap().size() <= 6, "Cache size should be close to max size after evictions");
    }

    @Test
    public void testCacheExpirationAfterTimeout() throws InterruptedException {
        // Create a deduper with very short expiration for faster testing
        FillDeduper shortExpirationDeduper = new FillDeduper(10, Duration.ofMillis(500));
        String fillId = "fill_expiration_test";

        // Add item to cache
        assertTrue(shortExpirationDeduper.firstTime(fillId), "First time should return true");
        assertFalse(shortExpirationDeduper.firstTime(fillId), "Should be cached initially");

        // Wait for expiration (500ms + buffer)
        Thread.sleep(600);

        // Item should have expired and return true again
        assertTrue(shortExpirationDeduper.firstTime(fillId), "Should return true after expiration");
    }

    @Test
    public void testCacheExpirationLongerTest() throws InterruptedException {
        String fillId = "fill_expiration_test_long";

        // Add item to cache (using the 15-second expiration from setUp)
        assertTrue(deduper.firstTime(fillId), "First time should return true");
        assertFalse(deduper.firstTime(fillId), "Should be cached initially");

        // Wait for expiration (15 seconds + buffer)
        Thread.sleep(16000);

        // Item should have expired and return true again
        assertTrue(deduper.firstTime(fillId), "Should return true after expiration");
    }

    @Test
    public void testEmptyAndNullFillIds() {
        // Test empty string
        assertTrue(deduper.firstTime(""), "Empty string should return true first time");
        assertFalse(deduper.firstTime(""), "Empty string should return false second time");

        // Test null (though this might throw NPE depending on Caffeine implementation)
        assertThrows(NullPointerException.class, () -> {
            deduper.firstTime(null);
        }, "Null fill ID should throw NullPointerException");
    }

    @Test
    public void testCacheStateConsistency() {
        String fillId1 = "fill_consistency_1";
        String fillId2 = "fill_consistency_2";

        // Add first item
        assertTrue(deduper.firstTime(fillId1));
        assertEquals(1, deduper.seen.asMap().size(), "Cache should contain 1 item");

        // Add second item
        assertTrue(deduper.firstTime(fillId2));
        assertEquals(2, deduper.seen.asMap().size(), "Cache should contain 2 items");

        // Verify both are still cached
        assertFalse(deduper.firstTime(fillId1));
        assertFalse(deduper.firstTime(fillId2));
        assertEquals(2, deduper.seen.asMap().size(), "Cache should still contain 2 items");
    }

    @Test
    public void testLargeFillIdHandling() {
        // Test with very long fill ID
        StringBuilder longFillId = new StringBuilder("fill_");
        for (int i = 0; i < 1000; i++) {
            longFillId.append("a");
        }
        String largeFillId = longFillId.toString();

        assertTrue(deduper.firstTime(largeFillId), "Large fill ID should work on first time");
        assertFalse(deduper.firstTime(largeFillId), "Large fill ID should be cached on second time");
    }

    @Test
    public void testSpecialCharacterFillIds() {
        String[] specialFillIds = { "fill-with-dashes", "fill_with_underscores", "fill.with.dots", "fill@with@symbols",
                "fill with spaces", "fill/with/slashes", "fill\\with\\backslashes" };

        for (String fillId : specialFillIds) {
            assertTrue(deduper.firstTime(fillId), "Special character fill ID should work: " + fillId);
            assertFalse(deduper.firstTime(fillId), "Special character fill ID should be cached: " + fillId);
        }
    }

    @Test
    public void testConcurrentAccess() throws InterruptedException {
        final String fillId = "concurrent_test_fill";
        final int threadCount = 10;
        final boolean[] results = new boolean[threadCount];
        final Thread[] threads = new Thread[threadCount];

        // Create threads that all try to process the same fill ID simultaneously
        for (int i = 0; i < threadCount; i++) {
            final int threadIndex = i;
            threads[i] = new Thread(() -> {
                results[threadIndex] = deduper.firstTime(fillId);
            });
        }

        // Start all threads
        for (Thread thread : threads) {
            thread.start();
        }

        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }

        // Exactly one thread should have returned true (first time)
        int trueCount = 0;
        for (boolean result : results) {
            if (result) {
                trueCount++;
            }
        }

        assertEquals(1, trueCount, "Exactly one thread should have returned true for first time");
    }

    @Test
    public void testParameterizedConstructor() {
        long testMaxSize = 3;
        Duration testExpiration = Duration.ofSeconds(5);

        FillDeduper customDeduper = new FillDeduper(testMaxSize, testExpiration);

        // Verify parameters are set correctly
        assertEquals(testMaxSize, customDeduper.maxSize);
        assertEquals(testExpiration, customDeduper.expiration);

        // Test basic functionality with custom parameters
        assertTrue(customDeduper.firstTime("fill_1"));
        assertTrue(customDeduper.firstTime("fill_2"));
        assertTrue(customDeduper.firstTime("fill_3"));

        // All should be cached
        assertFalse(customDeduper.firstTime("fill_1"));
        assertFalse(customDeduper.firstTime("fill_2"));
        assertFalse(customDeduper.firstTime("fill_3"));

        // Verify cache size is within expected bounds
        assertTrue(customDeduper.seen.asMap().size() <= testMaxSize, "Cache size should not exceed maxSize");
    }

    @Test
    public void testEdgeCasesAndBoundaryConditions() throws InterruptedException {
        // Test with maxSize = 1
        FillDeduper singleItemDeduper = new FillDeduper(1, Duration.ofMinutes(1));

        assertTrue(singleItemDeduper.firstTime("first"));
        assertFalse(singleItemDeduper.firstTime("first")); // Should be cached

        // Cache size should be 1 or less
        assertTrue(singleItemDeduper.seen.asMap().size() <= 1, "Single item cache should not exceed size 1");

        // Test with very large maxSize - basic functionality
        FillDeduper largeDeduper = new FillDeduper(1_000_000, Duration.ofMinutes(1));

        // Test a reasonable number of items
        for (int i = 0; i < 100; i++) {
            assertTrue(largeDeduper.firstTime("fill_" + i));
        }

        // All should still be cached
        for (int i = 0; i < 100; i++) {
            assertFalse(largeDeduper.firstTime("fill_" + i));
        }

        // Cache should contain all items
        assertEquals(100, largeDeduper.seen.asMap().size());
    }
}