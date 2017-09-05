package com.flurgle.camerakit;

import android.content.Context;
import android.util.SparseIntArray;
import android.view.Display;
import android.view.OrientationEventListener;
import android.view.Surface;

public abstract class DisplayOrientationDetector {

    private final OrientationEventListener mOrientationEventListener;

    static final SparseIntArray DISPLAY_ORIENTATIONS = new SparseIntArray();
    static {
        DISPLAY_ORIENTATIONS.put(Surface.ROTATION_0, 0);
        DISPLAY_ORIENTATIONS.put(Surface.ROTATION_90, 90);
        DISPLAY_ORIENTATIONS.put(Surface.ROTATION_180, 180);
        DISPLAY_ORIENTATIONS.put(Surface.ROTATION_270, 270);
    }

    private Display mDisplay;

    private int mLastKnownDisplayOrientation = 0;
    private int mLastKnownDeviceOrientation = 0;

    public DisplayOrientationDetector(Context context) {
        mOrientationEventListener = new OrientationEventListener(context) {

            private int mLastKnownDisplayRotation = -1;

            @Override
            public void onOrientationChanged(int orientation) {
                if (orientation == OrientationEventListener.ORIENTATION_UNKNOWN || mDisplay == null) {
                    return;
                }

                boolean somethingChanged = false;

                final int displayRotation = mDisplay.getRotation();
                if (mLastKnownDisplayRotation != displayRotation) {
                    mLastKnownDisplayRotation = displayRotation;
                    somethingChanged = true;
                }

                int deviceOrientation;
                if (orientation >= 60 && orientation <= 140){
                    // the mDisplay.getRotation stuff is flipped for 90 & 270 vs. deviceOrientation here. This keeps it consistent.
                    deviceOrientation = 270;
                } else if (orientation >= 140 && orientation <= 220) {
                    deviceOrientation = 180;
                } else if (orientation >= 220 && orientation <= 300) {
                    // the mDisplay.getRotation stuff is flipped for 90 & 270 vs. deviceOrientation here. This keeps it consistent.
                    deviceOrientation = 90;
                } else {
                    deviceOrientation = 0;
                }

                if (mLastKnownDeviceOrientation != deviceOrientation) {
                    mLastKnownDeviceOrientation = deviceOrientation;
                    somethingChanged = true;
                }

                if(somethingChanged){
                    dispatchOnDisplayOrientationChanged(DISPLAY_ORIENTATIONS.get(displayRotation));
                }
            }

        };
    }

    public void enable(Display display) {
        mDisplay = display;
        mOrientationEventListener.enable();
        dispatchOnDisplayOrientationChanged(DISPLAY_ORIENTATIONS.get(display.getRotation()));
    }

    public void disable() {
        mOrientationEventListener.disable();
        mDisplay = null;
    }

    public int getLastKnownDisplayOrientation() {
        return mLastKnownDisplayOrientation;
    }

    void dispatchOnDisplayOrientationChanged(int displayOrientation) {
        mLastKnownDisplayOrientation = displayOrientation;

        // If we don't have accelerometers, we can't detect the device orientation.
        if(mOrientationEventListener.canDetectOrientation()){
            onDisplayOrientationChanged(displayOrientation, mLastKnownDeviceOrientation);
        } else {
            onDisplayOrientationChanged(displayOrientation, displayOrientation);
        }

    }

    public abstract void onDisplayOrientationChanged(int displayOrientation, int deviceOrientation);

}