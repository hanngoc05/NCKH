package util;

import java.util.Random;

public final class RandomManager {
    private static long baseSeed = 42L;

    private RandomManager() {}

    public static void setBaseSeed(long seed) {
        baseSeed = seed;
    }

    public static Random createRandom(String streamName) {
        long mixed = mix64(baseSeed ^ streamName.hashCode());
        return new Random(mixed);
    }

    private static long mix64(long z) {
        z = (z ^ (z >>> 33)) * 0xff51afd7ed558ccdL;
        z = (z ^ (z >>> 33)) * 0xc4ceb9fe1a85ec53L;
        return z ^ (z >>> 33);
    }
}
