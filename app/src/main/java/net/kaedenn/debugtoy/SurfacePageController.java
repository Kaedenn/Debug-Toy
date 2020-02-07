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
    private MainActivity main;
    private Timer animTimer;
    private SurfaceAnimation anim = null;

    private static String LOG_TAG = "surf";

    SurfacePageController(MainActivity mainActivity) {
        main = mainActivity;
        animTimer = new Timer("anim", true);

        SurfaceHolder sh = getSurfaceView().getHolder();
        sh.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                Log.i(LOG_TAG, "surfaceCreated with holder " + holder.toString());
                anim = new SurfaceAnimation(holder);
                animTimer.scheduleAtFixedRate(anim, 0, 20);
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                Log.i(LOG_TAG, String.format("surfaceChanged with holder %s format=%d w=%d h=%d",
                        holder.toString(), format, width, height));
                /* TODO: Determine if redrawFrame is needed */
                if (anim == null) {
                    Log.e(LOG_TAG, "onSurfaceChanged with null anim!!");
                }
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                Log.i(LOG_TAG, "surfaceDestroyed with holder " + holder.toString());
                animTimer.cancel();
                animTimer.purge();
                anim = null;
            }
        });
    }

    /** Convenience function to get the managed SurfaceView.
     *
     * @return The SurfaceView this class manages
     */
    private SurfaceView getSurfaceView() {
        return main.findViewById(R.id.page2Surface);
    }

    /** Called when the surface appears.
     *
     * This method is called when the containing page goes from either
     * {@value View#GONE} or {@value View#INVISIBLE}to {@value View#VISIBLE}.
     */
    void doAppear() {
        Log.d(LOG_TAG, "page has appeared");
        if (anim != null) {
            anim.unpause();
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
        anim.pause();
    }
}
