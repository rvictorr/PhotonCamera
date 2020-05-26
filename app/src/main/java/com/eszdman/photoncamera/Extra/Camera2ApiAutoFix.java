package com.eszdman.photoncamera.Extra;

import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.params.BlackLevelPattern;
import android.hardware.camera2.params.RggbChannelVector;
import android.util.Log;
import android.util.Rational;
import com.eszdman.photoncamera.Camera2Api;

import java.lang.reflect.Field;

import static android.hardware.camera2.CaptureResult.*;

public class Camera2ApiAutoFix {
    private String TAG = "Camera2ApiAutoFix";
    private CameraCharacteristics characteristics;
    private CaptureResult result;
    Camera2ApiAutoFix(CameraCharacteristics characteristic) {
        characteristics = characteristic;
    }
    Camera2ApiAutoFix(CaptureResult res) {
        result = res;
    }
    public static void Apply(){
        CameraCharacteristics  characteristics= Camera2Api.mCameraCharacteristics;
        Camera2ApiAutoFix fix = new Camera2ApiAutoFix(characteristics);
    }
    public static void ApplyRes(){
        CaptureResult characteristics= Camera2Api.mCaptureResult;
        Camera2ApiAutoFix fix = new Camera2ApiAutoFix(characteristics);
        fix.gains();
        fix.dynBL();
    }
    public void gains(){
        CameraReflectionApi.setVERBOSE(true);
        Rational[] WB = result.get(SENSOR_NEUTRAL_COLOR_POINT);
        if(WB == null) return;
        RggbChannelVector rggbChannelVector = result.get(COLOR_CORRECTION_GAINS);
        if(rggbChannelVector == null){
            CameraReflectionApi.set(COLOR_CORRECTION_GAINS,new RggbChannelVector(WB[0].floatValue()*1.3f,WB[1].floatValue()/1.78f,WB[1].floatValue()/1.78f,WB[2].floatValue()*2f));
        }
        if(rggbChannelVector.getRed() == 1.0f)
        try {
            Field field = rggbChannelVector.getClass().getDeclaredField("mRed");
            field.setAccessible(true);
            field.set(rggbChannelVector, 1f/WB[0].floatValue());
            field = rggbChannelVector.getClass().getDeclaredField("mGreenEven");
            field.setAccessible(true);
            field.set(rggbChannelVector, 1f/WB[1].floatValue());
            field = rggbChannelVector.getClass().getDeclaredField("mGreenOdd");
            field.setAccessible(true);
            field.set(rggbChannelVector, 1f/WB[1].floatValue());
            field = rggbChannelVector.getClass().getDeclaredField("mBlue");
            field.setAccessible(true);
            field.set(rggbChannelVector, 1f/WB[2].floatValue());
            CameraReflectionApi.set(COLOR_CORRECTION_GAINS,null);
            CameraReflectionApi.set(COLOR_CORRECTION_GAINS,rggbChannelVector);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
        Log.d(TAG,"Overrided channelVector:"+rggbChannelVector.toString());
    }
    public void dynBL(){
       float[] level = result.get(SENSOR_DYNAMIC_BLACK_LEVEL);
        BlackLevelPattern ptr = Camera2Api.mCameraCharacteristics.get(CameraCharacteristics.SENSOR_BLACK_LEVEL_PATTERN);
        if(ptr == null) return;
       if(level == null){
           level = new float[4];
           for(int i =0; i<4; i++){
               level[i] = ptr.getOffsetForIndex(i%2,i/2);
           }
           CameraReflectionApi.set(SENSOR_DYNAMIC_BLACK_LEVEL,level);
           return;
       }
    }
}