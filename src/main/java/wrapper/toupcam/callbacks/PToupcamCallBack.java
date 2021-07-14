package wrapper.toupcam.callbacks;

import com.sun.jna.Pointer;
import wrapper.toupcam.models.ImageHeader;
import wrapper.toupcam.opencv400.Controller;
import wrapper.toupcam.test.SmallApp;
import wrapper.toupcam.util.ParserUtil;
import wrapper.toupcam.util.Util;

import java.awt.image.BufferedImage;
import java.io.IOException;

public class PToupcamCallBack implements PTOUPCAM_DATA_CALLBACK {

    private SmallApp app;

    @Override
    public void invoke(Pointer imagePointer, Pointer imageMetaData, boolean isSnapshot) throws IOException {
        ImageHeader header = ParserUtil.parseImageHeader(imageMetaData);
        BufferedImage image = Util.convertImagePointerToImage(imagePointer,
                header.getWidth(), header.getHeight());
//                BufferedImage image = Util.convertImagePointerToImage(camPointer,
//                        header.getWidth(), header.getHeight());


//        Controller.imagePointerController = imagePointer;

        app.getController().setImagePointerController(imagePointer);
        app.getIcon().setImage(image);
        app.getFrame().repaint();
        app.getFrame().setTitle(imagePointer.toString() + " " + app.getCamPointer().toString());
    }

    public SmallApp getApp() {
        return app;
    }

    public void setApp(SmallApp app) {
        this.app = app;
    }
}
