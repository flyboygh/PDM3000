package com.sindia.pdm3000.util;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;

import androidx.core.app.ActivityCompat;
import androidx.core.app.AppOpsManagerCompat;

import java.lang.reflect.Method;

public class LocationUtil {

    // 手机是否开启位置服务，如果没有开启那么所有app将不能使用定位功能
    public static boolean isLocServiceEnable(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        return gps || network;
    }

    // 检查权限列表
    public static int checkOp(Context context, int op, String opString) {
        final int version = Build.VERSION.SDK_INT;
        if (version >= 19) {
            Object object = context.getSystemService(Context.APP_OPS_SERVICE);
//            Object object = context.getSystemService("appops");
            Class c = object.getClass();
            try {
                Class[] cArg = new Class[3];
                cArg[0] = int.class;
                cArg[1] = int.class;
                cArg[2] = String.class;
                Method lMethod = c.getDeclaredMethod("checkOp", cArg);
                return (Integer) lMethod.invoke(object, op, Binder.getCallingUid(), context.getPackageName());
            } catch (Exception e) {
                e.printStackTrace();
                if (Build.VERSION.SDK_INT >= 23) {
                    return AppOpsManagerCompat.noteOp(context, opString, context.getApplicationInfo().uid,
                            context.getPackageName());
                }

            }
        }
        return -1;
    }

    // 检查定位服务、权限
    public static boolean checkLocationPermission(Context context) {
        if (!isLocServiceEnable(context)) {//检测是否开启定位服务
            //未开启定位服务的操作
            return false;
        } else {//检测用户是否将当前应用的定位权限拒绝
            int checkResult = checkOp(context, 2, AppOpsManager.OPSTR_FINE_LOCATION);//其中2代表AppOpsManager.OP_GPS，如果要判断悬浮框权限，第二个参数需换成24即AppOpsManager。OP_SYSTEM_ALERT_WINDOW及，第三个参数需要换成AppOpsManager.OPSTR_SYSTEM_ALERT_WINDOW
            int checkResult2 = checkOp(context, 1, AppOpsManager.OPSTR_FINE_LOCATION);
            //未开启定位权限或者被拒绝的操作
            return AppOpsManagerCompat.MODE_IGNORED != checkResult && AppOpsManagerCompat.MODE_IGNORED != checkResult2;
        }
    }
/*
    // 获取应用详情页面intent（如果找不到要跳转的界面，也可以先把用户引导到系统设置页面）
    private Intent getAppDetailSettingIntent() {
        Intent localIntent = new Intent();
        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= 9) {
            localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
            localIntent.setData(Uri.fromParts("package", getPackageName(), null));
        } else if (Build.VERSION.SDK_INT <= 8) {
            localIntent.setAction(Intent.ACTION_VIEW);
            localIntent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
            localIntent.putExtra("com.android.settings.ApplicationPkgName", getPackageName());
        }
        return localIntent;
    }
*/
    // 请求定位服务、权限
    public static void requestLocationPermission(final Activity activity) {
        //请求权限
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_COARSE_LOCATION) || ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_FINE_LOCATION)) {
            ActivityCompat.requestPermissions( activity, new String[] {
                    Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        } else {
            //Toast.makeText(activity, "定位服务已禁止", Toast.LENGTH_SHORT).show();
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle("提示").setMessage("获取位置信息权限已被禁止").setPositiveButton("设置", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Intent intent =  new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
                    intent.setData(uri);
                    activity.startActivityForResult(intent, 1);//CODE_REQUEST);
                    //activity.startActivity(intent);
                }
            }).show();
        }
        /*ActivityCompat.requestPermissions(activity,
                new String[]{
                        Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.WRITE_CONTACTS, Manifest.permission.ACCESS_FINE_LOCATION},
                0);*/
    }

}