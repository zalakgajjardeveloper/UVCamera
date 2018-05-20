package com.app.UVCamera;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.rey.material.widget.Slider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private int[] pixelsImg1;
    private int[] pixelsImg2;

    private int imgWidth = 0, imgHeight = 0, range = 0;
    private ImageView imgResult;
    private TextView txtSeekbarProgress;

    private int[] pixelsStep1;
    private int[] pixelsStep2;
    private int[] pixelsStep3;

    private int SELECT_FILE = 1;

    private ImageView img1, img2;
    private int currentView = 1;
    private Slider seekBarRange;
    private View layoutResult;
    private boolean mMenuState = false;
    private int[] intencityColors;

    private static final int MY_PERMISSIONS_REQUEST_GALLERY = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        img1 = findViewById(R.id.img1);
        img2 = findViewById(R.id.img2);

//        GlideApp.with(this).load(R.drawable.img111).into(img1);
        //      GlideApp.with(this).load(R.drawable.img111).into(img2);


        findViewById(R.id.fab1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentView = 1;
                checkGalleryPermission();
            }
        });


        findViewById(R.id.fab2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentView = 2;
                checkGalleryPermission();
            }
        });

        imgResult = findViewById(R.id.imgResult);
        txtSeekbarProgress = findViewById(R.id.txtSeekbarProgress);

        findViewById(R.id.btnGetDetail).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        Bitmap bm = ((BitmapDrawable) imgResult.getDrawable()).getBitmap();

                        String filePath = tempFileImage(MainActivity.this, bm, "name");
                        Intent intent = new Intent(MainActivity.this, ImageDetailActivity.class);
                        ActivityOptionsCompat options = ActivityOptionsCompat.
                                makeSceneTransitionAnimation(MainActivity.this,
                                        imgResult,
                                        ViewCompat.getTransitionName(imgResult));
                        intent.putExtra("path", filePath);
                        startActivity(intent, options.toBundle());

                        //showDialog();
                    }
                }, 300);

            }
        });


        View btnApplyIntensity = findViewById(R.id.btnApplyIntensity);
        btnApplyIntensity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        setResultImageStep3();
                    }
                }, 300);

            }
        });
        layoutResult = findViewById(R.id.layoutResult);
        seekBarRange = findViewById(R.id.seekBarRange);
        seekBarRange.setOnPositionChangeListener(onSeekBarChangeListener);

        intencityColors = new int[256];//TEMP
        for (int i = 0; i < intencityColors.length; i++) {
            intencityColors[i] = Color.rgb(new Random().nextInt(255), new Random().nextInt(255), new Random().nextInt(255));
        }

        updateImageResult();

    }


    public static String tempFileImage(Context context, Bitmap bitmap, String name) {

        File outputDir = context.getCacheDir();
        File imageFile = new File(outputDir, name + ".jpg");

        OutputStream os;
        try {
            os = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
            os.flush();
            os.close();
        } catch (Exception e) {
            Log.e(context.getClass().getSimpleName(), "Error writing file", e);
        }

        return imageFile.getAbsolutePath();
    }

    private void updateImageResult() {

        if (img1.getDrawable() == null || img2.getDrawable() == null)
            return;

        imgWidth = img1.getDrawable().getIntrinsicWidth();
        imgHeight = img1.getDrawable().getIntrinsicHeight();

        if (imgWidth != img2.getDrawable().getIntrinsicWidth() || imgHeight != img2.getDrawable().getIntrinsicHeight()) {
            layoutResult.setVisibility(View.GONE);
            mMenuState = false;
            invalidateOptionsMenu();
            Toast.makeText(this, R.string.image_difference_warning, Toast.LENGTH_SHORT).show();
            return;
        }
        mMenuState = true;
        invalidateOptionsMenu();
        layoutResult.setVisibility(View.VISIBLE);

        Bitmap bitmap1 = ((BitmapDrawable) img1.getDrawable()).getBitmap();
        pixelsImg1 = new int[imgWidth * imgHeight];
        bitmap1.getPixels(pixelsImg1, 0, imgWidth, 0, 0, imgWidth, imgHeight);

        Bitmap bitmap2 = ((BitmapDrawable) img2.getDrawable()).getBitmap();
        pixelsImg2 = new int[bitmap2.getWidth() * bitmap2.getHeight()];
        bitmap2.getPixels(pixelsImg2, 0, bitmap2.getWidth(), 0, 0, bitmap2.getWidth(), bitmap2.getHeight());

        seekBarRange.setValue(0, true);

        setResultImageStep1();
        setResultImageStep2();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem item = menu.findItem(R.id.action_name);
        if (item != null) {
            item.setVisible(mMenuState);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_name) {
            checkWritePermission();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void setResultImageStep1() {

        pixelsStep1 = new int[imgWidth * imgHeight];

        for (int i = 0; i < pixelsStep1.length; i++) {
            int colour1 = pixelsImg1[i];
            int colour2 = pixelsImg2[i];

            int blue = Color.blue(colour1) - Color.blue(colour2);
            if (blue < 0)
                blue = 0;


            pixelsStep1[i] = Color.rgb(0, 0, blue);
        }
        Bitmap bitmapResult = Bitmap.createBitmap(imgWidth, imgHeight, Bitmap.Config.ARGB_8888);
        bitmapResult.setPixels(pixelsStep1, 0, imgWidth, 0, 0, imgWidth, imgHeight);
        imgResult.setImageBitmap(bitmapResult);

    }


    private void setResultImageStep2() {

        pixelsStep2 = new int[imgWidth * imgHeight];
        ;
        for (int i = 0; i < pixelsStep1.length; i++) {
            int colour = pixelsStep1[i];

            int blue = Color.blue(colour) + range;
            if (blue > 255)
                blue = 255;

            pixelsStep2[i] = Color.rgb(0, 0, blue);
        }
        Bitmap bitmapResult = Bitmap.createBitmap(imgWidth, imgHeight, Bitmap.Config.ARGB_8888);
        bitmapResult.setPixels(pixelsStep2, 0, imgWidth, 0, 0, imgWidth, imgHeight);
        imgResult.setImageBitmap(bitmapResult);
    }


    private void setResultImageStep3() {
        if (pixelsStep2 == null)
            return;
        pixelsStep3 = new int[imgWidth * imgHeight];

        for (int i = 0; i < pixelsStep2.length; i++) {
            int colour = pixelsStep2[i];
            int blue = Color.blue(colour);

            pixelsStep3[i] = intencityColors[blue];
        }
        Bitmap bitmapResult = Bitmap.createBitmap(imgWidth, imgHeight, Bitmap.Config.ARGB_8888);
        bitmapResult.setPixels(pixelsStep3, 0, imgWidth, 0, 0, imgWidth, imgHeight);
        imgResult.setImageBitmap(bitmapResult);
    }

    private Slider.OnPositionChangeListener onSeekBarChangeListener = new Slider.OnPositionChangeListener() {
        @Override
        public void onPositionChanged(Slider view, boolean fromUser, float oldPos, float newPos, int oldValue, int newValue) {
            range = newValue;
            txtSeekbarProgress.setText(String.valueOf(range));
            handler.removeCallbacks(runnable);
            handler.postDelayed(runnable, 1000);

        }
    };

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            setResultImageStep2();
        }
    };

    Handler handler = new Handler();


    /**
     * Show dialog for Select Portion and Graph
     */
    /*public void showDialog() {
        final Dialog dialog = new Dialog(MainActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_image_detail);
        dialog.setCancelable(true);

        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT);

        final GraphView graph = dialog.findViewById(R.id.graph);
        final CropImageView cropImageView = dialog.findViewById(R.id.cropImageView);
        Button btnGetGraph = dialog.findViewById(R.id.btnGetGraph);
        btnGetGraph.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Single<Bitmap> croped = cropImageView.cropAsSingle();

                        if (croped != null) {
                            Bitmap portionBitmap = croped.blockingGet();
                            if (portionBitmap != null) {

                                int[] arrays = new int[256];
                                for (int i = 0; i < arrays.length; i++) {
                                    arrays[i] = 0;
                                }

                                int[] pixels = new int[portionBitmap.getWidth() * portionBitmap.getHeight()];
                                portionBitmap.getPixels(pixels, 0, portionBitmap.getWidth(), 0, 0, portionBitmap.getWidth(), portionBitmap.getHeight());
                                for (int i = 0; i < pixels.length; i++) {
                                    int blue = Color.blue(pixels[i]);
                                    arrays[blue] = arrays[blue] + 1;
                                }
                                double maxY = 0;
                                DataPoint[] points = new DataPoint[256];
                                for (int i = 0; i < points.length; i++) {
                                    points[i] = new DataPoint(i, arrays[i]);
                                    if (arrays[i] > maxY)
                                        maxY = arrays[i];


                                }
                                LineGraphSeries<DataPoint> series = new LineGraphSeries(points);

                                series.setAnimated(true);

                                graph.getViewport().setXAxisBoundsManual(true);
                                graph.getViewport().setMinX(0);
                                graph.getViewport().setMaxX(255);

                                graph.getViewport().setYAxisBoundsManual(true);
                                graph.getViewport().setMinY(0);
                                graph.getViewport().setMaxY(maxY);
                                graph.getGridLabelRenderer().setGridColor(Color.LTGRAY);

                                graph.setVisibility(View.VISIBLE);
                                graph.removeAllSeries();
                                graph.addSeries(series);

                            }
                        }
                    }
                }, 300);


            }
        });
        Bitmap bm = ((BitmapDrawable) imgResult.getDrawable()).getBitmap();
        cropImageView.setImageBitmap(bm);

        dialog.show();
    }*/
    private void checkGalleryPermission() {

        if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {


                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_GALLERY);


            } else {
                openGallery();
            }
        } else {
            openGallery();
        }
    }


    private void checkWritePermission() {

        if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        200);

            } else {
                saveImage();
            }
        } else {
            saveImage();
        }
    }

    private void saveImage() {
        Bitmap bm = ((BitmapDrawable) imgResult.getDrawable()).getBitmap();
        new SaveImageAsync(this, bm).execute();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_GALLERY:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openGallery();
                }
                break;
        }
    }


    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);//
        startActivityForResult(Intent.createChooser(intent, "Select Image"), SELECT_FILE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_FILE)
                onSelectFromGalleryResult(data);
        }
    }

    private void onSelectFromGalleryResult(Intent data) {

        Bitmap bitmap = null;
        if (data != null) {
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        if (currentView == 1)
            img1.setImageBitmap(bitmap);

        else if (currentView == 2)
            img2.setImageBitmap(bitmap);

        updateImageResult();

    }

}
