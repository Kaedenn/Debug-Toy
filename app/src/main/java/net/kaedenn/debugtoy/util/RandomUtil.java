package net.kaedenn.debugtoy.util;

import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.Random;

public class RandomUtil extends Random {
    public RandomUtil() {
        this(Date.from(Instant.now()).getTime());
    }

    public RandomUtil(long seed) {
        super(seed);
    }

    @SuppressWarnings("unused")
    public <T> T choice(T[] choices) {
        return choices[nextInt(choices.length)];
    }

}
