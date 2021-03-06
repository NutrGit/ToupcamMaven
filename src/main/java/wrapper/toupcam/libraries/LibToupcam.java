package wrapper.toupcam.libraries;

import com.sun.jna.Library;
import com.sun.jna.Pointer;
import wrapper.toupcam.callbacks.EventCallback;
import wrapper.toupcam.callbacks.PITOUPCAM_EXPOSURE_CALLBACK;
import wrapper.toupcam.callbacks.PTOUPCAM_DATA_CALLBACK;
import wrapper.toupcam.callbacks.PTOUPCAM_HOTPLUG_CALLBACK;

public interface LibToupcam extends Library {

    int Toupcam_Enum(Pointer pointer);

    Pointer Toupcam_Open(String id);

    int Toupcam_StartPullModeWithCallback(Pointer handler, EventCallback callback, int other);

    int Toupcam_PullImage(Pointer handler, Pointer pImageData, int bits, Pointer pnWidth, Pointer pnHeight);

    int Toupcam_PullStillImage(Pointer handler, Pointer pImageData, int bits, Pointer pnWidth, Pointer pnHeight);

    int Toupcam_Snap(Pointer handler, int resolutionIndex);

    int Toupcam_StartPushMode(Pointer handler, PTOUPCAM_DATA_CALLBACK callback, Pointer other);

    void Toupcam_HotPlug(PTOUPCAM_HOTPLUG_CALLBACK callback);

    int Toupcam_put_eSize(Pointer handler, int resolutionIndex);

    int Toupcam_get_RawFormat(Pointer handler, Pointer nFourCC, Pointer bitdepth);

    int Toupcam_Stop(Pointer handler);

    int Toupcam_Pause(Pointer handler);

    int Toupcam_Trigger(Pointer handler, int number);

    int Toupcam_get_ResolutionNumber(Pointer handler, int resolutionIndex, Pointer width, Pointer height);

    int Toupcam_get_Resolution(Pointer handler, int resolutionIndex, Pointer width, Pointer height);

    int Toupcam_get_ExpoTime(Pointer handler, int other);

    int Toupcam_put_ExpoTime(Pointer handler, int time);

    int Toupcam_put_AutoExpoEnable(Pointer handler, boolean isEnable);

    int Toupcam_put_ExpoCallback(Pointer handler, PITOUPCAM_EXPOSURE_CALLBACK callback, Pointer other);

    /**
     * To set various options for the toupcam to work.
     * Like set Raw format images, quality of received images.
     *
     * @param handler
     * @param iOption
     * @param iValue
     * @return
     */
    int Toupcam_put_Option(Pointer handler, int iOption, int iValue);

    int Toupcam_get_Option(Pointer handler, int iOption, Pointer iValue);

}
