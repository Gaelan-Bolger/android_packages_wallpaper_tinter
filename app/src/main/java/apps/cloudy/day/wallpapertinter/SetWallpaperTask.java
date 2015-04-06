package apps.cloudy.day.wallpapertinter;

import android.app.ProgressDialog;
import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;

/**
 * Created by Gaelan on 4/5/2015.
 */
public class SetWallpaperTask extends AsyncTask<Bitmap, Void, Exception> {

    private static final String TAG = "SetWallpaperTask";

    private Context sContext;
    private ProgressDialog sProgress;

    public SetWallpaperTask(Context context) {
        sContext = context;
    }

    @Override
    protected Exception doInBackground(Bitmap... params) {
        Bitmap bitmap = params[0];
        try {
            WallpaperManager.getInstance(sContext).setBitmap(bitmap);
        } catch (IOException e) {
            return e;
        }
        return null;
    }

    @Override
    protected void onPreExecute() {
        sProgress = new ProgressDialog(sContext);
        sProgress.setCancelable(false);
        sProgress.setCanceledOnTouchOutside(false);
        sProgress.setIndeterminate(true);
        sProgress.setMessage(sContext.getString(R.string.dialog_setting_wallpaper));
        sProgress.show();
    }

    @Override
    protected void onPostExecute(Exception e) {
        if (null != sProgress)
            sProgress.dismiss();
        if (null != sContext) {
            if (null != e) {
                Log.e(TAG, "Error setting wallpaper", e);
                Toast.makeText(sContext, sContext.getString(R.string.toast_error_setting_wallpaper), Toast.LENGTH_SHORT).show();
            } else {
                Log.d(TAG, "Wallpaper set");
                Toast.makeText(sContext, sContext.getString(R.string.toast_wallpaper_set), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
