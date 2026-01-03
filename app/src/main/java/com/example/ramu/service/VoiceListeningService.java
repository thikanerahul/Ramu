package com.example.ramu.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;

import androidx.core.app.NotificationCompat;

import com.example.ramu.R;
import com.example.ramu.ui.MainActivity;
import com.example.ramu.utils.AppCacheManager;
import com.example.ramu.utils.CommandProcessor;

import java.util.ArrayList;
import java.util.Locale;

public class VoiceListeningService extends Service {

    public static boolean isRunning = false;
    private static final String CHANNEL_ID = "RamuVoiceChannel";
    private static final int NOTIFICATION_ID = 1;

    private SpeechRecognizer speechRecognizer;
    private Intent recognizerIntent;
    private TextToSpeech textToSpeech;
    private CommandProcessor commandProcessor;
    private boolean isListening = false;
    
    // WakeLock to keep service alive
    private PowerManager.WakeLock wakeLock;

    @Override
    public void onCreate() {
        super.onCreate();
        isRunning = true;
        
        android.util.Log.i("VoiceListeningService", "🚀 Service CREATED - Acquiring WakeLock");
        
        // Acquire WakeLock to prevent service from being killed
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Ramu::VoiceServiceWakeLock");
        wakeLock.acquire();
        android.util.Log.i("VoiceListeningService", "✅ WakeLock ACQUIRED - Service will stay alive");

        // Initialize TTS
        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.setLanguage(Locale.US);
            }
        });

        // Initialize Command Processor
        commandProcessor = new CommandProcessor(this, textToSpeech);

        // Initialize Speech Recognizer
        initializeSpeechRecognizer();

        // Initialize App Cache (Async)
        AppCacheManager.getInstance(this).refreshAppCache();

        // Create notification channel
        createNotificationChannel();

        // Start foreground
        startForeground(NOTIFICATION_ID, createNotification("🎤 Always Listening - Service Active"));

        // Start listening
        startListening();
        
        // Schedule JobScheduler for additional protection
        ServiceRestartJob.scheduleJob(this);
        
        android.util.Log.i("VoiceListeningService", "✅ Service FULLY INITIALIZED and RUNNING");
        android.util.Log.i("VoiceListeningService", "🛡️ JobScheduler monitoring enabled - Service will stay alive like Google Assistant!");
    }

    private void initializeSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-IN");
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en-IN");
        // Support multiple languages including Marathi
        recognizerIntent.putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", new String[] { "mr-IN", "hi-IN" });
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 10);
        // OPTIMIZED TIMEOUTS - Fast response but not too aggressive
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 1000L); // 1 second silence = done
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 800L); // 0.8 seconds
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 300L); // Minimum 0.3 seconds
        // Prefer offline for faster response
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, false);

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                updateNotification("Listening...");
            }

            @Override
            public void onBeginningOfSpeech() {
            }

            @Override
            public void onRmsChanged(float rmsdB) {
            }

            @Override
            public void onBufferReceived(byte[] buffer) {
            }

            @Override
            public void onEndOfSpeech() {
            }

            @Override
            public void onError(int error) {
                String errorMsg = getErrorText(error);
                android.util.Log.e("VoiceListeningService", "Speech error " + error + ": " + errorMsg);
                isListening = false;
                
                // Handle specific errors differently
                if (error == SpeechRecognizer.ERROR_NO_MATCH || 
                    error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT ||
                    error == SpeechRecognizer.ERROR_NO_MATCH) {
                    // These are normal - just restart quickly
                    android.util.Log.d("VoiceListeningService", "Normal error, restarting immediately");
                } else if (error == SpeechRecognizer.ERROR_RECOGNIZER_BUSY) {
                    // Wait a bit longer if recognizer is busy
                    android.util.Log.w("VoiceListeningService", "Recognizer busy, waiting before restart");
                    try { Thread.sleep(1000); } catch (InterruptedException e) {}
                }
                
                // Always restart for continuous listening
                restartListening();
            }
            
            private String getErrorText(int errorCode) {
                switch (errorCode) {
                    case SpeechRecognizer.ERROR_AUDIO: return "Audio recording error";
                    case SpeechRecognizer.ERROR_CLIENT: return "Client side error";
                    case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS: return "Insufficient permissions";
                    case SpeechRecognizer.ERROR_NETWORK: return "Network error";
                    case SpeechRecognizer.ERROR_NETWORK_TIMEOUT: return "Network timeout";
                    case SpeechRecognizer.ERROR_NO_MATCH: return "No speech match";
                    case SpeechRecognizer.ERROR_RECOGNIZER_BUSY: return "Recognition service busy";
                    case SpeechRecognizer.ERROR_SERVER: return "Server error";
                    case SpeechRecognizer.ERROR_SPEECH_TIMEOUT: return "No speech input";
                    default: return "Unknown error";
                }
            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(
                        SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    String command = matches.get(0);
                    processCommand(command);
                }
                isListening = false;
                restartListening();
            }

            @Override
            public void onPartialResults(Bundle partialResults) {
                // DISABLED: Partial results cause issues with incomplete words
                // Let final results handle everything for accuracy
                // Just log for debugging
                ArrayList<String> partialMatches = partialResults.getStringArrayList(
                        SpeechRecognizer.RESULTS_RECOGNITION);
                if (partialMatches != null && !partialMatches.isEmpty()) {
                    String partialCommand = partialMatches.get(0);
                    android.util.Log.d("VoiceListeningService", "Partial result (not processing): " + partialCommand);
                }
            }

            @Override
            public void onEvent(int eventType, Bundle params) {
            }
        });
    }

    private void startListening() {
        if (!isListening) {
            isListening = true;
            try {
                speechRecognizer.startListening(recognizerIntent);
            } catch (Exception e) {
                isListening = false;
                restartListening();
            }
        }
    }

    private void restartListening() {
        new android.os.Handler().postDelayed(() -> {
            if (isRunning && !isListening) {
                android.util.Log.d("VoiceListeningService", "Restarting listening...");
                startListening();
            }
        }, 500); // Reduced delay for faster restart
    }

    private void processCommand(String command) {
        android.util.Log.d("VoiceListeningService", "Command heard: " + command);
        
        // ALEXA-STYLE: Process ALL commands, wake word is optional
        // Just log if wake word is present or not
        String lowerCommand = command.toLowerCase();
        boolean hasWakeWord = lowerCommand.contains("ramu") || lowerCommand.contains("ram") || 
                             lowerCommand.contains("रामू") || lowerCommand.contains("राम");
        
        if (hasWakeWord) {
            android.util.Log.d("VoiceListeningService", "Wake word detected in: " + command);
        } else {
            android.util.Log.d("VoiceListeningService", "No wake word, but processing anyway: " + command);
        }
        
        updateNotification("Processing: " + command);
        commandProcessor.processCommand(command);
        
        // Update notification back to listening after 2 seconds
        new android.os.Handler().postDelayed(() -> {
            updateNotification("Listening for commands...");
        }, 2000);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Ramu Voice Service",
                    NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("Ramu Voice Assistant Service");
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private Notification createNotification(String text) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Ramu Voice Assistant")
                .setContentText(text)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setSilent(true) // Completely silent notifications
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    private void updateNotification(String text) {
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.notify(NOTIFICATION_ID, createNotification(text));
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        android.util.Log.i("VoiceListeningService", "⚡ onStartCommand called - Service starting/restarting");
        
        // Ensure WakeLock is acquired
        if (wakeLock != null && !wakeLock.isHeld()) {
            wakeLock.acquire();
            android.util.Log.i("VoiceListeningService", "✅ WakeLock re-acquired in onStartCommand");
        }
        
        // START_STICKY ensures service is ALWAYS restarted if killed by system
        // This is the MOST IMPORTANT line for keeping service alive!
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        android.util.Log.w("VoiceListeningService", "📱 onTaskRemoved - App swiped away BUT SERVICE MUST CONTINUE!");
        
        // CRITICAL: Service MUST continue even if app is swiped away
        if (isRunning) {
            android.util.Log.i("VoiceListeningService", "🔄 App removed but service MUST stay alive - Restarting");
            
            // Immediate restart
            Intent restartServiceIntent = new Intent(getApplicationContext(), VoiceListeningService.class);
            restartServiceIntent.setPackage(getPackageName());
            PendingIntent restartServicePendingIntent = PendingIntent.getService(
                    getApplicationContext(), 1, restartServiceIntent,
                    PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

            android.app.AlarmManager alarmService = (android.app.AlarmManager) getApplicationContext()
                    .getSystemService(Context.ALARM_SERVICE);
            if (alarmService != null) {
                alarmService.set(android.app.AlarmManager.ELAPSED_REALTIME,
                        android.os.SystemClock.elapsedRealtime() + 500, restartServicePendingIntent);
                android.util.Log.i("VoiceListeningService", "⏰ Service restart scheduled after app removal");
            }
        }
        super.onTaskRemoved(rootIntent);
    }

    public static void stopService(Context context) {
        android.util.Log.i("VoiceListeningService", "🛑 stopService called - User MANUALLY stopping service");
        isRunning = false; // Set flag FIRST to prevent restart
        
        // Cancel JobScheduler monitoring
        ServiceRestartJob.cancelJob(context);
        android.util.Log.i("VoiceListeningService", "🛑 JobScheduler monitoring cancelled");
        
        Intent intent = new Intent(context, VoiceListeningService.class);
        context.stopService(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        
        android.util.Log.w("VoiceListeningService", "💀 onDestroy called - isRunning: " + isRunning);
        
        // If service is being destroyed but should still be running, restart it IMMEDIATELY
        if (isRunning) {
            android.util.Log.i("VoiceListeningService", "🔄 Service destroyed but SHOULD BE RUNNING - RESTARTING NOW!");
            
            // Method 1: Send broadcast to restart service
            Intent broadcastIntent = new Intent(this, com.example.ramu.receiver.RestartServiceReceiver.class);
            sendBroadcast(broadcastIntent);
            android.util.Log.i("VoiceListeningService", "📡 Broadcast sent to RestartServiceReceiver");
            
            // Method 2: Schedule restart via AlarmManager as backup
            Intent restartServiceIntent = new Intent(getApplicationContext(), VoiceListeningService.class);
            restartServiceIntent.setPackage(getPackageName());
            PendingIntent restartServicePendingIntent = PendingIntent.getService(
                    getApplicationContext(), 1, restartServiceIntent,
                    PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

            android.app.AlarmManager alarmService = (android.app.AlarmManager) getApplicationContext()
                    .getSystemService(Context.ALARM_SERVICE);
            if (alarmService != null) {
                alarmService.set(android.app.AlarmManager.ELAPSED_REALTIME,
                        android.os.SystemClock.elapsedRealtime() + 500, restartServicePendingIntent);
                android.util.Log.i("VoiceListeningService", "⏰ AlarmManager scheduled restart in 500ms");
            }
            
            // Method 3: Direct restart attempt
            try {
                Intent directRestartIntent = new Intent(getApplicationContext(), VoiceListeningService.class);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    getApplicationContext().startForegroundService(directRestartIntent);
                } else {
                    getApplicationContext().startService(directRestartIntent);
                }
                android.util.Log.i("VoiceListeningService", "🚀 Direct restart attempted");
            } catch (Exception e) {
                android.util.Log.e("VoiceListeningService", "❌ Direct restart failed: " + e.getMessage());
            }
        } else {
            android.util.Log.i("VoiceListeningService", "✅ Service stopped by user - NOT restarting");
        }

        isListening = false;

        // Release WakeLock
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
            android.util.Log.i("VoiceListeningService", "🔓 WakeLock released");
        }

        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        
        android.util.Log.w("VoiceListeningService", "💀 onDestroy COMPLETED");
    }
}
