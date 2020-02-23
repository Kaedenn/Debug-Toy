package net.kaedenn.debugtoy;

import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import net.kaedenn.debugtoy.util.Logf;

import java.util.Timer;

/** Controller class for the second page's surface.
 *
 */
class SurfacePageController implements SurfaceHolder.Callback {
    private final Timer mTimer;
    private SurfaceAnimation mAnim = null;
    private boolean mTimerActive = false;

    private static final String LOG_TAG = "surfacePage";

    SurfacePageController() {
        mTimer = new Timer("mAnim", true);
        getSurfaceView().getHolder().addCallback(this);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        Logf.i(LOG_TAG, "surfaceCreated with holder %s", holder.toString());
        mAnim = new SurfaceAnimation(holder);
        mTimer.scheduleAtFixedRate(mAnim, 0, 20);
        mTimerActive = true;
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Logf.i(LOG_TAG, "surfaceChanged with holder %s format=%d w=%d h=%d",
                holder.toString(), format, width, height);
        /* TODO: Determine if redrawFrame is needed */
        if (mAnim == null) {
            Log.e(LOG_TAG, "onSurfaceChanged with null mAnim!!");
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        Logf.i(LOG_TAG, "surfaceDestroyed with holder %s", holder.toString());
        if (mTimerActive) {
            mTimer.cancel();
            mTimer.purge();
            mAnim = null;
            mTimerActive = false;
        }
    }

    public boolean isTimerActive() {
        return mTimerActive;
    }

    /** Convenience function to get the managed SurfaceView.
     *
     * @return The SurfaceView this class manages
     */
    private SurfaceView getSurfaceView() {
        return MainActivity.getInstance().findViewById(R.id.page2Surface);
    }

    /** Called when the surface appears.
     *
     * This method is called when the containing page goes from either
     * {@value View#GONE} or {@value View#INVISIBLE}to {@value View#VISIBLE}.
     */
    void doAppear() {
        Log.d(LOG_TAG, "page has appeared");
        if (mAnim != null) {
            mAnim.unpause();
        }
    }

    /** Called when the surface disappears.
     *
     * This method is called when the containing page goes from
     * {@value View#GONE} to either  {@value View#INVISIBLE} or
     * {@value View#VISIBLE}.
     */
    void doDisappear() {
        Log.d(LOG_TAG, "page has disappeared");
        if (mAnim != null) {
            mAnim.pause();
        }
    }
}
