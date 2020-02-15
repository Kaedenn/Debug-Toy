package net.kaedenn.debugtoy;

import android.graphics.Paint;
import android.graphics.Point;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.animation.TranslateAnimation;
import android.widget.TextView;

import androidx.annotation.NonNull;

import net.kaedenn.debugtoy.util.Strings;

import org.jetbrains.annotations.NotNull;

/* TODO: investigate using a spannable
*   https://medium.com/androiddevelopers/spantastic-text-styling-with-spans-17b0c16b4568
*   https://developer.android.com/reference/android/text/style/ImageSpan */

@SuppressWarnings("unused")
class TitleController {
    private static final String LOG_TAG = "title";

    private TextView mTitleView;
    private Animation mAnimation = null;

    @NonNull
    private String mText = "";
    private boolean mRepeat = false;

    /** Animation data container.
     *
     * This class contains the information necessary to start an animation. The
     * members are calculated results from {@code calculateAnimationData}.
     */
    static class AnimData {
        String mDataText;
        float mStartX;
        float mEndX;
        long mDuration;
    }

    TitleController(@NotNull TextView textView) {
        mTitleView = textView;
        Log.d(LOG_TAG, "TitleController constructed");
    }

    /** Set the text and start an animation.
     *
     * This method does not change the repeating behavior. Default repeating
     * behavior is not repeating.
     *
     * @param newText The text to animate.
     */
    void setText(@NotNull String newText) {
        setText(newText, mRepeat);
    }

    /** Set the text and start an animation.
     *
     * Calling this function will cancel any animations currently taking place.
     * Use {@code queueText()} to start the animation after the current one
     * ends.
     *
     * @param newText The text to animate.
     * @param repeating Whether or not the animation should repeat.
     */
    void setText(@NotNull String newText, boolean repeating) {
        mText = newText;
        repeat(repeating);
        startAnimation();
        Log.d(LOG_TAG, String.format("Setting title to %s", Strings.escape(newText)));
    }

    /** Set the text and allow the current animation to complete.
     *
     * If an animation is already running, then the text will be used for the
     * next animation. Otherwise, an animation is started.
     *
     * @param newText The text to animate.
     * @param repeating Whether or not the animation should repeat.
     */
    void queueText(@NotNull String newText, boolean repeating) {
        mText = newText;
        repeat(repeating);
        if (mAnimation == null) {
            startAnimation();
        }
    }

    void repeat() {
        repeat(true);
    }

    void repeat(boolean repeat) {
        mRepeat = repeat;
    }

    /** Process the parameter and return the data needed for animation.
     *
     * @param animDataText The text to use
     * @return A reference to an {@code AnimData} class containing the deduced
     * values.
     */
    @NonNull
    private AnimData calculateAnimationData(@NotNull String animDataText) {
        AnimData data = new AnimData();
        data.mDataText = animDataText;

        Point screenSize = MainActivity.getInstance().getScreenSize();
        Paint p = mTitleView.getPaint();
        int width = Math.round(p.measureText(mText));
        int endWidth = width;

        if (endWidth < screenSize.x) {
            /* Append spaces to make the title text long enough to scroll
             * cleanly off screen. */
            StringBuilder sb = new StringBuilder(mText);
            double spaceWidth = p.measureText(" ");
            int numSpaces = (int)Math.ceil((screenSize.x - endWidth) / spaceWidth);
            sb.append(Strings.repeat(" ", numSpaces));
            data.mDataText = sb.toString();
            endWidth = Math.round(p.measureText(data.mDataText));
        }

        /* Ensure the mText box is large enough to store the string */
        ViewGroup.LayoutParams params = mTitleView.getLayoutParams();
        params.width = endWidth;
        mTitleView.setLayoutParams(params);

        /* Calculate the final starting and ending offsets */
        float margin = screenSize.x / 10.0f;
        data.mStartX = screenSize.x;
        data.mEndX = -(width + margin);
        data.mDuration = Math.round(3.5 * Math.abs(data.mStartX - data.mEndX));
        return data;
    }

    /** Get the interpolator reference for the animation.
     *
     * @return A reference to the native Android linear interpolator:
     * @see android.R.anim#linear_interpolator
     */
    private Interpolator getInterpolator() {
        return AnimationUtils.loadInterpolator(MainActivity.getInstance(), android.R.anim.linear_interpolator);
    }

    private void startAnimation() {
        AnimData data = calculateAnimationData(mText);
        final String originalText = mText;
        final String finalText = data.mDataText;

        mAnimation = new TranslateAnimation(data.mStartX, data.mEndX, 0.0f, 0.0f);
        mAnimation.setFillAfter(true);
        mAnimation.setDuration(data.mDuration);
        mAnimation.setInterpolator(getInterpolator());
        mAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (!originalText.equals(mText)) {
                    /* We have text queued; start a new one */
                    startAnimation();
                } else if (mRepeat) {
                    /* We're repeating; re-post the animation */
                    mTitleView.post(() -> mTitleView.startAnimation(animation));
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mTitleView.post(() -> {
            Spanned spannedText = Html.fromHtml(Html.escapeHtml(finalText), 0);
            mTitleView.setText(spannedText);
            mTitleView.startAnimation(mAnimation);
        });
    }
}
