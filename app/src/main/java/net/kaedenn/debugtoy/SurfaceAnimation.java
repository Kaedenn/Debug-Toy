package net.kaedenn.debugtoy;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.Surface;
import android.view.SurfaceHolder;

import net.kaedenn.debugtoy.util.RandomUtil;

import org.jetbrains.annotations.NotNull;

import java.util.TimerTask;

class SurfaceAnimation extends TimerTask {
    private final SurfaceHolder holder;
    private final Rect surfaceRect;
    private boolean isDrawing = false;
    private boolean isPaused = false;

    private static final int PARTICLE_SIZE = 10;
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

    private final Particle[] particles;

    private void resetParticle(@NotNull Particle p) {
        p.x = RandomUtil.getInstance().range(surfaceRect.left, surfaceRect.right);
        p.y = 0;
        p.dx = RandomUtil.getInstance().range(-PARTICLE_DX_RANGE/2f, PARTICLE_DX_RANGE/2f);
        p.dy = 0;
    }

    SurfaceAnimation(@NotNull SurfaceHolder sh) {
        holder = sh;
        surfaceRect = sh.getSurfaceFrame();
        particles = new Particle[25];
        for (int i = 0; i < particles.length; ++i) {
            particles[i] = new Particle(0, 0, PARTICLE_SIZE, PARTICLE_SIZE);
            resetParticle(particles[i]);
        }
    }

    @Override
    public void run() {
        animate();
        drawNextFrame();
    }

    void pause() {
        isPaused = true;
    }

    void unpause() {
        isPaused = false;
    }

    private int getWidth() {
        return surfaceRect.right - surfaceRect.left;
    }

    private int getHeight() {
        return surfaceRect.bottom - surfaceRect.top;
    }

    private void animate() {
        for (Particle p : particles) {
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
        if (isPaused) return;
        isDrawing = true;
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        canvas.clipRect(r);
        canvas.drawColor(Color.BLACK);
        for (Particle p : particles) {
            canvas.drawRect(p.x, p.y, p.x+p.w, p.y+p.h, paint);
        }
        isDrawing = false;
    }

    private synchronized void drawNextFrame() {
        if (!isDrawing) {
            Rect r = holder.getSurfaceFrame();
            Surface s = holder.getSurface();
            Canvas c = s.lockCanvas(r);
            draw(c, r);
            s.unlockCanvasAndPost(c);
        }
    }
}
