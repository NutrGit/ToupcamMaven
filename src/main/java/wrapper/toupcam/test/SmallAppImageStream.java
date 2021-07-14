package wrapper.toupcam.test;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import wrapper.toupcam.Toupcam;
import wrapper.toupcam.callbacks.BufferedImageStreamCallback;
import wrapper.toupcam.callbacks.ByteImageStreamCallback;
import wrapper.toupcam.callbacks.ImageStreamCallback;
import wrapper.toupcam.callbacks.PTOUPCAM_DATA_CALLBACK;
import wrapper.toupcam.enumerations.HResult;
import wrapper.toupcam.enumerations.Options;
import wrapper.toupcam.exceptions.StreamingException;
import wrapper.toupcam.libraries.LibToupcam;
import wrapper.toupcam.models.ImageHeader;
import wrapper.toupcam.models.Model;
import wrapper.toupcam.models.Resolution;
import wrapper.toupcam.models.ToupcamInst;
import wrapper.toupcam.util.Constants;
import wrapper.toupcam.util.NativeUtils;
import wrapper.toupcam.util.ParserUtil;
import wrapper.toupcam.util.Util;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SmallAppImageStream implements Toupcam {

    private LibToupcam libToupcam = null;
    private Pointer camHandler;

    private boolean isStreaming = false;

    // cache variable to store callback for image, for use case when streaming
    // has to be stopped and restarted.

    //ImageStreamCallback нужен только для сохранения изображения в файл
    //через метод onReceiveStillImage() в классе ImageStreamCallback
    /**
     *@see #initImageStreamCallback()
     */
    private ImageStreamCallback imageCallback = null;

    private JFrame frame;
    private ImageIcon icon;
    private JLabel label;

    public static void main(String[] args) {
        SmallAppImageStream app = new SmallAppImageStream();
        Native.setProtected(true);

        app.initJFrame();

        for (Resolution res : app.getResolutions())
            System.out.println(res);

        int camsConnected = app.countConnectedCams();

        if (camsConnected == 0) {
            System.out.println("No Toupcams detected");
            System.exit(-1);
        }

        System.out.println("Set Resolution Result: " + app.setResolution(app.camHandler, 2));


        app.initImageStreamCallback();


        app.startStreaming(app.imageCallback);


//        System.out.println("Trigger Mode : " + app.getOptions(Options.OPTION_TRIGGER));
//
//        try {
//            for (int i = 0; i < 10; i++) {
//                ////	Thread.sleep(5000);
//                System.out.println("Activating Video Mode: ");
//                app.setTriggerMode(0);
//                Thread.sleep(500);
//                System.out.println("Activating Trigger Mode: ");
//                app.setTriggerMode(1);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        app.stopStreaming();
//        System.out.println("app.stopStreaming()");

    }


    @Override
    public HResult startStreaming(ImageStreamCallback imageCallback) {
        isStreaming = true;
        this.imageCallback = imageCallback;        // caching imageCallback for later use, in case of start/restart

        PTOUPCAM_DATA_CALLBACK data_callback = this.initDataCallback();

        int result = libToupcam.Toupcam_StartPushMode(getCamHandler(), data_callback, Pointer.NULL);

        return HResult.key(result);
    }

    @Override
    public HResult stopStreaming() {
        isStreaming = false;
        return HResult.key(libToupcam.Toupcam_Stop(getCamHandler()));
    }

    private void initImageStreamCallback() {
        this.imageCallback = new BufferedImageStreamCallback() {
            int imageCounter = 0;

            @Override
            public void onReceivePreviewImage(BufferedImage image, ImageHeader imageHeader) {
                Native.setProtected(true);
                //	byte[] imageBytes = Util.compressBufferedImageByteArray(image);
                Util.writeImageToDisk(Util.compressBufferedImage(image));
				/*try {
					Util.saveJPG(Util.compressBufferedImage(image));
				} catch (IOException e) {

					e.printStackTrace();
				}*/

                System.out.println(imageHeader);
                System.out.println("Total Images Received: " + ++imageCounter);
                System.out.println("123");
            }

            int stillCounter = 0;

            @Override
            public void onReceiveStillImage(BufferedImage image, ImageHeader imageHeader) {

                try {
                    ImageIO.write(image, "jpg", new File("./stills" + stillCounter++ + ".jpg"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("Still Image: " + image);
            }
        };
    }

    private PTOUPCAM_DATA_CALLBACK initDataCallback() {
        PTOUPCAM_DATA_CALLBACK dataCallback = (Pointer imagePointer, Pointer imageMetaData, boolean isSnapshot) -> {

            ImageHeader header = ParserUtil.parseImageHeader(imageMetaData);

            if (this.imageCallback instanceof ByteImageStreamCallback) {
                System.out.println("1) imageCallback instanceof ByteImageStreamCallback");
                byte[] imageBytes = Util.convertImagePointerToByteArray(imagePointer, header.getWidth(), header.getHeight());

                if (isSnapshot) {
                    this.imageCallback.onReceiveStillImage(imageBytes, header);
                } else {
                    this.imageCallback.onReceivePreviewImage(imageBytes, header);
                }
            } else if (this.imageCallback instanceof BufferedImageStreamCallback) {
                isSnapshot = false;
                System.out.println("2) imageCallback instanceof BufferedImageStreamCallback " + isSnapshot);
                BufferedImage image = Util.convertImagePointerToImage(imagePointer, header.getWidth(), header.getHeight());

                icon.setImage(image);
                frame.repaint();
//                frame.setTitle(Double.toString(1000000000 / en));
                frame.setTitle(imagePointer.toString() + " " + camHandler.toString());


                //write to capturedImage folder
                if (isSnapshot) {
//                    this.imageCallback.onReceiveStillImage(image, header);
                } else {
//                    this.imageCallback.onReceivePreviewImage(image, header);
                }
            }

        };

        return dataCallback;
    }

    public void initJFrame() {
        this.frame = new JFrame();
        this.frame.getContentPane().setLayout(new FlowLayout());

        this.frame.pack();

        this.icon = new ImageIcon(new BufferedImage(640, 480, 10));
        this.label = new JLabel(this.icon, JLabel.CENTER);

        this.frame.add(this.label);

        this.frame.setVisible(true);
        this.frame.setSize(800, 600);
    }

    public SmallAppImageStream() {
        this.libToupcam = (LibToupcam) NativeUtils.getNativeLib();
        this.camHandler = this.openCam(null);
    }

    private Pointer openCam(String id) {
        this.camHandler = this.libToupcam.Toupcam_Open(id);
        return this.camHandler;
    }

    public int countConnectedCams() {
        Memory memory = new Memory(Constants.MEM_SIZE_FOR_TOUPCAMINST);
        return this.libToupcam.Toupcam_Enum(memory);
    }

    @Override
    public List<ToupcamInst> getToupcams() {
        List<ToupcamInst> toupcamInstList = new ArrayList<ToupcamInst>();
        Memory structurePointer = new Memory(Constants.MEM_SIZE_FOR_TOUPCAMINST);
        int count_cams = libToupcam.Toupcam_Enum(structurePointer);
        for (int i = 0; i < count_cams; i++) toupcamInstList.add(new ToupcamInst());

        toupcamInstList.forEach(toupcamInst -> {

            int structurePointerOffset = 0;
            toupcamInst.setDisplayName(structurePointer.getString(structurePointerOffset));
            System.out.println(toupcamInst.getDisplayName());
            structurePointerOffset += 64;
            toupcamInst.setId(structurePointer.getString(structurePointerOffset));
            System.out.println(toupcamInst.getId());
            structurePointerOffset += 64;

            Pointer modelPointer = structurePointer.getPointer(structurePointerOffset);
            int modelPointerOffset = 0;
            toupcamInst.setModel(new Model());

            toupcamInst.getModel().setName(modelPointer.getPointer(modelPointerOffset).getString(0));
            modelPointerOffset += Pointer.SIZE;
            toupcamInst.getModel().setFlag(modelPointer.getInt(modelPointerOffset));
            modelPointerOffset += Constants.INT_SIZE;
            toupcamInst.getModel().setMaxspeed(modelPointer.getInt(modelPointerOffset));
            modelPointerOffset += Constants.INT_SIZE;
            toupcamInst.getModel().setStill(modelPointer.getInt(modelPointerOffset));
            modelPointerOffset += Constants.INT_SIZE;
            toupcamInst.getModel().setPreview(modelPointer.getInt(modelPointerOffset));
            modelPointerOffset += Constants.INT_SIZE;

            int resolutions = (int) Math.max(toupcamInst.getModel().getPreview(),
                    toupcamInst.getModel().getStill());

            Resolution[] resolutionArray = new Resolution[resolutions];
            for (int i = 0; i < resolutions; i++) resolutionArray[i] = new Resolution();

            toupcamInst.getModel().setRes(resolutionArray);
            Resolution[] toupcamInstRes = toupcamInst.getModel().getRes();

            for (int i = 0; i < resolutions; i++) {
                toupcamInstRes[i].width = modelPointer.getInt(modelPointerOffset);
                modelPointerOffset += Constants.INT_SIZE;
                toupcamInstRes[i].height = modelPointer.getInt(modelPointerOffset);
                modelPointerOffset += Constants.INT_SIZE;
            }

        });

        return toupcamInstList;
    }

    public HResult setResolution(Pointer handler, int resolutionIndex) {
        return HResult.key(this.libToupcam.Toupcam_put_eSize(handler, resolutionIndex));
    }

    @Override
    public Resolution[] getResolutions() {
        int resolutionCount = getResolutionNumbers();
        Resolution[] resolutions = new Resolution[resolutionCount];
        for (int i = 0; i < resolutionCount; i++) {
            resolutions[i] = getResolution(i);
        }
        return resolutions;
    }

    public int getResolutionNumbers() {
        Pointer widthArray = new Memory(4);
        Pointer heightArray = new Memory(4);
        int result = this.libToupcam.Toupcam_get_ResolutionNumber(this.camHandler, 0, widthArray, heightArray);
        return result;
    }

    public Resolution getResolution(int resolutionIndex) {
        Pointer widthPointer = new Memory(4);
        Pointer heightPointer = new Memory(4);
        int result = this.libToupcam.Toupcam_get_Resolution(this.camHandler, resolutionIndex, widthPointer, heightPointer);
        return new Resolution(widthPointer.getInt(0), heightPointer.getInt(0));
    }

    private int getOptions(Options option) {
        Pointer pointer = new Memory(4);
        this.libToupcam.Toupcam_get_Option(getCamHandler(), option.getValue(), pointer);
        return pointer.getInt(0);
    }

    public Pointer getCamHandler() {
        return this.camHandler;
    }

    public HResult setTriggerMode(int mode) {
        return this.setOptions(Options.OPTION_TRIGGER, mode);
    }

    private HResult setOptions(Options option, int value) {
        return HResult.key(this.libToupcam.Toupcam_put_Option(getCamHandler(), option.getValue(), value));
    }

    @Override
    public HResult getTriggerImages(int numberOfImages) {
        return HResult.key(libToupcam.Toupcam_Trigger(camHandler, numberOfImages));
    }

    @Override
    public boolean isStreaming() {
        return isStreaming;
    }

    @Override
    public HResult restartStreaming() throws StreamingException {
        if (this.imageCallback == null) throw new StreamingException(Constants.RESTART_STREAM_EXCEP_MSG);
        return startStreaming(this.imageCallback);
    }

    @Override
    public HResult pauseStreaming() {
        HResult result = HResult.key(libToupcam.Toupcam_Pause(getCamHandler()));
        if (result.equals(HResult.S_OK) || result.equals(HResult.S_FALSE))
            isStreaming = false;

        return result;
    }

    @Override
    public HResult resumeStreaming() {
        HResult result = HResult.key(libToupcam.Toupcam_Pause(getCamHandler()));
        if (result.equals(HResult.S_OK) || result.equals(HResult.S_FALSE))
            isStreaming = true;

        return result;
    }

    @Override
    public HResult getStillImage(int resolutionIndex) {
        return getSnapShot(getCamHandler(), resolutionIndex);
    }

    public HResult getSnapShot(Pointer handler, int resolutionIndex) {
        return HResult.key(libToupcam.Toupcam_Snap(handler, resolutionIndex));
    }

    @Override
    public HResult setResolution(int resolutionIndex) {
        return HResult.key(libToupcam.Toupcam_put_eSize(getCamHandler(), resolutionIndex));
    }


}