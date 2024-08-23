package com.sjbt.sdk.sample.ui.muslim;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.provider.Settings;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import com.blankj.utilcode.util.LogUtils;
import com.sjbt.sdk.sample.base.BaseActivity;
import com.sjbt.sdk.sample.databinding.ActivityDeviceCompassBinding;
import com.sjbt.sdk.sample.utils.LocationWrapper;

/**
 * 指南针页面
 */
public class MuslimCompassActivity extends BaseActivity implements SensorEventListener {

    public static void jumpTo(Context context) {
        Intent intent = new Intent(context, MuslimCompassActivity.class);
        context.startActivity(intent);
    }

    public static final String TAG = "MuslimCompassActivity";

    private SensorManager mSensorManager;
    private Sensor mRotationVectorSensor;
//    private String mLocationProvider;// 位置提供者名称，GPS设备还是网络

    private float[] mRotationMatrix = new float[9];
    private float[] mOrientation = new float[3];
    private float[] mOrientationFiltered = new float[3];

    private static final float ALPHA = 0.25f; // 滤波系数，值越小平滑效果越明显

    private double MOSQUE_LONGITUDE = 39.8262, MOSQUE_LATITUDE = 21.4225;

    private double bearingToMakkah;
    private double currentLatitude;
    private double currentLongitude;

    private float lastDegree;

    private LocationWrapper locationWrapper;
    private Location mLocation;

    private ActivityDeviceCompassBinding binding;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDeviceCompassBinding.inflate(getLayoutInflater());

        locationWrapper = LocationWrapper.getInstance(this);
        locationWrapper.setLocationChangeListener(new LocationWrapper.LocationChangeListener() {
            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }

            @Override
            public void onLocationChanged(Location location) {
                if (location != null) {
                    mLocation = location;
                    updateLocation(location);
                }
            }
        });

        if (mLocation != null) {
            binding.layoutLocationFail.setVisibility(View.GONE);
            locationWrapper.requestLocation();
            updateLocation(mLocation);
        } else {
            binding.layoutLocationFail.setVisibility(View.VISIBLE);

            binding.layoutLocationFail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            });

        }

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mRotationVectorSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        mLocation = locationWrapper.getLocation();

    }

    @Override
    protected void onResume() {
        super.onResume();
        registerSensorService();
        locationWrapper.requestLocation();
        mLocation = locationWrapper.getLocation();

        if (mLocation != null) {
            binding.layoutLocationFail.setVisibility(View.GONE);
            updateLocation(mLocation);
        } else {
            binding.layoutLocationFail.setVisibility(View.VISIBLE);
        }
    }

    private void registerSensorService() {
        mSensorManager.registerListener(this, mRotationVectorSensor, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregister();
    }

    private void unregister() {
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(this);
        }
    }

    private float[] lowPassFilter(float[] input, float[] output) {
        if (output == null) return input;

        for (int i = 0; i < input.length; i++) {
            output[i] = output[i] + ALPHA * (input[i] - output[i]);
        }
        return output;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onSensorChanged(SensorEvent event) {

        if (mLocation == null) {
            return;
        }

        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(mRotationMatrix, event.values);
            SensorManager.getOrientation(mRotationMatrix, mOrientation);

            // 使用低通滤波器来平滑方向数据
            mOrientation = lowPassFilter(mOrientation, mOrientationFiltered);

            float degree = -(float) Math.toDegrees(mOrientation[0]);

            if (Math.abs(degree - lastDegree) >= 1) {
                lastDegree = degree;

                double direction = (Math.abs(360 - bearingToMakkah) - Math.abs(degree));
                LogUtils.i(TAG, "updateDirection 偏离角度：" + degree + " 夹角角度:" + direction + " bearingToMakkah:" + bearingToMakkah);

                binding.tvDeviceAngleQibla.setText(Math.round(direction) + "°");
                binding.ivCompass.setRotation(degree);
            }
        }
    }

    /**
     * 更新位置信息
     */
    private void updateLocation(Location location) {
        double longitude = location.getLongitude();
        double latitude = location.getLatitude();
        double altitude = location.getAltitude();

        currentLatitude = latitude;
        currentLongitude = longitude;

        LogUtils.i(TAG, "updateLocation currentLongitude：" + longitude + " currentLatitude：" + latitude);

        bearingToMakkah = calculateBearing(currentLatitude, currentLongitude, MOSQUE_LATITUDE, MOSQUE_LONGITUDE);
        LogUtils.i(TAG, "updateLocation 寺庙旋转角度：" + bearingToMakkah);

        if (mLocation == null) {
            return;
        }

        binding.ivCompass.setQiblaHouseAngle((float) (Math.round(bearingToMakkah - 90)));
        binding.tvQiblaAngle.setText(Math.round(bearingToMakkah) + "°");

    }

    private double calculateBearing(double startLat, double startLng, double endLat, double endLng) {
        double lat1Rad = Math.toRadians(startLat);
        double lon1Rad = Math.toRadians(startLng);
        double lat2Rad = Math.toRadians(endLat);
        double lon2Rad = Math.toRadians(endLng);

        double dLon = lon2Rad - lon1Rad;

        double y = Math.sin(dLon) * Math.cos(lat2Rad);
        double x = Math.cos(lat1Rad) * Math.sin(lat2Rad) -
                Math.sin(lat1Rad) * Math.cos(lat2Rad) * Math.cos(dLon);

        double bearingRad = Math.atan2(y, x);
        double bearingDeg = Math.toDegrees(bearingRad);

        // 将方位角转换到0-360范围内
        return (bearingDeg + 360) % 360;
    }

    /**
     * 适配android 6.0 检查权限
     */
    private boolean checkLocationPermission() {
        return (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        LogUtils.i(TAG, "onAccuracyChanged accuracy:" + accuracy);
    }

}
