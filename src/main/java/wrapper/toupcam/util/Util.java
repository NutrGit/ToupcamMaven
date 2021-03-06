package wrapper.toupcam.util;

import com.sun.jna.Pointer;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Iterator;

public class Util {

    /**
     * to keep the JVM running, for receiving callbacks
     * from toupcam.
     */
    public static void keepVMRunning() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                }
            }
        }).start();
    }

    public static void displayBytes(Pointer pointer) {
        for (int i = 0; i < 10; i++)
            System.out.print(pointer.getByte(i) + ", ");
        System.out.println();
    }

    public static BufferedImage convertImagePointerToImage(Pointer imagePointer, int width, int height) {
        BufferedImage newbImage = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
        int[] ints = convertImagePointerToIntArray(imagePointer, width, height);
        newbImage.setRGB(0, 0, width, height, ints, 0, width);
        return newbImage;
    }

    public static byte[] convertImagePointerToByteArray(Pointer imagePointer, int width, int height) {
        int[] imageData = convertImagePointerToIntArray(imagePointer, width, height);
//        ByteBuffer byteBuffer = ByteBuffer.allocate(imageData.length * 4);
        ByteBuffer byteBuffer = ByteBuffer.allocate(imageData.length * 8);
        IntBuffer intBuffer = byteBuffer.asIntBuffer();
        intBuffer.put(imageData);
        return byteBuffer.array();
    }

    private static int[] convertImagePointerToIntArray(Pointer imagePointer, int width, int height) {
        int counter = 0;
        int[] ints = new int[height * width * 3];
        for (int indexPix = 0; indexPix < height * width * 3; ) {
            int redVal = imagePointer.getByte(indexPix++);
            int greenVal = imagePointer.getByte(indexPix++);
            int blueVal = imagePointer.getByte(indexPix++);

            final int rgb = (0xff << 24) + (blueVal << 16) + (greenVal << 8) + redVal;

            if (indexPix < height * width * 3) {
                ints[counter++] = rgb;
            }
        }
        return ints;
    }

    public static void saveJPG(BufferedImage image) throws IOException {
        System.out.println("saveJPG");
        BufferedImage imageRGB = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.OPAQUE);
        Graphics2D graphics = imageRGB.createGraphics();
        graphics.drawImage(image, 0, 0, null);
        graphics.dispose();

        File compressedImageFile = new File(Constants.IMAGES_PATH + "/image" + imageCounter++ + "compress.jpg");
        OutputStream os = new FileOutputStream(compressedImageFile);

        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
        ImageWriter writer = (ImageWriter) writers.next();

        ImageOutputStream ios = ImageIO.createImageOutputStream(os);
        writer.setOutput(ios);

        ImageWriteParam param = writer.getDefaultWriteParam();

        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);

        //	param.setCompressionQuality(0.5f);
        writer.write(null, new IIOImage(imageRGB, null, null), param);

        os.close();
        ios.close();
        writer.dispose();
    }

    public static byte[] compressBufferedImageByteArray(BufferedImage image) {
        BufferedImage imageRGB = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.OPAQUE);
        Graphics2D graphics = imageRGB.createGraphics();
        graphics.drawImage(image, 0, 0, null);
        graphics.dispose();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
        ImageWriter writer = (ImageWriter) writers.next();

        ImageOutputStream imageOutputStream = null;
        try {
            imageOutputStream = ImageIO.createImageOutputStream(outputStream);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        writer.setOutput(imageOutputStream);
        ImageWriteParam param = writer.getDefaultWriteParam();

        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);

        param.setCompressionQuality(0.5f);
        try {
            writer.write(null, new IIOImage(imageRGB, null, null), param);
        } catch (IOException e) {
            e.printStackTrace();
        }

        writer.dispose();
        return outputStream.toByteArray();
    }

    public static BufferedImage compressBufferedImage(BufferedImage image) {
        byte[] imageBytes = compressBufferedImageByteArray(image);
        InputStream imageBytesInputStream = new ByteArrayInputStream(imageBytes);
        try {
            return ImageIO.read(imageBytesInputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static int imageCounter = 0;

    public static void writeImageToDisk(BufferedImage image) {
        System.out.println("savePNG");
        createImageDir();        // prefer displaying images on JFrame, in that case remove this line.

        try {
            ImageIO.write(image, "png", new File(
                    Constants.IMAGES_PATH + "/image" + imageCounter++ + ".png"));
        } catch (Exception e) {
            System.out.println("Exception thrown during convertion : " + e);
        }

    }

    private static void createImageDir() {
        File file = new File(Constants.IMAGES_PATH);
        if (!file.exists())
            file.mkdirs();
    }

    public static Image convertToFxImage(BufferedImage image) {
        WritableImage wr = null;
        if (image != null) {
            wr = new WritableImage(image.getWidth(), image.getHeight());
            PixelWriter pw = wr.getPixelWriter();
            for (int x = 0; x < image.getWidth(); x++) {
                for (int y = 0; y < image.getHeight(); y++) {
                    pw.setArgb(x, y, image.getRGB(x, y));
                }
            }
        }

        return new ImageView(wr).getImage();
    }

    public static Image matToImageFX(Mat mat) {
        MatOfByte byteMat = new MatOfByte();
        Imgcodecs.imencode(".bmp", mat, byteMat);
        return new Image(new ByteArrayInputStream(byteMat.toArray()));
    }


}
