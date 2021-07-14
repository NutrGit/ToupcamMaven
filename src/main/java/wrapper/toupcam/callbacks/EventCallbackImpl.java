package wrapper.toupcam.callbacks;

import wrapper.toupcam.enumerations.Event;
import wrapper.toupcam.enumerations.HResult;
import wrapper.toupcam.models.Image;
import wrapper.toupcam.test.SmallApp;
import wrapper.toupcam.util.Util;

import java.awt.image.BufferedImage;

public class EventCallbackImpl implements EventCallback {

    private SmallApp app;

    @Override
    public void invoke(long event) {
//        System.out.println(Event.key(event) + " event received");

        if (Event.key(event) == Event.EVENT_EXPOSURE) {
            //почему-то иногда
            // Image [width=35, height=480, imagePointer=allocated@0x1cdab040 (1228800 bytes), hresult=E_FAIL]

            System.out.println("Event exposure");
//            Image image = app.getImage(app.getCamPointer());
//            System.out.println(image);


//            int res = app.getLibToupcam().Toupcam_get_ExpoTime(app.getCamPointer(), 0);
//            System.out.println(res);
//            System.out.println(HResult.key(res));
        }
        if (Event.key(event) == Event.EVENT_IMAGE) { //4
//            app.getFrame().setTitle("EVENT_IMAGE");

            Image image = app.getImage(app.getCamPointer());

//            System.out.println(image);

            BufferedImage bufferedImage =
                    Util.convertImagePointerToImage(
                            image.getImagePointer(),
                            image.getWidth(),
                            image.getHeight()
                    );

//            try {
//
//                int res = app.getLibToupcam().Toupcam_get_ExpoTime(app.getCamPointer(), 0);
////                    int res = getLibToupcam().Toupcam_get_AutoExpoEnable(getCamPointer(), 0);
//                System.out.println(res);
//                System.out.println(HResult.key(res));
//
//            } catch (Error e) {
//                System.exit(0);
//            }

            app.getIcon().setImage(bufferedImage);
            app.getFrame().repaint();
            app.getFrame().setTitle(image.getImagePointer().toString() + " " + app.getCamPointer().toString());

        }
        if (Event.key(event) == Event.EVENT_STILLIMAGE) {
            Image image = app.getStillImage(app.getCamPointer());
            System.out.println(image);

            BufferedImage bufferedImage =
                    Util.convertImagePointerToImage(
                            image.getImagePointer(),
                            image.getWidth(),
                            image.getHeight());


        }
    }

    public SmallApp getApp() {
        return app;
    }

    public void setApp(SmallApp app) {
        this.app = app;
    }
}
