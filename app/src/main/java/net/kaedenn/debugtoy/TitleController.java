package net.kaedenn.debugtoy;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
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
import net.kaedenn.debugtoy.util.Functional;
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

    /* Margin to add to the translation start and/or end offsets */
    private static final float TEXT_MARGIN = 0.1f;

    /* Default duration coefficient */
    private static final float DEFAULT_DURATION_COEFFICIENT = 3.5f;

    /* "Disco" color animation field and values */
    private static final String ANIM_COLOR_FIELD = "TextColor";

    /* Scale animation field and values */
    private static final String ANIM_SCALE_FIELD = "TextScaleX";

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

    /* Color keyframes used by the color animation */
    private int[] mAnimColorValues;

    /* Scale keyframes used by the scale animation */
    private float[] mAnimScaleValues;

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
        mMessageQueue = Collections.synchronizedList(new ArrayList<>());
        mInterpolator = AnimationUtils.loadInterpolator(MainActivity.getInstance(), android.R.anim.linear_interpolator);
        mDurationCoeff = DEFAULT_DURATION_COEFFICIENT;
        mStartColor = mTitleView.getCurrentTextColor();

        mAnimColorValues = Res.getIntArray(R.array.tbDiscoColorValues);
        mAnimScaleValues = Functional.toFloatArray(Res.getTextArray(R.array.tbDiscoScaleValues));
    }

    /** Obtain the absolute x, y, width, and height for the title view.
     *
     * @return A rect with the view's current absolute coordinates.
     */
    public Rect getAbsoluteCoordinates() {
        int[] pos = new int[2];
        mTitleView.getLocationOnScreen(pos);
        return new Rect(pos[0], pos[1], pos[0]+mTitleView.getWidth(), pos[1]+mTitleView.getHeight());
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

    /** Construct and return the "disco" color animator */
    private ObjectAnimator makeColorAnim() {
        ObjectAnimator colorAnim = ObjectAnimator.ofArgb(mTitleView, ANIM_COLOR_FIELD, mAnimColorValues);
        colorAnim.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                mTitleView.setTextColor(mStartColor);
            }
        });
        /* TODO: Stop the animation once the text scrolls off-screen
        colorAnim.setRepeatCount(ValueAnimator.INFINITE);
        */
        colorAnim.setDuration(Res.getInteger(R.integer.tbTouchAnimDuration));
        return colorAnim;
    }

    /** Construct and return the scale animator */
    private ObjectAnimator makeScaleAnim() {
        ObjectAnimator scaleAnim = ObjectAnimator.ofFloat(mTitleView, ANIM_SCALE_FIELD, mAnimScaleValues);
        scaleAnim.setDuration(Res.getInteger(R.integer.tbTouchAnimDuration));
        return scaleAnim;
    }

    /** Process touch events on the titlebar
     *
     * @param e The touch event
     * @return True if the listener has consumed the event, false otherwise
     */
    public boolean processTouchEvent(MotionEvent e) {
        makeColorAnim().start();
        makeScaleAnim().start();
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
        if (!mMessageQueue.isEmpty()) {
            Logf.vc("getNextMessage: queue has %d items", mMessageQueue.size());
            mMessage = mMessageQueue.remove(0);
        } else {
            Logf.vc("getNextMessage: queue is empty; returning previous message");
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

    private boolean isViewCentered() {
        return (mTitleView.getGravity() & (~Gravity.START | ~Gravity.TOP | Gravity.CENTER)) != 0;
    }

    /** Get the xStart and xEnd coordinates for the given width.
     *
     * The calculations will differ if the {@code mTitleView} has centered
     * gravity, as (for some unknown reason) that changes what requires the 10%
     * margin.
     *
     * If the view is centered, the start coordinate has the margin applied to
     * half of the width and the end coordinate has the margin applied to the
     * width.
     *
     * Otherwise, both the start and end coordinates have the margin applied to
     * the screen's width.
     *
     * @param width The width of the message in pixels.
     * @return A pair of {@code Float}s {@code xStart} and {@code xEnd}.
     */
    private Pair<Float, Float> getAnimExtrema(int width) {
        int sw = MainActivity.getInstance().getScreenWidth();
        float xStart, xEnd;
        if (isViewCentered()) {
            xStart = sw + width/2f * TEXT_MARGIN;
            xEnd = -(width + width * TEXT_MARGIN);
            Logf.vc("Animating width=%d from x1=%g to x2=%g (sw=%d, center=true)", width, xStart, xEnd, sw);
        } else {
            xStart = sw + sw * TEXT_MARGIN;
            xEnd = -(width + sw * TEXT_MARGIN);
            Logf.vc("Animating width=%d from x1=%g to x2=%g (sw=%d, center=false)", width, xStart, xEnd, sw);
        }
        return new Pair<>(xStart, xEnd);
    }

    /** Start the next animation.
     *
     * This method obtains the next message (which can modify the queued
     * messages), calculates animation information (starting and ending X
     * coordinates, duration), resizes the view to contain the entire message,
     * constructs the animation object, sets the view's text and formatting
     * information, and then finally starts the animation.
     *
     * Note that the animation does not repeat, even though it seems like it
     * should. Each message requires different animation parameters and updating
     * the parameters of a running animation doesn't seem to be possible.
     * Therefore, the animation is recreated entirely once the previous one
     * completes.
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
            params.width = Math.round(width + width * TEXT_MARGIN);
            mTitleView.setLayoutParams(params);

            /* Construct the animation */
            mAnimation = new TranslateAnimation(xStart, xEnd, 0f, 0f);
            mAnimation.setDuration(duration);
            mAnimation.setInterpolator(mInterpolator);
            mAnimation.setAnimationListener(new AnimationListenerAdapter() {
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
