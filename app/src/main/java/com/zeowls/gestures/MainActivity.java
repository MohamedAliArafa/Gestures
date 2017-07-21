package com.zeowls.gestures;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FrameLayout frame = (FrameLayout) findViewById(R.id.graphics_holder);
        PlayAreaView image = new PlayAreaView(this);
        image.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        frame.addView(image);
    }

    private class PlayAreaView extends View {
        private final String DEBUG_TAG = PlayAreaView.class.getSimpleName();
        private final GestureDetector gestures;
        private Matrix translate;
        private Bitmap droid;
        private Matrix animateStart;
        private Interpolator animateInterpolator;
        private long startTime;
        private long endTime;
        private float totalAnimDx;
        private float totalAnimDy;


        public PlayAreaView(Context context) {
            super(context);
            translate = new Matrix();
            gestures = new GestureDetector(MainActivity.this,
                    new GestureListener(this));
            droid = BitmapFactory.decodeResource(getResources(),
                    R.mipmap.ic_launcher);
        }

        public void onMove(float dx, float dy) {
            translate.postTranslate(dx, dy);
            invalidate();
        }

        public void onResetLocation() {
            translate.reset();
            invalidate();
        }

        public void onAnimateMove(float dx, float dy, long duration) {
            animateStart = new Matrix(translate);
            animateInterpolator = new OvershootInterpolator();
            startTime = System.currentTimeMillis();
            endTime = startTime + duration;
            totalAnimDx = dx;
            totalAnimDy = dy;
            post(new Runnable() {
                @Override
                public void run() {
                    onAnimateStep();
                }
            });
        }

        private void onAnimateStep() {
            long curTime = System.currentTimeMillis();
            float percentTime = (float) (curTime - startTime)
                    / (float) (endTime - startTime);
            float percentDistance = animateInterpolator
                    .getInterpolation(percentTime);
            float curDx = percentDistance * totalAnimDx;
            float curDy = percentDistance * totalAnimDy;
            translate.set(animateStart);
            onMove(curDx, curDy);

            if (percentTime < 1.0f) {
                post(new Runnable() {
                    @Override
                    public void run() {
                        onAnimateStep();
                    }
                });
            }

            Log.v(DEBUG_TAG, "We're " + percentDistance + " of the way there!");
        }

        protected void onDraw(Canvas canvas) {
            canvas.drawBitmap(droid, translate, null);
            Matrix m = canvas.getMatrix();
            Log.d(DEBUG_TAG, "Matrix: " + translate.toShortString());
            Log.d(DEBUG_TAG, "Canvas: " + m.toShortString());
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            return gestures.onTouchEvent(event);
        }
    }

    private class GestureListener implements GestureDetector.OnGestureListener,
            GestureDetector.OnDoubleTapListener {
        private final String DEBUG_TAG = GestureListener.class.getSimpleName();
        PlayAreaView view;

        public GestureListener(PlayAreaView view) {
            this.view = view;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            Log.v(DEBUG_TAG, "onDoubleTap");
            view.onResetLocation();
            return true;
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            Log.v(DEBUG_TAG, "onDown");
            return true;
        }

        @Override
        public void onShowPress(MotionEvent e) {

        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            Log.v(DEBUG_TAG, "onScroll");
            view.onMove(-distanceX, -distanceY);
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {

        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            Log.v(DEBUG_TAG, "onFling");
            final float distanceTimeFactor = 0.2f;
            final float totalDx = (distanceTimeFactor * velocityX / 2);
            final float totalDy = (distanceTimeFactor * velocityY / 2);

            view.onAnimateMove(totalDx, totalDy, (long) (1000 * distanceTimeFactor));
            return true;
        }
    }
}
