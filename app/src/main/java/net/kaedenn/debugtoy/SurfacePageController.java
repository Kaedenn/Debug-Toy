package net.kaedenn.debugtoy;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.util.Timer;
import java.util.TimerTask;

/** Controller class for the second page's surface.
 *
 */
public class SurfacePageController {
    private MainActivity main;
    private Timer animTimer;

    private static String LOG_TAG = "surf";

    SurfacePageController(MainActivity mainActivity) {
        main = mainActivity;
        animTimer = new Timer("anim", true);

        SurfaceHolder sh = getSurfaceView().getHolder();
        sh.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                onSurfaceCreated(holder);
                animTimer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        SurfacePageController.this.drawFrame(false);
                    }
                }, 0, 200);
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                onSurfaceChanged(holder, format, width, height);
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                onSurfaceDestroyed(holder);
                animTimer.cancel();
            }
        });
    }

    /** Process the surface holder's {@code surfaceCreated} event.
     *
     * @param holder The SurfaceHolder whose surface has changed
     *
     * @see SurfaceHolder.Callback#surfaceCreated(SurfaceHolder)
     */
    private void onSurfaceCreated(SurfaceHolder holder) {
        Log.d(LOG_TAG, "surface created");
    }

    /** Process the surface holder's {@code surfaceChanged} event.
     *
     * @param holder The SurfaceHolder whose surface has changed
     * @param format The pixel format of the surface
     * @param width The width of the surface
     * @param height The height of the surface
     *
     * @see SurfaceHolder.Callback#surfaceCreated(SurfaceHolder)
     */
    private void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d(LOG_TAG, "surface changed");
        drawFrame(true);
    }

    /** Process the surface holder's {@code surfaceDestroyed} event.
     *
     * @param holder The SurfaceHolder whose surface has changed
     *
     * @see SurfaceHolder.Callback#surfaceCreated(SurfaceHolder)
     */
    private void onSurfaceDestroyed(SurfaceHolder holder) {
        Log.d(LOG_TAG, "surface destroyed");
    }

    /** Convenience function to get the managed SurfaceView.
     *
     * @return The SurfaceView this class manages
     */
    public SurfaceView getSurfaceView() {
        return main.findViewById(R.id.page2Surface);
    }

    /** Called when the surface appears.
     *
     * This method is called when the containing page goes from either
     * {@value View#GONE} or {@value View#INVISIBLE}to {@value View#VISIBLE}.
     */
    public void doAppear() {
        Log.d(LOG_TAG, "page has appeared");
    }

    /** Called when the surface disappears.
     *
     * This method is called when the containing page goes from
     * {@value View#GONE} to either  {@value View#INVISIBLE} or
     * {@value View#VISIBLE}.
     */
    public void doDisappear() {
        Log.d(LOG_TAG, "page has disappeared");
    }

    public synchronized void drawFrame(boolean isRedraw) {
        Log.d(LOG_TAG, "drawing frame, redraw? " + isRedraw);
        SurfaceHolder holder = getSurfaceView().getHolder();
        Rect r = holder.getSurfaceFrame();
        Surface s = holder.getSurface();
        Canvas c = s.lockCanvas(r);
        c.clipRect(r);
        c.drawColor(Color.argb(1.0f, 0.0f, 0.0f, 1.0f));
        s.unlockCanvasAndPost(c);
    }
}
