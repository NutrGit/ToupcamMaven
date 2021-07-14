package wrapper.toupcam.opencv400;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import wrapper.toupcam.callbacks.PTOUPCAM_DATA_CALLBACK;
import wrapper.toupcam.libraries.LibToupcam;
import wrapper.toupcam.models.ImageHeader;
import wrapper.toupcam.models.Resolution;
import wrapper.toupcam.util.NativeUtils;
import wrapper.toupcam.util.ParserUtil;
import wrapper.toupcam.util.Util;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;


public class AppCV extends Application {

    {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    //работало, если статик убрать, но с ним почему-то лучше
    public LibToupcam libToupcam;
    public Pointer camHandler;
    public static JFrame frame;
    public static ImageIcon icon;
    private static JLabel label;
    private long str, en;
    public PTOUPCAM_DATA_CALLBACK callBack;
    private static Mat img;

    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) throws Exception {
        String path = "C:/Users/UserCV/Downloads/ToupcamWrapper-master/src/main/java/wrapper/toupcam/opencv400/Frame.fxml";
        URL url = new File(path).toURI().toURL();
        FXMLLoader loader = new FXMLLoader(url);
        Pane rootElement = (Pane) loader.load();
        Scene scene = new Scene(rootElement, 800, 600);
        //scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());

        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("OpenCV-4.0.0");
        this.primaryStage.setScene(scene);
        this.primaryStage.show();

        Controller controller = loader.getController();
        controller.setStage(this.primaryStage);
        //controller.init();
    }

    public static void main(String[] args) {

        System.out.println("1");
        AppCV app = new AppCV();
        app.initJFrame();
        Native.setProtected(true);
        app.setResolution(app.camHandler, 2);

        app.initCallback();
        System.out.println("after runVideo");

        System.out.println("app.camHandler = " + app.camHandler.toString());
//        System.out.println("app.callBack = " + app.callBack.toString());

        app.libToupcam.Toupcam_StartPushMode(app.camHandler, app.callBack, Pointer.NULL);

        System.out.println("2");

//        launch(args);
        System.out.println("3");
    }

    public void initCallback() {
        callBack = (imagePointer, imageMetaData, isSnapshot) -> {
            ImageHeader header = ParserUtil.parseImageHeader(imageMetaData);
            BufferedImage image = Util.convertImagePointerToImage(imagePointer,
                    header.getWidth(), header.getHeight());


            // делается Канни, но при этом падает фпс
            // можно убрать две нижние строчки для обычного изображения
            img = AppCV.doCanny(AppCV.bufferedImage2Mat(image));

            image = AppCV.Mat2BufferedImage(img);


            en = System.nanoTime() - str;

            icon.setImage(image);
            frame.repaint();
//            frame.setTitle(Double.toString(1000000000 / en));
            frame.setTitle(imagePointer.toString() + " " + image.hashCode());

            str = System.nanoTime();
        };
    }

    public AppCV() {
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

    public void initJFrame() {
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

    public static Mat bufferedImage2Mat(BufferedImage image) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "png", byteArrayOutputStream);
        byteArrayOutputStream.flush();
        return Imgcodecs.imdecode(new MatOfByte(byteArrayOutputStream.toByteArray()), Imgcodecs.IMREAD_UNCHANGED);
    }

    public static BufferedImage Mat2BufferedImage(Mat matrix) throws IOException {
        MatOfByte mob = new MatOfByte();
        Imgcodecs.imencode(".jpg", matrix, mob);
        return ImageIO.read(new ByteArrayInputStream(mob.toArray()));
    }


    private static BufferedImage bufferedImage(Mat m) {
        //Mat to BufferedImage
        int type = BufferedImage.TYPE_BYTE_GRAY;
        if (m.channels() > 1) {
            type = BufferedImage.TYPE_3BYTE_BGR;
        }
        BufferedImage image = new BufferedImage(m.cols(), m.rows(), type);
        m.get(0, 0, ((DataBufferByte) image.getRaster().getDataBuffer()).getData());
        return image;
    }

    public static Mat bufferedImageToMat(BufferedImage bi) {
        Mat mat = new Mat(bi.getHeight(), bi.getWidth(), CvType.CV_8UC3);
        byte[] data = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();
        mat.put(0, 0, data);
        return mat;
    }

    private static Mat doCanny(Mat frame) {
        // init
        Mat grayImage = new Mat();
        Mat detectedEdges = new Mat();

        // convert to grayscale
        Imgproc.cvtColor(frame, grayImage, Imgproc.COLOR_BGR2GRAY);

        // reduce noise with a 3x3 kernel
        Imgproc.blur(grayImage, detectedEdges, new Size(3, 3));

        // canny detector, with ratio of lower:upper threshold of 3:1
        Imgproc.Canny(detectedEdges, detectedEdges, 20, 20 * 3);

        // using Canny's output as a mask, display the result
        Mat dest = new Mat();
        frame.copyTo(dest, detectedEdges);

        return dest;
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
