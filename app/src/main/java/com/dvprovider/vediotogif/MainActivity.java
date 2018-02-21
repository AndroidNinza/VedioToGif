package com.dvprovider.vediotogif;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.bumptech.glide.Glide;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getName();

    Activity activity;

    Button btn_convert;
    ImageView iv_gif_loader;
    VideoView mVideoView;

    ProgressDialog pd;
    private int REQUEST_FOR_PERMISSION = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        activity = MainActivity.this;

        btn_convert = (Button) findViewById(R.id.btn_convert);
        iv_gif_loader = (ImageView) findViewById(R.id.iv_gif_loader);
        mVideoView  = (VideoView)findViewById(R.id.videoView);

        btn_convert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkPermission();
            }
        });

        loadVideo();
    }

    private void loadVideo()
    {
        try {
            String uri = "android.resource://" + getPackageName() + "/" + R.raw.countdown;
            getWindow().setFormat(PixelFormat.TRANSLUCENT);
            mVideoView.setMediaController(new MediaController(this));
            Uri video = Uri.parse(uri);
            mVideoView.setVideoURI(video);
            mVideoView.start();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private File startConvertProcess() {
        File gitFile = null;
        try {

            ArrayList<Bitmap> frames = GenerateGif(activity);
            if (frames != null) {
                Log.e(TAG, "frames are available");
                byte[] gif = generateGIF(frames);
                if (gif != null)
                {
                    gitFile = saveGIF(gif);
                    Log.e(TAG, "gif is available");
                }else{
                    Log.e(TAG, "gif is not available");
                }
            } else {
                Log.e(TAG, "frames are not available");
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return gitFile;
    }

    /*private void loadGif(byte[] gif) {

        Bitmap bmp = BitmapFactory.decodeByteArray(gif, 0, gif.length);
        iv_gif_loader.setImageBitmap(Bitmap.createScaledBitmap(bmp, iv_gif_loader.getWidth(),
                iv_gif_loader.getHeight(), false));
    }*/

    private File saveGIF(byte[] gif)
    {
        File gifFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "shared_gif_shai" + System.currentTimeMillis() + ".gif");
        try
        {
            Log.d(TAG, "on do in background, url open connection");
            InputStream in = new ByteArrayInputStream(gif);
            Log.d(TAG, "on do in background, url get input stream");
            BufferedInputStream bis = new BufferedInputStream(in);
            Log.d(TAG, "on do in background, create buffered input stream");

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Log.d(TAG, "on do in background, create buffered array output stream");

            byte[] img = new byte[1024];

            int current = 0;

            Log.d(TAG, "on do in background, write byte to baos");
            while ((current = bis.read()) != -1) {
                baos.write(current);
            }


            Log.d(TAG, "on do in background, done write");

            Log.d(TAG, "on do in background, create fos");
            FileOutputStream fos = new FileOutputStream(gifFile);
            fos.write(baos.toByteArray());

            Log.d(TAG, "on do in background, write to fos");
            fos.flush();

            fos.close();
            in.close();
            Log.d(TAG, "on do in background, done write to fos");

            if (gifFile.exists()) {

                Log.e(TAG, "GIF saved successfully");
            }else {
                Log.e(TAG, "Something went's wrong when saved GIF ");
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return gifFile;
    }

    private void showDialog()
    {

        new AsyncTask<Void, Void, Void>(){

            File file = null;
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                pd = new ProgressDialog(MainActivity.this);
                pd.setMessage("Please wait a while we are processing...");
                pd.show();
            }

            @Override
            protected Void doInBackground(Void... voids) {
                file = startConvertProcess();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                dismissDialog();
                if (file != null)
                {
                    Glide.with(activity)
                            .load(file)
                            .into(iv_gif_loader);
                }else{
                    Toast.makeText(activity, "Oppes, File not converted successfully, please try again",
                            Toast.LENGTH_LONG).show();
                }
            }
        }.execute();

    }
    private void dismissDialog(){
        if (pd != null){
            pd.dismiss();
            pd.cancel();
            pd = null;
        }
    }

    /**
     * This method is used for get the all frame from video file
     * @param context
     * @return
     */
    public static ArrayList<Bitmap> GenerateGif(Context context) {
        ArrayList<Bitmap> frames = new ArrayList<Bitmap>();

        try {
            final AssetFileDescriptor afd =  context.getResources().openRawResourceFd(R.raw.countdown);

            // MediaMetadataRetriever instance
            MediaMetadataRetriever mmRetriever = new MediaMetadataRetriever();
            mmRetriever.setDataSource(afd.getFileDescriptor(),afd.getStartOffset(),afd.getLength());
            for (int i = 0; i < 5; i++) {

                Bitmap bArray = mmRetriever.getFrameAtTime(
                        1000000 * i,
                        MediaMetadataRetriever.OPTION_CLOSEST);
                frames.add(bArray);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return frames;
    }

    /**
     * This method is used for generate the byte[] of Bitmap array
     * @param bitmaps
     * @return
     */
    public byte[] generateGIF(ArrayList<Bitmap> bitmaps) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        AnimatedGifEncoder encoder = new AnimatedGifEncoder();
        encoder.start(bos);
        for (Bitmap bitmap : bitmaps) {
            encoder.addFrame(bitmap);
        }
        encoder.finish();
        return bos.toByteArray();
    }

    /**
     * This method is used for overlay the image on GIF.
     * ie.if user want to add log on newly created GIF
     * @param bmp1
     * @param bmp2
     * @return
     */
    private Bitmap overlay(Bitmap bmp1, Bitmap bmp2) {
        Bitmap bmOverlay = Bitmap.createBitmap(bmp1.getWidth(), bmp1.getHeight(), bmp1.getConfig());
        Canvas canvas = new Canvas(bmOverlay);
        canvas.drawBitmap(bmp1, new Matrix(), null);
        canvas.drawBitmap(bmp2, new Matrix(), null);
        return bmOverlay;
    }

    private void checkPermission()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE)
                    + ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_FOR_PERMISSION);
            }else{
                showDialog();
            }
        }else{
            showDialog();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_FOR_PERMISSION)
        {
            if (permissions.length >0 && grantResults[0]+grantResults[1]
                    ==PackageManager.PERMISSION_GRANTED){
                showDialog();
            }else{
                Toast.makeText(activity, "Both permission are required for converting process", Toast.LENGTH_LONG).show();
            }
        }
    }
}
