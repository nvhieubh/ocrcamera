package cordova.plugin.ocrcamera;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.LinearLayout;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.content.ContentValues.TAG;
import static com.theartofdev.edmodo.cropper.CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE;
import static com.theartofdev.edmodo.cropper.CropImage.isExplicitCameraPermissionRequired;

import cordova.plugin.ocrcamera.R;
import cordova.plugin.ocrcamera.FileProvider;

public class MainActivity extends AppCompatActivity implements CropImageView.OnSetImageUriCompleteListener,
        CropImageView.OnCropImageCompleteListener {

    private static int SCANNING_ID_NOTHING = 0;
    private static int SCANNING_ID_MERCHANT_NAME = 1;
    private static int SCANNING_ID_DATE = 2;
    private static int SCANNING_ID_AMOUNT = 3;
    
    RelativeLayout rootView;

    ImageView imgCancel;

    CropImageView mCropImageView;

    ImageView imgMerchantNameScan;
    ImageView imgMerchantNameDone;
    ImageView imgDateScan;
    ImageView imgDateDone;
    ImageView imgAmountScan;
    ImageView imgAmountDone;

    EditText edtMerchantName;
    EditText edtDate;
    EditText edtAmount;

    LinearLayout llExpense;
    ImageView imgExpense;

    TextView tvMerchantName;
    TextView tvDate;
    TextView tvAmount;

    private int scanningID = 0;

    private Uri mCropImageUri;

    private Handler cropHandler = new Handler();

    private long delay = 2000;

    private String mCurrentPhotoPath;

    private R r;

    private String MerchantName = new String(),
            Date = new String(),
            Amount = new String(),
            image = new String();

    private Runnable cropRunnable = new Runnable() {

        @Override
        public void run() {

            cropHandler.postDelayed(cropRunnable, delay);

            // Crop Text Image
            mCropImageView.getCroppedImageAsync();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // TODO: Change status bar background color
        /**Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        int currentApiVersion = android.os.Build.VERSION.SDK_INT;

        if (currentApiVersion >= Build.VERSION_CODES.M) {
             window.setStatusBarColor(ContextCompat.getColor(this, R.color.color_black));
        }*/

        r = new R(this);
        setContentView(r.getId("layout", "main_activity"));

        // Declare Views
        rootView = (RelativeLayout) findViewById(r.getId("id", "root_view"));
        imgCancel = (ImageView) findViewById(r.getId("id", "img_right"));
        mCropImageView = (CropImageView) findViewById(r.getId("id", "cropImageView"));

        imgMerchantNameScan = (ImageView) findViewById(r.getId("id", "img_merchant_name_scan"));
        imgMerchantNameDone = (ImageView) findViewById(r.getId("id", "img_merchant_name_done"));
        imgDateScan = (ImageView) findViewById(r.getId("id", "img_date_scan"));
        imgDateDone = (ImageView) findViewById(r.getId("id", "img_date_done"));
        imgAmountScan = (ImageView) findViewById(r.getId("id", "img_amount_scan"));
        imgAmountDone = (ImageView) findViewById(r.getId("id", "img_amount_done"));

        edtMerchantName = (EditText) findViewById(r.getId("id", "edt_merchant_name"));
        edtDate = (EditText) findViewById(r.getId("id", "edt_date"));
        edtAmount = (EditText) findViewById(r.getId("id", "edt_amount"));

        llExpense = (LinearLayout) findViewById(r.getId("id", "ll_expense"));
        imgExpense = (ImageView) findViewById(r.getId("id", "img_expense"));

        tvMerchantName = (TextView) findViewById(r.getId("id", "tv_merchant_name"));
        tvDate = (TextView) findViewById(r.getId("id", "tv_date"));
        tvAmount = (TextView) findViewById(r.getId("id", "tv_amount"));

        setImageResouceByBase64();
        initUI();
        onClick();
        initImageData();
    }

    private void initUI() {
        rootView.setVisibility(View.GONE);

        // TODO: Init Crop Image View
        initCropImageView();
    }

    private void initCropImageView() {
        mCropImageView.setOnSetImageUriCompleteListener(this);
        mCropImageView.setOnCropImageCompleteListener(this);

        mCropImageView.setShowProgressBar(false);
        mCropImageView.setGuidelines(CropImageView.Guidelines.OFF);
        mCropImageView.setShowCropOverlay(false);

        mCropImageView.setOnCropWindowChangedListener(new CropImageView.OnSetCropWindowChangeListener() {
            @Override
            public void onCropWindowChanged() {

                // TODO: Remove Previous Runnable if it isn't finish
                cropHandler.removeCallbacks(cropRunnable);

                // TODO: Post Runnable Delay
                cropHandler.postDelayed(cropRunnable, delay);
            }
        });
    }

    // =========================== Handle Load and Crop Image ===========================
    private void initImageData() {
        cropHandler.removeCallbacks(cropRunnable);

        if (isExplicitCameraPermissionRequired(getApplicationContext())) {
            int currentApiVersion = android.os.Build.VERSION.SDK_INT;

            if (currentApiVersion >= Build.VERSION_CODES.M) {
                requestPermissions(
                        new String[]{Manifest.permission.CAMERA},
                        CropImage.CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE);
            }

        } else {
            startPickImageActivity(MainActivity.this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CropImage.CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startPickImageActivity(MainActivity.this);
            } else {
                Toast.makeText(this, "Cancelling, required permissions are not granted", Toast.LENGTH_LONG)
                        .show();
            }
        }
//        if (requestCode == CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE) {
//            if (mCropImageUri != null
//                    && grantResults.length > 0
//                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                mCropImageView.setImageUriAsync(mCropImageUri);
//            } else {
//                Toast.makeText(this, "Cancelling, required permissions are not granted", Toast.LENGTH_LONG)
//                        .show();
//            }
//        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_IMAGE_CHOOSER_REQUEST_CODE
                && resultCode == AppCompatActivity.RESULT_OK) {
            Uri imageUri = CropImage.getPickImageResultUri(this, data);

            image = getBase64FromPath(mCurrentPhotoPath);

            mCropImageView.setImageUriAsync(Uri.fromFile(new File(mCurrentPhotoPath)));

            rootView.setVisibility(View.VISIBLE);

        } else{
            processCameraCanceled();
        }
    }

    private void processCameraCanceled(){
        Intent data = new Intent();
        setResult(RESULT_CANCELED, data);

        finish();
    }

    private void onClick(){
        // On Click
        imgCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Toast.makeText(MainActivity.this, "on cancel clicked", Toast.LENGTH_SHORT).show();

                Intent data = new Intent();
                setResult(RESULT_CANCELED, data);

                finish();
            }
        });

        tvMerchantName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MerchantName = new String();

                imgMerchantNameScan.setVisibility(View.GONE);
                imgMerchantNameDone.setVisibility(View.VISIBLE);
                imgDateScan.setVisibility(View.VISIBLE);
                imgDateDone.setVisibility(View.GONE);
                imgAmountScan.setVisibility(View.VISIBLE);
                imgAmountDone.setVisibility(View.GONE);

                if(!mCropImageView.isShowCropOverlay()){
                    mCropImageView.setShowCropOverlay(true);
                }
                scanningID = SCANNING_ID_MERCHANT_NAME;
            }
        });

        tvDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Date = new String();

                imgMerchantNameScan.setVisibility(View.VISIBLE);
                imgMerchantNameDone.setVisibility(View.GONE);
                imgDateScan.setVisibility(View.GONE);
                imgDateDone.setVisibility(View.VISIBLE);
                imgAmountScan.setVisibility(View.VISIBLE);
                imgAmountDone.setVisibility(View.GONE);

                if(!mCropImageView.isShowCropOverlay()){
                    mCropImageView.setShowCropOverlay(true);
                }
                scanningID = SCANNING_ID_DATE;
            }
        });

        tvAmount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Amount = new String();

                imgMerchantNameScan.setVisibility(View.VISIBLE);
                imgMerchantNameDone.setVisibility(View.GONE);
                imgDateScan.setVisibility(View.VISIBLE);
                imgDateDone.setVisibility(View.GONE);
                imgAmountScan.setVisibility(View.GONE);
                imgAmountDone.setVisibility(View.VISIBLE);

                if(!mCropImageView.isShowCropOverlay()){
                    mCropImageView.setShowCropOverlay(true);
                }
                scanningID = SCANNING_ID_AMOUNT;
            }
        });

        imgMerchantNameScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MerchantName = new String();

                imgMerchantNameScan.setVisibility(View.GONE);
                imgMerchantNameDone.setVisibility(View.VISIBLE);
                imgDateScan.setVisibility(View.VISIBLE);
                imgDateDone.setVisibility(View.GONE);
                imgAmountScan.setVisibility(View.VISIBLE);
                imgAmountDone.setVisibility(View.GONE);

                if(!mCropImageView.isShowCropOverlay()){
                    mCropImageView.setShowCropOverlay(true);
                }
                scanningID = SCANNING_ID_MERCHANT_NAME;
            }
        });

        imgMerchantNameDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MerchantName = edtMerchantName.getText().toString().trim();

                imgMerchantNameDone.setVisibility(View.GONE);
                imgMerchantNameScan.setVisibility(View.VISIBLE);

                mCropImageView.setShowCropOverlay(false);
                scanningID = SCANNING_ID_NOTHING;
            }
        });

        imgDateScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Date = new String();

                imgMerchantNameScan.setVisibility(View.VISIBLE);
                imgMerchantNameDone.setVisibility(View.GONE);
                imgDateScan.setVisibility(View.GONE);
                imgDateDone.setVisibility(View.VISIBLE);
                imgAmountScan.setVisibility(View.VISIBLE);
                imgAmountDone.setVisibility(View.GONE);

                if(!mCropImageView.isShowCropOverlay()){
                    mCropImageView.setShowCropOverlay(true);
                }
                scanningID = SCANNING_ID_DATE;
            }
        });

        imgDateDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Date = edtDate.getText().toString().trim();

                imgDateDone.setVisibility(View.GONE);
                imgDateScan.setVisibility(View.VISIBLE);

                mCropImageView.setShowCropOverlay(false);
                scanningID = SCANNING_ID_NOTHING;
            }
        });

        imgAmountScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Amount = new String();

                imgMerchantNameScan.setVisibility(View.VISIBLE);
                imgMerchantNameDone.setVisibility(View.GONE);
                imgDateScan.setVisibility(View.VISIBLE);
                imgDateDone.setVisibility(View.GONE);
                imgAmountScan.setVisibility(View.GONE);
                imgAmountDone.setVisibility(View.VISIBLE);

                if(!mCropImageView.isShowCropOverlay()){
                    mCropImageView.setShowCropOverlay(true);
                }
                scanningID = SCANNING_ID_AMOUNT;
            }
        });

        imgAmountDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Amount = edtAmount.getText().toString().trim();

                imgAmountDone.setVisibility(View.GONE);
                imgAmountScan.setVisibility(View.VISIBLE);

                mCropImageView.setShowCropOverlay(false);
                scanningID = SCANNING_ID_NOTHING;
            }
        });

        llExpense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(MainActivity.this, "on expense click callbacked 1", Toast.LENGTH_SHORT).show();
                Intent resultIntent = new Intent();
                Bundle res = new Bundle();

                ArrayList<String> results = new ArrayList<>();
                results.add(MerchantName);
                results.add(Date);
                results.add(Amount);
                results.add(image);

                res.putStringArrayList("MULTIPLEFILENAMES", results);
                resultIntent.putExtras(res);
                setResult(RESULT_OK, resultIntent);
                //Toast.makeText(MainActivity.this, "on expense click callbacked 2", Toast.LENGTH_LONG).show();
                finish();
            }
        });
        
    }

    @Override
    public void onCropImageComplete(CropImageView view, CropImageView.CropResult result) {
        handleCropResult(result);
    }

    @Override
    public void onSetImageUriComplete(CropImageView view, Uri uri, Exception error) {

    }

    private void handleCropResult(CropImageView.CropResult result) {
        if (result.getError() == null) {
            String scanResult = new String();

            TextRecognizer ocrFrame = new TextRecognizer.Builder(getApplicationContext()).build();
            Frame frame = new Frame.Builder().setBitmap(mCropImageView.getCropShape() == CropImageView.CropShape.OVAL
                    ? CropImage.toOvalBitmap(result.getBitmap())
                    : result.getBitmap()).build();
            if (ocrFrame.isOperational()) {
                Log.e(TAG, "Textrecognizer is operational");
            }
            SparseArray<TextBlock> textBlocks = ocrFrame.detect(frame);

            if (textBlocks.size() > 0) {
                for (int i = 0; i < textBlocks.size(); i++) {

                    TextBlock textBlock = textBlocks.get(textBlocks.keyAt(i));
                    scanResult = scanResult.length() > 1 ? scanResult + " " + textBlock.getValue() : textBlock.getValue();
                }

                if (scanningID == SCANNING_ID_MERCHANT_NAME) {
                    edtMerchantName.setText(scanResult);
                } else if (scanningID == SCANNING_ID_DATE) {
                    edtDate.setText(scanResult);
                } else if (scanningID == SCANNING_ID_AMOUNT) {
                    edtAmount.setText(scanResult);
                }

                cropHandler.removeCallbacks(cropRunnable);
            }

        } else {
            Log.e("AIC", "Failed to crop image", result.getError());
            /**Toast.makeText(
                    MainActivity.this,
                    "Image crop failed: " + result.getError().getMessage(),
                    Toast.LENGTH_SHORT)
                    .show();*/
        }
    }

    private void startPickImageActivity(Activity activity) {

        dispatchTakePictureIntent();

//        CropImage.startPickImageActivity(activity);
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.oracle.scanreceive.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivityForResult(takePictureIntent, PICK_IMAGE_CHOOSER_REQUEST_CODE);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }


    /**
     * Get String Base64 From File Path
     * @param path
     * @return
     */
    private String getBase64FromPath(String path) {
        File imageFile = new File(path);
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(imageFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Bitmap b = BitmapFactory.decodeStream(fis);

        Bitmap bm = adjustImageOrientation(b, imageFile);

        // original measurements
        int origWidth = bm.getWidth();
        int origHeight = bm.getHeight();

        int destWidth = 375;
        int destHeight;

        if(origWidth > destWidth) {
            // picture is wider than we want it, we calculate its target height
            destHeight = origHeight / (origWidth / destWidth);
        }else {
            destWidth = origWidth;
            destHeight = origHeight;
        }

        Toast.makeText(MainActivity.this, "Width = " + destWidth + " - Height = " + destHeight, Toast.LENGTH_SHORT).show();
        Log.d("NamNT", "Compressed Image Width = " + destWidth + " - Height = " + destHeight);

        // we create an scaled bitmap so it reduces the image, not just trim it
        Bitmap bm2 = Bitmap.createScaledBitmap(bm, destWidth, destHeight, false);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bm2.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);

        byte[] bytes = byteArrayOutputStream.toByteArray();
        String encImage = Base64.encodeToString(bytes, Base64.DEFAULT);
        //Base64.de

        Log.d("NamNT", encImage.toString() + "");
        return encImage;
    }

    private Bitmap adjustImageOrientation(Bitmap image, File f) {
        int rotate = 0;
        try {
    
            ExifInterface exif = new ExifInterface(f.getAbsolutePath());
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_270:
                rotate = 270;
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                rotate = 180;
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                rotate = 90;
                break;
    
            case ExifInterface.ORIENTATION_NORMAL:
                rotate = 0;
                break;
    
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (rotate != 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(rotate);
    
            image = Bitmap.createBitmap(image, 0, 0, image.getWidth(),
                    image.getHeight(), matrix, true);
        }
    
    
        return image;
    }

    private void setImageResouceByBase64(){
        imgCancel.setImageBitmap(getBitmapFromBase64("iVBORw0KGgoAAAANSUhEUgAAADAAAAAwCAYAAABXAvmHAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAJcEhZcwAADsMAAA7DAcdvqGQAAAKMSURBVGhD3ZnPLgRBEIc3shISHBxwcHGTeAoX7+AZ9iJeADcH4Sk4iEQQHkNCXHkJF5HY9U1vVdAzvdOz038mvqQzO9XVVb/qrtje6P07RqPRzHA43GPMi6kToGeDMZBXNzhdUMSI5x2Pvpizgo419LyIrgMxl2HyvHBSeL/lkbUI8hfin40goVQEtgVbvIL9hkeWIsi9aov/xSFjrAun/bGtmhxFkK+08zbM7xhnPiwzinZxIvNJiiCPj/hTHnOyxCzqYyx22onMRy2CHJPaxsD8ibj/hbmsRRDXZ+erxSv4FEUkbyfitRev4Jv0JIg1fdu4YE2SIlgfbudtWBu1nVgXT7xCjCgnwZrwbeOCWEGLwC/+ztsQM0g7MZ9evELsVifBXLq2cUGOqYrgPd/O25DLt51mxb874hVy+pzEFWOL8SSmSpKLV8jtU8SHfKwkm3gFDbXt5CK7eAUttSdh0xnxCpr6jEuEfRmFDpj/5HEsy7oF4tYZ72Op1TD/Ku7dAmHFl9Sj6JwIftc8Gt2dooKY2r/zNviXvuyygIjG4hXW1d6dooIAn7vNm3yshPk8J0FSn50/xmdJRDpJXgTJGt1teJ3l3efuFL8IEk11Jcbsc+2IexIEb3WrZDpfEQRtJV7BrfbuJPPhiiBg0F9SuKc7CYIE2XkblsUvgsVRxCssj9dOLEzyA5ww4U8C56g7b0O4cEXglFS8Qtj27YRDkrZxQfjpTwJjlp23IU3zInjZxGj+D+sihXiFdD7tdM9YMQv4MBB7JSnFK6T1OYltcTdFHIj9DznEK6R3FoF9V9x+sIvIKV5BRqmdKsUrTB6J05mYsoOcooiHWvEKTuP/gHcINC0yfnre0Ot9AxcybB82W+uRAAAAAElFTkSuQmCC"));
        imgMerchantNameScan.setImageBitmap(getBitmapFromBase64("iVBORw0KGgoAAAANSUhEUgAAADAAAAAwCAYAAABXAvmHAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAJcEhZcwAACxMAAAsTAQCanBgAAALwSURBVGhD7Zo9axRRFIaDaKOdYhexVUQstLcSbaKtYGEUUWxk860RjOjPCGggZWI+TRqTTvwFUbQRLBQkjUQx+LX6vHfeZHd1nAnOXjLCPPCyuWfOueed3Z25y510/E69Xt+L7qOXqP4Tml7X0DGnRoH5L6mXaOr7FT1D1532J+TtQHdJequiv8HxmkuiwPwTbpUKx1fQBacnEN+ZV7gBebddFgXmX3SrPG66JBSNOLgVrrosCngZd59cyD2lgv1o1bFNiC2hIdSDelE/usyh3e4VBXocQDdQH1LfGj311X4ejDVBbFEF9zxupvHxlAR87kEtX3PG73XghccBxsuuKR142yfTtiqv6wr+8DjAeMD5pQR/s7Ya0B2oBRJ6nFtK8Ddpq4G0E+hzbinB35StBtJOYNC5pQR/c7YaUGDNfwcYX3FuKcHiWOI0QSdwBOm+e0vmiWXe58nR2rCAZiLoMTrnVqlwvBPVkPxedHjrULSUnHscmP+OW8WBBjPuFQXmj3sN0mDZvaLA/CNuFQcadOldQvqd0m4Ncg7H3aqioqIiBd0p0CyaiiDNm7kSF4YG1UqcBfNXK3EmNDiL9ItUuwbtVrUSV1RU5KA7BZpDaStpnlTX5am2BwwUWompj7o9nwsGCq3E1G/vxhkGCq3Ebf8EmLObSSeR9nrG0UEfSoXjWomH0cA/aJh+hVZa5jiJdD3J72ja1mKvc0sJ/nJ3p8u+vd5yDaadwH+/Ox31MWpR8NfymEmBL/47wDju7/GC4O+JrcprXYGnHgcY62ngLueXCrwdRZ8Tp8HrJwWvebwJMd1WO11XCvBzAq3YYoDxG10Dekr/Kgk1ILaK5tEjpPuurv4xXjPXiaIwv+7zE2gaqa/eTD2z/pY4a0DswUbRecdyIbc/FEWC+efdKhPy9GTpkMtC4VByKBvyYv+vxIJbZfGBvDMuaUDwNNLm0ju0jsK/u2zA8Dsv3U6PAj0eJt0ayAd8RK/RKDqcZHd0/AKzDTeA2uqE9AAAAABJRU5ErkJggg=="));
        imgMerchantNameDone.setImageBitmap(getBitmapFromBase64("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"));
        imgDateScan.setImageBitmap(getBitmapFromBase64("iVBORw0KGgoAAAANSUhEUgAAADAAAAAwCAYAAABXAvmHAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAJcEhZcwAACxMAAAsTAQCanBgAAALwSURBVGhD7Zo9axRRFIaDaKOdYhexVUQstLcSbaKtYGEUUWxk860RjOjPCGggZWI+TRqTTvwFUbQRLBQkjUQx+LX6vHfeZHd1nAnOXjLCPPCyuWfOueed3Z25y510/E69Xt+L7qOXqP4Tml7X0DGnRoH5L6mXaOr7FT1D1532J+TtQHdJequiv8HxmkuiwPwTbpUKx1fQBacnEN+ZV7gBebddFgXmX3SrPG66JBSNOLgVrrosCngZd59cyD2lgv1o1bFNiC2hIdSDelE/usyh3e4VBXocQDdQH1LfGj311X4ejDVBbFEF9zxupvHxlAR87kEtX3PG73XghccBxsuuKR142yfTtiqv6wr+8DjAeMD5pQR/s7Ya0B2oBRJ6nFtK8Ddpq4G0E+hzbinB35StBtJOYNC5pQR/c7YaUGDNfwcYX3FuKcHiWOI0QSdwBOm+e0vmiWXe58nR2rCAZiLoMTrnVqlwvBPVkPxedHjrULSUnHscmP+OW8WBBjPuFQXmj3sN0mDZvaLA/CNuFQcadOldQvqd0m4Ncg7H3aqioqIiBd0p0CyaiiDNm7kSF4YG1UqcBfNXK3EmNDiL9ItUuwbtVrUSV1RU5KA7BZpDaStpnlTX5am2BwwUWompj7o9nwsGCq3E1G/vxhkGCq3Ebf8EmLObSSeR9nrG0UEfSoXjWomH0cA/aJh+hVZa5jiJdD3J72ja1mKvc0sJ/nJ3p8u+vd5yDaadwH+/Ox31MWpR8NfymEmBL/47wDju7/GC4O+JrcprXYGnHgcY62ngLueXCrwdRZ8Tp8HrJwWvebwJMd1WO11XCvBzAq3YYoDxG10Dekr/Kgk1ILaK5tEjpPuurv4xXjPXiaIwv+7zE2gaqa/eTD2z/pY4a0DswUbRecdyIbc/FEWC+efdKhPy9GTpkMtC4VByKBvyYv+vxIJbZfGBvDMuaUDwNNLm0ju0jsK/u2zA8Dsv3U6PAj0eJt0ayAd8RK/RKDqcZHd0/AKzDTeA2uqE9AAAAABJRU5ErkJggg=="));
        imgDateDone.setImageBitmap(getBitmapFromBase64("iVBORw0KGgoAAAANSUhEUgAAADAAAAAwCAYAAABXAvmHAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAJcEhZcwAAEbAAABGwAcgn9VQAAAWMSURBVGhDzZpriFR1GMZX0jY1ysgueCkIypQkMLcEu2AlVhh+KRFCUyvoosKWpB/sQ30pEsXIPtl2WSsCKyo/ZUXWxq5d3CC7SCql5opCZa7Zpu5sv+d/njk7szOzOzN7ZmZ/8HDOe/m/7zs7s2fOZeqSJJVKTUHL0XtoH/oPnUYn0C70OlqIxnlJbenp6RnFMHejTWg36sI3IOQdR9+g5zFvdLnqQdNLab4G7QsTDRLqfIUeYPcct6gMNKmnyWq2R0PnPuA/g/agbagJrUfr0Ab0BvoCHXR6DsR+RAvcLlkoPBN9514x+LrQh2gpmoxrhJfkhZzRaDp6En0ZVckGv/6HJnrJ4KFYIzrj+gHsP9Cz6EqnlQXrZ6DNqNulA9gd6E6nlQ9FNrpmDL5NbC5zSiJQswF9HHXoBd9DTikdFr/iOgHs39FchysC9R9Hp90ygL3M4eJhUdZfHnsHutzhikKfWeiIWwew73d4YEhu9LoAto4qox2uCvSbjA54BM2gI9xMhwtD0g0o/odi/2s2oxyuKvS+Bv0ZTRJm+Y3NBQ7nQkI9+iFKDwsOowkO1wT6z0Epj6SZXnUoF4KrnBfAvsOhmsIcz3ikNLmnHyRdhP5ygoZvcqjmMM5ZzLMrmizM1uJQLzifclwcw77EoSEB88zxbAHsWQ6F4EgU/8fDcw5VDfrXe7cg5LR4Pr2Ad+0Ogbn2K/AvqsrxXtBrAvoM7UX9fkkSv8djas6TKLquYKfJfgW2BmcVoJeG/8mt1XunQ3khRdcfHVF2yF8SAuzssU/OpcFZYeiTNbzAXuNwQciJT2/Y3yyHvvHCuYe26CrnVgx6aPifwxQGe73D/ULefV6iNe1yrLAtx142w51bEegxHpU1vCBX387hTIFthxwfhCrA/kfOqwjUz/exKXp4wZLzWROuCNl2qmh8Xcv+y85LHGqX/bHJhGXDWPe9159S4fhOQrEFyZuG1qKirpjIG/RfPhPWtrlGSkZ88cD+OucUhLRzyTsUrQhrFjqUF+KJDi9Y3+o64QWcCFWB/Q3OKQhpegGHoxUR2IsczgJ/Ih+bvlBjh2t1y8g8fW52Tr+Qdyv6x8sC2FnvBPagjjaFoIxO7Ha7XpcaNcsQ7H/uvAEh9zaU90Ww1fCJfmzSUGosOhaKslWzRTbU5BAa6dwBITffi9BF+U6bAexEhhfUanBZ1d0vxzh00g4xzblFwbLbWZP1IjIhltjwgnqPuLRqt6ad8R039lcGZwmwJuedEPgSHV5Q832XV/2X0s619snZFpwlwtKsd4L9Sgx/MTruFuoxLx2YYV+a60KgRKij+6fvsH61XYlC7eXReGH4o+i8EMDW13N7FArBt0JgCMFYI5jrl2jCMONGhyJwLHFMQd1EutahIQHzPOzxNF83muJQBA7dE4ovbGC7QzWHWcYyW/xMgv03HcqGQHzNKbAbHaopzLHFI2kmHfKvcCgXErZGqSFZV2i3OFQT6P+Exwlgr3IoPyToiy3z7dL+1Q5XFfreG4boZTsa5nBhGHp2lB+BvR9Ndbgq0G8+yjzV1xOb8Q4PDMkPem0AW4+U7nK4otBnpdsGsP9G0x0uHhYtc40YfE+zOdspiUJtfXzfjjpFYGv4m5xSOtRYTIG+j3zaUWKPmqilQ/ijKL5pJbAPoganlQ9FbkY5D7Xx6ZbgAhR9pZcI6/QXX4Gyrh0Evk/YJPcgkYIXovgWZCb4DyiG9DuIqbjGoKyjBfZwYjoZux49hnRWmb4wicHfifo/VA4GiusB3KfulxfiR5B+3NGGWpEeEOq3FPHzh74Q0+lLM5rkVpWFRrPRFtTpGcqC9boKfBHV5tyLxhOZYzF6DX2LrWfJ+jXKKZRGJ1+6/6QHJ7+iFvQCmodvjEuVQV3d/zlZh8lKioz4AAAAAElFTkSuQmCC"));
        imgAmountScan.setImageBitmap(getBitmapFromBase64("iVBORw0KGgoAAAANSUhEUgAAADAAAAAwCAYAAABXAvmHAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAJcEhZcwAACxMAAAsTAQCanBgAAALwSURBVGhD7Zo9axRRFIaDaKOdYhexVUQstLcSbaKtYGEUUWxk860RjOjPCGggZWI+TRqTTvwFUbQRLBQkjUQx+LX6vHfeZHd1nAnOXjLCPPCyuWfOueed3Z25y510/E69Xt+L7qOXqP4Tml7X0DGnRoH5L6mXaOr7FT1D1532J+TtQHdJequiv8HxmkuiwPwTbpUKx1fQBacnEN+ZV7gBebddFgXmX3SrPG66JBSNOLgVrrosCngZd59cyD2lgv1o1bFNiC2hIdSDelE/usyh3e4VBXocQDdQH1LfGj311X4ejDVBbFEF9zxupvHxlAR87kEtX3PG73XghccBxsuuKR142yfTtiqv6wr+8DjAeMD5pQR/s7Ya0B2oBRJ6nFtK8Ddpq4G0E+hzbinB35StBtJOYNC5pQR/c7YaUGDNfwcYX3FuKcHiWOI0QSdwBOm+e0vmiWXe58nR2rCAZiLoMTrnVqlwvBPVkPxedHjrULSUnHscmP+OW8WBBjPuFQXmj3sN0mDZvaLA/CNuFQcadOldQvqd0m4Ncg7H3aqioqIiBd0p0CyaiiDNm7kSF4YG1UqcBfNXK3EmNDiL9ItUuwbtVrUSV1RU5KA7BZpDaStpnlTX5am2BwwUWompj7o9nwsGCq3E1G/vxhkGCq3Ebf8EmLObSSeR9nrG0UEfSoXjWomH0cA/aJh+hVZa5jiJdD3J72ja1mKvc0sJ/nJ3p8u+vd5yDaadwH+/Ox31MWpR8NfymEmBL/47wDju7/GC4O+JrcprXYGnHgcY62ngLueXCrwdRZ8Tp8HrJwWvebwJMd1WO11XCvBzAq3YYoDxG10Dekr/Kgk1ILaK5tEjpPuurv4xXjPXiaIwv+7zE2gaqa/eTD2z/pY4a0DswUbRecdyIbc/FEWC+efdKhPy9GTpkMtC4VByKBvyYv+vxIJbZfGBvDMuaUDwNNLm0ju0jsK/u2zA8Dsv3U6PAj0eJt0ayAd8RK/RKDqcZHd0/AKzDTeA2uqE9AAAAABJRU5ErkJggg=="));
        imgAmountDone.setImageBitmap(getBitmapFromBase64("iVBORw0KGgoAAAANSUhEUgAAADAAAAAwCAYAAABXAvmHAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAJcEhZcwAAEbAAABGwAcgn9VQAAAWMSURBVGhDzZpriFR1GMZX0jY1ysgueCkIypQkMLcEu2AlVhh+KRFCUyvoosKWpB/sQ30pEsXIPtl2WSsCKyo/ZUXWxq5d3CC7SCql5opCZa7Zpu5sv+d/njk7szOzOzN7ZmZ/8HDOe/m/7zs7s2fOZeqSJJVKTUHL0XtoH/oPnUYn0C70OlqIxnlJbenp6RnFMHejTWg36sI3IOQdR9+g5zFvdLnqQdNLab4G7QsTDRLqfIUeYPcct6gMNKmnyWq2R0PnPuA/g/agbagJrUfr0Ab0BvoCHXR6DsR+RAvcLlkoPBN9514x+LrQh2gpmoxrhJfkhZzRaDp6En0ZVckGv/6HJnrJ4KFYIzrj+gHsP9Cz6EqnlQXrZ6DNqNulA9gd6E6nlQ9FNrpmDL5NbC5zSiJQswF9HHXoBd9DTikdFr/iOgHs39FchysC9R9Hp90ygL3M4eJhUdZfHnsHutzhikKfWeiIWwew73d4YEhu9LoAto4qox2uCvSbjA54BM2gI9xMhwtD0g0o/odi/2s2oxyuKvS+Bv0ZTRJm+Y3NBQ7nQkI9+iFKDwsOowkO1wT6z0Epj6SZXnUoF4KrnBfAvsOhmsIcz3ikNLmnHyRdhP5ygoZvcqjmMM5ZzLMrmizM1uJQLzifclwcw77EoSEB88zxbAHsWQ6F4EgU/8fDcw5VDfrXe7cg5LR4Pr2Ad+0Ogbn2K/AvqsrxXtBrAvoM7UX9fkkSv8djas6TKLquYKfJfgW2BmcVoJeG/8mt1XunQ3khRdcfHVF2yF8SAuzssU/OpcFZYeiTNbzAXuNwQciJT2/Y3yyHvvHCuYe26CrnVgx6aPifwxQGe73D/ULefV6iNe1yrLAtx142w51bEegxHpU1vCBX387hTIFthxwfhCrA/kfOqwjUz/exKXp4wZLzWROuCNl2qmh8Xcv+y85LHGqX/bHJhGXDWPe9159S4fhOQrEFyZuG1qKirpjIG/RfPhPWtrlGSkZ88cD+OucUhLRzyTsUrQhrFjqUF+KJDi9Y3+o64QWcCFWB/Q3OKQhpegGHoxUR2IsczgJ/Ih+bvlBjh2t1y8g8fW52Tr+Qdyv6x8sC2FnvBPagjjaFoIxO7Ha7XpcaNcsQ7H/uvAEh9zaU90Ww1fCJfmzSUGosOhaKslWzRTbU5BAa6dwBITffi9BF+U6bAexEhhfUanBZ1d0vxzh00g4xzblFwbLbWZP1IjIhltjwgnqPuLRqt6ad8R039lcGZwmwJuedEPgSHV5Q832XV/2X0s619snZFpwlwtKsd4L9Sgx/MTruFuoxLx2YYV+a60KgRKij+6fvsH61XYlC7eXReGH4o+i8EMDW13N7FArBt0JgCMFYI5jrl2jCMONGhyJwLHFMQd1EutahIQHzPOzxNF83muJQBA7dE4ovbGC7QzWHWcYyW/xMgv03HcqGQHzNKbAbHaopzLHFI2kmHfKvcCgXErZGqSFZV2i3OFQT6P+Exwlgr3IoPyToiy3z7dL+1Q5XFfreG4boZTsa5nBhGHp2lB+BvR9Ndbgq0G8+yjzV1xOb8Q4PDMkPem0AW4+U7nK4otBnpdsGsP9G0x0uHhYtc40YfE+zOdspiUJtfXzfjjpFYGv4m5xSOtRYTIG+j3zaUWKPmqilQ/ijKL5pJbAPoganlQ9FbkY5D7Xx6ZbgAhR9pZcI6/QXX4Gyrh0Evk/YJPcgkYIXovgWZCb4DyiG9DuIqbjGoKyjBfZwYjoZux49hnRWmb4wicHfifo/VA4GiusB3KfulxfiR5B+3NGGWpEeEOq3FPHzh74Q0+lLM5rkVpWFRrPRFtTpGcqC9boKfBHV5tyLxhOZYzF6DX2LrWfJ+jXKKZRGJ1+6/6QHJ7+iFvQCmodvjEuVQV3d/zlZh8lKioz4AAAAAElFTkSuQmCC"));
        imgExpense.setImageBitmap(getBitmapFromBase64("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"));
    }

    private Bitmap getBitmapFromBase64(String strBase64){
        byte[] decodedString = Base64.decode(strBase64, Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

        return decodedByte;
    }
}
