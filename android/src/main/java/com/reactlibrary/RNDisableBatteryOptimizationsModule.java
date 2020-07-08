package com.reactlibrary;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Promise;

import android.content.Intent;
import android.provider.Settings;
import android.os.PowerManager;
import android.net.Uri;
import android.os.Build;


public class RNDisableBatteryOptimizationsModule extends ReactContextBaseJavaModule implements ActivityEventListener {
  static final int REQUEST_REMOVAL_BATTERY_OPTIMIZATION = 1;

  private final ReactApplicationContext reactContext;
  private Promise promise;

  public RNDisableBatteryOptimizationsModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
    this.reactContext.addActivityEventListener(this);
  }

  @ReactMethod
  public void openBatteryModal() {
	  if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
		    String packageName = reactContext.getPackageName();
			Intent intent = new Intent();
			intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
			intent.setData(Uri.parse("package:" + packageName));
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			reactContext.startActivityForResult(intent);
    
	  }

  }

  @ReactMethod
  public void isBatteryOptimizationEnabled(Promise promise) {
    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        String packageName = reactContext.getPackageName();
        PowerManager pm = (PowerManager) reactContext.getSystemService(reactContext.POWER_SERVICE);
        if (!pm.isIgnoringBatteryOptimizations(packageName)) {
          promise.resolve(true);
          return ;
        }
    }
    promise.resolve(false);
  }

  @ReactMethod
  public void enableBackgroundServicesDialogue(Promise promise) {
    this.promise = promise;

    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      Intent myIntent = new Intent();
      String packageName =  reactContext.getPackageName();
      PowerManager pm = (PowerManager) reactContext.getSystemService(reactContext.POWER_SERVICE);
      
      if (pm.isIgnoringBatteryOptimizations(packageName)) {
        myIntent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
      } else {
        myIntent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
        myIntent.setData(Uri.parse("package:" + packageName));
      }
      
	    myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      reactContext.startActivityForResult(myIntent, REQUEST_REMOVAL_BATTERY_OPTIMIZATION, null);
    }
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    this.promise.resolve(data.getDataString());
  }

  @Override
  public String getName() {
    return "RNDisableBatteryOptimizationsAndroid";
  }
}