package com.example.ramu.ui;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.ramu.R;
import com.example.ramu.databinding.ActivityMainBinding;
import com.example.ramu.service.VoiceListeningService;
import com.example.ramu.utils.CommandProcessor;
import com.example.ramu.utils.PermissionManager;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    
    private ActivityMainBinding binding;
    private TextToSpeech textToSpeech;
    private SpeechRecognizer speechRecognizer;
    private Intent recognizerIntent;
    private CommandProcessor commandProcessor;
    private boolean isListening = false;
    private SharedPreferences prefs;
    
    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final String PREFS_NAME = "RamuPrefs";
    private static final String KEY_SERVICE_ENABLED = "service_enabled";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        
        initializeComponents();
        setupClickListeners();
        checkAndRequestPermissions();
        restoreServiceState();
    }
    
    private void initializeComponents() {
        // Check if speech recognition is available
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            Toast.makeText(this, "Speech recognition not available on this device", Toast.LENGTH_LONG).show();
            binding.micButton.setEnabled(false);
            return;
        }
        
        // Initialize TTS
        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = textToSpeech.setLanguage(Locale.US);
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Toast.makeText(this, "Text-to-speech language not supported", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Text-to-speech initialization failed", Toast.LENGTH_SHORT).show();
            }
        });
        
        // Initialize Command Processor
        commandProcessor = new CommandProcessor(this, textToSpeech);
        
        // Initialize Speech Recognizer
        try {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
            if (speechRecognizer == null) {
                Toast.makeText(this, "Speech recognizer creation failed", Toast.LENGTH_LONG).show();
                binding.micButton.setEnabled(false);
                return;
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error creating speech recognizer: " + e.getMessage(), Toast.LENGTH_LONG).show();
            binding.micButton.setEnabled(false);
            return;
        }
        
        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, 
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-IN");
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en-IN");
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 10000L);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 10000L);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 10000L);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);
        
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                android.util.Log.d("MainActivity", "onReadyForSpeech - Mic is ready");
                runOnUiThread(() -> {
                    updateStatus("🎤 Listening... Say 'Ramu' first");
                    Toast.makeText(MainActivity.this, "Mic is ready - Speak now!", Toast.LENGTH_SHORT).show();
                    startMicAnimation();
                });
            }
            
            @Override
            public void onBeginningOfSpeech() {
                android.util.Log.d("MainActivity", "onBeginningOfSpeech - Speech detected");
                runOnUiThread(() -> {
                    updateStatus("👂 Hearing you...");
                });
            }
            
            @Override
            public void onRmsChanged(float rmsdB) {
                // Audio level changed - mic is working
            }
            
            @Override
            public void onBufferReceived(byte[] buffer) {}
            
            @Override
            public void onEndOfSpeech() {
                runOnUiThread(() -> {
                    stopMicAnimation();
                    updateStatus("Processing...");
                });
            }
            
            @Override
            public void onError(int error) {
                runOnUiThread(() -> {
                    handleSpeechError(error);
                    isListening = false;
                    stopMicAnimation();
                });
            }
            
            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(
                    SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    String command = matches.get(0);
                    android.util.Log.d("MainActivity", "Speech recognized: " + command);
                    runOnUiThread(() -> {
                        updateStatus("Heard: " + command);
                        processCommand(command);
                    });
                } else {
                    android.util.Log.w("MainActivity", "No speech matches found");
                }
                isListening = false;
            }
            
            @Override
            public void onPartialResults(Bundle partialResults) {
                ArrayList<String> matches = partialResults.getStringArrayList(
                    SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    String partial = matches.get(0);
                    runOnUiThread(() -> updateStatus("Hearing: " + partial));
                }
            }
            
            @Override
            public void onEvent(int eventType, Bundle params) {}
        });
    }
    
    private void setupClickListeners() {
        // Mic button now toggles continuous listening mode
        binding.micButton.setOnClickListener(v -> {
            android.util.Log.d("MainActivity", "Mic button clicked, isListening=" + isListening);
            if (isListening) {
                stopListening();
                binding.micButton.setAlpha(0.5f);
                Toast.makeText(this, "Continuous listening stopped", Toast.LENGTH_SHORT).show();
            } else {
                startListening();
                binding.micButton.setAlpha(1.0f);
                Toast.makeText(this, "Continuous listening started - Say 'Ramu' anytime", Toast.LENGTH_SHORT).show();
            }
        });
        
        binding.serviceToggle.setOnClickListener(v -> {
            android.util.Log.d("MainActivity", "Service toggle clicked");
            toggleForegroundService();
        });
        
        binding.settingsButton.setOnClickListener(v -> {
            android.util.Log.d("MainActivity", "Settings button clicked");
            startActivity(new Intent(this, PermissionActivity.class));
        });
    }
    
    private void checkAndRequestPermissions() {
        // Check for RECORD_AUDIO permission at runtime (required for Android 6.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) 
                != PackageManager.PERMISSION_GRANTED) {
                android.util.Log.w("MainActivity", "RECORD_AUDIO permission not granted, requesting...");
                ActivityCompat.requestPermissions(this, 
                    new String[]{Manifest.permission.RECORD_AUDIO}, 
                    PERMISSION_REQUEST_CODE);
                return;
            }
        }
        
        // Request to disable battery optimization for persistent service
        requestDisableBatteryOptimization();
        
        if (!PermissionManager.hasAllPermissions(this)) {
            startActivity(new Intent(this, PermissionActivity.class));
        }
    }
    
    private void requestDisableBatteryOptimization() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent();
            String packageName = getPackageName();
            android.os.PowerManager pm = (android.os.PowerManager) getSystemService(POWER_SERVICE);
            
            if (pm != null && !pm.isIgnoringBatteryOptimizations(packageName)) {
                android.util.Log.i("MainActivity", "Requesting to disable battery optimization");
                intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(android.net.Uri.parse("package:" + packageName));
                try {
                    startActivity(intent);
                    Toast.makeText(this, "Please allow 'Don't optimize' for Ramu to keep service running", Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    android.util.Log.e("MainActivity", "Error requesting battery optimization", e);
                }
            } else {
                android.util.Log.i("MainActivity", "Battery optimization already disabled");
            }
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, 
                                          @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                android.util.Log.i("MainActivity", "RECORD_AUDIO permission granted");
                Toast.makeText(this, "Microphone permission granted! Tap mic to start", Toast.LENGTH_SHORT).show();
            } else {
                android.util.Log.e("MainActivity", "RECORD_AUDIO permission denied");
                Toast.makeText(this, "Microphone permission is required for voice commands", Toast.LENGTH_LONG).show();
            }
        }
    }
    
    private void startListening() {
        android.util.Log.d("MainActivity", "startListening() called");
        
        if (!PermissionManager.hasAudioPermission(this)) {
            android.util.Log.e("MainActivity", "Audio permission not granted");
            Toast.makeText(this, "Please grant microphone permission", Toast.LENGTH_LONG).show();
            speak("Please grant microphone permission");
            startActivity(new Intent(this, PermissionActivity.class));
            return;
        }
        
        if (speechRecognizer == null) {
            android.util.Log.e("MainActivity", "Speech recognizer is null");
            Toast.makeText(this, "Speech recognizer not initialized", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Don't start if already listening
        if (isListening) {
            android.util.Log.d("MainActivity", "Already listening, skipping");
            return;
        }
        
        try {
            isListening = true;
            updateStatus("🎤 Listening... Say 'Ramu'");
            android.util.Log.d("MainActivity", "Starting speech recognizer");
            speechRecognizer.startListening(recognizerIntent);
        } catch (Exception e) {
            isListening = false;
            android.util.Log.e("MainActivity", "Error starting mic: " + e.getMessage(), e);
            updateStatus("Error - Retrying...");
            // Auto-retry
            new android.os.Handler().postDelayed(this::startListening, 2000);
        }
    }
    
    private void stopListening() {
        isListening = false;
        if (speechRecognizer != null) {
            speechRecognizer.stopListening();
        }
        updateStatus("Stopped - Tap mic to start");
        stopMicAnimation();
    }
    
    private void processCommand(String command) {
        android.util.Log.d("MainActivity", "Processing command: " + command);
        updateStatus("Processing: " + command);
        
        // Check if command contains wake word
        String lowerCommand = command.toLowerCase();
        if (!lowerCommand.contains("ramu") && !lowerCommand.contains("ram") && 
            !lowerCommand.contains("रामू") && !lowerCommand.contains("राम")) {
            android.util.Log.d("MainActivity", "No wake word detected, ignoring: " + command);
            updateStatus("🎤 Listening... Say 'Ramu'");
            // Auto-restart listening for continuous mode
            new android.os.Handler().postDelayed(this::startListening, 500);
            return;
        }
        
        commandProcessor.processCommand(command);
        
        // Auto-restart listening after command execution for continuous mode
        updateStatus("✓ Done - Listening again...");
        new android.os.Handler().postDelayed(this::startListening, 1500);
    }
    
    private void toggleForegroundService() {
        Intent serviceIntent = new Intent(this, VoiceListeningService.class);
        if (VoiceListeningService.isRunning) {
            stopService(serviceIntent);
            binding.serviceToggle.setText("Start Service");
            speak("Service stopped");
            prefs.edit().putBoolean(KEY_SERVICE_ENABLED, false).apply();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }
            binding.serviceToggle.setText("Stop Service");
            speak("Service started");
            prefs.edit().putBoolean(KEY_SERVICE_ENABLED, true).apply();
        }
    }
    
    private void restoreServiceState() {
        boolean serviceEnabled = prefs.getBoolean(KEY_SERVICE_ENABLED, false);
        if (serviceEnabled && !VoiceListeningService.isRunning && PermissionManager.hasAllPermissions(this)) {
            Intent serviceIntent = new Intent(this, VoiceListeningService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }
            binding.serviceToggle.setText("Stop Service");
        } else if (VoiceListeningService.isRunning) {
            binding.serviceToggle.setText("Stop Service");
        }
    }
    
    private void handleSpeechError(int error) {
        String message = "Error occurred";
        boolean shouldRestart = true; // Always restart for continuous listening
        
        android.util.Log.e("MainActivity", "Speech recognition error: " + error);
        
        switch (error) {
            case SpeechRecognizer.ERROR_AUDIO:
                message = "Mic error - Retrying...";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = "Restarting...";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = "❌ Permission denied";
                Toast.makeText(this, "Please grant microphone permission", Toast.LENGTH_LONG).show();
                shouldRestart = false;
                startActivity(new Intent(this, PermissionActivity.class));
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                message = "No internet - Retrying...";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = "Network timeout - Retrying...";
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                message = "🎤 Listening...";
                shouldRestart = true; // Auto-restart for continuous listening
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = "Busy - Retrying...";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                message = "Server error - Retrying...";
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = "🎤 Listening...";
                shouldRestart = true; // Auto-restart for continuous listening
                break;
        }
        
        updateStatus(message);
        
        if (shouldRestart) {
            binding.getRoot().postDelayed(this::startListening, 1000);
        }
    }
    
    private void updateStatus(String status) {
        runOnUiThread(() -> binding.statusText.setText(status));
    }
    
    private void speak(String text) {
        if (textToSpeech != null) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }
    
    private void startMicAnimation() {
        ScaleAnimation scaleAnimation = new ScaleAnimation(
            1.0f, 1.2f, 1.0f, 1.2f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f);
        scaleAnimation.setDuration(500);
        scaleAnimation.setRepeatCount(Animation.INFINITE);
        scaleAnimation.setRepeatMode(Animation.REVERSE);
        binding.micButton.startAnimation(scaleAnimation);
    }
    
    private void stopMicAnimation() {
        binding.micButton.clearAnimation();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }
}
