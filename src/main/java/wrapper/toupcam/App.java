package wrapper.toupcam;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import wrapper.toupcam.callbacks.*;
import wrapper.toupcam.enumerations.Event;
import wrapper.toupcam.enumerations.HResult;
import wrapper.toupcam.enumerations.Options;
import wrapper.toupcam.exceptions.StreamingException;
import wrapper.toupcam.libraries.LibToupcam;
import wrapper.toupcam.models.Image;
import wrapper.toupcam.models.*;
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

public class App implements Toupcam {

    private LibToupcam libToupcam = null;
    private Pointer camHandler;

    private boolean isStreaming = false;

    // cache variable to store callback for image, for use case when streaming
    // has to be stopped and restarted.
    private ImageStreamCallback imageCallback = null;

    private JFrame frame;
    private ImageIcon icon;
    private JLabel label;

    public static void main(String[] args) {
        App app = new App();
        Native.setProtected(true);
        app.initJFrame();
        //	List<ToupcamInst> cams = app.getToupcams();	// some pointer issue in windows
        //	System.out.println(cams);

//        	System.out.println(app.getResolutionNumbers());
//        	System.out.println(app.getResolutions());

        for (Resolution res : app.getResolutions())
            System.out.println(res);

        int camsConnected = app.countConnectedCams();
        //	System.out.println(app.getToupcams());
        if (camsConnected == 0) {
            System.out.println("No Toupcams detected");
            System.exit(-1);
        }

        //app.registerPlugInOrOut(); 		// not available in windows
        //	app.camHandler = app.openCam(null);
//        Util.keepVMRunning();

        System.out.println("Set Resolution Result: " + app.setResolution(app.camHandler, 0));
//        System.out.println("Set RAW Options Result: " + app.setOptions(app.camHandler, Options.OPTION_RAW, 1));

//        System.out.println("Set bit depth Options Result = " +
//                app.setOptions(Options.OPTION_BITDEPTH, 32));

        app.imageCallback = new BufferedImageStreamCallback() {
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

        //	app.startStreaming(imageCallback);
        //	app.stopStreaming();
        //	System.out.println("Trigger images");
        //	app.getTriggerImages(10);

        System.out.println("1");
//        app.startPushModeCam(app.getCamHandler());
        System.out.println("2");
//        app.startStreaming(app.imageCallback);
        System.out.println("3");
        app.startPullMode(app.getCamHandler());
        System.out.println("4");

//        	System.out.println("Set Trigger Mode Result: " + app.setTriggerMode(TriggerMode.TOUPCAM_FLAG_TRIGGER_SINGLE.getValue()));
        //	System.out.println("Get Trigger Images Result: " + app.getTriggerImages(10));

//        app.startStreaming(app.imageCallback);
        System.out.println("Trigger Mode : " + app.getOptions(Options.OPTION_TRIGGER));
        //for(int i = 0; i < 10; i++)
        //	app.getStillImage(0);

        try {
            for (int i = 0; i < 100; i++) {
                //	Thread.sleep(5000);
                System.out.println("Activating Video Mode: ");
                app.setTriggerMode(0);
                Thread.sleep(500);
                System.out.println("Activating Trigger Mode: ");
                app.setTriggerMode(1);
            }
        } catch (Exception e) {
        }

        //	app.setTriggerMode(0);

        app.stopStreaming();
    }

    public HResult setTriggerMode(int mode) {
        return setOptions(Options.OPTION_TRIGGER, mode);
    }

    private HResult setOptions(Options option, int value) {
        return HResult.key(libToupcam.Toupcam_put_Option(getCamHandler(), option.getValue(), value));
    }

    public HResult setOptions(Pointer handler, Options option, int value) {
        return HResult.key(libToupcam.Toupcam_put_Option(handler, option.getValue(), value));
    }

    private int getOptions(Options option) {
        Pointer pointer = new Memory(4);
        libToupcam.Toupcam_get_Option(getCamHandler(), option.getValue(), pointer);
        return pointer.getInt(0);
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
    public HResult stopStreaming() {
        isStreaming = false;
        return HResult.key(libToupcam.Toupcam_Stop(getCamHandler()));
    }

    @Override
    public HResult getStillImage(int resolutionIndex) {
        return getSnapShot(getCamHandler(), resolutionIndex);
    }

    @Override
    public HResult setResolution(int resolutionIndex) {
        return HResult.key(libToupcam.Toupcam_put_eSize(getCamHandler(), resolutionIndex));
    }

    @Override
    public HResult startStreaming(ImageStreamCallback imageCallback) {
        isStreaming = true;
        this.imageCallback = imageCallback;        // caching imageCallback for later use, in case of start/restart

        int result = libToupcam.Toupcam_StartPushMode(getCamHandler(),
                (Pointer imagePointer, Pointer imageMetaData, boolean isSnapshot) -> {

                    ImageHeader header = ParserUtil.parseImageHeader(imageMetaData);

                    if (imageCallback instanceof ByteImageStreamCallback) {
                        byte[] imageBytes = Util.convertImagePointerToByteArray(imagePointer,
                                header.getWidth(), header.getHeight());

                        if (isSnapshot)
                            imageCallback.onReceiveStillImage(imageBytes, header);
                        else imageCallback.onReceivePreviewImage(imageBytes, header);

                    } else if (imageCallback instanceof BufferedImageStreamCallback) {
                        BufferedImage image = Util.convertImagePointerToImage(
                                imagePointer, header.getWidth(), header.getHeight());

                        if (isSnapshot)
                            imageCallback.onReceiveStillImage(image, header);
                        else imageCallback.onReceivePreviewImage(image, header);
                    }

                }, Pointer.NULL);
        return HResult.key(result);
    }

	/*@Override
	public Toupcam getInstance() {
		return new App();
	}*/

    public void startPullMode(Pointer handler) {
        HResult result = startPullWithCallBack(handler);
        System.out.println("Start Pull Result: " + result);
    }

    public void startPushModeCam(Pointer handler) {
        HResult result = startPushMode(handler);
        System.out.println("Start Push Result: " + result);
    }

    public App() {
        libToupcam = (LibToupcam) NativeUtils.getNativeLib();
        camHandler = openCam(null);        // by default picks up the first toupcam connected to system.
        //	Util.keepVMRunning();				// keep JVM from terminating, not needed inside tomcat.
    }

    public void registerPlugInOrOut() {
        libToupcam.Toupcam_HotPlug(new PTOUPCAM_HOTPLUG_CALLBACK() {
            @Override
            public void invoke() {
                System.out.println("Camera is pluged in or out.");
            }
        });
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

    public RawFormat getRawFormat(Pointer handler) {
        Pointer nFourCC = new Memory(4), bitdepth = new Memory(4);
        int result = libToupcam.Toupcam_get_RawFormat(handler, nFourCC, bitdepth);
        return new RawFormat(nFourCC.getInt(0), bitdepth.getInt(0), HResult.key(result));
    }

    public HResult setResolution(Pointer handler, int resolutionIndex) {
        return HResult.key(libToupcam.Toupcam_put_eSize(handler, resolutionIndex));
    }

    @Override
    public int countConnectedCams() {
        Memory memory = new Memory(Constants.MEM_SIZE_FOR_TOUPCAMINST);
        return libToupcam.Toupcam_Enum(memory);
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

    public Pointer openCam(String id) {
        camHandler = libToupcam.Toupcam_Open(id);
        return camHandler;
    }

    public HResult startPullWithCallBack(Pointer handler) {
        int result = libToupcam.Toupcam_StartPullModeWithCallback(handler, new EventCallback() {
            @Override
            public void invoke(long event) {
                System.out.println("event = " + event);
                System.out.println(Event.key(event) + " event received");

                if (Event.key(event) == Event.EVENT_EXPOSURE) {
                    System.out.println("Event exposure");
//                    System.out.println(libToupcam.Toupcam_get_ExpoTime(0));
                }

                if (Event.key(event) == Event.EVENT_STILLIMAGE) {
                    Image image = getStillImage(handler);
                    System.out.println(image);

                    BufferedImage bufferedImage =
                            Util.convertImagePointerToImage(
                                    image.getImagePointer(),
                                    image.getWidth(),
                                    image.getHeight());


                } else if (Event.key(event) == Event.EVENT_IMAGE) {
                    Image image = getImage(handler);
                    System.out.println(image);


                    //Util.convertImagePointerToImage(image.getImagePointer(), image.getWidth(), image.getHeight());

                    BufferedImage bufferedImage =
                            Util.convertImagePointerToImage(
                                    image.getImagePointer(),
                                    image.getWidth(),
                                    image.getHeight()
                            );

                    icon.setImage(bufferedImage);
                    frame.repaint();
//                    frame.setTitle(Double.toString(1000000000 / en));
                    frame.setTitle(image.getImagePointer().toString() + " " + handler.toString());

                }
            }
        }, 0);
        return HResult.key(result);
    }

    public HResult startPushMode(Pointer handler) {
        int result = libToupcam.Toupcam_StartPushMode(handler, new PTOUPCAM_DATA_CALLBACK() {
            @Override
            public void invoke(Pointer imagePointer, Pointer imageMetaDataPointer, boolean isSnapshot) {
                ImageHeader header = ParserUtil.parseImageHeader(imageMetaDataPointer);
                System.out.println(header);
                Util.convertImagePointerToImage(imagePointer,
                        header.getWidth(), header.getHeight());

                //JLabel label = (JLabel) jFrame.getComponent(0);
                //label.setIcon(new ImageIcon(image));
            }
        }, Pointer.NULL);
        return HResult.key(result);
    }

    public HResult getSnapShot(Pointer handler, int resolutionIndex) {
        return HResult.key(libToupcam.Toupcam_Snap(handler, resolutionIndex));
    }

    public Image getImage(Pointer handler) {
        //width=1280, height=960
        Pointer imageBuffer = new Memory(640 * 480 * 4);
        Pointer width = new Memory(4), height = new Memory(4);
        int result = libToupcam.Toupcam_PullImage(handler, imageBuffer, 24, width, height);
        return new Image(imageBuffer, width.getInt(0), height.getInt(0), HResult.key(result));
    }

    public Image getStillImage(Pointer handler) {
        //width=2592, height=1944
        Pointer imageBuffer = new Memory(2592 * 1944);
        Pointer width = new Memory(4), height = new Memory(4);
        int result = libToupcam.Toupcam_PullStillImage(handler, imageBuffer, 8, width, height);
        return new Image(imageBuffer, width.getInt(0), height.getInt(0), HResult.key(result));
    }

    public Pointer getCamHandler() {
        return camHandler;
    }

    public void setCamHandler(Pointer camHandler) {
        this.camHandler = camHandler;
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


}