package pl.tajchert.smartmirror;

/**
 * Created by tajchert on 12.04.15.
 */
public class ImageCapturObject {
    public byte [] imageByteArray;
    public int brightnessChange;

    public int width;
    public int height;
    public int prevBrightness;

    public ImageCapturObject(byte[] imageByteArray, int width, int height, int prevBrightness) {
        this.imageByteArray = imageByteArray;
        this.width = width;
        this.height = height;
        this.prevBrightness = prevBrightness;
    }
}
