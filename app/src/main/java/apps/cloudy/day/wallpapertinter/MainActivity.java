package apps.cloudy.day.wallpapertinter;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {

    private static final String TAG = "MainActivity";
    private static final int PHOTO_PICKER_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public void onSupportContentChanged() {
        super.onSupportContentChanged();
        findViewById(R.id.b_chroma_default).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSourceSelected("chroma_default");
            }
        });
        findViewById(R.id.b_current_wallpaper).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSourceSelected("current_wallpaper");
            }
        });
        findViewById(R.id.b_select_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(Intent.createChooser(intent, getString(R.string.dialog_title_select_image)), PHOTO_PICKER_CODE);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        ((ImageView) findViewById(R.id.iv_current_wallpaper)).setImageBitmap(getCurrentWallpaper());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PHOTO_PICKER_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                Uri uri = data.getData();
                onSourceSelected(uri.toString());
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
            case R.id.action_settings:
                Toast.makeText(this, R.string.action_settings, Toast.LENGTH_SHORT).show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private Bitmap getCurrentWallpaper() {
        return ((BitmapDrawable) WallpaperManager.getInstance(this).getDrawable()).getBitmap();
    }

    private void onSourceSelected(String uri) {
        Log.d(TAG, "onSourceSelected");
        Intent intent = new Intent(this, ImageEditActivity.class);
        intent.putExtra(ImageEditActivity.EXTRA_SOURCE_URI, uri);
        startActivity(intent);
    }

}
