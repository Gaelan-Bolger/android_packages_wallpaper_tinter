package apps.cloudy.day.wallpapertinter.tasks;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;

import apps.cloudy.day.wallpapertinter.BlendMode;

public class ProcessBlendTask extends ProcessBaseTask {

    private static final String TAG = "BitmapBlendTask";

    public ProcessBlendTask(Context context, Bitmap source, ProcessBitmapCallback callback) {
        super(context, source, callback);
    }

    @Override
    protected Bitmap doInBackground(Integer... params) {
        BlendMode blendMode = BlendMode.values()[params[0]];
        int blend = params[1];
        int redVal = Color.red(blend);
        int greenVal = Color.green(blend);
        int blueVal = Color.blue(blend);
        int w = sSource.getWidth();
        int h = sSource.getHeight();
        int[] pixels = new int[w * h];
        int pixelCount = pixels.length;
        sSource.getPixels(pixels, 0, w, 0, 0, w, h);

        for (int i = 0; i < pixelCount; i++) {
            int pixel = pixels[i];
            int pixelRed = Color.red(pixel);
            int pixelGreen = Color.green(pixel);
            int pixelBlue = Color.blue(pixel);

            if (blendMode == BlendMode.ADD) {
                pixels[i] = Color.rgb(add(pixelRed, redVal), add(pixelGreen, greenVal), add(pixelBlue, blueVal));
            } else if (blendMode == BlendMode.SUBTRACT) {
                pixels[i] = Color.rgb(subtract(pixelRed, redVal), subtract(pixelGreen, greenVal), subtract(pixelBlue, blueVal));
            } else if (blendMode == BlendMode.MULTIPLY) {
                pixels[i] = Color.rgb(multiply(pixelRed, redVal), multiply(pixelGreen, greenVal), multiply(pixelBlue, blueVal));
            } else if (blendMode == BlendMode.SCREEN) {
                pixels[i] = Color.rgb(screen(pixelRed, redVal), screen(pixelGreen, greenVal), screen(pixelBlue, blueVal));
            } else if (blendMode == BlendMode.OVERLAY) {
                pixels[i] = Color.rgb(overlay(pixelRed, redVal), overlay(pixelGreen, greenVal), overlay(pixelBlue, blueVal));
            }
        }

        Bitmap b = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        b.setPixels(pixels, 0, w, 0, 0, w, h);
        return b;
    }

    private int add(int pixel, int value) {
        return pixel + value;
    }

    private int subtract(int pixel, int value) {
        return pixel - value;
    }

    private int multiply(int pixel, int value) {
        return pixel * value / 255;
    }

    private int screen(int pixel, int value) {
        return 255 - (((255 - pixel) * (255 - value)) / 255);
    }

    private int overlay(int pixel, int value) {
        return (pixel < 128 ? 2 * pixel * value / 255 : 255 - 2 * (255 - pixel) * (255 - value) / 255);
    }

}