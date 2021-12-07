package aditechco.phuhq.qrreader;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import github.nisrulz.qreader.QRDataListener;
import github.nisrulz.qreader.QREader;

public class ScanActivity extends AppCompatActivity {
    //region AVAILABLE
    private SurfaceView mySurfaceView;
    private QREader qrEader;
    private TextView tvResult;
    static final int REQUEST_PERMISSION_CODE = 12322;
    private String currentQR;
    //endregion

    //region FROM EVENTS
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        addControls();
        mainLoad();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.CAMERA)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        if (qrEader != null)
                            qrEader.releaseAndCleanup();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Toast.makeText(ScanActivity.this, "Bạn chưa cấp quyền Camera", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                    }
                }).check();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.CAMERA)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        if (qrEader != null)
                            qrEader.initAndStart(mySurfaceView);
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Toast.makeText(ScanActivity.this, "Bạn chưa cấp quyền Camera", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                    }
                }).check();
    }
    //endregion

    //region MAIN LOAD
    private void addControls() {
        try {
            checkCameraPermission();
            Dexter.withActivity(this)
                    .withPermission(Manifest.permission.CAMERA)
                    .withListener(new PermissionListener() {
                        @Override
                        public void onPermissionGranted(PermissionGrantedResponse response) {
                            setupCamera();
                        }

                        @Override
                        public void onPermissionDenied(PermissionDeniedResponse response) {
                            Toast.makeText(ScanActivity.this, "Bạn chưa cấp quyền Camera", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                        }
                    }).check();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void mainLoad() {
        try {
            setupQREader();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //endregion

    //region SETUP CAMERA
    private void setupCamera() {
        try {
            tvResult = findViewById(R.id.tvResult);
            mySurfaceView = (SurfaceView) findViewById(R.id.camera_view);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupQREader() {
        try {
//            BarcodeDetector detector = new BarcodeDetector(getApplicationContext().);
            qrEader = new QREader.Builder(this, mySurfaceView, new QRDataListener() {
                @Override
                public void onDetected(final String data) {
                    tvResult.post(new Runnable() {
                        @Override
                        public void run() {
                            if (!data.equals(currentQR)) {
                                tvResult.setText(data);
                                onVibrator(ScanActivity.this);
                                onSoundScanOK();
                                currentQR = data;
                            }
                        }
                    });
                }
            }).facing(QREader.BACK_CAM)
                    .enableAutofocus(true)
                    .height(mySurfaceView.getHeight())
                    .width(mySurfaceView.getWidth())
                    .build();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //endregion

    //region VIBRATOR
    @SuppressLint("MissingPermission")
    public static void onVibrator(Context context) {
        try {
            Vibrator vibrator = (Vibrator) context.getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
            assert vibrator != null;
            vibrator.vibrate(500);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //endregion

    //region SOUND SCAN
    public static void onSoundScanOK() {
        try {
            ToneGenerator toneGenerator = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 500);
            toneGenerator.startTone(ToneGenerator.TONE_PROP_PROMPT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void onSoundScanNG() {
        try {
            ToneGenerator toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 500);
            toneGenerator.startTone(ToneGenerator.TONE_CDMA_PIP);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //endregion

    //region HANDLE PERMISSION PHONE CAMERA
    public void checkCameraPermission() {
        try {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(((ScanActivity) this), new String[]{Manifest.permission.CAMERA}, REQUEST_PERMISSION_CODE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale((this), Manifest.permission.CAMERA)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this)
                            .setMessage("Cấp quyền cho ứng dụng")
                            .setPositiveButton("OK", (dialogInterface, i)
                                    -> ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                                    REQUEST_PERMISSION_CODE))
                            .setNegativeButton("Cancel", null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        }
    }
    //endregion
}