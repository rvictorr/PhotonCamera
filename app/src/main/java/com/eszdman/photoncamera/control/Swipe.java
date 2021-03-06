package com.eszdman.photoncamera.control;

import android.graphics.RectF;
import android.hardware.camera2.CaptureRequest;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.eszdman.photoncamera.R;
import com.eszdman.photoncamera.api.CameraReflectionApi;
import com.eszdman.photoncamera.app.PhotonCamera;

public class Swipe {
    private static final String TAG = "Swipe";
    private GestureDetector gestureDetector;
    FrameLayout manualMode;
    ImageView ocManual;
    private static int arrowState;
    Animation slideUp;
    Animation slideDown;

    public void init() {
        Log.d(TAG, "SwipeDetection - ON");
        manualMode = PhotonCamera.getCameraActivity().findViewById(R.id.manual_mode);
        ocManual = PhotonCamera.getCameraActivity().findViewById(R.id.open_close_manual);
        ocManual.setOnClickListener((v) -> {
            if (arrowState == 0) {
                SwipeUp();
                Log.d(TAG, "Arrow Clicked:SwipeUp");
            } else {
                SwipeDown();
                Log.d(TAG, "Arrow Clicked:SwipeDown");
            }
        });
        slideUp = AnimationUtils.loadAnimation(PhotonCamera.getCameraActivity(), R.anim.slide_up);
        slideDown = AnimationUtils.loadAnimation(PhotonCamera.getCameraActivity(), R.anim.animate_slide_down_exit);
        gestureDetector = new GestureDetector(PhotonCamera.getCameraActivity(), new GestureDetector.SimpleOnGestureListener() {
            private static final int SWIPE_THRESHOLD = 100;
            private static final int SWIPE_VELOCITY_THRESHOLD = 100;

            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                startTouchToFocus(e);
                return false;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                float diffY = e2.getY() - e1.getY();
                float diffX = e2.getX() - e1.getX();
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            Log.d(TAG, "Right");
                            SwipeRight();
                        } else {
                            Log.d(TAG, "Left");
                            SwipeLeft();
                        }
                        return true;
                    }
                } else if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffY > 0) {
                        Log.d(TAG, "Bottom");//it swipes from top to bottom
                        SwipeDown();
                    } else {
                        Log.d(TAG, "Top");//it swipes from bottom to top
                        SwipeUp();
                    }
                    return true;
                }
                return false;
            }
        });
        View.OnTouchListener touchListener = (view, motionEvent) -> gestureDetector.onTouchEvent(motionEvent);
        View holder = PhotonCamera.getCameraActivity().findViewById(R.id.textureHolder);
        Log.d(TAG, "input:" + holder);
        if (holder != null) holder.setOnTouchListener(touchListener);
    }

    private void startTouchToFocus(MotionEvent event) {
        //takes into consideration the top and bottom translation of camera_container(if it has been moved due to different display ratios)
        // for calculation of size of viewfinder RectF.(for touch focus detection)
        ConstraintLayout camera_container = PhotonCamera.getCameraActivity().findViewById(R.id.camera_container);
        ConstraintLayout layout_viewfinder = PhotonCamera.getCameraActivity().findViewById(R.id.layout_viewfinder);
        RectF viewfinderRect = new RectF(
                layout_viewfinder.getLeft(),//left edge of viewfinder
                camera_container.getY(), //y position of camera_container
                layout_viewfinder.getRight(), //right edge of viewfinder
                layout_viewfinder.getBottom() + camera_container.getY() //bottom edge of viewfinder + y position of camera_container
        );
        // Interface.getCameraFragment().showToast(previewRect.toString()+"\nCurX"+event.getX()+"CurY"+event.getY());
        if (viewfinderRect.contains(event.getX(), event.getY())) {
            float translateX = event.getX() - camera_container.getLeft();
            float translateY = event.getY() - camera_container.getTop();
            if (PhotonCamera.getManualMode().getCurrentFocusValue() == -1.0)
                PhotonCamera.getTouchFocus().processTouchToFocus(translateX, translateY);
        }
    }

    public void SwipeUp() {
        if (!PhotonCamera.getSettings().ManualMode) {
            manualMode.startAnimation(slideUp);
            PhotonCamera.getSettings().ManualMode = true;
            ocManual.animate().rotation(180).setDuration(250).start();
        }
        PhotonCamera.getCameraFragment().rebuildPreview();
        manualMode.setVisibility(View.VISIBLE);
        arrowState ^= 1;
    }

    public void SwipeDown() {
        if (PhotonCamera.getSettings().ManualMode) {
            manualMode.startAnimation(slideDown);
            ocManual.animate().rotation(0).setDuration(250).start();
        }
        PhotonCamera.getSettings().ManualMode = false;

        CameraReflectionApi.set(PhotonCamera.getCameraFragment().mPreviewRequest, CaptureRequest.CONTROL_AE_MODE, PhotonCamera.getSettings().aeModeOn);
        CameraReflectionApi.set(PhotonCamera.getCameraFragment().mPreviewRequest, CaptureRequest.CONTROL_AF_MODE, PhotonCamera.getSettings().afMode);
        PhotonCamera.getCameraFragment().rebuildPreview();
        manualMode.setVisibility(View.GONE);
        PhotonCamera.getManualMode().retractAllKnobs();
        arrowState ^= 1;
    }

    public void SwipeRight() {

    }

    public void SwipeLeft() {

    }
}
