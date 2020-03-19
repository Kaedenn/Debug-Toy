package animation;

import android.graphics.Point;
import android.graphics.Rect;
import android.text.Html;
import android.text.Spanned;
import android.text.SpannedString;
import android.util.Pair;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.animation.TranslateAnimation;
import android.widget.TextView;

import androidx.annotation.NonNull;

import net.kaedenn.debugtoy.MainActivity;
import net.kaedenn.debugtoy.util.AnimationListenerAdapter;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** "News Ticker" animation controller class.
 *
 * Note that if no messages are marked as being able to repeat, then the very
 * last message will repeat forever until another one is added.
 */
@SuppressWarnings("WeakerAccess")
public class Ticker {
    public static final float TEXT_MARGIN = 0.1f;
    public static final float DURATION = 3.5f;
    public static final int RIGHT_TO_LEFT = 0;
    public static final int LEFT_TO_RIGHT = 1;
    public static final float SLOW_SPEED = 0.5f;
    public static final float NORMAL_SPEED = 1.0f;
    public static final float FAST_SPEED = 2f;

    public static final class Message {
        Spanned mMessage;
        boolean mCanRepeat;
        Message(Spanned message) {
            this(message, false);
        }
        Message(Spanned message, boolean repeat) {
            mMessage = message;
            mCanRepeat = repeat;
        }
        Message(CharSequence message) {
            this(new SpannedString(message), false);
        }
        Message(CharSequence message, boolean repeat) {
            this(new SpannedString(message), repeat);
        }
    }

    @NonNull
    private TextView mTextView;
    @NonNull
    private TextView mHelperView;
    private Interpolator mInterpolator;
    private Animation mAnimation;
    private List<Message> mMessages;
    private int mDefaultColor;

    /* Original calculated duration for mMessage */
    private float mDuration;

    /* Original start/end coordinates for mMessage */
    private Pair<Float, Float> mXRange;

    /* Current message being animated */
    private Message mMessage;

    /* Options */

    private boolean mResetColorOnStart = false;
    private float mSpeed = 1f;
    private int mDirection = RIGHT_TO_LEFT;

    /** Construct a new Ticker animation.
     *
     * @param ticker The TextView to contain the animation.
     * @param helper An invisible exact clone of the TextView.
     */
    public Ticker(@NotNull TextView ticker, @NotNull TextView helper) {
        mTextView = ticker;
        mHelperView = helper;
        mInterpolator = AnimationUtils.loadInterpolator(MainActivity.getInstance(), android.R.anim.linear_interpolator);
        mMessages = Collections.synchronizedList(new ArrayList<>());
        mDefaultColor = mTextView.getCurrentTextColor();
    }

    /** Add a character sequence (or {@code Spanned} string) message.
     *
     * {@link Ticker#addMessage(CharSequence, boolean)} wrapper.
     *
     * @param s The character sequence or {@code Spanned} string.
     */
    public void addMessage(@NotNull CharSequence s) {
        addMessage(s, false);
    }

    /** Add a character sequence that may or may not be able to repeat.
     *
     * @param s The character sequence or {@code Spanned} string.
     * @param canRepeat If True, then the message can appear more than once.
     */
    public void addMessage(@NotNull CharSequence s, boolean canRepeat) {
        mMessages.add(new Message(s, canRepeat));
    }

    /** Add an HTML message.
     *
     * {@link Ticker#addHtmlMessage(String, boolean)} wrapper.
     *
     * @param s The string with embedded HTML sequences.
     */
    public void addHtmlMessage(@NotNull String s) {
        addHtmlMessage(s, false);
    }

    /** Add an HTML message that may or may not be able to repeat.
     *
     * @param s The string with embedded HTML sequences.
     * @param canRepeat If True, the message can appear more than once.
     */
    public void addHtmlMessage(@NotNull String s, boolean canRepeat) {
        addMessage(Html.fromHtml(s, 0), canRepeat);
    }

    /** Return whether or not an animation is in progress.
     *
     * @return True if the animation is running, false otherwise.
     */
    public boolean isAnimating() {
        if (mAnimation != null) {
            return mAnimation.hasStarted() && !mAnimation.hasEnded();
        }
        return false;
    }

    /** Get the current configured speed.
     *
     * @return The current speed.
     */
    public float getSpeed() {
        return mSpeed;
    }

    /** Set the current configured speed.
     *
     * This applies to the current message and to all subsequent messages.
     *
     * Default speed is 1.0. Larger values give faster movement.
     *
     * @param speed The new speed
     */
    public void setSpeed(float speed) {
        if (speed <= 0f) {
            throw new IllegalArgumentException(String.format("invalid speed %g; must be > 0", speed));
        }
        mSpeed = speed;
        mDuration = DURATION * Math.abs(mXRange.second - mXRange.first) / mSpeed;
        /* TODO: mAnimation.setStartOffset()
        if (isAnimating()) {
            mAnimation.setDuration(Math.round(mDuration));
            long currTime = AnimationUtils.currentAnimationTimeMillis();
            long timeSince = currTime - mAnimation.getStartTime() - mAnimation.getStartOffset();
        }
        */
    }

    /** Get the direction of animation.
     *
     * @return The animation direction.
     */
    public int getDirection() {
        return mDirection;
    }

    /** Set the direction of animation.
     *
     * Valid values are either {@code RIGHT_TO_LEFT} for typical animations and
     * {@code LEFT_TO_RIGHT} for backwards animations.
     *
     * Changes made by calling this function will apply to the next message to
     * animate; animations in progress will continue as-is.
     *
     * @param direction The animation direction.
     */
    public void setDirection(int direction) {
        if (direction != LEFT_TO_RIGHT && direction != RIGHT_TO_LEFT) {
            throw new IllegalArgumentException(String.format("Invalid direction %d", direction));
        }
        mDirection = direction;
    }

    /** Get the next message in the queue.
     *
     * If the queue is empty, then the most recently used message is returned.
     * Messages marked as being able to repeat are added back to the end of the
     * queue after being removed.
     *
     * @return The next message.
     */
    private Message getNextMessage() {
        /* Get the first item in the queue */
        if (!mMessages.isEmpty()) {
            mMessage = mMessages.remove(0);
        }
        /* If that item is marked as repeatable, append it back to the queue */
        if (mMessage != null) {
            if (mMessage.mCanRepeat) {
                mMessages.add(mMessage);
            }
        }
        return mMessage;
    }

    /** Helper: Get the current absolute Rect for a View.
     *
     * @param tv The view to examine.
     * @return A Rect of the View's x, y, width, and height.
     */
    private Rect getAbsoluteLocation(TextView tv) {
        return MainActivity.getInstance().getAbsoluteLocation(tv);
    }

    /** Helper: Get the current screen size.
     *
     * This is provided because the screen size can change periodically.
     *
     * @return The current size of the screen as a Point instance.
     */
    private Point getScreenSize() {
        return MainActivity.getInstance().getScreenSize();
    }

    /** Return whether or not text in the title view is centered.
     *
     * @return True if the text is centered, false otherwise.
     */
    private boolean isViewCentered() {
        return (mTextView.getGravity() & (~Gravity.START | ~Gravity.TOP | Gravity.CENTER)) != 0;
    }

    /** Return the starting and ending X coordinate for the given message width.
     *
     * @param width The width of the message being animated.
     * @return A pair of floats for the starting and ending X coordinates
     */
    private Pair<Float, Float> getAnimExtrema(int width) {
        int sw = getScreenSize().x;
        float marginStart = sw * TEXT_MARGIN;
        float marginEnd = sw * TEXT_MARGIN;
        if (isViewCentered()) {
            marginStart = width/2f * TEXT_MARGIN;
            marginEnd = width * TEXT_MARGIN;
        }
        float xStart = sw + marginStart;
        float xEnd = -(width + marginEnd);
        Pair<Float, Float> result = new Pair<>(xStart, xEnd);
        if (mDirection == LEFT_TO_RIGHT) {
            result = new Pair<>(xEnd, xStart);
        }
        return result;
    }

    public void startAnimation() {
        Message message = getNextMessage();
        if (message != null) {
            Spanned msg = message.mMessage;
            /* Measure the width of the message */
            mHelperView.setText(msg);
            mHelperView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            int width = mHelperView.getMeasuredWidth();

            /* Ensure the text box is large enough to store the entire string */
            ViewGroup.LayoutParams params = mTextView.getLayoutParams();
            params.width = Math.round(width + width * TEXT_MARGIN);
            mTextView.setLayoutParams(params);

            /* Calculate offsets and the duration */
            mXRange = getAnimExtrema(width);
            float xStart = mXRange.first;
            float xEnd = mXRange.second;
            mDuration = DURATION * Math.abs(xEnd - xStart) / mSpeed;

            /* Construct the animation */
            mAnimation = new TranslateAnimation(xStart, xEnd, 0f, 0f);
            mAnimation.setDuration(Math.round(mDuration));
            mAnimation.setInterpolator(mInterpolator);
            mAnimation.setAnimationListener(new AnimationListenerAdapter() {
                public void onAnimationEnd(Animation animation) {
                    startAnimation();
                }
            });

            /* Dispatch the animation */
            mTextView.post(() -> {
                mTextView.clearAnimation();
                if (mResetColorOnStart) {
                    mTextView.setTextColor(mDefaultColor);
                }
                mTextView.setText(msg);
                mTextView.startAnimation(mAnimation);
            });
        }
    }
}
