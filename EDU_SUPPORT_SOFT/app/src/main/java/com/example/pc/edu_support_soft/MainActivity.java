package com.example.pc.edu_support_soft;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.xw.repo.BubbleSeekBar;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import at.markushi.ui.CircleButton;
import cn.pedant.SweetAlert.SweetAlertDialog;

public class MainActivity extends AppCompatActivity {

    CircleButton btnTakeAPicture;
    CircleButton btnRotate;
    CircleButton btnShow;
    private Bitmap bitmap;
    private ImageView imageView;
    private EditText editTextInfo;
    private DatabaseReference databaseReference;
    private StorageReference mStorageRef;
    private Uri imageUri;
    private static int PICTURE_RESULT =1;
    private int countLab = 0;
    private BubbleSeekBar bubbleSeekBar;
    private static final int REQUEST_PERMISSION = 101;
    private TextView detectedView;
    private static final String TAG = "Error";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA,
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION);
       /* getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);*/
       if(getSupportActionBar() != null){
           getSupportActionBar().hide();

       }
        imageView = (ImageView) findViewById(R.id.imageView);
        btnTakeAPicture = (CircleButton) findViewById(R.id.btnTakeAPicture);
        btnRotate = (CircleButton) findViewById(R.id.btnRotate);
        bitmap = BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher);
        btnShow = (CircleButton) findViewById(R.id.btnShow);
        editTextInfo = (EditText) findViewById(R.id.editTextInfo);
        detectedView = (TextView)findViewById(R.id.detectedView);
        databaseReference = FirebaseDatabase.getInstance().getReference();
        mStorageRef = FirebaseStorage.getInstance().getReferenceFromUrl("gs://edusupportsoft.appspot.com");
        bubbleSeekBar = (BubbleSeekBar)findViewById(R.id.bubbleSeekBar);
        bubbleSeekBar.setOnProgressChangedListener(new BubbleSeekBar.OnProgressChangedListener() {
            @Override
            public void onProgressChanged(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat) {
                configBrightness(imageView);
            }

            @Override
            public void getProgressOnActionUp(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat) {

            }

            @Override
            public void getProgressOnFinally(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat) {

            }
        });


        ///take pic
        btnTakeAPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    imageUri = null;
                    imageView.setVisibility(View.VISIBLE);
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Images.Media.TITLE, "New Picture");
                    values.put(MediaStore.Images.Media.DESCRIPTION, "From your camera");
                    imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                    startActivityForResult(intent, PICTURE_RESULT);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        btnRotate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bitmap = RotateBitmap(bitmap);
                imageView.setImageBitmap(bitmap);
            }
        });

        btnShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(MainActivity.this, SweetAlertDialog.PROGRESS_TYPE);
                sweetAlertDialog.setTitle(R.string.progress_title);
                sweetAlertDialog.setContentText(getString(R.string.progress_content));
                sweetAlertDialog.show();
                Calendar calendar = Calendar.getInstance();
                StorageReference storeRef = mStorageRef.child(calendar.getTimeInMillis()+".jpg");
                // Get the data from an ImageView as bytes
                imageView.setDrawingCacheEnabled(true);
                imageView.buildDrawingCache();
                Bitmap bitmap = imageView.getDrawingCache();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] data = baos.toByteArray();
                UploadTask uploadTask = storeRef.putBytes(data);
                updateCountLab();
                final Uri[] downloadUrl = new Uri[1];
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.

                        downloadUrl[0] = taskSnapshot.getDownloadUrl();
                        Solution solution;
                        if(downloadUrl[0] != null){
                            if(editTextInfo.getText().toString().trim().equals("")){
                                if(detectedView.getText() != null && detectedView.getText().toString().trim().equals("")){
                                    solution = new Solution(editTextInfo.getText().toString().equals("")? "Unknown":
                                            editTextInfo.getText().toString(),
                                            downloadUrl[0].toString(),0);
                                }else{
                                    solution = new Solution(editTextInfo.getText().toString().equals("")? "Unknown":
                                            editTextInfo.getText().toString(),
                                            downloadUrl[0].toString(),0);
                                }

                            }else {
                                solution = new Solution(editTextInfo.getText().toString().equals("")? editTextInfo.getText().toString().trim():
                                        editTextInfo.getText().toString(),
                                        downloadUrl[0].toString(),0);
                            }

                            databaseReference.child("Lab1").child(""+countLab).setValue(solution);
                            sweetAlertDialog.dismissWithAnimation();
                            SweetAlertDialog sweetAlertSuccess = new SweetAlertDialog(MainActivity.this, SweetAlertDialog.SUCCESS_TYPE);
                            sweetAlertSuccess.setTitle(R.string.success);
                            sweetAlertSuccess.setContentText(getString(R.string.success_content));
                            sweetAlertSuccess.show();
                            sweetAlertSuccess.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sweetAlertDialog) {
                                    finish();
                                    overridePendingTransition( 0, 0);
                                    startActivity(getIntent());
                                    overridePendingTransition( 0, 0);
                                }
                            });
                        }else{
                            final SweetAlertDialog alertShowError = new SweetAlertDialog(MainActivity.this, SweetAlertDialog.WARNING_TYPE);
                            alertShowError.setTitle(R.string.warning);
                            alertShowError.setContentText(getString(R.string.warning_content));
                            alertShowError.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sweetAlertDialog) {
                                    alertShowError.dismissWithAnimation();
                                }
                            });
                        }

                    }
                });

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if(requestCode == PICTURE_RESULT && resultCode == Activity.RESULT_OK){
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                imageView.setImageBitmap(bitmap);
               /* if (imageUri != null) {
                    inspect(imageUri);
                }*/
            }
        }catch (NullPointerException e){
            Toast.makeText(this, "You haven't take a picture yet !", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_PERMISSION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1]==PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    Log.e("Error","Permission not allow");
                } else {
                    System.exit(1);
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }

            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
    private void updateCountLab(){
        databaseReference.child("Lab1").addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    countLab = (int)dataSnapshot.getChildrenCount() +1;
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
    }
    public static Bitmap RotateBitmap(Bitmap source)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate((float) 90);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    private void configBrightness(ImageView iv){

        float brightness = (float)(bubbleSeekBar.getProgress() - 255);

        float[] colorMatrix = {
                1,0,0,0,brightness,
                0,1f,0,0,brightness,
                0,0,1f,0,brightness,
                0,0,0,1f,0
        };

        ColorFilter colorFilter = new ColorMatrixColorFilter(colorMatrix);
        iv.setColorFilter(colorFilter);
    }

    private void inspectFromBitmap(Bitmap bitmap) {
        TextRecognizer textRecognizer = new TextRecognizer.Builder(this).build();
        try {
            if (!textRecognizer.isOperational()) {
                new AlertDialog.
                        Builder(this).
                        setMessage("Text recognizer could not be set up on your device").show();
                return;
            }

            Frame frame = new Frame.Builder().setBitmap(bitmap).build();
            SparseArray<TextBlock> origTextBlocks = textRecognizer.detect(frame);
            List<TextBlock> textBlocks = new ArrayList<>();
            for (int i = 0; i < origTextBlocks.size(); i++) {
                TextBlock textBlock = origTextBlocks.valueAt(i);
                textBlocks.add(textBlock);
            }
            Collections.sort(textBlocks, new Comparator<TextBlock>() {
                @Override
                public int compare(TextBlock o1, TextBlock o2) {
                    int diffOfTops = o1.getBoundingBox().top - o2.getBoundingBox().top;
                    int diffOfLefts = o1.getBoundingBox().left - o2.getBoundingBox().left;
                    if (diffOfTops != 0) {
                        return diffOfTops;
                    }
                    return diffOfLefts;
                }
            });

            StringBuilder detectedText = new StringBuilder();
            for (TextBlock textBlock : textBlocks) {
                if (textBlock != null && textBlock.getValue() != null) {
                    detectedText.append(textBlock.getValue());
                    detectedText.append("\n");
                }
            }

            detectedView.setText(detectedText);
        }
        finally {
            textRecognizer.release();
        }
    }

    private void inspect(Uri uri) {
        InputStream is = null;
        Bitmap bitmap = null;
        try {
            is = getContentResolver().openInputStream(uri);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            options.inSampleSize = 2;
            options.inScreenDensity = DisplayMetrics.DENSITY_LOW;
            bitmap = BitmapFactory.decodeStream(is, null, options);
            inspectFromBitmap(bitmap);
        } catch (FileNotFoundException e) {
            Log.w(TAG, "Failed to find the file: " + uri, e);
        } finally {
            if (bitmap != null) {
                bitmap.recycle();
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    Log.w(TAG, "Failed to close InputStream", e);
                }
            }
        }
    }

}
