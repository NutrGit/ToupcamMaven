package wrapper.toupcam.opencv400;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.opencv.core.Core;

import java.io.File;
import java.net.URL;

public class MainFX extends Application {

    private Stage zoomStage = new Stage();
    private SnapWindow snapWindow = new SnapWindow();

    @Override
    public void start(Stage primaryStage) throws Exception {

//        FXMLLoader loader = new FXMLLoader(getClass().getResource("Frame.fxml"));
//        Pane root = (Pane) loader.load();
//        primaryStage.setTitle("Hello World");
//        Scene scene = new Scene(root, 1250, 700);
//        primaryStage.setScene(scene);
//        primaryStage.show();

        String path = "C:/Users/UserCV/Downloads/ToupcamWrapper-master/src/main/java/wrapper/toupcam/opencv400/Frame.fxml";
        String path2 = System.getProperty("user.dir") + "\\src\\main\\java\\wrapper\\toupcam\\opencv400\\Frame.fxml";
        System.out.println(path2);
        URL url = new File(path2).toURI().toURL();
        FXMLLoader loader = new FXMLLoader(url);
        AnchorPane rootElement = (AnchorPane) loader.load();
        Scene scene = new Scene(rootElement, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();

        Controller controller = loader.getController();
        controller.setStage(primaryStage);
        controller.init();
        controller.setMainFXInstance(this);

        String path3 = System.getProperty("user.dir") + "\\src\\main\\resources\\IMG_3977.JPG";
        File file = new File(path3);
        Image image1 = new Image(file.toURI().toString());
        snapWindow.initView(zoomStage, image1);

    }


    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        launch(args);
    }

    public Stage getZoomStage() {
        return this.zoomStage;
    }

    public SnapWindow getSnapWindow(){
        return this.snapWindow;
    }


}