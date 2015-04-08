package apps.cloudy.day.wallpapertinter;

import android.app.AlertDialog;
import android.app.WallpaperManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import java.io.IOException;

import apps.cloudy.day.wallpapertinter.tasks.ProcessBaseTask;
import apps.cloudy.day.wallpapertinter.tasks.ProcessBlendTask;
import apps.cloudy.day.wallpapertinter.tasks.SetWallpaperTask;

public class ImageEditActivity extends ActionBarActivity {

    private static final String TAG = "ImageEditActivity";
    public static final String EXTRA_SOURCE_BITMAP = "source_bitmap";
    public static final String EXTRA_SOURCE_URI = "source_uri";
    private static final int DEFAULT_BRIGHTNESS = 0;
    private static final int DEFAULT_CONTRAST = 0;
    private static final int DEFAULT_SATURATION = 100;
    private static final int DEFAULT_HUE = 0;
    private static final int DEFAULT_BLEND = 0;

    private int mBrightness = DEFAULT_BRIGHTNESS;
    private int mContrast = DEFAULT_CONTRAST;
    private int mSaturation = DEFAULT_SATURATION;
    private int mHue = DEFAULT_HUE;
    private int mBlend = DEFAULT_BLEND;
    private BlendMode mBlendMode = BlendMode.ADD;
    private String mSourceUri;
    private Bitmap mSource;
    private Bitmap mModified;
    private ImageView mImage;
    private SeekBar mSeekBarHue;
    private SeekBar mSeekBarBrightness;
    private SeekBar mSeekBarContrast;
    private SeekBar mSeekBarSaturation;
    private SeekBar mSeekBarBlend;
    private View.OnTouchListener mImageTouchListener = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            int action = event.getAction();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    mImage.setImageBitmap(mSource);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_OUTSIDE:
                case MotionEvent.ACTION_CANCEL:
                    mImage.setImageBitmap(mModified);
                    break;
            }
            return true;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        Intent intent = getIntent();
        if (null != intent) {
            if (intent.hasExtra(EXTRA_SOURCE_URI)) {
                mSourceUri = intent.getStringExtra(EXTRA_SOURCE_URI);
                loadSource();
            }
        } else if (null != savedInstanceState)
            mSource = savedInstanceState.getParcelable(EXTRA_SOURCE_BITMAP);
        if (null == mSource)
            throw new IllegalArgumentException("No source bitmap provided");

        setContentView(R.layout.activity_image_edit);
    }

    @Override
    public void onSupportContentChanged() {
        super.onSupportContentChanged();
        mImage = (ImageView) findViewById(R.id.image);
        mImage.setOnTouchListener(mImageTouchListener);
        updateImage();

        // Brightness
        mSeekBarBrightness = (SeekBar) findViewById(R.id.sb_brightness);
        mSeekBarBrightness.setMax(100);
        mSeekBarBrightness.setOnSeekBarChangeListener(new SimpleSeekBarListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mBrightness = seekBar.getProgress();
                adjustColor();
            }
        });
        // Contrast
        mSeekBarContrast = (SeekBar) findViewById(R.id.sb_contrast);
        mSeekBarContrast.setMax(100);
        mSeekBarContrast.setOnSeekBarChangeListener(new SimpleSeekBarListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mContrast = seekBar.getProgress();
                adjustColor();
            }
        });
        // Saturation
        mSeekBarSaturation = (SeekBar) findViewById(R.id.sb_saturation);
        mSeekBarSaturation.setMax(100);
        mSeekBarSaturation.setOnSeekBarChangeListener(new SimpleSeekBarListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mSaturation = seekBar.getProgress();
                adjustColor();
            }
        });
        // Hue
        mSeekBarHue = (SeekBar) findViewById(R.id.sb_hue);
        mSeekBarHue.setMax(180);
        mSeekBarHue.setOnSeekBarChangeListener(new SimpleSeekBarListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mHue = seekBar.getProgress();
                adjustColor();
            }
        });
        // Blend
        mSeekBarBlend = (SeekBar) findViewById(R.id.sb_blend);
        mSeekBarBlend.setMax(Color.BLACK * -1);
        mSeekBarBlend.setOnSeekBarChangeListener(new SimpleSeekBarListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mBlend = seekBar.getProgress();
                blend();
            }
        });
        updateLevels();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(EXTRA_SOURCE_BITMAP, mSource);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_image_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_set_wallpaper:
                showSetWallpaperDialog();
                return true;
            case R.id.action_export:
                showExportWallpaperDialog();
                return true;
            case R.id.action_reset:
                showResetChangesDialog();
                return true;
            case R.id.blend_mode_add:
                item.setChecked(true);
                setBlendMode(BlendMode.ADD);
                return true;
            case R.id.blend_mode_subtract:
                item.setChecked(true);
                setBlendMode(BlendMode.SUBTRACT);
                return true;
            case R.id.blend_mode_multiply:
                item.setChecked(true);
                setBlendMode(BlendMode.MULTIPLY);
                return true;
            case R.id.blend_mode_screen:
                item.setChecked(true);
                setBlendMode(BlendMode.SCREEN);
                return true;
            case R.id.blend_mode_overlay:
                item.setChecked(true);
                setBlendMode(BlendMode.OVERLAY);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadSource() {
        if (mSourceUri.equals("chroma_default")) {
            mSource = BitmapFactory.decodeResource(getResources(), R.drawable.chroma_wallpaper);
        } else if (mSourceUri.equals("current_wallpaper")) {
            mSource = ((BitmapDrawable) WallpaperManager.getInstance(this).getDrawable()).getBitmap();
        } else {
            try {
                mSource = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.parse(mSourceUri));
            } catch (IOException e) {
                Log.e(TAG, "Error loading image from uri", e);
            }
        }
        mModified = mSource;
    }

    private void reset() {
        loadSource();
        updateImage();
        updateLevels();
    }

    private void updateImage() {
        mImage.setImageBitmap(mModified);
    }

    private void updateLevels() {
        mSeekBarBrightness.setProgress(mBrightness = DEFAULT_BRIGHTNESS);
        mSeekBarContrast.setProgress(mContrast = DEFAULT_CONTRAST);
        mSeekBarSaturation.setProgress(mSaturation = DEFAULT_SATURATION);
        mSeekBarHue.setProgress(mHue = DEFAULT_HUE);
        mSeekBarBlend.setProgress(mBlend = DEFAULT_BLEND);
    }

    private void adjustColor() {
        int w = mSource.getWidth();
        int h = mSource.getHeight();
        Bitmap b = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(b);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColorFilter(ColorFilterGenerator.adjustColor(mBrightness, mContrast, mSaturation, mHue));
        canvas.drawBitmap(mSource, 0, 0, paint);
        mModified = b;
        blend();
    }

    private void setBlendMode(BlendMode mode) {
        mBlendMode = mode;
        blend();
    }

    private void blend() {
        new ProcessBlendTask(this, mModified, new ProcessBaseTask.ProcessBitmapCallback() {
            @Override
            public void onImageProcessed(Bitmap bitmap) {
                if (null != bitmap) {
                    mModified = bitmap;
                    updateImage();
                }
            }
        }).execute(mBlendMode.ordinal(), mBlend);
    }

    private void showResetChangesDialog() {
        new AlertDialog.Builder(this)
                .setCancelable(true)
                .setMessage(getString(R.string.dialog_reset_all_changes))
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        reset();
                    }
                }).create().show();
    }

    private void showExportWallpaperDialog() {
        new AlertDialog.Builder(this)
                .setCancelable(true)
                .setMessage(getString(R.string.dialog_save_to_sdcard))
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MediaStore.Images.Media.insertImage(getContentResolver(), mModified, getString(R.string.image_title), getString(R.string.image_description));
                        Toast.makeText(ImageEditActivity.this, getString(R.string.toast_wallpaper_saved), Toast.LENGTH_SHORT).show();
                    }
                }).create().show();
    }

    private void showSetWallpaperDialog() {
        new AlertDialog.Builder(this)
                .setCancelable(true)
                .setMessage(getString(R.string.dialog_set_as_wallpaper))
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new SetWallpaperTask(ImageEditActivity.this, new SetWallpaperTask.SetWallpaperCallback() {
                            @Override
                            public void onWallpaperSet(Boolean success) {
                                if (success) {
                                    Toast.makeText(ImageEditActivity.this, getString(R.string.toast_wallpaper_set), Toast.LENGTH_SHORT).show();
                                    finish();
                                } else {
                                    Toast.makeText(ImageEditActivity.this, getString(R.string.toast_error_setting_wallpaper), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }).execute(null != mModified ? mModified : mSource);
                    }
                }).create().show();
    }
}
