package net.kaedenn.debugtoy;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.Point;
import android.text.Spanned;
import android.text.SpannedString;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.TextView;

import androidx.annotation.NonNull;

import net.kaedenn.debugtoy.util.Logf;
import net.kaedenn.debugtoy.util.RandUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class TitleController {
    private static final String LOG_TAG = "title";
    private static final float TEXT_MARGIN = 0.1f;

    @NonNull
    private TextView mTitleView;

    @NonNull
    private TextView mHelperView;

    private Spanned mMessage = null;
    private List<Spanned> mMessageQueue = Collections.synchronizedList(new ArrayList<>());
    private AnimationSet mAnimation = null;
    private Interpolator mInterpolator;

    private int mStartColor/* = Color.BLACK*/;

    @SuppressLint("ClickableViewAccessibility")
    TitleController() {
        mTitleView = MainActivity.getInstance().findViewById(R.id.titlebar);
        mHelperView = MainActivity.getInstance().findViewById(R.id.titlebarHelper);
        mTitleView.setOnTouchListener(this::onTitlebarTouchEvent);
        mInterpolator = AnimationUtils.loadInterpolator(MainActivity.getInstance(), android.R.anim.linear_interpolator);
        Log.d(LOG_TAG, "TitleController constructed");
    }

    /** Set the default text color.
     *
     * This color is applied at the start of every message. Messages can have
     * their own colors if desired. This color is used to reset special
     * interactive messages.
     *
     * @param c The color to use
     */
    synchronized void setTextColor(int c) {
        mStartColor = c;
    }

    /** Process touch events on the titlebar
     *
     * @param v The titlebar view
     * @param event The touch event
     * @return True if the listener has consumed the event, false otherwise
     */
    private boolean onTitlebarTouchEvent(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            /* Apply a random color */
            mTitleView.setTextColor(RandUtil.getColor(1f, 0.5f));
            /* TODO: Add other effects? */
            Animation scaleAnim = AnimationUtils.loadAnimation(MainActivity.getInstance().getApplicationContext(), R.anim.title_scale);
            scaleAnim.setDuration(50);
            mAnimation.addAnimation(scaleAnim);
            /* TODO: Ensure effects are cleared for next message */
            return true;
        }
        return false;
    }

    /** Add a {@code Spanned} instance to the queue.
     *
     * @param s The {@code Spanned} instance.
     */
    synchronized void addMessage(Spanned s) {
        mMessageQueue.add(s);
        maybeStartAnimation();
    }

    /** Add a basic string to the queue.
     *
     * @param s The string to add.
     * @see TitleController#addMessage(Spanned)
     */
    synchronized void addMessage(String s) {
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
        Logf.v(LOG_TAG, "getNextMessage: queue has %d items", mMessageQueue.size());
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
            Log.d(LOG_TAG, "Animation is not running; starting");
            startAnimation();
        }
    }

    /** Calculate the duration to use for the given distance.
     *
     * @param xDistance The distance the text will animate
     * @return The length of the animation in milliseconds
     */
    private long getDurationFor(float xDistance) {
        return Math.round(3.5 * xDistance);
    }

    /** Custom animation listener.
     *
     * Used for the translation animation to trigger the next message when the
     * current one has finished.
     */
    private Animation.AnimationListener mSetListener = new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {
            Log.v(LOG_TAG, "Animation set is starting");
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            Log.v(LOG_TAG, "Animation set has ended");
            startAnimation();
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
            Log.v(LOG_TAG, "Animation set is repeating");
        }
    };

    /** Custom animation listener.
     *
     * Used for the translation animation to trigger the next message when the
     * current one has finished.
     */
    private Animation.AnimationListener mTranslateListener = new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {
            Log.v(LOG_TAG, "Translation animation is starting");
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            Log.v(LOG_TAG, "Translation animation has ended");
            startAnimation();
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
            Log.v(LOG_TAG, "Translation animation is repeating");
        }
    };

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
        mAnimation = new AnimationSet(true);
        mAnimation.setInterpolator(mInterpolator);
        mAnimation.setAnimationListener(mSetListener);
        if (data != null) {
            /* Measure the width of the spanned using the helper view */
            mHelperView.setText(data);
            mHelperView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            int width = mHelperView.getMeasuredWidth();

            /* Calculate offsets and the duration */
            Point screenSize = MainActivity.getInstance().getScreenSize();
            float xStart = screenSize.x;
            float xEnd = -(width + screenSize.x * TEXT_MARGIN);
            long duration = getDurationFor(Math.abs(xStart - xEnd));

            /* Ensure the text box is large enough to store the string */
            ViewGroup.LayoutParams params = mTitleView.getLayoutParams();
            params.width = Math.round(Math.abs(xStart - xEnd));
            mTitleView.setLayoutParams(params);

            /* Construct the animation */
            Animation anim = new TranslateAnimation(xStart, xEnd, 0f, 0f);
            anim.setDuration(duration);
            anim.setAnimationListener(mTranslateListener);

            /* Start the animation */
            mAnimation.addAnimation(anim);
            mTitleView.post(() -> {
                mTitleView.setTextColor(mStartColor);
                mTitleView.setText(data);
                mTitleView.startAnimation(mAnimation);
            });
        }
    }
}
