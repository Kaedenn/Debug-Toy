package net.kaedenn.debugtoy;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.text.Spanned;
import android.text.SpannedString;
import android.util.Pair;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.animation.TranslateAnimation;
import android.widget.TextView;

import androidx.annotation.NonNull;

import net.kaedenn.debugtoy.util.AnimationListenerAdapter;
import net.kaedenn.debugtoy.util.Logf;
import net.kaedenn.debugtoy.util.Res;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("WeakerAccess")
class TitleController {
    private static final String LOG_TAG = "title-controller";
    static {
        Logf.getInstance().add(TitleController.class, LOG_TAG);
    }

    /* Extra margin to add to the scroll ending x coordinate (multiplied by the
     * current screen width) */
    private static final float TEXT_MARGIN = 0.1f;

    /* Default duration coefficient */
    private static final float DEFAULT_DURATION_COEFFICIENT = 3.5f;

    /* Values used for interactive titlebar animations */
    private static final int[] TITLE_ANIM_COLORS = {0xFFFF0000, 0xFF00FF00, 0xFF0000FF, 0xFFFF0000};
    private static final float[] TITLE_ANIM_SCALE_VALUES = {1f, .8f, 1.2f, 1f};

    /* View that the user will see */
    @NonNull
    private TextView mTitleView;

    /* Helper view used to calculate the width of the next message */
    @NonNull
    private TextView mHelperView;

    /* Current message set by getNextMessage. Null if no message is animating */
    private Spanned mMessage = null;

    /* Queue of messages to animate */
    @NonNull
    private List<Spanned> mMessageQueue;

    /* Current animation being applied to mTitleView */
    private Animation mAnimation = null;

    /* mAnimation interpolator */
    @NonNull
    private Interpolator mInterpolator;

    /* Duration coefficient for mAnimation */
    private float mDurationCoeff;

    /* Color to apply by default at the start of every message */
    private int mStartColor;

    /** Construct the controller class.
     *
     * Default interpolator: {@code android.R.anim.linear_interpolator}.
     * Default duration coefficient: {@code DEFAULT_DURATION_COEFFICIENT}.
     * Default starting color: {@code mTitleView}'s current text color.
     */
    @SuppressLint("ClickableViewAccessibility")
    public TitleController() {
        mTitleView = MainActivity.getInstance().requireViewById(R.id.titlebar);
        mHelperView = MainActivity.getInstance().requireViewById(R.id.titlebarHelper);
        mTitleView.setOnTouchListener(this::onTitlebarTouchEvent);
        mMessageQueue = Collections.synchronizedList(new ArrayList<>());
        mInterpolator = AnimationUtils.loadInterpolator(MainActivity.getInstance(), android.R.anim.linear_interpolator);
        mDurationCoeff = DEFAULT_DURATION_COEFFICIENT;
        mStartColor = mTitleView.getCurrentTextColor();

        /* Ensure the title view and helper view are truly identical */
        if (mTitleView.getGravity() != mHelperView.getGravity()) {
            Logf.ec("mHelperView gravity %d differs from mTitleView gravity %d", mHelperView.getGravity(), mTitleView.getGravity());
            mHelperView.setGravity(mTitleView.getGravity());
        }
        Logf.dc("TitleController constructed");
    }

    /** Set the default text color.
     *
     * This color is applied at the start of every message. Messages can have
     * their own colors if desired. This color is used to reset special
     * interactive messages.
     *
     * @param c The color to use
     */
    public void setTextColor(int c) {
        mStartColor = c;
    }

    /** Process touch events on the titlebar
     *
     * @param v The titlebar view (ignored; {@code mTitleView} is used)
     * @param event The touch event
     * @return True if the listener has consumed the event, false otherwise
     */
    private boolean onTitlebarTouchEvent(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            ObjectAnimator colorAnim = ObjectAnimator.ofArgb(mTitleView, "TextColor", TITLE_ANIM_COLORS);
            colorAnim.addListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animation) {
                    mTitleView.setTextColor(mStartColor);
                }
            });
            colorAnim.setDuration(Res.getInteger(R.integer.tbTouchAnimDuration));
            colorAnim.start();
            ObjectAnimator.ofFloat(mTitleView, "TextScaleX", TITLE_ANIM_SCALE_VALUES)
                    .setDuration(Res.getInteger(R.integer.tbTouchAnimDuration))
                    .start();
            return true;
        }
        return false;
    }

    /** Add a {@code Spanned} instance to the queue.
     *
     * @param s The {@code Spanned} instance.
     */
    public void addMessage(@NotNull Spanned s) {
        mMessageQueue.add(s);
        maybeStartAnimation();
    }

    /** Add a basic string to the queue.
     *
     * @param s The string to add.
     * @see TitleController#addMessage(Spanned)
     */
    public void addMessage(@NotNull String s) {
        addMessage(new SpannedString(s));
        maybeStartAnimation();
    }

    /** Obtain the next message.
     *
     * This method modifies the internal queue such that subsequent calls will
     * iterate over all queued messages.
     *
     * @return The next message to display. This is null if there is no message
     * queued, which should result in the animation terminating.
     */
    private Spanned getNextMessage() {
        Logf.vc("getNextMessage: queue has %d items", mMessageQueue.size());
        if (!mMessageQueue.isEmpty()) {
            mMessage = mMessageQueue.remove(0);
        }
        return mMessage;
    }

    /** Start the animation if the animation is not currently running.
     *
     * The animation starts off as not running until a message is added for the
     * first time.
     */
    private void maybeStartAnimation() {
        if (mAnimation == null) {
            Logf.dc("Animation is not running; starting");
            startAnimation();
        }
    }

    /** Set the duration coefficient for how fast messages move.
     *
     * The coefficient is multiplied by the distance the message must travel to
     * scroll completely across the screen. The result is then used as the
     * duration of the animation.
     *
     * @param coeff The coefficient to use.
     */
    public void setDurationCoeff(float coeff) {
        if (coeff > 0f) {
            mDurationCoeff = coeff;
        } else {
            Logf.ec("Invalid duration coefficient %g; must be greater than zero", coeff);
        }
    }

    /** Get the duration coefficient for how fast messages move.
     *
     * @return The coefficient currently being used.
     */
    public float getDurationCoeff() {
        return mDurationCoeff;
    }

    /** Calculate the duration to use for the given distance.
     *
     * @param xDistance The distance the text will animate
     * @return The length of the animation in milliseconds
     */
    private long getDurationFor(float xDistance) {
        return Math.round(getDurationCoeff() * xDistance);
    }

    private Pair<Float, Float> getAnimExtrema(int width) {
        int sw = MainActivity.getInstance().getScreenWidth();
        float margin = sw * TEXT_MARGIN;
        float xStart = sw + margin;
        float xEnd = -(width + margin);
        /*
        if ((mTitleView.getGravity() & Gravity.CENTER_VERTICAL) != 0) {
            xStart += width;
            xEnd -= width;
        }
        Logf.dc("Animating %d from %g to %g (sw=%d, m=%g)", width, xStart, xEnd, sw, margin);
        */
        return new Pair<>(xStart, xEnd);
    }

    /** Start the animation.
     *
     * This method obtains the next message (which can modify the queued
     * messages), calculates animation information (starting and ending X
     * coordinates, duration), resizes the view to contain the entire message,
     * constructs the animation object, sets the view's text and formatting
     * information, and then finally starts the animation.
     */
    private void startAnimation() {
        Spanned data = getNextMessage();
        if (data != null) {
            /* Measure the width of the spanned using the helper view */
            mHelperView.setText(data);
            mHelperView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            int width = mHelperView.getMeasuredWidth();

            /* Calculate offsets and the duration */
            Pair<Float, Float> extrema = getAnimExtrema(width);
            float xStart = extrema.first;
            float xEnd = extrema.second;
            long duration = getDurationFor(xStart - xEnd);

            /* Ensure the text box is large enough to store the string */
            ViewGroup.LayoutParams params = mTitleView.getLayoutParams();
            params.width = Math.round(Math.abs(xStart - xEnd));
            mTitleView.setLayoutParams(params);

            /* Construct the animation */
            mAnimation = new TranslateAnimation(xStart, xEnd, 0f, 0f);
            mAnimation.setDuration(duration);
            mAnimation.setInterpolator(mInterpolator);
            mAnimation.setAnimationListener(new AnimationListenerAdapter() {
                public void onAnimationStart(Animation animation) {
                    Logf.d(LOG_TAG, "Animation is starting");
                }
                public void onAnimationEnd(Animation animation) {
                    startAnimation();
                }
            });

            /* Start the animation */
            mTitleView.post(() -> {
                mTitleView.clearAnimation(); /* Just in case */
                mTitleView.setTextColor(mStartColor);
                mTitleView.setText(data);
                mTitleView.startAnimation(mAnimation);
            });
        }
    }
}
