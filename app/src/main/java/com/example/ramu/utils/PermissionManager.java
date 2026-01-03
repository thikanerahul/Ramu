package com.example.ramu.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;

import androidx.core.content.ContextCompat;

import com.example.ramu.service.RamuAccessibilityService;

public class PermissionManager {
    
    public static boolean hasAudioPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, 
            Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }
    
    public static boolean hasContactsPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, 
            Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED;
    }
    
    public static boolean hasPhonePermission(Context context) {
        boolean hasCallPermission = ContextCompat.checkSelfPermission(context, 
            Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED;
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            boolean hasBluetoothPermission = ContextCompat.checkSelfPermission(context, 
                Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED;
            return hasCallPermission && hasBluetoothPermission;
        }
        
        return hasCallPermission;
    }
    
    public static boolean isAccessibilityServiceEnabled(Context context) {
        String serviceName = context.getPackageName() + "/" + 
            RamuAccessibilityService.class.getCanonicalName();
        
        String enabledServices = Settings.Secure.getString(
            context.getContentResolver(),
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        
        if (TextUtils.isEmpty(enabledServices)) {
            return false;
        }
        
        TextUtils.SimpleStringSplitter splitter = new TextUtils.SimpleStringSplitter(':');
        splitter.setString(enabledServices);
        
        while (splitter.hasNext()) {
            String service = splitter.next();
            if (service.equalsIgnoreCase(serviceName)) {
                return true;
            }
        }
        
        return false;
    }
    
    public static boolean hasAllPermissions(Context context) {
        return hasAudioPermission(context) &&
               hasContactsPermission(context) &&
               hasPhonePermission(context) &&
               isAccessibilityServiceEnabled(context);
    }
}
