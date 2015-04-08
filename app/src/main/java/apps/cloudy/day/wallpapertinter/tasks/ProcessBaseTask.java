package apps.cloudy.day.wallpapertinter.tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;

import apps.cloudy.day.wallpapertinter.R;

public abstract class ProcessBaseTask extends AsyncTask<Integer, Void, Bitmap> {

    public interface ProcessBitmapCallback {
        public void onImageProcessed(Bitmap bitmap);
    }

    private static final String TAG = "TintBitmapTask";

    private ProgressDialog sProgress;
    private Context sContext;
    private ProcessBitmapCallback sCallback;
    protected Bitmap sSource;

    public ProcessBaseTask(Context context, Bitmap source, ProcessBitmapCallback callback) {
        sContext = context;
        sSource = source;
        sCallback = callback;
    }

    @Override
    protected void onPreExecute() {
        sProgress = new ProgressDialog(sContext);
        sProgress.setCancelable(false);
        sProgress.setCanceledOnTouchOutside(false);
        sProgress.setIndeterminate(true);
        sProgress.setMessage(sContext.getString(R.string.dialog_processing_image));
        sProgress.show();
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (null != sProgress)
            sProgress.dismiss();
        if (null != sCallback)
            sCallback.onImageProcessed(bitmap);
    }

}