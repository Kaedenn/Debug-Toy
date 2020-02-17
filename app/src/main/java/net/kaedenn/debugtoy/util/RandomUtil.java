package net.kaedenn.debugtoy.util;

import android.graphics.Point;

import java.time.Instant;
import java.util.Date;
import java.util.Random;

@SuppressWarnings({"WeakerAccess", "unused"})
public class RandomUtil extends Random {
    private static RandomUtil self;

    public static RandomUtil getInstance() {
        if (self == null) self = new RandomUtil();
        return self;
    }

    public RandomUtil() {
        this(Date.from(Instant.now()).getTime());
    }

    public RandomUtil(long seed) {
        super(seed);
    }

    public <T> T choice(T[] choices) {
        return choices[nextInt(choices.length)];
    }

    public int range(int min, int max) {
        /* Ignore nonsensical values */
        if (max <= min) return 0;
        return nextInt(max - min) + min;
    }

    public float range(float min, float max) {
        /* Ignore nonsensical values */
        if (max <= min) return 0;
        return nextFloat() * (max - min) + min;
    }

    public double range(double min, double max) {
        /* Ignore nonsensical values */
        if (max <= min) return 0;
        return nextDouble() * (max - min) + min;
    }

    public static <T> T getChoice(T[] choices) {
        return getInstance().choice(choices);
    }

    public static float getRange(Point pt) {
        return getRange((float)pt.x, (float)pt.y);
    }

    public static int getRange(int min, int max) {
        return getInstance().range(min, max);
    }

    public static float getRange(float min, float max) {
        return getInstance().range(min, max);
    }

    public static double getRange(double min, double max) {
        return getInstance().range(min, max);
    }

}
