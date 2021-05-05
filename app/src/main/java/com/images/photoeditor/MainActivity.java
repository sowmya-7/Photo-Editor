package com.images.photoeditor;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;


public class MainActivity extends Activity {

    ImageView displayImage;
    Button camera,compress,original;
    private static final int CAMERA_REQUEST = 100;
    private static final int STORAGE_REQUEST = 200;
    String cameraPermission[];
    String storagePermission[];
    Uri resultUri;
    Bitmap originalImage;
    int width;
    int height;
    int newWidth = 200;
    int newHeight = 200;
    Matrix matrix;
    Bitmap resizedBitmap;
    float scaleWidth ;
    float scaleHeight;
    ByteArrayOutputStream outputStream;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        displayImage = (ImageView) findViewById(R.id.image);
        camera = (Button) findViewById(R.id.camera);
        compress = (Button) findViewById(R.id.compress);
        original = (Button) findViewById(R.id.original);

        compress.setVisibility(View.GONE);
        original.setVisibility(View.GONE);


        cameraPermission = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImagePicDialog();
            }
        });


        compress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    originalImage = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), resultUri);
                    width = originalImage.getWidth();
                    Log.i("Old width", width + "");
                    height = originalImage.getHeight();
                    Log.i("Old height", height + "");

                    matrix = new Matrix();
                    scaleWidth = ((float) newWidth) / width;
                    scaleHeight = ((float) newHeight) / height;
                    matrix.postScale(scaleWidth, scaleHeight);

                    resizedBitmap = Bitmap.createBitmap(originalImage, 0, 0, width, height, matrix, true);
                    outputStream = new ByteArrayOutputStream();
                    resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                    displayImage=(ImageView)findViewById(R.id.image);
                    displayImage.setImageBitmap(resizedBitmap);
                    width = resizedBitmap.getWidth();
                    Log.i("new width", width + "");
                    height = resizedBitmap.getHeight();
                    Log.i("new height", height + "");
                } catch (IOException e) {
                    e.printStackTrace();
                }



            }
        });

        original.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Picasso.with(getApplicationContext()).load(resultUri).into(displayImage);
            }
        });


    }

   private void showImagePicDialog() {
       String options[] = {"Camera", "Gallery"};
       AlertDialog.Builder builder = new AlertDialog.Builder(this);
       builder.setTitle("Pick Image From");
       builder.setItems(options, new DialogInterface.OnClickListener() {
           @Override
           public void onClick(DialogInterface dialog, int which) {
               if (which == 0) {
                   if (!checkCameraPermission()) {
                       requestCameraPermission();
                   } else {
                       pickFromGallery();
                   }
               } else if (which == 1) {
                   if (!checkStoragePermission()) {
                       requestStoragePermission();
                   } else {
                       pickFromGallery();
                   }
               }
           }
       });
       builder.create().show();
   }


    private Boolean checkStoragePermission() {
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestStoragePermission() {
        requestPermissions(storagePermission, STORAGE_REQUEST);
    }


    private Boolean checkCameraPermission() {
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }


    private void requestCameraPermission() {
        requestPermissions(cameraPermission, CAMERA_REQUEST);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case CAMERA_REQUEST: {
                if (grantResults.length > 0) {
                    boolean camera_accepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageaccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (camera_accepted && writeStorageaccepted) {
                        pickFromGallery();
                    } else {
                        Toast.makeText(this, "Please Enable Camera and Storage Permissions", Toast.LENGTH_LONG).show();
                    }
                }
            }
            break;
            case STORAGE_REQUEST: {
                if (grantResults.length > 0) {
                    boolean writeStorageaccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (writeStorageaccepted) {
                        pickFromGallery();
                    } else {
                        Toast.makeText(this, "Please Enable Storage Permissions", Toast.LENGTH_LONG).show();
                    }
                }
            }
            break;
        }
    }


    private void pickFromGallery() {
        CropImage.activity().start(MainActivity.this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                resultUri = result.getUri();
                Picasso.with(this).load(resultUri).into(displayImage);

                compress.setVisibility(View.VISIBLE);
                original.setVisibility(View.VISIBLE);

            }
        }
    }

   }