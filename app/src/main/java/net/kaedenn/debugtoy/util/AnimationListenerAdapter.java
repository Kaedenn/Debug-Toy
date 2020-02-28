package net.kaedenn.debugtoy.util;

import android.view.animation.Animation;

/** Adapter class for the Animation.AnimationListener interface.
 *
 * This class provides default methods for the {@link android.view.animation.Animation.AnimationListener}
 * class so that listeners don't have to implement all three functions when
 * adding an animation listener.
 *
 * @see android.view.animation.Animation.AnimationListener
 */
public class AnimationListenerAdapter implements Animation.AnimationListener {
    /**
     * <p>Notifies the start of the animation.</p>
     *
     * @param animation The started animation.
     */
    public void onAnimationStart(Animation animation) {

    }

    /**
     * <p>Notifies the end of the animation. This callback is not invoked
     * for animations with repeat count set to INFINITE.</p>
     *
     * @param animation The animation which reached its end.
     */
    public void onAnimationEnd(Animation animation) {

    }

    /**
     * <p>Notifies the repetition of the animation.</p>
     *
     * @param animation The animation which was repeated.
     */
    public void onAnimationRepeat(Animation animation) {

    }
}
