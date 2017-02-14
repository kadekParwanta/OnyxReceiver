package com.cordova.ble;


import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.util.Log;

import com.google.gson.Gson;
import com.onyxbeacon.OnyxBeaconApplication;
import com.onyxbeacon.OnyxBeaconManager;
import com.onyxbeacon.listeners.OnyxBeaconsListener;
import com.onyxbeacon.listeners.OnyxTagsListener;
import com.onyxbeacon.rest.auth.util.AuthData;
import com.onyxbeacon.rest.auth.util.AuthenticationMode;
import com.onyxbeacon.rest.model.content.Tag;
import com.onyxbeacon.service.logging.LoggingStrategy;
import com.onyxbeaconservice.Beacon;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;


public class Ble extends CordovaPlugin implements BleStateListener {

    private static final String TAG = "ONYXBLE";
    private static final String ACCESS_FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final int REQUEST_INIT_SDK = 0;

    public static final String ACTION_ADD_ONYX_BEACONS_LISTENER = "addOnyxBeaconsListener";
    public static final String ACTION_ADD_WEB_LISTENER = "addWebListener";
    public static final String ACTION_ADD_TAGS_LISTENER = "addTagsListener";

    private CallbackContext messageChannel;
    // OnyxBeacon SDK
    private OnyxBeaconManager beaconManager;
    private String CONTENT_INTENT_FILTER;
    private String BLE_INTENT_FILTER;
    private ContentReceiver mContentReceiver;
    private BleStateReceiver mBleReceiver;
    private boolean receiverRegistered = false;
    private boolean bleStateRegistered = false;
    private boolean sendfalse;
    private Ble instance;
    private Gson gson = new Gson();
    private Boolean isRequestingPermission = false;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {

        Log.v(TAG, "initialized BLE=" );

        super.initialize(cordova, webView);
        instance = this;
        beaconManager = OnyxBeaconApplication.getOnyxBeaconManager(cordova.getActivity());

        mContentReceiver = ContentReceiver.getInstance();
        //Register for BLE events
        mBleReceiver = BleStateReceiver.getInstance();
        mBleReceiver.setBleStateListener(this);

        BLE_INTENT_FILTER = cordova.getActivity().getPackageName() + ".scan";
        Log.d("Ble","initialize scan intent filter = " + BLE_INTENT_FILTER);
        cordova.getActivity().registerReceiver(mBleReceiver, new IntentFilter(BLE_INTENT_FILTER));
        bleStateRegistered = true;

        CONTENT_INTENT_FILTER = cordova.getActivity().getPackageName() + ".content";
        Log.d("Ble","initialize content intent filter = " + CONTENT_INTENT_FILTER);
        cordova.getActivity().registerReceiver(mContentReceiver, new IntentFilter(CONTENT_INTENT_FILTER));
        receiverRegistered = true;
    }
    /**
     * Executes the request and returns PluginResult.
     *
     * @param action          The action to execute.
     * @param args            JSONArry of arguments for the plugin.
     * @param callbackContext The callback id used when calling back into JavaScript.
     * @return True if the action was valid, false if not.
     */
    @Override
    public boolean execute(final String action, final JSONArray args, final CallbackContext callbackContext)   {
        messageChannel = callbackContext;
        Log.v(TAG, "action=" + action);
        try {
            if (action.equals("initSDK")) {
                enableLocation(args.getString(0), args.getString(1));
            } else if (action.equalsIgnoreCase(ACTION_ADD_ONYX_BEACONS_LISTENER)) {
                addOnyxBeaconsListener(callbackContext);
            } else if (action.equalsIgnoreCase(ACTION_ADD_WEB_LISTENER)) {
                addWebListener(callbackContext);
            } else if (action.equalsIgnoreCase(ACTION_ADD_TAGS_LISTENER)) {
                addTagsListener(callbackContext);
            } else if (action.equals("getTags")) {
                beaconManager.getTags();
                callbackContext.success("Success");
            } else if (action.equals("sendGenericUserProfile")) {
                beaconManager.sendGenericUserProfile(jsonToMap(args.getJSONObject(0)));
                callbackContext.success("Success");
            } else if (action.equals("setForegroundMode")) {
                beaconManager.setForegroundMode(args.getBoolean(0));
                callbackContext.success("Success");
            } else if (action.equals("setBackgroundBetweenScanPeriod")) {
                beaconManager.setBackgroundBetweenScanPeriod(args.getLong(0));
                callbackContext.success("Success");
            } else if (action.equals("startScan")) {
                beaconManager.startScan();
                callbackContext.success("Success");
            } else if (action.equals("stopScan")) {
                beaconManager.stopScan();
                callbackContext.success("Success");
            } else if (action.equals("isInForeground")) {
                Boolean isInForeground = beaconManager.isInForeground();
                callbackContext.success(isInForeground ? 1 : 0);
            } else if (action.equals("setCouponEnabled")) {
                beaconManager.setCouponEnabled(args.getBoolean(0));
                callbackContext.success("Success");
            } else if (action.equals("setLocationTrackingEnabled")) {
                beaconManager.setLocationTrackingEnabled(args.getBoolean(0));
                callbackContext.success("Success");
            } else if (action.equals("logOut")) {
                beaconManager.logOut();
                callbackContext.success("Success");
            } else if (action.equals("restartLocationTracking")) {
                beaconManager.restartLocationTracking();
                callbackContext.success("Success");
            } else if (action.equals("enableGeofencing")) {
                beaconManager.enableGeofencing(args.getBoolean(0));
                callbackContext.success("Success");
            } else if (action.equals("setAPIContentEnabled")) {
                beaconManager.setAPIContentEnabled(args.getBoolean(0));
                callbackContext.success("Success");
            } else if (action.equals("sendDeviceToken")) {
                beaconManager.sendDeviceToken(args.getString(0), args.getString(1));
                callbackContext.success("Success");
            } else if (action.equals("onPause")) {
                beaconManager.setForegroundMode(false);
                // Unregister content receiver
                if (bleStateRegistered) {
                    cordova.getActivity().unregisterReceiver(mBleReceiver);
                    bleStateRegistered = false;
                }

                if (receiverRegistered){
                    cordova.getActivity().unregisterReceiver(mContentReceiver);
                    receiverRegistered = false;
                }

                callbackContext.success("Success");
            } else if (action.equals("onResume")) {
                if (mBleReceiver == null) mBleReceiver = BleStateReceiver.getInstance();
                cordova.getActivity().registerReceiver(mContentReceiver, new IntentFilter(CONTENT_INTENT_FILTER));
                receiverRegistered = true;
                mBleReceiver.setBleStateListener(instance);

                if (mContentReceiver == null)
                    mContentReceiver = ContentReceiver.getInstance();
                cordova.getActivity().registerReceiver(mContentReceiver, new IntentFilter(CONTENT_INTENT_FILTER));
                receiverRegistered = true;
                if (BluetoothAdapter.getDefaultAdapter() == null) {
                    Log.e(TAG, "Device does not support Bluetooth");
                } else {
                    if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
                        Log.e(TAG, "Please turn on bluetooth");
                    } else {
                        // Enable scanner in foreground mode and register receiver
                        beaconManager = OnyxBeaconApplication.getOnyxBeaconManager(cordova.getActivity());
                        beaconManager.setForegroundMode(true);
                    }
                }
                callbackContext.success("Success");
            } else if (action.equals("setTagsFilterForCoupons")) {


                ArrayList<Tag> tagsFilter = new ArrayList<Tag>();

                for (int i = 0; i < args.length(); i++) {

                    JSONArray arg = args.getJSONArray(i);
                    Tag rtag = new Tag();
                    rtag.id = arg.getInt(0);
                    rtag.name = arg.getString(1);
                    rtag.state = arg.getString(2);

                    tagsFilter.add(rtag);
                }
                beaconManager.setTagsFilterForCoupons(tagsFilter);
                callbackContext.success("Success");
            }
            else if (action.equals("sendReport")) {
                String reporter = args.getString(0);
                beaconManager.sendLogs(reporter);
                callbackContext.success("sendReport Invoked");
            } else {
                Log.e("Ble","Unknown action " + action);
                sendfalse = true;
                callbackContext.error(action);
            }
        } catch (JSONException e) {
            callbackContext.success(e.getMessage());
            Log.e("Ble","Unknown JSONException " + e.getMessage());

            sendfalse = true;
        }

        return !sendfalse;
    }

    private void addTagsListener(final CallbackContext callbackContext) {
        OnyxTagsListener mOnyxTagsListener = new OnyxTagsListener() {
            @Override
            public void onTagsReceived(List<Tag> list) {
                PluginResult result = new PluginResult(PluginResult.Status.OK, gson.toJson(list));
                result.setKeepCallback(true);
                callbackContext.sendPluginResult(result);
            }
        };

        if (mContentReceiver != null) {
            mContentReceiver.setOnyxTagsListener(mOnyxTagsListener);
        } else {
            PluginResult result = new PluginResult(PluginResult.Status.ERROR, "Failed to add listener");
            result.setKeepCallback(true);
            callbackContext.sendPluginResult(result);
        }
    }

    private void addWebListener(final CallbackContext callbackContext) {
        BleWebRequestListener mBleWebRequestListener = new BleWebRequestListener() {
            @Override
            public void onRequested(String info) {
                PluginResult result = new PluginResult(PluginResult.Status.OK, info);
                result.setKeepCallback(true);
                callbackContext.sendPluginResult(result);
            }
        };

        if (mContentReceiver != null) {
            mContentReceiver.setBlewWebRequestListener(mBleWebRequestListener);
        } else {
            PluginResult result = new PluginResult(PluginResult.Status.ERROR, "Failed to add listener");
            result.setKeepCallback(true);
            callbackContext.sendPluginResult(result);
        }
    }

    private void addOnyxBeaconsListener(final CallbackContext callbackContext) {
        OnyxBeaconsListener mOnyxBeaconsListener = new OnyxBeaconsListener() {
            @Override
            public void didRangeBeaconsInRegion(List<Beacon> list) {
                PluginResult result = new PluginResult(PluginResult.Status.OK, gson.toJson(list));
                result.setKeepCallback(true);
                callbackContext.sendPluginResult(result);
            }
        };

        if (mContentReceiver != null) {
            mContentReceiver.setOnyxBeaconsListener(mOnyxBeaconsListener);
        } else {
            PluginResult result = new PluginResult(PluginResult.Status.ERROR, "Failed to add listener");
            result.setKeepCallback(true);
            callbackContext.sendPluginResult(result);
        }
    }

    private static HashMap<String, Object> jsonToMap(JSONObject json) throws JSONException {
        HashMap<String, Object> retMap = new HashMap<String, Object>();

        if(json != JSONObject.NULL) {
            retMap = toMap(json);
        }
        return retMap;
    }

    private static HashMap<String, Object> toMap(JSONObject object) throws JSONException {
        HashMap<String, Object> map = new HashMap<String, Object>();

        Iterator<String> keysItr = object.keys();
        while(keysItr.hasNext()) {
            String key = keysItr.next();
            Object value = object.get(key);

            if(value instanceof JSONArray) {
                value = toList((JSONArray) value);
            }

            else if(value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }
            map.put(key, value);
        }
        return map;
    }

    private static List<Object> toList(JSONArray array) throws JSONException {
        List<Object> list = new ArrayList<Object>();
        for(int i = 0; i < array.length(); i++) {
            Object value = array.get(i);
            if(value instanceof JSONArray) {
                value = toList((JSONArray) value);
            }

            else if(value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }
            list.add(value);
        }
        return list;
    }

    private void enableLocation(String clientId, String secret) {
        if (hasPermission(ACCESS_FINE_LOCATION)) {
            initSDK(clientId, secret);
        } else {
            String[] permissions = {ACCESS_FINE_LOCATION};
            requestPermissions(REQUEST_INIT_SDK, permissions);
        }
    }

    private void initSDK(String clientId, String secret) {
        beaconManager.setDebugMode(LoggingStrategy.DEBUG);
        beaconManager.setAPIEndpoint("https://connect.onyxbeacon.com");
        beaconManager.setCouponEnabled(true);
        beaconManager.setAPIContentEnabled(true);
        beaconManager.enableGeofencing(true);
        beaconManager.setLocationTrackingEnabled(true);
        if (preferences.contains("com-cordova-ble-clientId")) {
            clientId = preferences.getString("com-cordova-ble-clientId", "");
        }
        if (preferences.contains("com-cordova-ble-secret")) {
            secret = preferences.getString("com-cordova-ble-secret", "");
        }
        AuthData authData = new AuthData();
        authData.setAuthenticationMode(AuthenticationMode.CLIENT_SECRET_BASED);
        authData.setSecret(secret);
        authData.setClientId(clientId);
        beaconManager.setAuthData(authData);
        if (BluetoothAdapter.getDefaultAdapter() == null) {
            PluginResult result = new PluginResult(PluginResult.Status.ERROR,"Device does not support Bluetooth");
            result.setKeepCallback(true);
            messageChannel.sendPluginResult(result);
        } else {
            if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
                PluginResult result = new PluginResult(PluginResult.Status.ERROR,"Please turn on bluetooth");
                result.setKeepCallback(true);
                messageChannel.sendPluginResult(result);
            } else {
                // Enable scanner in foreground mode and register receiver
                beaconManager.setForegroundMode(true);
                PluginResult result = new PluginResult(PluginResult.Status.OK,"Success");
                result.setKeepCallback(true);
                messageChannel.sendPluginResult(result);
            }
        }
    }

    private boolean hasPermission(String action) {
        try {
            Method methodToFind = cordova.getClass().getMethod("hasPermission", String.class);
            if (methodToFind != null) {
                try {
                    return (Boolean) methodToFind.invoke(cordova, action);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        } catch(NoSuchMethodException e) {
            // Probably SDK < 23 (MARSHMALLOW implmements fine-grained, user-controlled permissions).
            return true;
        }
        return true;
    }

    private void requestPermissions(int requestCode, String[] action) {
        try {
            Method methodToFind = cordova.getClass().getMethod("requestPermissions", CordovaPlugin.class, int.class, String[].class);
            if (methodToFind != null) {
                try {
                    methodToFind.invoke(cordova, this, requestCode, action);
                    isRequestingPermission = true;
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        } catch(NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) throws JSONException {
        isRequestingPermission = false;
        for (int r: grantResults) {
            if (r == PackageManager.PERMISSION_DENIED) {
                if (requestCode == REQUEST_INIT_SDK) {
                    if (messageChannel != null) {
                        messageChannel.error(requestCode);
                    }
                }

                return;
            }
        }

        switch (requestCode) {
            case REQUEST_INIT_SDK:
                initSDK("","");
                break;
        }
    }

    private void onError(int errorCode, Exception e) {
        String js = String.format(
                "window.cordova.plugins.Ble.onyxBeaconError('%d:%s');",
                errorCode,e.getMessage());
        webView.loadUrl("javascript:"+js);
    }


    @Override
    public void onBleStackEvent(int event) {
        System.out.println(event);
        switch (event) {
            case 1:
                onError(event,new Exception("Probably your bluetooth stack has crashed. Please restart your bluetooth"));
                break;
            case 2:
                onError(event, new Exception("Beacons with invalid RSSI detected. Please restart your bluetooth."));
                break;

            default:onError(event, new Exception("This Error is unknown"));
                break;

        }
    }

    @Override
    public void onResume(boolean multitasking) {
        super.onResume(multitasking);
        if (mBleReceiver == null) mBleReceiver = BleStateReceiver.getInstance();
        cordova.getActivity().registerReceiver(mContentReceiver, new IntentFilter(CONTENT_INTENT_FILTER));
        receiverRegistered = true;
        mBleReceiver.setBleStateListener(instance);

        if (mContentReceiver == null)
            mContentReceiver = ContentReceiver.getInstance();
        cordova.getActivity().registerReceiver(mContentReceiver, new IntentFilter(CONTENT_INTENT_FILTER));
        receiverRegistered = true;
        if (BluetoothAdapter.getDefaultAdapter() == null) {
            Log.e(TAG, "Device does not support Bluetooth");
        } else {
            if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
                Log.e(TAG, "Please turn on bluetooth");
            } else {
                // Enable scanner in foreground mode and register receiver
                beaconManager = OnyxBeaconApplication.getOnyxBeaconManager(cordova.getActivity());
                beaconManager.setForegroundMode(true);
            }
        }
    }

    @Override
    public void onPause(boolean multitasking) {
        super.onPause(multitasking);
        beaconManager.setForegroundMode(false);
        // Unregister content receiver
        if (bleStateRegistered) {
            cordova.getActivity().unregisterReceiver(mBleReceiver);
            bleStateRegistered = false;
        }

        if (receiverRegistered){
            cordova.getActivity().unregisterReceiver(mContentReceiver);
            receiverRegistered = false;
        }
    }
}


