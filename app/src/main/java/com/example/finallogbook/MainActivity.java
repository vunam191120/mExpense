package com.example.finallogbook;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.ImageCapture;
import androidx.core.app.ActivityCompat;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.common.util.concurrent.ListenableFuture;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    ImageCapture imageCapture;
    Executor executor = Executors.newSingleThreadExecutor();
    final int REQUEST_CODE_PERMISSIONS = 1001;
    final String[] REQUIRED_PERMISSIONS = new String[]{
            "android.permission.CAMERA",
            "android.permission.RECORD_AUDIO"
    };
    ImageView imageView;
    PreviewView previewView;
    EditText inputPictureUri;
    Button btn;
    DatabaseHandler database;
    List<Upload> arrlist;

    static Integer currentNum = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        database = new DatabaseHandler(getApplicationContext());

        arrlist = database.getImages();

        Button prev = findViewById(R.id.prev);
        Button next = findViewById(R.id.next);
        imageView = findViewById(R.id.imageView);
        btn = findViewById(R.id.buttonAction);
        EditText inputPictureUri = findViewById(R.id.textImage);

        arrlist.add( new Upload("https://cdn-fastly.petguide.com/media/2022/02/28/8263745/samoyed.jpg?size=720x845&nocrop=1") );
        arrlist.add( new Upload("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQxyYbyPjyXRNxtes9LRfpNHRerDHDaddEkmw&usqp=CAU") );

        if (arrlist.size() > 0) {
            Picasso.with(this)
                    .load(arrlist.get(currentNum).getUrl())
                    .into(imageView);
            File imgFile = new File(arrlist.get(currentNum).getUrl());

            if (imgFile.exists()) {
                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                imageView.setImageBitmap(myBitmap);
            }
        }

        prev.setOnClickListener(view -> {
            if (currentNum - 1 >= 0) {
                currentNum = currentNum - 1;
            }

            Toast.makeText(this, "Current Num " + currentNum, Toast.LENGTH_SHORT).show();
                Picasso.with(this)
                        .load(arrlist.get(currentNum).getUrl())
                        .into(imageView);
            File imgFile1 = new File(arrlist.get(currentNum).getUrl());

            if (imgFile1.exists()) {
                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile1.getAbsolutePath());
                imageView.setImageBitmap(myBitmap);
            }
        });

        next.setOnClickListener(view -> {
            Integer count = currentNum + 1;
            if (count + 1 <= arrlist.size()) {
                currentNum = currentNum + 1;
            }
            Toast.makeText(this, "Currrent Num " + currentNum, Toast.LENGTH_SHORT).show();

            if (currentNum < arrlist.size()) {
                Picasso.with(this)
                        .load(arrlist.get(currentNum).getUrl())
                        .into(imageView);
                File imgFile2 = new File(arrlist.get(currentNum).getUrl());

                if (imgFile2.exists()) {
                    Bitmap myBitmap = BitmapFactory.decodeFile(imgFile2.getAbsolutePath());
                    imageView.setImageBitmap(myBitmap);
                }
            }
        });

        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }
        startCamera();

        previewView = findViewById(R.id.previewView);
        btn.setOnClickListener(view -> {
            final String[] items = {"Take a photo", "Choose picture from gallery", "View picture from an URI"};
            AlertDialog.Builder dlg = new AlertDialog.Builder(this);
            dlg.setItems(items, (diaglog, item) -> {
                if(items[item].equals("Take a photo")) {
                    Toast.makeText(this, "Take a photo", Toast.LENGTH_LONG);
                    takePhoto();
                } else if (items[item].equals("Choose picture from gallery")) {
                    Toast.makeText(this, "Choose picture from gallery", Toast.LENGTH_LONG);
                    selectPictureFromGallery();

                } else if (items[item].equals("View picture from an URI")) {
                    Toast.makeText(this, "View picture from an URI", Toast.LENGTH_LONG);
                    String uri = inputPictureUri.getText().toString();

                    if (isValidURL(uri)) {
                        database.insertImage(uri);
                        arrlist.add(new Upload(uri));
                        inputPictureUri.getText().clear();
                        diaglog.dismiss();
                    } else {
                        Toast.makeText(this, "URI is not valid", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            dlg.show();
        });
    }

    private void selectPictureFromGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(intent.ACTION_GET_CONTENT);
        launchActivity.launch(intent);
    }
    ActivityResultLauncher<Intent> launchActivity = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    Uri selectedImage = data.getData();
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImage);
                        imageView.setImageBitmap(bitmap);
                        long timestamp = System.currentTimeMillis();
                        File file = bitmapToFile(bitmap, getApplicationContext().getFilesDir().getAbsolutePath() + File.pathSeparator + String.valueOf(timestamp));
                        inputPictureUri.setText(file.getAbsolutePath());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
    );

    private File bitmapToFile(Bitmap bitmap, String filepath) {
        File file = null;
        try {
            file = new File(filepath);
            file.createNewFile();

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 0,bos);
            byte[] bitmapdata = bos.toByteArray();

            FileOutputStream fos = new FileOutputStream(file);
            fos.write(bitmapdata);
            fos.flush();
            fos.close();
            return file;
        } catch (Exception e) {
            e.printStackTrace();
            return file;
        }
    }


    private void takePhoto() {
        long timestamp = System.currentTimeMillis();
        File savedFile = new File(getApplicationContext().getFilesDir(), String.valueOf(timestamp));
        ImageCapture.OutputFileOptions option2 = new ImageCapture.OutputFileOptions.Builder(
                savedFile
        ).build();
        imageCapture.takePicture(option2, executor,
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        Uri selectingImage = outputFileResults.getSavedUri();
                        runOnUiThread(() -> {
                            //                                imageView.setImageBitmap(MediaStore.Images.Media.getBitmap(getContentResolver(), selectingImage));
                            database.insertImage(selectingImage.getPath());
                            arrlist.add(new Upload(selectingImage.getPath()));
                        });
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {

                    }
                });
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderListenableFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderListenableFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderListenableFuture.get();
                CameraSelector cameraSelector =  CameraSelector.DEFAULT_BACK_CAMERA;
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder().build();
                try {
                    cameraProvider.unbindAll();
                    cameraProvider.bindToLifecycle(
                            this, cameraSelector, preview, imageCapture);
                } catch (Exception ex) {
                    Log.e("Test", "Usse casese ");
                }
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    boolean allPermissionsGranted()
    {
        for (String permission: REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;

    }

    public boolean isValidURL(String url) {
        try {
            new URL(url).toURI();
        } catch (MalformedURLException e) {
            return false;
        } catch (URISyntaxException e) {
            return false;
        }
        return true;
    }
}