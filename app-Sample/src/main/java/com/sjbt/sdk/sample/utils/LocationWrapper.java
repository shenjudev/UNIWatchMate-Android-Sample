package com.sjbt.sdk.sample.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;

import com.blankj.utilcode.util.LogUtils;

import java.util.List;

public class LocationWrapper {
    private volatile static LocationWrapper uniqueInstance;
    String TAG = "FLY.LocationWrapper";
    private LocationManager mLocationManager;
    private String locationProvider;
    private Location location;
    private Context mContext;
    private boolean gpsStatus;
    private static final double EARTH_RADIUS = 6371000; // 地球半径（单位：千米）

    private LocationChangeListener locationChangeListener;

    public boolean isGpsStatus() {
        gpsStatus = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
//        LogUtilss.logBlueTooth("GPS是否开启:" + gpsStatus);
        return gpsStatus;
    }

    public void setLocationChangeListener(LocationChangeListener locationChangeListener) {
        this.locationChangeListener = locationChangeListener;
    }

    LocationListener locationListener = new LocationListener() {
        /**
         * 当某个位置提供者的状态发生改变时
         */
        @Override
        public void onStatusChanged(String provider, int status, Bundle arg2) {

        }

        /**
         * 某个设备打开时
         */
        @Override
        public void onProviderEnabled(String provider) {

            if (locationChangeListener != null) {
                locationChangeListener.onProviderEnabled(provider);
            }
        }

        /**
         * 某个设备关闭时
         */
        @Override
        public void onProviderDisabled(String provider) {
            if (locationChangeListener != null) {
                locationChangeListener.onProviderDisabled(provider);
            }
        }

        /**
         * 手机位置发生变动
         */
        @Override
        public void onLocationChanged(Location location) {
            updateLocation(location);
        }
    };

    private LocationWrapper(Context context) {
        mContext = context;
        requestLocation();
    }

    //采用Double CheckLock(DCL)实现单例
    public static LocationWrapper getInstance(Context context) {
        if (uniqueInstance == null) {
            synchronized (LocationWrapper.class) {
                if (uniqueInstance == null) {
                    uniqueInstance = new LocationWrapper(context);
                }
            }
        }
        return uniqueInstance;
    }

    public void requestLocation() {
        //1.获取位置管理器
        mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        gpsStatus = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
//        Criteria criteria = new Criteria();
//        // 设置定位精确度 Criteria.ACCURACY_COARSE比较粗略，Criteria.ACCURACY_FINE则比较精细
//        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
//        // 设置是否要求速度
//        criteria.setSpeedRequired(false);
//        // 设置是否需要海拔信息
//        criteria.setAltitudeRequired(false);
//        // 设置是否需要方位信息
//        criteria.setBearingRequired(false);
//        // 设置是否允许运营商收费
//        criteria.setCostAllowed(true);
//        // 设置对电源的需求
//        criteria.setPowerRequirement(Criteria.POWER_LOW); // 低功耗
//
//        // 为获取地理位置信息时设置查询条件
//        locationProvider = locationManager.getBestProvider(criteria, true);

        //2.获取位置提供器，GPS或是NetWork
        List<String> providers = mLocationManager.getProviders(true);
        if (providers.contains(LocationManager.NETWORK_PROVIDER)) {
            //如果是网络定位
            locationProvider = LocationManager.NETWORK_PROVIDER;
        } else if (providers.contains(LocationManager.GPS_PROVIDER)) {
            //如果是GPS定位
            locationProvider = LocationManager.GPS_PROVIDER;
        } else {
            return;
        }

        if (ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        LogUtils.i(TAG, "位置PROVIDER:" + locationProvider);
        //3.获取上次的位置，一般第一次运行，此值为null
        Location location = mLocationManager.getLastKnownLocation(locationProvider);
        if (location != null) {
            LogUtils.i("location:" + location.getLongitude() + "-" + location.getLatitude());
            updateLocation(location);
        }
        // 监视地理位置变化，第二个和第三个参数分别为更新的最短时间minTime和最短距离minDistace
        mLocationManager.requestLocationUpdates(locationProvider, 1, 1, locationListener);
    }

    private void updateLocation(Location location) {
        this.location = location;
        gpsStatus = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        double oldLatitude = CacheDataHelper.INSTANCE.getLatitude();
        double oldLongitude = CacheDataHelper.INSTANCE.getLongitude();

        double newLatitude = location.getLatitude();
        double newLongitude = location.getLongitude();

        if (oldLatitude == 0 || oldLongitude == 0) {
            oldLatitude = newLatitude;
            oldLongitude = newLongitude;

            LogUtils.i(TAG, "没有获取过经纬度");
            CacheDataHelper.INSTANCE.setLatitude(newLatitude);
            CacheDataHelper.INSTANCE.setLongitude(newLongitude);
        }

        double distance = calculateDistance(oldLatitude, oldLongitude, newLatitude, newLongitude);

        LogUtils.i(TAG, "位置信息更新 oldLatitude:" + oldLatitude + " oldLongitude:" + " newLatitude:" +
                newLatitude + " newLongitude:" + newLongitude +" distance:"+ distance);

        if (locationChangeListener != null && distance > 50) {
            locationChangeListener.onLocationChanged(location);
        }
    }

    //获取经纬度
    public Location getLocation() {
        requestLocation();
        return location;
    }

    // 移除定位监听
    public void removeLocationUpdatesListener() {
        // 需要检查权限,否则编译不过
        if (Build.VERSION.SDK_INT >= 23 &&
                ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (mLocationManager != null) {
            uniqueInstance = null;
            mLocationManager.removeUpdates(locationListener);
        }
    }

    public double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        double distance = EARTH_RADIUS * c;

        return distance;
    }


    public interface LocationChangeListener {
        void onProviderEnabled(String provider);

        void onProviderDisabled(String provider);

        void onLocationChanged(Location location);
    }

}

