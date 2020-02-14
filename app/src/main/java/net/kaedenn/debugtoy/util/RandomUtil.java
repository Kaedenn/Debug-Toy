package net.kaedenn.debugtoy.util;

import java.time.Instant;
import java.util.Date;
import java.util.Random;

@SuppressWarnings("WeakerAccess")
public class RandomUtil extends Random {
    private static RandomUtil self;

    public RandomUtil() {
        this(Date.from(Instant.now()).getTime());
    }

    public static RandomUtil getInstance() {
        if (self == null) self = new RandomUtil();
        return self;
    }

    public RandomUtil(long seed) {
        super(seed);
    }

    @SuppressWarnings("unused")
    public <T> T choice(T[] choices) {
        return choices[nextInt(choices.length)];
    }

    @SuppressWarnings("unused")
    public int range(int min, int max) {
        return nextInt(max - min) + min;
    }

    @SuppressWarnings("unused")
    public float range(float min, float max) {
        return nextFloat() * (max - min) + min;
    }

    @SuppressWarnings("unused")
    public double range(double min, double max) {
        return nextDouble() * (max - min) + min;
    }
}
