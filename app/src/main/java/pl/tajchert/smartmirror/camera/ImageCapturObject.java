package pl.tajchert.smartmirror.camera;

/**
 * Created by tajchert on 12.04.15.
 */
public class ImageCapturObject {
    public byte [] imageByteArray;
    public int brightnessChangeTemporary;
    public int brightnessChangeLongterm;
    public int brightnessCurrent;

    public int width;
    public int height;

    public ImageCapturObject(byte[] imageByteArray, int width, int height) {
        this.imageByteArray = imageByteArray;
        this.width = width;
        this.height = height;
    }
}
