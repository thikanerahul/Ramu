package com.example.ramu.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.example.ramu.service.VoiceListeningService;

public class RestartServiceReceiver extends BroadcastReceiver {
    
    private static final String TAG = "RestartServiceReceiver";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Service killed - Restarting Ramu Voice Service");
        
        // Restart the voice listening service
        Intent serviceIntent = new Intent(context, VoiceListeningService.class);
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent);
        } else {
            context.startService(serviceIntent);
        }
        
        Log.i(TAG, "Ramu Voice Service restarted successfully");
    }
}
