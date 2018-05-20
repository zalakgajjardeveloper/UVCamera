package com.app.UVCamera;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.edmodo.cropper.CropImageView;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.File;

public class ImageDetailActivity extends AppCompatActivity {


    private boolean showGraph = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.dialog_image_detail);
        if (getSupportActionBar() != null)
            getSupportActionBar().hide();
        final CropImageView cropImageView = findViewById(R.id.cropImageView);

        String filePath = getIntent().getStringExtra("path");

        File file = new File(filePath);
        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
        cropImageView.setImageBitmap(bitmap);

        final GraphView graph = findViewById(R.id.graph);

        final Button btnGetGraph = findViewById(R.id.btnGetGraph);
        btnGetGraph.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (showGraph) {
                            showGraph = false;
                            graph.setVisibility(View.GONE);
                            cropImageView.setVisibility(View.VISIBLE);
                            btnGetGraph.setText(R.string.show_chart);
                        } else {
                            Bitmap portionBitmap = cropImageView.getCroppedImage();
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
                                cropImageView.setVisibility(View.GONE);
                                showGraph = true;
                                btnGetGraph.setText(R.string.back);
                                graph.removeAllSeries();
                                graph.addSeries(series);

                            }

                        }
                    }
                }, 300);


            }
        });


    }


}
