package wrapper.toupcam.callbacks;

import com.sun.jna.Callback;
import com.sun.jna.Pointer;

import java.io.IOException;

public interface PTOUPCAM_DATA_CALLBACK extends Callback{

	void invoke(Pointer imagePointer, Pointer imageMetaData, boolean isSnapshot) throws IOException;
	
}