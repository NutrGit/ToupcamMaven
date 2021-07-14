package wrapper.toupcam.opencv400;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class SnapWindow {

    static double initx;
    static double inity;
    static int height;
    static int width;
    static Scene view;
    static double offSetX, offSetY, zoomValue;

    public void initView(Stage s, Image source) {
        s.setResizable(true);
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);

        ImageView image = new ImageView(source);
        double ratio = source.getWidth() / source.getHeight();

//        if (500 / ratio < 500) {
//            width = 500;
//            height = (int) (500 / ratio);
//        } else if (500 * ratio < 500) {
        height = 1000;
        width = (int) (1000 * ratio);
//        } else {
//            height = 500;
//            width = 500;
//        }

        image.setPreserveRatio(false);
        image.setFitWidth(width);
        image.setFitHeight(height);
        height = (int) source.getHeight();
        width = (int) source.getWidth();
        System.out.println("height = " + height + "\nwidth = " + width);
        HBox zoom = new HBox(10);
        zoom.setAlignment(Pos.CENTER);

        Slider zoomSlider = new Slider();
        zoomSlider.setMax(4);
        zoomSlider.setMin(1);
        zoomSlider.setMaxWidth(200);
        zoomSlider.setMinWidth(200);
        Label labelHint = new Label("Zoom Level");
        Label labelValue = new Label("1.0");

        offSetX = width / 2;
        offSetY = height / 2;

        zoom.getChildren().addAll(labelHint, zoomSlider, labelValue);

        Slider Hscroll = new Slider();
        Hscroll.setMin(0);
        Hscroll.setMax(width);
        Hscroll.setMaxWidth(image.getFitWidth());
        Hscroll.setMinWidth(image.getFitWidth());
        Hscroll.setTranslateY(-20);

        Slider Vscroll = new Slider();
        Vscroll.setMin(0);
        Vscroll.setMax(height);
        Vscroll.setMaxHeight(image.getFitHeight());
        Vscroll.setMinHeight(image.getFitHeight());
        Vscroll.setOrientation(Orientation.VERTICAL);
        Vscroll.setTranslateX(-20);

        BorderPane imageView = new BorderPane();
        BorderPane.setAlignment(Hscroll, Pos.CENTER);
        BorderPane.setAlignment(Vscroll, Pos.CENTER_LEFT);

        Hscroll.valueProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable e) {
                offSetX = Hscroll.getValue();
                zoomValue = zoomSlider.getValue();
                double newValue = (double) ((int) (zoomValue * 10)) / 10;
                labelValue.setText(newValue + "");
                if (offSetX < (width / newValue) / 2) {
                    offSetX = (width / newValue) / 2;
                }
                if (offSetX > width - ((width / newValue) / 2)) {
                    offSetX = width - ((width / newValue) / 2);
                }

                image.setViewport(new Rectangle2D(offSetX - ((width / newValue) / 2), offSetY - ((height / newValue) / 2), width / newValue, height / newValue));
            }
        });

        Vscroll.valueProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable e) {
                offSetY = height - Vscroll.getValue();
                zoomValue = zoomSlider.getValue();
                double newValue = (double) ((int) (zoomValue * 10)) / 10;
                labelValue.setText(newValue + "");
                if (offSetY < (height / newValue) / 2) {
                    offSetY = (height / newValue) / 2;
                }
                if (offSetY > height - ((height / newValue) / 2)) {
                    offSetY = height - ((height / newValue) / 2);
                }
                image.setViewport(new Rectangle2D(offSetX - ((width / newValue) / 2), offSetY - ((height / newValue) / 2), width / newValue, height / newValue));
            }
        });

        imageView.setCenter(image);
        imageView.setTop(Hscroll);
        imageView.setRight(Vscroll);

        zoomSlider.valueProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable e) {
                zoomValue = zoomSlider.getValue();
                double newValue = (double) ((int) (zoomValue * 10)) / 10;
                labelValue.setText(newValue + "");
                if (offSetX < (width / newValue) / 2) {
                    offSetX = (width / newValue) / 2;
                }
                if (offSetX > width - ((width / newValue) / 2)) {
                    offSetX = width - ((width / newValue) / 2);
                }
                if (offSetY < (height / newValue) / 2) {
                    offSetY = (height / newValue) / 2;
                }
                if (offSetY > height - ((height / newValue) / 2)) {
                    offSetY = height - ((height / newValue) / 2);
                }
                Hscroll.setValue(offSetX);
                Vscroll.setValue(height - offSetY);
                image.setViewport(new Rectangle2D(offSetX - ((width / newValue) / 2), offSetY - ((height / newValue) / 2), width / newValue, height / newValue));
            }
        });

        imageView.setCursor(Cursor.OPEN_HAND);

        image.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                initx = e.getSceneX();
                inity = e.getSceneY();
                imageView.setCursor(Cursor.CLOSED_HAND);
            }
        });

        image.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                imageView.setCursor(Cursor.OPEN_HAND);
            }
        });

        image.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                Hscroll.setValue(Hscroll.getValue() + (initx - e.getSceneX()));
                Vscroll.setValue(Vscroll.getValue() - (inity - e.getSceneY()));
                initx = e.getSceneX();
                inity = e.getSceneY();
            }
        });

        image.setOnScroll(new EventHandler<ScrollEvent>() {
            @Override
            public void handle(ScrollEvent event) {

//                System.out.println("event.getTextDeltaX() = " + event.getTextDeltaY());
//                System.out.println("event.getDeltaX() = " + event.getDeltaY());
//                System.out.println("event.getMultiplierX() = " + event.getMultiplierY());
                String s1 = Double.toString(event.getTextDeltaY());
                String s2 = Double.toString(event.getDeltaY());
                String s3 = Double.toString(event.getMultiplierY());

//                s.setTitle("" + s1 + " " + s2 + " " + s3);

                if (event.getDeltaY() > 0) {
                    zoomValue += 0.05;

                } else if (event.getDeltaY() < 0) {
                    zoomValue -= 0.05;
                }

                double newValue = (double) ((int) (zoomValue * 10)) / 10;
                labelValue.setText(newValue + "");
                if (offSetX < (width / newValue) / 2) {
                    offSetX = (width / newValue) / 2;
                }
                if (offSetX > width - ((width / newValue) / 2)) {
                    offSetX = width - ((width / newValue) / 2);
                }
                if (offSetY < (height / newValue) / 2) {
                    offSetY = (height / newValue) / 2;
                }
                if (offSetY > height - ((height / newValue) / 2)) {
                    offSetY = height - ((height / newValue) / 2);
                }
                Hscroll.setValue(offSetX);
                Vscroll.setValue(height - offSetY);
                zoomSlider.setValue(zoomValue);
                image.setViewport(new Rectangle2D(offSetX - ((width / newValue) / 2), offSetY - ((height / newValue) / 2), width / newValue, height / newValue));
            }
        });

        root.getChildren().addAll(imageView, zoom);

//        View = new Scene(root, (image.getFitWidth()) + 70, (image.getFitHeight()) + 150);
        view = new Scene(root, 1600, 900);
        s.setScene(view);
//        s.show();
    }
}