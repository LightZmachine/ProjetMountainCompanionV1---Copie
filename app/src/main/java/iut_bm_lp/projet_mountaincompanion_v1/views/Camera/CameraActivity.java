package iut_bm_lp.projet_mountaincompanion_v1.views.Camera;

import android.content.Intent;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import iut_bm_lp.projet_mountaincompanion_v1.R;
import iut_bm_lp.projet_mountaincompanion_v1.controllers.MountainDataSource;
import iut_bm_lp.projet_mountaincompanion_v1.models.Mountain;
import iut_bm_lp.projet_mountaincompanion_v1.views.Azimuth.MyCurrentAzimuth;
import iut_bm_lp.projet_mountaincompanion_v1.views.Azimuth.OnAzimuthChangedListener;

public class CameraActivity extends AppCompatActivity implements OnAzimuthChangedListener{

    private Camera mCamera = null;
    private CameraView mCameraView = null;

    private FragmentManager fm = null;
    private Fragment mFragment =null;

    private MountainDataSource mDataSource;

    private ArrayList<Mountain> mMountains;

    private float mMyLatitude = (float) 47.6483579;
    private float mMyLongitude = (float) 6.8465929;

    private static double AZIMUTH_ACCURACY = 5;

    private double mAzimuthReal = 0;
    private double mAzimuthTeoretical = 0;

    private Mountain mMountain;


    private MyCurrentAzimuth mMyCurrentAzimuth;






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
/*
        mDataSource = MountainDataSource.getInstance(this);
        mDataSource.open();
        mMountains = mDataSource.getAllMountains();
        mDataSource.close();
*/

setupListeners();

        try {

            mCamera = Camera.open(0);
        } catch (Exception e) {

            Log.d("ERROR", "Failed to get camera: " + e.getMessage());
        }

        if(mCamera != null ){

            mCameraView = new CameraView(this, mCamera);
            FrameLayout camera_view = (FrameLayout)findViewById(R.id.camera_view);
            camera_view.addView(mCameraView);

            mMountain= new Mountain(1, (float) 47.649766, (float) 6.846521, "ballon", 1247);



        }
    }

    public double calculateTeoreticalAzimuth() {

//        Log.e("latMountain", "" + mMountain.getLatitude());
//        Log.e("longMountain", "" + mMountain.getLongitude());
//
//        Log.e("MyLat", "" + mMyLatitude);
//        Log.e("MyLong", "" + mMyLongitude);

        double dX = mMountain.getLatitude() - mMyLatitude;
        double dY = mMountain.getLongitude() - mMyLongitude;

        double phiAngle;
        double tanPhi;
        double azimuth = 0;

        tanPhi = Math.abs(dY / dX);
        phiAngle = Math.atan(tanPhi);
        phiAngle = Math.toDegrees(phiAngle);

        if(dX > 0 && dY > 0) {
            return azimuth = phiAngle;
        }else if (dX < 0 && dY > 0) { // II
            return azimuth = 180 - phiAngle;
        } else if (dX < 0 && dY < 0) { // III
            return azimuth = 180 + phiAngle;
        } else if (dX > 0 && dY < 0) { // IV
            return azimuth = 360 - phiAngle;
        }

        return phiAngle;
    }

    private List<Double> calculateAzimuthAccuracy(double azimuth) {

        double minAngle = azimuth - AZIMUTH_ACCURACY;
        double maxAngle = azimuth + AZIMUTH_ACCURACY;
        List<Double> minMax = new ArrayList<Double>();

        if (minAngle < 0)
            minAngle += 360;

        if (maxAngle >= 360)
            maxAngle -= 360;

        minMax.clear();
        minMax.add(minAngle);
        minMax.add(maxAngle);

        return minMax;
    }

    private boolean isBetween(double minAngle, double maxAngle, double azimuth) {
        if (minAngle > maxAngle) {
            if (isBetween(0, maxAngle, azimuth) && isBetween(minAngle, 360, azimuth))
                return true;
        } else {
            if (azimuth > minAngle && azimuth < maxAngle)
                return true;
        }
        return false;
    }

    @Override
    public void onAzimuthChanged(float azimuthFrom, float azimuthTo) {

        Log.e("AzimuthFrom", "" + azimuthFrom);
        Log.e("AzimutTo", "" + azimuthTo);
        mAzimuthReal = azimuthTo;

        mAzimuthTeoretical = calculateTeoreticalAzimuth();

        double minAngle = calculateAzimuthAccuracy(mAzimuthTeoretical).get(0);
        double maxAngle = calculateAzimuthAccuracy(mAzimuthTeoretical).get(1);

        //Log.e("mAzimuthReal", "" + mAzimuthReal);
        if(isBetween(minAngle, maxAngle, mAzimuthReal)){

            Log.e("test", "HELOOOOOOOOOOOOOoo");
            Toast.makeText(this,"BALLOOONNN", Toast.LENGTH_LONG).show();
        }
    }

    private void setupListeners() {

        mMyCurrentAzimuth = new MyCurrentAzimuth(this, this);
        mMyCurrentAzimuth.start();
    }
}
