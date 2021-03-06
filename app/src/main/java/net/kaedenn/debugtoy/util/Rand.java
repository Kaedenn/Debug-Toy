package net.kaedenn.debugtoy.util;

import android.graphics.Color;
import android.graphics.Point;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

@SuppressWarnings({"WeakerAccess", "unused"})
public class Rand extends Random {
    private static Rand self;

    public static Rand getInstance() {
        if (self == null) self = new Rand();
        return self;
    }

    public Rand() {
        this(Date.from(Instant.now()).getTime());
    }

    public Rand(long seed) {
        super(seed);
    }

    public <T> T choice(T[] choices) {
        return choices[nextInt(choices.length)];
    }

    public <T> T choice(ArrayList<T> choices) {
       return choices.get(nextInt(choices.size()));
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

    public static int getColor(float s, float v) {
        float h = getRange(0f, 360f);
        return Color.HSVToColor(new float[]{h, s, v});
    }
}
