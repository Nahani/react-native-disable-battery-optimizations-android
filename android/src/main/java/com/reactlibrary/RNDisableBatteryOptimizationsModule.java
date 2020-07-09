package com.reactlibrary;

import android.app.Activity;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.BaseActivityEventListener;

import android.content.Intent;
import android.provider.Settings;
import android.os.PowerManager;
import android.net.Uri;
import android.os.Build;

public class RNDisableBatteryOptimizationsModule extends ReactContextBaseJavaModule {
  static final int REQUEST_REMOVAL_BATTERY_OPTIMIZATION = 1;

  private final ReactApplicationContext reactContext;
  private Promise promise;

  private final ActivityEventListener mActivityEventListener = new BaseActivityEventListener() {
    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent intent) {
      promise.resolve(resultCode);
    }
  };

  public RNDisableBatteryOptimizationsModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
    this.reactContext.addActivityEventListener(mActivityEventListener);
  }

  @ReactMethod
  public void openBatteryModal() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      String packageName = reactContext.getPackageName();
      Intent intent = new Intent();
      intent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
      intent.setData(Uri.parse("package:" + packageName));
      intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      reactContext.startActivity(intent);
    }
  }

  @ReactMethod
  public void isBatteryOptimizationEnabled(Promise promise) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      String packageName = reactContext.getPackageName();
      PowerManager pm = (PowerManager) reactContext.getSystemService(reactContext.POWER_SERVICE);
      if (!pm.isIgnoringBatteryOptimizations(packageName)) {
        promise.resolve(true);
        return;
      }
    }
    promise.resolve(false);
  }

  @ReactMethod
  public void enableBackgroundServicesDialogue(Promise promise) {
    this.promise = promise;

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      Intent myIntent = new Intent();
      String packageName = reactContext.getPackageName();
      PowerManager pm = (PowerManager) reactContext.getSystemService(reactContext.POWER_SERVICE);

      if (pm.isIgnoringBatteryOptimizations(packageName)) {
        myIntent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
      } else {
        myIntent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
        myIntent.setData(Uri.parse("package:" + packageName));
      }

      reactContext.startActivityForResult(myIntent, REQUEST_REMOVAL_BATTERY_OPTIMIZATION, null);
    }
  }

  @Override
  public String getName() {
    return "RNDisableBatteryOptimizationsAndroid";
  }
}
