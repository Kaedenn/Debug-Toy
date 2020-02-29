package net.kaedenn.debugtoy;

import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

import net.kaedenn.debugtoy.util.Logf;
import net.kaedenn.debugtoy.util.Res;

import java.util.Timer;

/** Controller class for the second page's surface.
 *
 */
class SurfacePageController implements SurfaceHolder.Callback {
    private static final String LOG_TAG = "surface-controller";
    static {
        Logf.getInstance().add(SurfacePageController.class, LOG_TAG);
    }
    private static final String TIMER_NAME = "mAnim";

    private Timer mTimer;
    private SurfaceAnimation mAnim;

    /** Construct the controller.
     *
     * Creates the timer and binds the surface callbacks. Does not start the
     * timer; that's done when the page appears.
     */
    SurfacePageController() {
        SurfaceHolder holder = getSurfaceView().getHolder();
        holder.addCallback(this);
        mAnim = new SurfaceAnimation(holder);
    }

    /** Start (or restart) the animation timer. */
    private void startTimer(@NonNull SurfaceHolder holder) {
        if (mTimer != null) {
            stopTimer();
        }
        if (mAnim == null) {
            mAnim = new SurfaceAnimation(holder);
        }
        mAnim.updateHolder(holder);
        mTimer = new Timer(TIMER_NAME, true);
        mTimer.scheduleAtFixedRate(mAnim, 0, Res.getInteger(R.integer.animRate));
    }

    /** Stop the animation timer. */
    private void stopTimer() {
        if (mTimer != null) {
            mTimer.cancel(); /* stop animation from running */
            mTimer.purge(); /* forcefully ensure animation does not run */
            mTimer = null;
        }
    }

    /** Callback: the contained surface was created.
     *
     * @param holder surface container for the surface that was created
     */
    public void surfaceCreated(SurfaceHolder holder) {
        Logf.ic("surfaceCreated holder=%s", holder.toString());
        startTimer(holder);
    }

    /** Callback: the contained surface has changed.
     *
     * @param holder surface container for the surface that changed
     * @param format current (post-change) pixel format
     * @param width current (post-change) surface width
     * @param height current (post-change) surface height
     */
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Logf.ic("surfaceChanged holder=%s format=%d w=%d h=%d",
                holder.toString(), format, width, height);
        mAnim.updateHolder(holder);
        /* TODO: Determine if redrawFrame is needed */
    }

    /** Callback: the contained surface was destroyed.
     *
     * @param holder surface container for the surface that was destroyed
     */
    public void surfaceDestroyed(SurfaceHolder holder) {
        Logf.ic("surfaceDestroyed holder=%s", holder.toString());
        stopTimer();
    }

    /** Convenience function to get the managed SurfaceView.
     *
     * @return The SurfaceView this class manages
     */
    private SurfaceView getSurfaceView() {
        return MainActivity.getInstance().findViewById(R.id.page2Surface);
    }

    /** Called when the user navigates to the surface page. */
    void doEnterPage() {
        Logf.dc("page has appeared");
        if (mAnim != null) {
            mAnim.unpause();
        }
    }

    /** Called when the user navigates away from the surface page. */
    void doLeavePage() {
        Logf.dc("page has disappeared");
        if (mAnim != null) {
            mAnim.pause();
        }
    }
}
