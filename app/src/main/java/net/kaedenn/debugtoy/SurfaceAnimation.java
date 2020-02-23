package net.kaedenn.debugtoy;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.Surface;
import android.view.SurfaceHolder;

import net.kaedenn.debugtoy.util.Logf;
import net.kaedenn.debugtoy.util.RandUtil;
import net.kaedenn.debugtoy.util.Res;

import org.jetbrains.annotations.NotNull;

import java.util.TimerTask;

class SurfaceAnimation extends TimerTask {

    private static final String LOG_TAG = "surfaceAnim";
    static {
        Logf.getInstance().add(SurfaceAnimation.class, LOG_TAG);
    }

    private SurfaceHolder mHolder;
    private Rect mSurfaceRect;
    private boolean mIsDrawing = false;
    private boolean mIsPaused = false;

    private static final int PARTICLE_DX_RANGE = 20;
    private static final float PARTICLE_DXDT = 0f;
    private static final float PARTICLE_DYDT = 0.2f;

    @SuppressWarnings({"CanBeFinal", "SameParameterValue"})
    class Particle {
        final float w, h;
        float x, y;
        float dx, dy;
        Particle(int px, int py, int pw, int ph) {
            x = px;
            y = py;
            w = pw;
            h = ph;
            dx = dy = 0;
        }
        void move() {
            x += dx;
            y += dy;
        }
        void push(float dxv, float dyv) {
            dx += dxv;
            dy += dyv;
        }

        @SuppressWarnings("unused")
        @SuppressLint("DefaultLocale")
        public String toDebugString() {
            return String.format("Particle({pos:[%g,%g], vel:[%g, %g], size:[%g, %g]})", x, y, dx, dy, w, h);
        }
    }

    private final Particle[] mParticles;

    private Point getDefaultParticleSize() {
        return new Point(Res.getInteger(R.integer.pSizeX), Res.getInteger(R.integer.pSizeY));
    }

    private void resetParticle(@NotNull Particle p) {
        p.x = RandUtil.getRange(mSurfaceRect.left, mSurfaceRect.right);
        p.y = 0;
        p.dx = RandUtil.getRange(-PARTICLE_DX_RANGE/2f, PARTICLE_DX_RANGE/2f);
        p.dy = 0;
    }

    SurfaceAnimation(@NotNull SurfaceHolder sh) {
        mHolder = sh;
        mSurfaceRect = sh.getSurfaceFrame();
        mParticles = new Particle[25];
        for (int i = 0; i < mParticles.length; ++i) {
            Point pSize = getDefaultParticleSize();
            mParticles[i] = new Particle(0, 0, pSize.x, pSize.y);
            resetParticle(mParticles[i]);
        }

        /*
        funcNative();
        float[] bounds = new float[2];
        bounds[0] = mSurfaceRect.right;
        bounds[1] = mSurfaceRect.bottom;
        float[] ddxy = new float[2];
        ddxy[0] = 0f;
        ddxy[1] = 0f;

        animateNative(mParticles, bounds, ddxy);
        */
    }

    void updateHolder(SurfaceHolder holder) {
        mHolder = holder;
        mSurfaceRect = mHolder.getSurfaceFrame();
    }

    @Override
    public void run() {
        if (!mIsPaused) {
            animate();
            drawNextFrame();
        }
    }

    void pause() {
        mIsPaused = true;
    }

    void unpause() {
        mIsPaused = false;
    }

    private int getWidth() {
        return mSurfaceRect.right - mSurfaceRect.left;
    }

    private int getHeight() {
        return mSurfaceRect.bottom - mSurfaceRect.top;
    }

    private void animate() {
        /* Replace with JNI
         animate([LParticle;LPoint;)V
         */
        for (Particle p : mParticles) {
            if (p.x + p.w < 0) resetParticle(p);
            else if (p.x - p.w > getWidth()) resetParticle(p);
            else if (p.y + p.h < 0) resetParticle(p);
            else if (p.y - p.h > getHeight()) {
                p.y = getHeight() - p.h;
                p.dy = -p.dy;
            } else {
                p.move();
                p.push(PARTICLE_DXDT, PARTICLE_DYDT);
            }
        }
    }

    private void draw(@NotNull Canvas canvas, Rect r) {
        if (mIsPaused) return;
        mIsDrawing = true;
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        canvas.clipRect(r);
        canvas.drawColor(Res.getColor(R.color.colorPrimary));
        for (Particle p : mParticles) {
            canvas.drawRect(p.x, p.y, p.x+p.w, p.y+p.h, paint);
        }
        mIsDrawing = false;
    }

    private synchronized void drawNextFrame() {
        if (!mIsDrawing) {
            Rect r = mHolder.getSurfaceFrame();
            Surface s = mHolder.getSurface();
            Canvas c = s.lockCanvas(r);
            draw(c, r);
            s.unlockCanvasAndPost(c);
        }
    }

    /*
    private native void animateNative(Particle[] particles, float[] wh, float[] ddxy);
    private native void funcNative();
    */
}
