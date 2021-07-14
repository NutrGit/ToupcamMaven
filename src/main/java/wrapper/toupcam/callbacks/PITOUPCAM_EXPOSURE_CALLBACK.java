package wrapper.toupcam.callbacks;


import com.sun.jna.Callback;

public interface PITOUPCAM_EXPOSURE_CALLBACK extends Callback {

    void invoke();
}
