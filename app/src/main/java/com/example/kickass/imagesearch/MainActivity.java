package com.example.kickass.imagesearch;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Main Activity";

    static {
        if (OpenCVLoader.initDebug()) {
            Log.d(TAG, "OpenCV successfully loaded");
        } else {
            Log.d(TAG, "OpenCV is not loaded");
        }
    }

    private String userChoosenTask;
    private String userChoosenImage;
    static final int REQUEST_CAMERA = 1;
    static final int SELECT_FILE = 2;
    static final String template = "template";
    static final String actual = "actual";
    private Button btnSelect;
    private ImageView tempImage;
    private ImageView tempImageOut;
    private ImageView tempImageIn;
    private ImageView actualImageIn;
    private Button btnSearch;

    Context context = this;

    Mat templateMat = null;
    Mat actualMat = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnSelect = (Button) findViewById(R.id.btnSelectPhoto);
        tempImageIn = (ImageView) findViewById(R.id.tempImageIn);
        actualImageIn = (ImageView) findViewById(R.id.actualImageIn);
        tempImage = (ImageView) findViewById(R.id.tempImage);
        tempImage.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                selectImage(template);
            }
        });
        btnSelect.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                selectImage(actual);
            }
        });
        tempImageOut = (ImageView) findViewById(R.id.tempImageOut);
        btnSearch = (Button) findViewById(R.id.btnSearch);
        btnSearch.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                searchImage();
            }
        });

    }

    private void selectImage(String imagetype) {
        userChoosenImage = imagetype;
        final CharSequence[] items = {"Take Photo", "Choose from Library", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                boolean result = Utility.checkPermission(MainActivity.this);
                if (items[item].equals("Take Photo")) {
                    userChoosenTask = "Take Photo";
                    if (result) cameraIntent();
                } else if (items[item].equals("Choose from Library")) {
                    userChoosenTask = "Choose from Library";
                    if (result) galleryIntent();
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    private void cameraIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    private void galleryIntent()
    {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);//
        startActivityForResult(Intent.createChooser(intent, "Select File"),SELECT_FILE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case Utility.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(userChoosenTask.equals("Take Photo"))
                        cameraIntent();
                    else if(userChoosenTask.equals("Choose from Library"))
                        galleryIntent();
                } else {
                    //code for deny
                }
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_FILE)
                onSelectFromGalleryResult(data);
            else if (requestCode == REQUEST_CAMERA)
                onCaptureImageResult(data);
        }
    }

    @SuppressWarnings("deprecation")
    private void onSelectFromGalleryResult(Intent data) {
        Bitmap bm=null;
        if (data != null) {
            try {
                bm = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(userChoosenImage.equals(template)){
            tempImageIn.setImageBitmap(bm);
            templateMat = new Mat (bm.getHeight(), bm.getWidth(), CvType.CV_8UC4);
            Utils.bitmapToMat(bm, templateMat);
            System.out.println("\n template: "+templateMat.cols()+"rows: "+templateMat.rows());
        } else {
            actualImageIn.setImageBitmap(bm);
            actualMat = new Mat (bm.getHeight(), bm.getWidth(), CvType.CV_8UC4);
            Utils.bitmapToMat(bm, actualMat);
            System.out.println("\n actual: "+actualMat.cols()+"rows: "+actualMat.rows());
        }

    }

    private void onCaptureImageResult(Intent data) {
        Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
        //ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        //thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
        /*File destination = new File(Environment.getExternalStorageDirectory(),
                System.currentTimeMillis() + ".jpg");
        FileOutputStream fo;
        try {
            destination.createNewFile();
            fo = new FileOutputStream(destination);
            fo.write(bytes.toByteArray());
            fo.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        if(userChoosenImage.equals(template)){
            tempImageIn.setImageBitmap(thumbnail);
            templateMat = new Mat (thumbnail.getHeight(), thumbnail.getWidth(), CvType.CV_8UC4);
            Utils.bitmapToMat(thumbnail, templateMat);
        } else {
            actualImageIn.setImageBitmap(thumbnail);
            actualMat = new Mat (thumbnail.getHeight(), thumbnail.getWidth(), CvType.CV_8UC4);
            Utils.bitmapToMat(thumbnail, actualMat);
        }

    }

    private void searchImage(){
        TemplateMatch tem = new TemplateMatch();
        Mat img = tem.run(templateMat, actualMat, Imgproc.TM_CCOEFF);
        Bitmap bm = Bitmap.createBitmap(img.cols(), img.rows(),Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(img, bm);
        ImageView iv = (ImageView) findViewById(R.id.tempImageOut);
        iv.setImageBitmap(bm);
    }

}
