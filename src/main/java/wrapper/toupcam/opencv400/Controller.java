package wrapper.toupcam.opencv400;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import wrapper.toupcam.callbacks.PTOUPCAM_DATA_CALLBACK;
import wrapper.toupcam.enumerations.HResult;
import wrapper.toupcam.models.Resolution;
import wrapper.toupcam.test.SmallApp;
import wrapper.toupcam.util.Util;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class Controller {

//    fx:controller="wrapper.toupcam.opencv400.Controller"

    private String path = "C:\\Users\\UserCV\\IdeaProjects\\OpenCV400\\src\\files\\";

    private Stage stage;

    private MainFX mainFXInstance;

    private Pointer imagePointerController;
    private Pointer snapPointer;

    private Resolution resolution;
    private int resIndex;
    private int snapIndex;
    private Image snapImage;

    private List<MenuItem> menuItemList;

    @FXML
    private ImageView imageView;

    @FXML
    private TextField textField1;

    @FXML
    private Slider slider1;

    @FXML
    private MenuButton menuButtonResolution;

    @FXML
    private MenuItem menuItem0, menuItem1, menuItem2;

    private SmallApp app;

    private Pointer pointer;

    @FXML
    protected void init() {
        app = new SmallApp();
        app.setController(this);
        app.initJFrame();
        Native.setProtected(true);

        menuItemList = new ArrayList<>();


        if (app.getResolutions().length == 3) {
            //большая камера
            menuItemList.add(menuItem0);
            menuItemList.add(menuItem1);
            menuItemList.add(menuItem2);

        } else if (app.getResolutions().length == 2) {
            //мелка камера
            menuItemList.add(menuItem0);
            menuItemList.add(menuItem1);
        }

        int i = 0;
        for (MenuItem item : menuItemList) {
            item.setVisible(true);
            System.out.println("i = " + i);
            String strRes0 = "" + app.getResolutions()[i].getWidth() + " x "
                    + app.getResolutions()[i].getHeight();
            item.setText(strRes0);
            i++;
        }

        //resolutions
        //for 8000 camera
        resIndex = menuItemList.size() - 1; //800x600
        snapIndex = resIndex;
        menuButtonResolution.setText(menuItemList.get(snapIndex).getText());
//        resIndex = 1; //1600x1200
//        resIndex = 0; //3264x2448


        resolution = app.getResolution(resIndex);


        for (Resolution res : app.getResolutions()) {
            System.out.println(res);
        }

        System.out.println("set resolution HResult = " +
                app.setResolution(app.getCamPointer(), resIndex));

        System.out.println("get resolution = " + app.getResolution(resIndex));

        app.getLibToupcam().Toupcam_put_AutoExpoEnable(app.getCamPointer(), false);
        app.getLibToupcam().Toupcam_put_ExpoTime(app.getCamPointer(), 30000);

//        System.out.println("Set RAW Options Result: " + app.setOptions(Options.OPTION_RAW, 1));
        app.getLibToupcam().Toupcam_StartPushMode(app.getCamPointer(), app.getpDataCallback(), Pointer.NULL);
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public Pointer getImagePointerController() {
        return imagePointerController;
    }

    public void setImagePointerController(Pointer imagePointerController) {
        this.imagePointerController = imagePointerController;
    }

    @FXML
    private void getImage() throws IOException {
//        imageView.setFitWidth(640);
//        imageView.setFitHeight(480);

        pointer = app.getCamPointer();
        PTOUPCAM_DATA_CALLBACK callBack = app.getpDataCallback();

//        System.out.println("Set CURVE Options Result:" + app.setOptions(Options.OPTION_CURVE, 0));
//        System.out.println("Set HISTOGRAM Options Result:" + app.setOptions(Options.OPTION_HISTOGRAM, 1));
//        System.out.println("Set BITDEPTH Options Result:" + app.setOptions(Options.OPTION_BITDEPTH, 1));

//        BufferedImage bufferedImage = Util.convertImagePointerToImage(imagePointerController, 640, 480);
        BufferedImage bufferedImage = Util.convertImagePointerToImage(
                imagePointerController, (int) resolution.getWidth(), (int) resolution.getHeight());

//        Mat mat = AppCV.bufferedImage2Mat(bufferedImage);
        System.out.println("controller");
        System.out.println("camPointer = " + pointer.toString());
        System.out.println("callBack = " + callBack.toString());
        System.out.println("imagePointerController = " + imagePointerController);

        try {
//            app.getLibToupcam().Toupcam_get_ExpoTime(imagePointerController, 1);
        } catch (Exception e) {
//            e.printStackTrace();
            System.out.println("exception");
        }

        Image image = SwingFXUtils.toFXImage(bufferedImage, null);
//        Image image = Util.convertToFxImage(bufferedImage);
//        Image image = Util.matToImageFX(mat);


        imageView.setImage(image);
    }

    @FXML
    private void sliderMove() {
        int sliderVal = (int) slider1.getValue();
        textField1.setText("" + sliderVal);
    }

    @FXML
    private void applyButton() {
        System.out.println("set new exposure");
        int expo = Integer.parseInt(textField1.getText());
        slider1.setValue(expo);
        System.out.println("new exposure = " + expo);
        System.out.println(HResult.key(app.getLibToupcam().Toupcam_put_ExpoTime(app.getCamPointer(), expo)));
    }

    @FXML
    private void start() {
//        System.out.println(HResult.key(app.getLibToupcam().Toupcam_Stop(app.getCamPointer())));

//        System.out.println(
//                HResult.key(
//                        app
//                                .getLibToupcam()
//                                .Toupcam_StartPushMode(app.getCamPointer(), app.getpDataCallback(), Pointer.NULL)
//                )
//        );
    }

    @FXML
    private void getSnap() {
        System.out.println("getSnap " +
                HResult.key(
                        app.getLibToupcam().Toupcam_Snap(app.getCamPointer(), snapIndex)
                )
        );
    }

    public void getSnapFromApp(Pointer imagePointer) {
        System.out.println("getSnapFromApp");

        int w = (int) app.getResolution(snapIndex).getWidth();
        int h = (int) app.getResolution(snapIndex).getHeight();

        BufferedImage bufferedImage = Util.convertImagePointerToImage(imagePointer, w, h);

        snapImage = SwingFXUtils.toFXImage(bufferedImage, null);
//        imageView.setFitWidth(1600);
//        imageView.setFitHeight(1200);
        imageView.setImage(snapImage);
        this.showZoomStage();

    }

    @FXML
    private void menuItem0Action() {
        snapIndex = 0;
        menuButtonResolution.setText(menuItemList.get(snapIndex).getText());
        System.out.println("snapIndex = " + snapIndex);
    }

    @FXML
    private void menuItem1Action() {
        snapIndex = 1;
        menuButtonResolution.setText(menuItemList.get(snapIndex).getText());
        System.out.println("snapIndex = " + snapIndex);
    }

    @FXML
    private void menuItem2Action() {
        snapIndex = 2;
        menuButtonResolution.setText(menuItemList.get(snapIndex).getText());
        System.out.println("snapIndex = " + snapIndex);
    }

    public void setMainFXInstance(MainFX mainFXInstance) {
        this.mainFXInstance = mainFXInstance;
    }

    @FXML
    private void showZoomStage() {
        System.out.println("getSnap " +
                HResult.key(
                        app.getLibToupcam().Toupcam_Snap(app.getCamPointer(), snapIndex)
                )
        );

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("getSnapFromApp");

        int w = (int) app.getResolution(snapIndex).getWidth();
        int h = (int) app.getResolution(snapIndex).getHeight();

        BufferedImage bufferedImage = Util.convertImagePointerToImage(snapPointer, w, h);

        snapImage = SwingFXUtils.toFXImage(bufferedImage, null);
//        imageView.setFitWidth(1600);
//        imageView.setFitHeight(1200);
        imageView.setImage(snapImage);

        Stage zoomStage = mainFXInstance.getZoomStage();
        SnapWindow snapWindow = mainFXInstance.getSnapWindow();

//        String path3 = System.getProperty("user.dir") + "\\src\\main\\resources\\1.png";
//        File file = new File(path3);
//        Image image1 = new Image(file.toURI().toString());


//        snapWindow.initView(zoomStage, image1);
        snapWindow.initView(zoomStage, snapImage);
        zoomStage.show();
    }

    public void setSnapPointer(Pointer snapPointer) {
        this.snapPointer = snapPointer;
    }


}
