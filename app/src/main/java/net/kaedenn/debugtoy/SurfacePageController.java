package net.kaedenn.debugtoy;

import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.util.Timer;

/** Controller class for the second page's surface.
 *
 */
class SurfacePageController {
    private final MainActivity mActivity;
    private final Timer mTimer;
    private SurfaceAnimation mAnim = null;

    private static final String LOG_TAG = "surf";

    SurfacePageController() {
        mActivity = MainActivity.getInstance();
        mTimer = new Timer("mAnim", true);

        SurfaceHolder sh = getSurfaceView().getHolder();
        sh.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                Log.i(LOG_TAG, "surfaceCreated with holder " + holder.toString());
                mAnim = new SurfaceAnimation(holder);
                mTimer.scheduleAtFixedRate(mAnim, 0, 20);
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                Log.i(LOG_TAG, String.format("surfaceChanged with holder %s format=%d w=%d h=%d",
                        holder.toString(), format, width, height));
                /* TODO: Determine if redrawFrame is needed */
                if (mAnim == null) {
                    Log.e(LOG_TAG, "onSurfaceChanged with null mAnim!!");
                }
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                Log.i(LOG_TAG, "surfaceDestroyed with holder " + holder.toString());
                mTimer.cancel();
                mTimer.purge();
                mAnim = null;
            }
        });
    }

    /** Convenience function to get the managed SurfaceView.
     *
     * @return The SurfaceView this class manages
     */
    private SurfaceView getSurfaceView() {
        return mActivity.findViewById(R.id.page2Surface);
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
        mAnim.pause();
    }
}
