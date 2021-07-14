package wrapper.toupcam.test;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import wrapper.toupcam.callbacks.EventCallbackImpl;
import wrapper.toupcam.callbacks.PTOUPCAM_DATA_CALLBACK;
import wrapper.toupcam.callbacks.PToupcamCallBack;
import wrapper.toupcam.enumerations.HResult;
import wrapper.toupcam.enumerations.Options;
import wrapper.toupcam.libraries.LibToupcam;
import wrapper.toupcam.models.Image;
import wrapper.toupcam.models.ImageHeader;
import wrapper.toupcam.models.Resolution;
import wrapper.toupcam.opencv400.Controller;
import wrapper.toupcam.util.NativeUtils;
import wrapper.toupcam.util.ParserUtil;
import wrapper.toupcam.util.Util;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;


public class SmallApp {

    private LibToupcam libToupcam;

    private Pointer camPointer;

    private EventCallbackImpl eventCallbackImpl;
    private PToupcamCallBack pDataCallback2;

    private PTOUPCAM_DATA_CALLBACK pDataCallback;

    private Controller controller;

    private JFrame frame;
    private ImageIcon icon;
    private JLabel label;
    private long str, en;

    public static void main(String[] args) {
        SmallApp app = new SmallApp();
        app.initJFrame();
        Native.setProtected(true);

        for (Resolution res : app.getResolutions()) {
            System.out.println(res);
        }

        System.out.println("set resolution HResult = " +
                app.setResolution(app.camPointer, 2));

        System.out.println("get resolution = " + app.getResolution(2));


//        System.out.println("Set RAW Options Result: " + app.setOptions(Options.OPTION_RAW, 1));
//        System.out.println("Set PROCESSMODE Options Result: " + app.setOptions(Options.OPTION_PROCESSMODE, 0));
//        System.out.println("Set CURVE Options Result:" + app.setOptions(Options.OPTION_CURVE, 0));


        //Start with EventCallBackImpl
        //had some issue, but works
//        app.eventCallbackImpl = new EventCallbackImpl();
//        app.eventCallbackImpl.setApp(app);
//        app.libToupcam.Toupcam_StartPullModeWithCallback(app.camPointer, app.eventCallbackImpl, 0);


        //works with push mode
//        System.out.println("Set expoCallback = " + HResult.key(app.getLibToupcam().Toupcam_put_ExpoCallback(app.getCamPointer(),
//                new PITOUPCAM_EXPOSURE_CALLBACK() {
//                    @Override
//                    public void invoke() {
//                        System.out.println("Exposure invoke callback");
//
//                    }
//                }, Pointer.NULL)));


//        try {
//
//            int res = app.getLibToupcam().Toupcam_put_ExpoTime(app.getCamPointer(), 120);
//            System.out.println(res);
//            System.out.println(HResult.key(res));
//
////            res = app.getLibToupcam().Toupcam_get_ExpoTime(app.getCamPointer(), 0);
////            System.out.println(res);
////            System.out.println(HResult.key(res));
//
//        } catch (Error e) {
//            System.exit(0);
//        }


        //Start with EventCallBackImpl
//        app.eventCallbackImpl = new EventCallbackImpl();
//        app.eventCallbackImpl.setApp(app);
//        app.libToupcam.Toupcam_StartPullModeWithCallback(app.camPointer, app.eventCallbackImpl, 0);


        try {

//            for (int i = 0; i <= 190000; i++) {
//                int res = app.getLibToupcam().Toupcam_put_ExpoTime(app.getCamPointer(), i);
//
////                System.out.println(res);
//                System.out.println("i = " + i + " " + HResult.key(res));
//            }


//            res = app.getLibToupcam().Toupcam_get_ExpoTime(app.getCamPointer(), 0);
//            System.out.println(res);
//            System.out.println(HResult.key(res));

        } catch (Error e) {
            System.exit(0);
        }

        app.libToupcam.Toupcam_put_AutoExpoEnable(app.camPointer, false);
        app.libToupcam.Toupcam_put_ExpoTime(app.camPointer, 1000);

        //Start with PTOUPCAM_DATA_CALLBACK,
        /**
         @see  #initCallback()
         from SmallApp() constructor
         **/
        //works
        app.libToupcam.Toupcam_StartPushMode(app.camPointer, app.pDataCallback, Pointer.NULL);


        //Start with PTOUPCAM_DATA_CALLBACK implementation
        //works
//        app.pDataCallback2 = new PToupcamCallBack();
//        app.pDataCallback2.setApp(app);
//        app.libToupcam.Toupcam_StartPushMode(app.camPointer, app.pDataCallback2, Pointer.NULL);


    }

    public SmallApp() {
        this.libToupcam = (LibToupcam) NativeUtils.getNativeLib();
        this.camPointer = this.openCam(null);
        this.initCallback();
    }

    private void initCallback() {
        this.pDataCallback = new PTOUPCAM_DATA_CALLBACK() {
            @Override
            public void invoke(Pointer imagePointer, Pointer imageMetaData, boolean isSnapshot) throws IOException {
                en = System.nanoTime() - str;
                ImageHeader header = ParserUtil.parseImageHeader(imageMetaData);
                BufferedImage image = Util.convertImagePointerToImage(imagePointer,
                        header.getWidth(), header.getHeight());

                controller.setImagePointerController(imagePointer);

                if (isSnapshot) {
                    System.out.println("snap from callback");
//                    controller.getSnapFromApp(imagePointer);
                    controller.setSnapPointer(imagePointer);
                }

                //E_POINTER
//                int res = getLibToupcam().Toupcam_get_ExpoTime(getCamPointer(), 0);

                //E_INVALIDARG
//                int res = getLibToupcam().Toupcam_get_ExpoTime(0);

                try {

//                    int res = getLibToupcam().Toupcam_put_ExpoTime(getCamPointer(), 2400);
//                    System.out.println(res);
//                    System.out.println(HResult.key(res));
//                    frame.setTitle("HResult = " + HResult.key(res));

//                    System.out.println("get expotime");
//                    res = getLibToupcam().Toupcam_get_ExpoTime(getCamPointer(), 0);
//                    System.out.println(res);
//                    System.out.println(HResult.key(res));

                } catch (Error e) {
                    System.exit(0);
                }


                icon.setImage(image);
                frame.repaint();
                frame.setTitle(Double.toString(1000000000 / en) + " isSnap = " + isSnapshot);
//                frame.setTitle(imagePointer.toString() + " " + camPointer.toString());
                str = System.nanoTime();
            }
        };
    }

    private Pointer openCam(String id) {
        this.camPointer = this.libToupcam.Toupcam_Open(id);
        return this.camPointer;
    }

    public void initJFrame() {
        this.str = 0;

        this.frame = new JFrame();
        this.frame.getContentPane().setLayout(new FlowLayout());

        this.frame.pack();

//        this.icon = new ImageIcon(new BufferedImage(640, 480, 10));
        this.icon = new ImageIcon(new BufferedImage(800, 600, 10));
        this.label = new JLabel(this.icon, JLabel.CENTER);

        this.frame.add(this.label);

        this.frame.setVisible(true);
        this.frame.setSize(800, 600);
    }

    public HResult setResolution(Pointer handler, int resolutionIndex) {
        int res = this.libToupcam.Toupcam_put_eSize(handler, resolutionIndex);
        return HResult.key(res);
    }

    private int getResolutionNumbers() {
        Pointer widthArray = new Memory(4);
        Pointer heightArray = new Memory(4);
        int result = this.libToupcam.Toupcam_get_ResolutionNumber(this.camPointer, 0, widthArray, heightArray);
        return result;
    }

    public Resolution getResolution(int resolutionIndex) {
        Pointer widthPointer = new Memory(4);
        Pointer heightPointer = new Memory(4);
        int result = this.libToupcam.Toupcam_get_Resolution(this.camPointer, resolutionIndex, widthPointer, heightPointer);
        return new Resolution(widthPointer.getInt(0), heightPointer.getInt(0));
    }

    public Resolution[] getResolutions() {
        int resolutionCount = getResolutionNumbers();
        Resolution[] resolutions = new Resolution[resolutionCount];
        for (int i = 0; i < resolutionCount; i++) {
            resolutions[i] = getResolution(i);
        }
        return resolutions;
    }

    public Image getStillImage(Pointer handler) {
        //width=2592, height=1944
        Pointer imageBuffer = new Memory(2592 * 1944);
        Pointer width = new Memory(4), height = new Memory(4);
        int result = libToupcam.Toupcam_PullStillImage(handler, imageBuffer, 8, width, height);
        return new Image(imageBuffer, width.getInt(0), height.getInt(0), HResult.key(result));
    }

    public Image getImage(Pointer handler) {
        //width=1280, height=960
        Pointer imageBuffer = new Memory(640 * 480 * 4);
        Pointer width = new Memory(4), height = new Memory(4);
        int result = libToupcam.Toupcam_PullImage(handler, imageBuffer, 24, width, height);
        return new Image(imageBuffer, width.getInt(0), height.getInt(0), HResult.key(result));
    }

    public LibToupcam getLibToupcam() {
        return libToupcam;
    }

    public void setLibToupcam(LibToupcam libToupcam) {
        this.libToupcam = libToupcam;
    }

    public Pointer getCamPointer() {
        return camPointer;
    }

    public void setCamPointer(Pointer camPointer) {
        this.camPointer = camPointer;
    }

    public PTOUPCAM_DATA_CALLBACK getpDataCallback() {
        return pDataCallback;
    }

    public void setpDataCallback(PTOUPCAM_DATA_CALLBACK pDataCallback) {
        this.pDataCallback = pDataCallback;
    }

    public JFrame getFrame() {
        return frame;
    }

    public void setFrame(JFrame frame) {
        this.frame = frame;
    }

    public ImageIcon getIcon() {
        return icon;
    }

    public void setIcon(ImageIcon icon) {
        this.icon = icon;
    }

    public JLabel getLabel() {
        return label;
    }

    public void setLabel(JLabel label) {
        this.label = label;
    }

    public HResult setOptions(Options option, int value) {
        return HResult.key(libToupcam.Toupcam_put_Option(this.getCamPointer(), option.getValue(), value));
    }

    public Controller getController() {
        return controller;
    }

    public void setController(Controller controller) {
        this.controller = controller;
    }
}