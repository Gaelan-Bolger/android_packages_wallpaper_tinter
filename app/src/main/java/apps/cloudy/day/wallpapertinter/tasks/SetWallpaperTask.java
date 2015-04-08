package apps.cloudy.day.wallpapertinter.tasks;

import android.app.ProgressDialog;
import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;

import apps.cloudy.day.wallpapertinter.R;

public class SetWallpaperTask extends AsyncTask<Bitmap, Void, Exception> {

    public interface SetWallpaperCallback {
        public void onWallpaperSet(Boolean success);
    }

    private static final String TAG = "SetWallpaperTask";

    private ProgressDialog sProgress;
    private Context sContext;
    private final SetWallpaperCallback sCallback;

    public SetWallpaperTask(Context context, SetWallpaperCallback callback) {
        sContext = context;
        sCallback = callback;
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
        if (null != e)
            Log.e(TAG, "Error setting wallpaper", e);
        if (null != sProgress)
            sProgress.dismiss();
        if (null != sCallback)
            sCallback.onWallpaperSet(null == e);
    }
}
