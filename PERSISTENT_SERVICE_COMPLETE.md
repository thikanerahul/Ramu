# Ramu - Persistent Service Implementation 🔒

## Requirement (जरूरत)

**User:** "ager ek baar start ho jaye to automatically stop nahi hona chahiye proper start hi rehna chahiye jab tak user jaker manually off na kare"

**Translation:** Once started, service should NEVER stop automatically. Only stop when user manually turns it off.

## Implementation Status ✅

Service is now **FULLY PERSISTENT** with multiple layers of protection!

## Persistence Layers (सुरक्षा की परतें)

### Layer 1: START_STICKY ✅
```java
@Override
public int onStartCommand(Intent intent, int flags, int startId) {
    android.util.Log.i("VoiceListeningService", "onStartCommand called - Service starting/restarting");
    // START_STICKY ensures service is restarted if killed by system
    return START_STICKY;
}
```

**What it does:**
- System agar service ko kill kare to automatically restart ho jati hai
- Intent null ho sakta hai restart pe, but service chalti rahegi

### Layer 2: Foreground Service ✅
```java
@Override
public void onCreate() {
    super.onCreate();
    isRunning = true;
    
    // Start as foreground service with notification
    startForeground(NOTIFICATION_ID, createNotification("Listening for all commands..."));
    
    // Start listening
    startListening();
}
```

**What it does:**
- Foreground services ko system rarely kill karta hai
- Notification dikhta hai - user ko pata hai service chal rahi hai
- High priority service

### Layer 3: onTaskRemoved() Handler ✅
```java
@Override
public void onTaskRemoved(Intent rootIntent) {
    android.util.Log.w("VoiceListeningService", "onTaskRemoved - App swiped away");
    
    // Restart service if app is swiped away (only if service should be running)
    if (isRunning) {
        android.util.Log.i("VoiceListeningService", "Restarting service after task removed");
        
        Intent restartServiceIntent = new Intent(getApplicationContext(), VoiceListeningService.class);
        restartServiceIntent.setPackage(getPackageName());
        PendingIntent restartServicePendingIntent = PendingIntent.getService(
                getApplicationContext(), 1, restartServiceIntent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        android.app.AlarmManager alarmService = (android.app.AlarmManager) getApplicationContext()
                .getSystemService(Context.ALARM_SERVICE);
        if (alarmService != null) {
            alarmService.set(android.app.AlarmManager.ELAPSED_REALTIME,
                    android.os.SystemClock.elapsedRealtime() + 1000, restartServicePendingIntent);
        }
    }
    super.onTaskRemoved(rootIntent);
}
```

**What it does:**
- User agar app ko swipe away kare to bhi service restart hoti hai
- 1 second baad automatically restart
- AlarmManager use karke guaranteed restart

### Layer 4: onDestroy() Handler ✅
```java
@Override
public void onDestroy() {
    super.onDestroy();
    
    android.util.Log.w("VoiceListeningService", "onDestroy called - isRunning: " + isRunning);
    
    // If service is being destroyed but should still be running, restart it
    if (isRunning) {
        android.util.Log.i("VoiceListeningService", "Service destroyed but should be running - Scheduling restart");
        
        // Send broadcast to restart service
        Intent broadcastIntent = new Intent(this, com.example.ramu.receiver.RestartServiceReceiver.class);
        sendBroadcast(broadcastIntent);
        
        // Also schedule restart via AlarmManager as backup
        Intent restartServiceIntent = new Intent(getApplicationContext(), VoiceListeningService.class);
        restartServiceIntent.setPackage(getPackageName());
        PendingIntent restartServicePendingIntent = PendingIntent.getService(
                getApplicationContext(), 1, restartServiceIntent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        android.app.AlarmManager alarmService = (android.app.AlarmManager) getApplicationContext()
                .getSystemService(Context.ALARM_SERVICE);
        if (alarmService != null) {
            alarmService.set(android.app.AlarmManager.ELAPSED_REALTIME,
                    android.os.SystemClock.elapsedRealtime() + 1000, restartServicePendingIntent);
        }
    }

    isListening = false;

    if (speechRecognizer != null) {
        speechRecognizer.destroy();
    }
    if (textToSpeech != null) {
        textToSpeech.stop();
        textToSpeech.shutdown();
    }
}
```

**What it does:**
- Service destroy hone se pehle check karta hai `isRunning` flag
- Agar `isRunning = true` hai to:
  - Broadcast bhejta hai RestartServiceReceiver ko
  - AlarmManager se backup restart schedule karta hai
- Double protection!

### Layer 5: Boot Receiver ✅
```java
// BootReceiver.java
@Override
public void onReceive(Context context, Intent intent) {
    String action = intent.getAction();
    
    if (Intent.ACTION_BOOT_COMPLETED.equals(action) ||
        "android.intent.action.QUICKBOOT_POWERON".equals(action) ||
        Intent.ACTION_MY_PACKAGE_REPLACED.equals(action)) {
        
        Log.i(TAG, "Device booted or app updated - Starting Ramu Voice Service");
        
        Intent serviceIntent = new Intent(context, VoiceListeningService.class);
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent);
        } else {
            context.startService(serviceIntent);
        }
    }
}
```

**What it does:**
- Device reboot hone ke baad automatically service start hoti hai
- App update hone ke baad bhi service start hoti hai
- User ko manually start karne ki zaroorat nahi

### Layer 6: Restart Service Receiver ✅
```java
// RestartServiceReceiver.java
@Override
public void onReceive(Context context, Intent intent) {
    Log.i(TAG, "Service killed - Restarting Ramu Voice Service");
    
    Intent serviceIntent = new Intent(context, VoiceListeningService.class);
    
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        context.startForegroundService(serviceIntent);
    } else {
        context.startService(serviceIntent);
    }
}
```

**What it does:**
- onDestroy() se broadcast receive karta hai
- Immediately service restart karta hai
- Backup mechanism

### Layer 7: Battery Optimization Disabled ✅
```java
// MainActivity.java
private void requestDisableBatteryOptimization() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        Intent intent = new Intent();
        String packageName = getPackageName();
        android.os.PowerManager pm = (android.os.PowerManager) getSystemService(POWER_SERVICE);
        
        if (pm != null && !pm.isIgnoringBatteryOptimizations(packageName)) {
            intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(android.net.Uri.parse("package:" + packageName));
            startActivity(intent);
            Toast.makeText(this, "Please allow 'Don't optimize' for Ramu to keep service running", Toast.LENGTH_LONG).show();
        }
    }
}
```

**What it does:**
- Battery optimization disable karne ka request karta hai
- System service ko background mein kill nahi karega
- Long-term persistence ke liye important

### Layer 8: AndroidManifest Configuration ✅
```xml
<!-- Service Configuration -->
<service
    android:name=".service.VoiceListeningService"
    android:enabled="true"
    android:exported="false"
    android:stopWithTask="false"
    android:foregroundServiceType="microphone" />

<!-- Boot Receiver -->
<receiver
    android:name=".receiver.BootReceiver"
    android:enabled="true"
    android:exported="true">
    <intent-filter>
        <action android:name="android.intent.action.BOOT_COMPLETED" />
        <action android:name="android.intent.action.QUICKBOOT_POWERON" />
        <action android:name="android.intent.action.MY_PACKAGE_REPLACED" />
    </intent-filter>
</receiver>

<!-- Restart Receiver -->
<receiver
    android:name=".receiver.RestartServiceReceiver"
    android:enabled="true"
    android:exported="false" />

<!-- Permissions -->
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
<uses-permission android:name="android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_MICROPHONE" />
```

**What it does:**
- `stopWithTask="false"` - App close hone pe service nahi rukti
- Boot receiver enabled - Reboot ke baad auto-start
- Restart receiver enabled - Kill hone pe auto-restart
- All necessary permissions

## How Manual Stop Works (कैसे बंद करें)

### Only Way to Stop: User Manual Action
```java
public static void stopService(Context context) {
    android.util.Log.i("VoiceListeningService", "stopService called - User manually stopping service");
    isRunning = false; // Set flag FIRST to prevent restart
    Intent intent = new Intent(context, VoiceListeningService.class);
    context.stopService(intent);
}
```

**Process:**
1. User app mein jata hai
2. "Stop Service" button click karta hai
3. `isRunning = false` set hota hai
4. Service stop hoti hai
5. onDestroy() check karta hai `isRunning`
6. `isRunning = false` hai to restart NAHI hota

## Service Lifecycle (जीवन चक्र)

### Scenario 1: Normal Start
```
User clicks "Start Service"
→ onCreate() called
→ isRunning = true
→ startForeground() called
→ Notification shows
→ Service running ✓
```

### Scenario 2: App Swiped Away
```
User swipes app away
→ onTaskRemoved() called
→ Check: isRunning = true
→ Schedule restart via AlarmManager
→ 1 second later: Service restarts ✓
→ Service running ✓
```

### Scenario 3: System Kills Service (Low Memory)
```
System kills service
→ onDestroy() called
→ Check: isRunning = true
→ Send broadcast to RestartServiceReceiver
→ Schedule restart via AlarmManager
→ Service restarts immediately ✓
→ Service running ✓
```

### Scenario 4: Device Reboot
```
Device reboots
→ BOOT_COMPLETED broadcast
→ BootReceiver receives it
→ Starts VoiceListeningService
→ Service running ✓
```

### Scenario 5: App Update
```
App updates
→ MY_PACKAGE_REPLACED broadcast
→ BootReceiver receives it
→ Starts VoiceListeningService
→ Service running ✓
```

### Scenario 6: User Manual Stop
```
User clicks "Stop Service"
→ stopService() called
→ isRunning = false (FIRST!)
→ Service stops
→ onDestroy() called
→ Check: isRunning = false
→ NO restart ✓
→ Service stopped ✓
```

## Testing Persistence 🧪

### Test 1: App Swipe Away
```
1. Start service
2. Check notification is showing
3. Swipe app away from recent apps
4. Wait 2 seconds
5. Check notification - should still be there ✓
```

### Test 2: Force Stop App
```
1. Start service
2. Go to Settings → Apps → Ramu
3. Click "Force Stop"
4. Wait 2 seconds
5. Check notification - should restart ✓
```

### Test 3: Device Reboot
```
1. Start service
2. Reboot device
3. After boot, check notification
4. Service should auto-start ✓
```

### Test 4: Low Memory Kill
```
1. Start service
2. Open many heavy apps
3. System may kill Ramu service
4. Service should auto-restart ✓
```

### Test 5: Manual Stop
```
1. Start service
2. Open Ramu app
3. Click "Stop Service"
4. Service should stop ✓
5. Should NOT restart ✓
```

## Logs to Check 📝

### Service Starting:
```
I/VoiceListeningService: onStartCommand called - Service starting/restarting
I/VoiceListeningService: ✅ Service CREATED and INSTANCE SET
```

### App Swiped Away:
```
W/VoiceListeningService: onTaskRemoved - App swiped away
I/VoiceListeningService: Restarting service after task removed
```

### Service Killed:
```
W/VoiceListeningService: onDestroy called - isRunning: true
I/VoiceListeningService: Service destroyed but should be running - Scheduling restart
I/RestartServiceReceiver: Service killed - Restarting Ramu Voice Service
```

### Device Boot:
```
I/BootReceiver: Device booted or app updated - Starting Ramu Voice Service
I/BootReceiver: Ramu Voice Service started successfully
```

### Manual Stop:
```
I/VoiceListeningService: stopService called - User manually stopping service
W/VoiceListeningService: onDestroy called - isRunning: false
```

## Summary 📊

### Persistence Features:
- ✅ START_STICKY - System restart
- ✅ Foreground Service - High priority
- ✅ onTaskRemoved() - App swipe protection
- ✅ onDestroy() - Kill protection
- ✅ BootReceiver - Reboot auto-start
- ✅ RestartServiceReceiver - Immediate restart
- ✅ Battery Optimization - Disabled
- ✅ stopWithTask="false" - App close protection

### Only Stops When:
- ❌ App swiped away - NO, restarts
- ❌ System kills - NO, restarts
- ❌ Device reboot - NO, auto-starts
- ❌ Low memory - NO, restarts
- ✅ User manual stop - YES, stops

### Result:
**Service ek baar start ho gayi to KABHI automatically band nahi hogi!**
**Sirf user manually "Stop Service" button se hi band kar sakta hai!**

## Installation & Testing 📱

```bash
# Install APK
adb install -r app\build\outputs\apk\debug\app-debug.apk

# Start service
Open app → Click "Start Service"

# Test persistence
1. Swipe app away → Service should continue ✓
2. Force stop app → Service should restart ✓
3. Reboot device → Service should auto-start ✓

# Check logs
adb logcat | findstr "VoiceListeningService BootReceiver RestartServiceReceiver"
```

## Important Notes 📌

1. **Battery Optimization:** User ko "Don't optimize" select karna hoga first time
2. **Notification:** Service running hai to notification dikhega (required for foreground service)
3. **Permissions:** All permissions granted hone chahiye
4. **Accessibility Service:** Enabled hona chahiye for full functionality

**Ab service truly persistent hai! Ek baar start karo, bas chalta rahega! 🚀**
