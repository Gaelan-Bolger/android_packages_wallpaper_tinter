package apps.cloudy.day.wallpapertinter;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.lang.ref.WeakReference;

/**
 * Created by Gaelan on 4/5/2015.
 */
public class TintBitmapTask extends AsyncTask<Integer, Void, Bitmap> {

    private Context sContext;
    private WeakReference<ImageView> sImageReference;
    private Bitmap sSource;
    private PorterDuff.Mode sBlendMode;
    private ProgressDialog sProgress;

    public TintBitmapTask(ImageView imageView, Bitmap source, PorterDuff.Mode blendMode) {
        sContext = imageView.getContext();
        sImageReference = new WeakReference<>(imageView);
        sSource = source;
        sBlendMode = blendMode;
    }

    @Override
    protected void onPreExecute() {
        sProgress = new ProgressDialog(sContext);
        sProgress.setIndeterminate(true);
        sProgress.setMessage(sContext.getString(R.string.dialog_processing_image));
        sProgress.show();
    }

    @Override
    protected Bitmap doInBackground(Integer... params) {
        int redVal = params[0];
        int greenVal = params[1];
        int blueVal = params[2];
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

            int newColor = pixel;
            if (sBlendMode == PorterDuff.Mode.ADD) {
                newColor = Color.rgb(
                        pixelRed + Color.red(redVal),
                        pixelGreen + Color.green(greenVal),
                        pixelBlue + Color.blue(blueVal));
            } else if (sBlendMode == PorterDuff.Mode.MULTIPLY) {
                newColor = Color.rgb(
                        pixelRed * Color.red(redVal) / 255,
                        pixelGreen * Color.green(greenVal) / 255,
                        pixelBlue * Color.blue(blueVal) / 255);
            } else if (sBlendMode == PorterDuff.Mode.SCREEN) {
                newColor = Color.rgb(
                        255 - (((255 - redVal) * (255 - pixelRed)) / 255),
                        255 - (((255 - greenVal) * (255 - pixelGreen)) / 255),
                        255 - (((255 - blueVal) * (255 - pixelBlue)) / 255));
            } else if (sBlendMode == PorterDuff.Mode.OVERLAY) {
                newColor = Color.rgb(
                        (pixelRed < 128 ? 2 * redVal * pixelRed / 255 : 255 - 2 * (255 - redVal) * (255 - pixelRed) / 255),
                        (pixelGreen < 128 ? 2 * greenVal * pixelGreen / 255 : 255 - 2 * (255 - greenVal) * (255 - pixelGreen) / 255),
                        (pixelBlue < 128 ? 2 * blueVal * pixelBlue / 255 : 255 - 2 * (255 - blueVal) * (255 - pixelBlue) / 255));
            }
            pixels[i] = newColor;
        }

        Bitmap b = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        b.setPixels(pixels, 0, w, 0, 0, w, h);
        return b;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (null != sProgress)
            sProgress.hide();
        if (null != bitmap) {
            ImageView imageView = sImageReference.get();
            if (null != imageView)
                imageView.setImageBitmap(bitmap);
        }
    }

}