package com.example.rathish.myapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import nl.bravobit.ffmpeg.ExecuteBinaryResponseHandler;
import nl.bravobit.ffmpeg.FFmpeg;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import android.support.v7.app.AlertDialog;
import java.sql.Timestamp;
import android.content.DialogInterface;

import android.provider.MediaStore;
import android.database.Cursor;

import android.content.ContentUris;
import android.app.ProgressDialog;

import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.VideoView;
import org.florescu.android.rangeseekbar.RangeSeekBar;
import android.os.Handler;
import java.io.File;
import android.os.Environment;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import android.support.design.widget.Snackbar;
import android.provider.DocumentsContract;




public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_TAKE_GALLERY_VIDEO = 100;
    private VideoView videoView;
    private RangeSeekBar rangeSeekBar;
    private int stopPosition;
    private Uri selectedVideoUri;
    private TextView tvLeft, tvRight;
    private int duration;
    private Runnable r;
    private int choice = 0;
    private ScrollView mainlayout;
    private static final String TAG = "BHUVNESH";
    private String filePath;
    private FFmpeg ffmpeg;
    private ProgressDialog progressDialog;
    private static final String FILEPATH = "filepath";
    private String[] lastReverseCommand;
private String appname="AppName";
int totalFiles=0;
int successFiles=0;
ArrayList<String> cutFileNames=new ArrayList<String>();
    Date dateDT;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final TextView uploadVideo = (TextView) findViewById(R.id.uploadVideo);
        TextView cutVideo = (TextView) findViewById(R.id.cropVideo);

        tvLeft = (TextView) findViewById(R.id.tvLeft);
        tvRight = (TextView) findViewById(R.id.tvRight);
        videoView = (VideoView) findViewById(R.id.videoView);
        rangeSeekBar = (RangeSeekBar) findViewById(R.id.rangeSeekBar);
        mainlayout = (ScrollView) findViewById(R.id.mainlayout);
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(null);
        progressDialog.setCancelable(false);



//        loadFFMpegBinary();
//temp*
        uploadVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= 23) {
                    Log.d("View", "Permission");
                    getPermission();
                }
                else{
                    Log.d("View", "Upload");
                    uploadVideo();
                }
            }
        });

        cutVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                choice = 2;

                if (selectedVideoUri != null) {
//                    executeCutVideoCommand(rangeSeekBar.getSelectedMinValue().intValue() * 1000, rangeSeekBar.getSelectedMaxValue().intValue() * 1000);
                    dateDT= new Date();
                    cutVideoInLoop();
                } else
                    Snackbar.make(mainlayout, "Please upload a video", 4000).show();
            }
        });



    }

    public void cutVideoInLoop(){
        int totalDuration=videoView.getDuration();
        int start=0;
        int end=15000;
        int no=1;
        while(end<totalDuration){
            Log.d("Start:",String.valueOf(start));
            Log.d("End:",String.valueOf(end));
            executeCutVideoCommand(start,end,no++);
            start+=15000;
            end+=15000;
        }
        Log.d("Start:",String.valueOf(start));
        Log.d("End:",String.valueOf(totalDuration));
        totalFiles=no;
        Log.d("totalFiles", String.valueOf(totalFiles));
        executeCutVideoCommand(start,end,no++);

    }




    private void getPermission() {
        Log.d("View", "IPermission");
        String[] params = null;
        String writeExternalStorage = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        String readExternalStorage = Manifest.permission.READ_EXTERNAL_STORAGE;

        int hasWriteExternalStoragePermission = ActivityCompat.checkSelfPermission(this, writeExternalStorage);
        int hasReadExternalStoragePermission = ActivityCompat.checkSelfPermission(this, readExternalStorage);
        List<String> permissions = new ArrayList<String>();

        if (hasWriteExternalStoragePermission != PackageManager.PERMISSION_GRANTED)
            permissions.add(writeExternalStorage);
        if (hasReadExternalStoragePermission != PackageManager.PERMISSION_GRANTED)
            permissions.add(readExternalStorage);

        if (!permissions.isEmpty()) {
            params = permissions.toArray(new String[permissions.size()]);
        }
        if (params != null && params.length > 0) {

            Log.d("View", "OPermission");
            ActivityCompat.requestPermissions(MainActivity.this,
                    params,
                    100);
        } else
            uploadVideo();
    }


    /**
     * Handling response for permission request
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 100: {

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    uploadVideo();
                }
            }
            break;
//            case 200: {
//
//                if (grantResults.length > 0
//                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    extractAudioVideo();
//                }
//            }


        }
    }



    private void uploadVideo() {
        try {
            Intent intent = new Intent();
            intent.setType("video/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Video"), REQUEST_TAKE_GALLERY_VIDEO);
        } catch (Exception e) {

        }
    }



    @Override
    protected void onPause() {
        super.onPause();
        stopPosition = videoView.getCurrentPosition(); //stopPosition is an int
        videoView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        videoView.seekTo(stopPosition);
        videoView.start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_TAKE_GALLERY_VIDEO) {
                Log.d("Main", "onActivityResult: data.getData()");
                selectedVideoUri = data.getData();
                videoView.setVideoURI(selectedVideoUri);
                videoView.start();


                videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        // TODO Auto-generated method stub
                        duration = mp.getDuration() / 1000;
                        tvLeft.setText("00:00:00");

                        tvRight.setText(getTime(mp.getDuration() / 1000));
                        mp.setLooping(true);
                        rangeSeekBar.setRangeValues(0, duration);
                        rangeSeekBar.setSelectedMinValue(0);
                        rangeSeekBar.setSelectedMaxValue(duration);
                        rangeSeekBar.setEnabled(true);

                        rangeSeekBar.setOnRangeSeekBarChangeListener(new RangeSeekBar.OnRangeSeekBarChangeListener() {
                            @Override
                            public void onRangeSeekBarValuesChanged(RangeSeekBar bar, Object minValue, Object maxValue) {
                                videoView.seekTo((int) minValue * 1000);

                                tvLeft.setText(getTime((int) bar.getSelectedMinValue()));

                                tvRight.setText(getTime((int) bar.getSelectedMaxValue()));

                            }
                        });

                        final Handler handler = new Handler();
                        handler.postDelayed(r = new Runnable() {
                            @Override
                            public void run() {

                                if (videoView.getCurrentPosition() >= rangeSeekBar.getSelectedMaxValue().intValue() * 1000)
                                    videoView.seekTo(rangeSeekBar.getSelectedMinValue().intValue() * 1000);
                                handler.postDelayed(r, 1000);
                            }
                        }, 1000);

                    }
                });

//                }
            }
        }
    }

    private String getTime(int seconds) {
        int hr = seconds / 3600;
        int rem = seconds % 3600;
        int mn = rem / 60;
        int sec = rem % 60;
        return String.format("%02d", hr) + ":" + String.format("%02d", mn) + ":" + String.format("%02d", sec);
    }



    /**
     * Command for cutting video
     */
    private void executeCutVideoCommand(int startMs, int endMs,int prefix) {
        File moviesDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_MOVIES
        );

        String filePrefix = "cut_video"+prefix;
        String fileExtn = ".mp4";
        String yourRealPath = getPath(MainActivity.this, selectedVideoUri);
        File dest = new File(moviesDir, filePrefix + fileExtn);
        int fileNo = 0;
        while (dest.exists()) {
            fileNo++;
            dest = new File(moviesDir, filePrefix + fileNo + fileExtn);
        }

        File file=new File(Environment.getExternalStorageDirectory()+"/"+appname);
        if(!file.exists()){
            file.mkdir();
        }

        //Date object
        //getTime() returns current time in milliseconds
        long time = dateDT.getTime();
        Log.d("DATETIME123",String.valueOf(time));
        //Passed the milliseconds to constructor of Timestamp class
        Timestamp ts = new Timestamp(time);
        Log.d("Current Time Stamp: ",ts.toString());

        String[] dateArr=ts.toString().split(" ");
        dateArr[1].replaceAll(".","a");
        //String date=ts.toString().replace(" ","d");
//            File dir=new File(file.getPath()+"/"+dateArr[0]);
//            if(!dir.exists()){
//                dir.mkdir();
//            }
//            int i=1;
//        File dir1=new File(file.getPath()+"/"+dateArr[0]+"/"+String.valueOf(i));
//        while(!dir1.exists()){
//         i++;
//         dir1=new File(file.getPath()+"/"+dateArr[0]+"/"+String.valueOf(i));
//        }
//        dir1.mkdir();
        File dir1=new File(file.getPath()+"/"+String.valueOf(time));
        if(!dir1.exists()){
            dir1.mkdir();
        }

        filePath=file.getPath()+"/"+String.valueOf(time)+"/"+filePrefix + fileExtn;
//            File dir=new File(file.getPath()+date);




        Log.d(TAG, "startTrim: src: " + yourRealPath);
        Log.d(TAG, "startTrim: dest: " + dest.getAbsolutePath());
        Log.d(TAG, "startTrim: startMs: " + startMs);
        Log.d(TAG, "startTrim: endMs: " + endMs);
//        filePath = dest.getAbsolutePath();

        //String[] complexCommand = {"-i", yourRealPath, "-ss", "" + startMs / 1000, "-t", "" + endMs / 1000, dest.getAbsolutePath()};
        cutFileNames.add(filePath);
        Log.d( "filepath123",filePath);
        String[] complexCommand = {"-ss", "" + startMs / 1000, "-y", "-i", yourRealPath, "-t", "" + (endMs - startMs) / 1000,"-vcodec", "mpeg4", "-b:v", "2097152", "-b:a", "48000", "-ac", "2", "-ar", "22050", filePath};
        Log.d("AAAAAAAAAAAAAAAAAAAAAa", complexCommand[0]);
        execFFmpegBinary(complexCommand);

    }

    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     */
    private String getPath(final MainActivity context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Executing ffmpeg binary
     */
    private void execFFmpegBinary(final String[] command) {
        try {
            if (FFmpeg.getInstance(this).isSupported()) {
                // ffmpeg is supported
                Log.d(TAG,"Supported");
                //ffmpegTestTaskQuit();
            }
            else{

                Log.d(TAG,"Not Supported");
            }
            FFmpeg.getInstance(this).execute(command, new ExecuteBinaryResponseHandler() {

                @Override

                public void onFailure(String s) {
                    Log.d(TAG, "FAILED with output : " + s);
                }

                @Override
                public void onSuccess(String s) {
                    successFiles++;
                    Log.d(TAG, "SUCCESS with output : " + s);
                    Log.d("successFiles", String.valueOf(successFiles));
                    if (totalFiles==successFiles) {
                        successFiles=0;
                        Intent intent = new Intent(MainActivity.this, PreviewActivity.class);
                        Bundle args = new Bundle();
                        args.putSerializable("ARRAYLIST",(Serializable)cutFileNames);
                        intent.putExtra(FILEPATH, args);
                        startActivity(intent);
                    } else if (choice == 8) {
                        choice = 9;
                        reverseVideoCommand();
                    } else if (Arrays.equals(command, lastReverseCommand)) {
                        choice = 10;
                        Log.d(TAG, "onSuccess() returned: " + "FAILURE..................................................");
//                        concatVideoCommand();
                    } else if (choice == 10) {
                        File moviesDir = Environment.getExternalStoragePublicDirectory(
                                Environment.DIRECTORY_MOVIES
                        );
                        File destDir = new File(moviesDir, ".VideoPartsReverse");
                        File dir = new File(moviesDir, ".VideoSplit");
                        if (dir.exists())
                            deleteDir(dir);
                        if (destDir.exists())
                            deleteDir(destDir);
                        choice = 11;
                        Intent intent = new Intent(MainActivity.this, PreviewActivity.class);
                        intent.putExtra(FILEPATH, filePath);
                        startActivity(intent);
                    }
                }

                @Override
                public void onProgress(String s) {
                    Log.d(TAG, "Started command : ffmpeg " + command);
                    if (choice == 8)
                        progressDialog.setMessage("progress : splitting video " + s);
                    else if (choice == 9)
                        progressDialog.setMessage("progress : reversing splitted videos " + s);
                    else if (choice == 10)
                        progressDialog.setMessage("progress : concatenating reversed videos " + s);
                    else
                        progressDialog.setMessage("progress : " + s);
                    Log.d(TAG, "progress : " + s);
                }

                @Override
                public void onStart() {
                    Log.d(TAG, "Started command : ffmpeg " + command);
                    progressDialog.setMessage("Processing...");
                    progressDialog.show();
                }

                @Override
                public void onFinish() {
                    Log.d(TAG, "Finished command : ffmpeg " + command);
                    if (choice != 8 && choice != 9 && choice != 10) {
                        progressDialog.dismiss();
                    }

                }
            });
        } catch (Exception e) {
            // do nothing for now
        }
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    private boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }


    private boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    private boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }



    /**
     * Get the value of the data column for this Uri.
     */
    private String getDataColumn(MainActivity context, Uri uri, String selection,
                                 String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * Command for reversing segmented videos
     */
    private void reverseVideoCommand() {
        File moviesDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_MOVIES
        );
        File srcDir = new File(moviesDir, ".VideoSplit");
        File[] files = srcDir.listFiles();
        String filePrefix = "reverse_video";
        String fileExtn = ".mp4";
        File destDir = new File(moviesDir, ".VideoPartsReverse");
        if (destDir.exists())
            deleteDir(destDir);
        destDir.mkdir();
        for (int i = 0; i < files.length; i++) {
            File dest = new File(destDir, filePrefix + i + fileExtn);
            String command[] = {"-i", files[i].getAbsolutePath(), "-vf", "reverse", "-af", "areverse", dest.getAbsolutePath()};
            if (i == files.length - 1)
                lastReverseCommand = command;
//            execFFmpegBinary(command);
        }


    }


    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            if (children != null) {
                for (int i = 0; i < children.length; i++) {
                    boolean success = deleteDir(new File(dir, children[i]));
                    if (!success) {
                        return false;
                    }
                }
            }
        }
        return dir.delete();
    }

//    private void loadFFMpegBinary() {
//        try {
//            if (ffmpeg == null) {
//                Log.d(TAG, "ffmpeg : era nulo");
//                ffmpeg = FFmpeg.getInstance(this);
//            }
//            ffmpeg.loadBinary(new LoadBinaryResponseHandler() {
//                @Override
//                public void onFailure() {
//                    showUnsupportedExceptionDialog();
//                }
//
//                @Override
//                public void onSuccess() {
//                    Log.d(TAG, "ffmpeg : correct Loaded");
//                }
//            });
//        } catch (FFmpegNotSupportedException e) {
//            showUnsupportedExceptionDialog();
//        } catch (Exception e) {
//            Log.d(TAG, "EXception no controlada : " + e);
//        }
//    }

    private void showUnsupportedExceptionDialog() {
        new AlertDialog.Builder(MainActivity.this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Not Supported")
                .setMessage("Device Not Supported")
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MainActivity.this.finish();
                    }
                })
                .create()
                .show();

    }




}



