package net.kaedenn.debugtoy;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.Surface;
import android.view.SurfaceHolder;

import androidx.annotation.NonNull;

import java.util.Random;
import java.util.TimerTask;

class DTAnimation extends TimerTask {
    private SurfaceHolder holder;
    private Rect surfaceRect;
    private int frameCount = 0;
    private boolean isDrawing = false;
    private boolean isPaused = false;
    private Random rand = new Random();

    private static int PARTICLE_SIZE = 5;
    private static int PARTICLE_DX_RANGE = 20;
    private static int PARTICLE_DY_RANGE = 10;
    private static float PARTICLE_DX_ACCEL = 0f;
    private static float PARTICLE_DY_ACCEL = 0.2f;

    class Particle {
        float x, y, w, h;
        float dx, dy;
        Particle(int x, int y, int w, int h) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            dx = dy = 0;
        }
        void move() {
            x += dx;
            y += dy;
        }
        void push(float dx, float dy) {
            this.dx += dx;
            this.dy += dy;
        }

        @SuppressWarnings("unused")
        @SuppressLint("DefaultLocale")
        public String toDebugString() {
            return String.format("Particle(%g, %g, %g, %g %g, %g", x, y, w, h, dx, dy);
        }
    }

    private Particle[] particles;

    private void resetParticle(Particle p) {
        p.x = rand.nextInt(surfaceRect.right - surfaceRect.left) + surfaceRect.left;
        p.y = 0;
        p.dx = rand.nextInt(PARTICLE_DX_RANGE) - PARTICLE_DX_RANGE/2.f;
        p.dy = 0; //rand.nextInt(PARTICLE_DY_RANGE) * 1.f;
    }

    DTAnimation(SurfaceHolder sh) {
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
                p.push(PARTICLE_DX_ACCEL, PARTICLE_DY_ACCEL);
            }
        }
    }

    private void draw(Canvas canvas, Rect r) {
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
            frameCount += 1;
            Rect r = holder.getSurfaceFrame();
            Surface s = holder.getSurface();
            Canvas c = s.lockCanvas(r);
            draw(c, r);
            s.unlockCanvasAndPost(c);
        }
    }

    /* TODO: Determine if this is really needed
    public synchronized void redrawFrame(int format, int width, int height) {
        Rect r = holder.getSurfaceFrame();
        Surface s = holder.getSurface();
        Canvas c = s.lockCanvas(r);
        s.unlockCanvasAndPost(c);
    }
     */


}
