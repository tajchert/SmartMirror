package pl.tajchert.smartmirror.camera;

/**
 * Created by tajchert on 12.04.15.
 */
public class ImageCaptureObject {
    public byte [] imageByteArray;

    public int colorValueRed;
    public int colorValueGreen;
    public int colorValueBlue;

    public int colorRedChangeVal;
    public int colorGreenChangeVal;
    public int colorBlueChangeVal;

    public int colorRedChangeValLongTerm;
    public int colorGreenChangeValLongTerm;
    public int colorBlueChangeValLongTerm;

    public int width;
    public int height;

    public ImageCaptureObject(byte[] imageByteArray, int width, int height) {
        this.imageByteArray = imageByteArray;
        this.width = width;
        this.height = height;
    }
}
