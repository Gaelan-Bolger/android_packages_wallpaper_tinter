package apps.cloudy.day.wallpapertinter;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import java.io.IOException;

public class MainActivity extends ActionBarActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";
    private static final String EXTRA_SOURCE = "source";
    private static final int PHOTO_PICKER_CODE = 1001;

    private PorterDuff.Mode mBlendMode = PorterDuff.Mode.ADD;
    private Bitmap mSource;

    private ImageView mImage;
    private SeekBar mSeekBarRed;
    private SeekBar mSeekBarGreen;
    private SeekBar mSeekBarBlue;
    private Button bGetSetWallpaper;
    private Button bSelectReset;
    private SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            performTint();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (null != savedInstanceState)
            mSource = savedInstanceState.getParcelable(EXTRA_SOURCE);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(EXTRA_SOURCE, mSource);
    }

    @Override
    public void onSupportContentChanged() {
        super.onSupportContentChanged();
        mImage = (ImageView) findViewById(R.id.image);
        mImage.setOnTouchListener(new View.OnTouchListener() {

            public Bitmap sModified;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (null != mSource) {
                    int action = event.getAction();
                    switch (action) {
                        case MotionEvent.ACTION_DOWN:
                            sModified = ((BitmapDrawable) mImage.getDrawable()).getBitmap();
                            mImage.setImageBitmap(mSource);
                            break;
                        case MotionEvent.ACTION_UP:
                        case MotionEvent.ACTION_CANCEL:
                            mImage.setImageBitmap(sModified);
                            break;
                    }
                    return true;
                }
                return false;
            }
        });

        mSeekBarRed = (SeekBar) findViewById(R.id.sb_red);
        mSeekBarRed.setOnSeekBarChangeListener(seekBarChangeListener);
        mSeekBarGreen = (SeekBar) findViewById(R.id.sb_green);
        mSeekBarGreen.setOnSeekBarChangeListener(seekBarChangeListener);
        mSeekBarBlue = (SeekBar) findViewById(R.id.sb_blue);
        mSeekBarBlue.setOnSeekBarChangeListener(seekBarChangeListener);

        bGetSetWallpaper = (Button) findViewById(R.id.b_get_wallpaper);
        bGetSetWallpaper.setOnClickListener(this);
        bSelectReset = (Button) findViewById(R.id.b_select_image);
        bSelectReset.setOnClickListener(this);

        if (null != mSource)
            onSourceChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PHOTO_PICKER_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                Uri uri = data.getData();
                try {
                    mSource = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                    onSourceChanged();
                } catch (IOException e) {
                    Log.e(TAG, "Error selecting image", e);
                }
            } else {
                Toast.makeText(this, getString(R.string.toast_no_image_selected), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.blend_mode_add:
                item.setChecked(true);
                setBlendMode(PorterDuff.Mode.ADD);
                return true;
            case R.id.blend_mode_multiply:
                item.setChecked(true);
                setBlendMode(PorterDuff.Mode.MULTIPLY);
                return true;
            case R.id.blend_mode_screen:
                item.setChecked(true);
                setBlendMode(PorterDuff.Mode.SCREEN);
                return true;
            case R.id.blend_mode_overlay:
                item.setChecked(true);
                setBlendMode(PorterDuff.Mode.OVERLAY);
                return true;
            case R.id.action_settings:
                Toast.makeText(this, R.string.action_settings, Toast.LENGTH_SHORT).show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setBlendMode(PorterDuff.Mode mode) {
        mBlendMode = mode;
        performTint();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.b_get_wallpaper:
                if (null == mSource) {
                    mSource = ((BitmapDrawable) WallpaperManager.getInstance(this).getDrawable()).getBitmap();
                    onSourceChanged();
                } else {
                    BitmapDrawable drawable = (BitmapDrawable) mImage.getDrawable();
                    new SetWallpaperTask(this).execute(drawable.getBitmap());
                }
                break;
            case R.id.b_select_image:
                if (null == mSource) {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/*");
                    intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                    startActivityForResult(Intent.createChooser(intent, getString(R.string.dialog_title_select_image)), PHOTO_PICKER_CODE);
                } else {
                    mSource = null;
                    resetViews();
                }
                break;
            default:
                break;
        }

    }

    private void onSourceChanged() {
        mSeekBarRed.setProgress(0);
        mSeekBarBlue.setProgress(0);
        mSeekBarGreen.setProgress(0);
        findViewById(R.id.seekbars).setVisibility(View.VISIBLE);
        bGetSetWallpaper.setText(getString(R.string.set_wallpaper));
        bSelectReset.setText(getString(R.string.reset));
        mImage.setImageBitmap(mSource);
    }

    private void resetViews() {
        mImage.setImageResource(0);
        bGetSetWallpaper.setText(getString(R.string.get_wallpaper));
        bSelectReset.setText(getString(R.string.select_image));
        mSeekBarRed.setProgress(0);
        mSeekBarBlue.setProgress(0);
        mSeekBarGreen.setProgress(0);
        findViewById(R.id.seekbars).setVisibility(View.GONE);
    }

    private void performTint() {
        if (null != mSource)
            new TintBitmapTask(mImage, mSource, mBlendMode).execute(mSeekBarRed.getProgress(), mSeekBarGreen.getProgress(), mSeekBarBlue.getProgress());
    }

}
