package com.manual.model;

import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.util.Range;

import androidx.annotation.NonNull;

import com.eszdman.photoncamera.R;
import com.eszdman.photoncamera.app.PhotonCamera;
import com.manual.KnobInfo;
import com.manual.KnobItemInfo;
import com.manual.KnobView;
import com.manual.KnobViewChangedListener;
import com.manual.ManualMode;
import com.manual.ShadowTextDrawable;

import java.util.ArrayList;
import java.util.List;

public abstract class ManualModel<T extends Comparable<? super T>> implements KnobViewChangedListener, IModel {

    private final int SET_TO_CAM = 1;
    //    private Handler mainHandler;
    private final HandlerThread mBackgroundThread;

    /*  private class UpdateTextHandler extends Handler
      {
          public UpdateTextHandler(Looper mainLooper) {
              super(mainLooper);
          }

          @Override
          public void handleMessage(@NonNull Message msg) {
              if (msg.arg1 == UPDATE_TEXT) {
                  if (valueChangedEvent != null)
                      valueChangedEvent.onValueChanged((String)msg.obj);
              }
          }
      }*/
    protected Range<T> range;
    protected KnobInfo knobInfo;
    private final List<KnobItemInfo> knobInfoList;
    private final ValueChangedEvent valueChangedEvent;
    private final Handler backgroundHandler;
    //    private final int UPDATE_TEXT = 1;
    protected KnobItemInfo currentInfo, autoModel;

    public ManualModel(Range range, ValueChangedEvent valueChangedEvent) {
        this.range = range;
        this.valueChangedEvent = valueChangedEvent;
        knobInfoList = new ArrayList<>();
        mBackgroundThread = new HandlerThread(ManualMode.class.getName());
        mBackgroundThread.start();
        backgroundHandler = new ManualModelHandler(mBackgroundThread.getLooper());
//        mainHandler = new UpdateTextHandler(Looper.getMainLooper());
        fillKnobInfoList();
    }

    @Override
    protected void finalize() throws Throwable {
        mBackgroundThread.quitSafely();
        super.finalize();
    }

    public void fireValueChangedEvent(final String txt) {
        if (valueChangedEvent != null)
            valueChangedEvent.onValueChanged(txt);
       /* Message msg = new Message();
        msg.arg1 = UPDATE_TEXT;
        msg.obj = txt;
        mainHandler.sendMessage(msg);*/
    }

    public KnobItemInfo getAutoModel() {
        return autoModel;
    }

    protected KnobItemInfo getNewAutoItem(double defaultVal, String defaultText) {
        ShadowTextDrawable autoDrawable = new ShadowTextDrawable();
        String auto_string = PhotonCamera.getCameraActivity().getString(R.string.manual_mode_auto);
        if (defaultText != null)
            auto_string = defaultText;
        autoDrawable.setText(auto_string);
        autoDrawable.setTextAppearance(PhotonCamera.getCameraActivity(), R.style.ManualModeKnobText);
        ShadowTextDrawable autoDrawableSelected = new ShadowTextDrawable();
        autoDrawableSelected.setText(auto_string);
        autoDrawableSelected.setTextAppearance(PhotonCamera.getCameraActivity(), R.style.ManualModeKnobTextSelected);
        StateListDrawable autoStateDrawable = new StateListDrawable();
        autoStateDrawable.addState(new int[]{-16842913}, autoDrawable);
        autoStateDrawable.addState(new int[]{-16842913}, autoDrawableSelected);
        autoModel = new KnobItemInfo(autoStateDrawable, auto_string, 0, defaultVal);
        return autoModel;
    }

    public KnobItemInfo getItemInfo(String text, double val, int tick) {
        ShadowTextDrawable autoDrawable = new ShadowTextDrawable();
        autoDrawable.setText(text);
        autoDrawable.setTextAppearance(PhotonCamera.getCameraActivity(), R.style.ManualModeKnobText);
        ShadowTextDrawable autoDrawableSelected = new ShadowTextDrawable();
        autoDrawableSelected.setText(text);
        autoDrawableSelected.setTextAppearance(PhotonCamera.getCameraActivity(), R.style.ManualModeKnobTextSelected);
        StateListDrawable autoStateDrawable = new StateListDrawable();
        autoStateDrawable.addState(new int[]{-16842913}, autoDrawable);
        autoStateDrawable.addState(new int[]{-16842913}, autoDrawableSelected);
        return new KnobItemInfo(autoStateDrawable, text, tick, val);
    }

    protected abstract void fillKnobInfoList();

    @Override
    public List<KnobItemInfo> getKnobInfoList() {
        return knobInfoList;
    }

    @Override
    public KnobItemInfo getCurrentInfo() {
        return currentInfo;
    }

    @Override
    public KnobInfo getKnobInfo() {
        return knobInfo;
    }

    @Override
    public void onSelectedKnobItemChanged(KnobView knobView, KnobItemInfo knobItemInfo, final KnobItemInfo knobItemInfo2) {
        Log.d(ManualMode.class.getSimpleName(), "onSelectedKnobItemChanged");
        if (knobItemInfo == knobItemInfo2)
            return;
        Message msg = new Message();
        msg.arg1 = SET_TO_CAM;
        msg.obj = knobItemInfo2;
        backgroundHandler.sendMessage(msg);
        fireValueChangedEvent(knobItemInfo2.text);
        //backgroundHandler.post(()->onSelectedKnobItemChanged(knobItemInfo2));
        //backgroundHandler.post(()->onSelectedKnobItemChanged(knobItemInfo2));
    }

    public void resetModel() {
        onSelectedKnobItemChanged(null, null, autoModel);
    }

    public abstract void onSelectedKnobItemChanged(KnobItemInfo knobItemInfo2);

    public interface ValueChangedEvent {
        void onValueChanged(String value);
    }

    private class ManualModelHandler extends Handler {
        public ManualModelHandler(@NonNull Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg.arg1 == SET_TO_CAM) {
                onSelectedKnobItemChanged((KnobItemInfo) msg.obj);
                //fireValueChangedEvent(((KnobItemInfo) msg.obj).text);
            }
        }
    }
}
