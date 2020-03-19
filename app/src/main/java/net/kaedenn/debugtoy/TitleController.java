package net.kaedenn.debugtoy;

import android.widget.TextView;

import androidx.annotation.NonNull;

import net.kaedenn.debugtoy.util.Logf;

import org.jetbrains.annotations.NotNull;

import animation.Ticker;

@SuppressWarnings("WeakerAccess")
class TitleController {
    private static final String LOG_TAG = "title-controller";

    static {
        Logf.getInstance().add(TitleController.class, LOG_TAG);
    }

    /* "Disco" color animation field and values */
    private static final String ANIM_COLOR_FIELD = "TextColor";

    /* Scale animation field and values */
    private static final String ANIM_SCALE_FIELD = "TextScaleX";

    @NonNull
    private Ticker mTicker;

    /**
     * Construct the controller class.
     */
    public TitleController() {
        TextView titleView = MainActivity.getInstance().requireViewById(R.id.titlebar);
        TextView helperView = MainActivity.getInstance().requireViewById(R.id.titlebarHelper);
        mTicker = new Ticker(titleView, helperView);
    }

    public Ticker getTicker() {
        return mTicker;
    }

    public void setLeftToRight() {
        mTicker.setDirection(Ticker.LEFT_TO_RIGHT);
    }

    public void setRightToLeft() {
        mTicker.setDirection(Ticker.RIGHT_TO_LEFT);
    }

    public void setSpeedSlow() {
        mTicker.setSpeed(Ticker.SLOW_SPEED);
    }

    public void setSpeedNormal() {
        mTicker.setSpeed(Ticker.NORMAL_SPEED);
    }

    public void setSpeedFast() {
        mTicker.setSpeed(Ticker.FAST_SPEED);
    }

    public void addRepeatingMessage(@NotNull CharSequence s) {
        mTicker.addMessage(s, true);
    }
}

