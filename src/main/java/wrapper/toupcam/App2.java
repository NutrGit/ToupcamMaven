package wrapper.toupcam;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import wrapper.toupcam.callbacks.PTOUPCAM_DATA_CALLBACK;
import wrapper.toupcam.libraries.LibToupcam;
import wrapper.toupcam.models.ImageHeader;
import wrapper.toupcam.models.Resolution;
import wrapper.toupcam.util.NativeUtils;
import wrapper.toupcam.util.ParserUtil;
import wrapper.toupcam.util.Util;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class App2 {

    private LibToupcam libToupcam;
    private Pointer camHandler;
    public static JFrame frame;
    public static ImageIcon icon;
    public static JLabel label;
    public static long str, en;
    public PTOUPCAM_DATA_CALLBACK callBack;
    private static int resolutionIndex = 2;

    public static void main(String[] args) {

        App2 app = new App2();


        for (Resolution res : app.getResolutions())
            System.out.println(res);

        app.setResolution(app.camHandler, resolutionIndex);
        app.initJFrame();
        Native.setProtected(true);
        app.initCallback();
        System.out.println("after runVideo");
        app.libToupcam.Toupcam_StartPushMode(app.camHandler, app.callBack, Pointer.NULL);

    }

    private void initCallback() {
        callBack = (imagePointer, imageMetaData, isSnapshot) -> {
            ImageHeader header = ParserUtil.parseImageHeader(imageMetaData);
            BufferedImage image = Util.convertImagePointerToImage(imagePointer, header.getWidth(), header.getHeight());

            en = System.nanoTime() - str;

            icon.setImage(image);
            frame.repaint();
            frame.setTitle(Double.toString(1000000000 / en));

            str = System.nanoTime();
        };
    }

    public App2() {
        libToupcam = (LibToupcam) NativeUtils.getNativeLib();
        camHandler = openCam(null);        // by default picks up the first toupcam connected to system.
    }

    public void setResolution(Pointer handler, int resolutionIndex) {
        libToupcam.Toupcam_put_eSize(handler, resolutionIndex);
    }

    public Pointer openCam(String id) {
        camHandler = libToupcam.Toupcam_Open(id);
        return camHandler;
    }

    private void initJFrame() {

        str = 0;

        frame = new JFrame();
        frame.getContentPane().setLayout(new FlowLayout());

        frame.pack();

        icon = new ImageIcon(new BufferedImage(640, 480, 10));
        label = new JLabel(icon, JLabel.CENTER);

        frame.add(label);

        frame.setVisible(true);
        frame.setSize(800, 600);
    }

    public int getResolutionNumbers() {
        Pointer widthArray = new Memory(4);
        Pointer heightArray = new Memory(4);
        int result = libToupcam.Toupcam_get_ResolutionNumber(camHandler, 0, widthArray, heightArray);
        return result;
    }

    public Resolution getResolution(int resolutionIndex) {
        Pointer widthPointer = new Memory(4);
        Pointer heightPointer = new Memory(4);
        int result = libToupcam.Toupcam_get_Resolution(camHandler, resolutionIndex, widthPointer, heightPointer);
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


}