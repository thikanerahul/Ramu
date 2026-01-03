package com.example.ramu.ui;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.ramu.databinding.ActivityPermissionBinding;
import com.example.ramu.utils.PermissionManager;

public class PermissionActivity extends AppCompatActivity {
    
    private ActivityPermissionBinding binding;
    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final int ACCESSIBILITY_REQUEST_CODE = 200;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPermissionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        setupClickListeners();
        updatePermissionStatus();
    }
    
    private void setupClickListeners() {
        binding.grantAudioButton.setOnClickListener(v -> requestAudioPermission());
        binding.grantContactsButton.setOnClickListener(v -> requestContactsPermission());
        binding.grantPhoneButton.setOnClickListener(v -> requestPhonePermission());
        binding.grantAccessibilityButton.setOnClickListener(v -> openAccessibilitySettings());
        binding.continueButton.setOnClickListener(v -> {
            if (PermissionManager.hasAllPermissions(this)) {
                finish();
            } else {
                Toast.makeText(this, "Please grant all permissions", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void requestAudioPermission() {
        ActivityCompat.requestPermissions(this,
            new String[]{Manifest.permission.RECORD_AUDIO},
            PERMISSION_REQUEST_CODE);
    }
    
    private void requestContactsPermission() {
        ActivityCompat.requestPermissions(this,
            new String[]{Manifest.permission.READ_CONTACTS},
            PERMISSION_REQUEST_CODE);
    }
    
    private void requestPhonePermission() {
        String[] permissions;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions = new String[]{
                Manifest.permission.CALL_PHONE,
                Manifest.permission.BLUETOOTH_CONNECT
            };
        } else {
            permissions = new String[]{
                Manifest.permission.CALL_PHONE,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN
            };
        }
        ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
    }
    
    private void openAccessibilitySettings() {
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        startActivityForResult(intent, ACCESSIBILITY_REQUEST_CODE);
        Toast.makeText(this, "Please enable 'Ramu' service", Toast.LENGTH_LONG).show();
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                          @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        updatePermissionStatus();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        updatePermissionStatus();
    }
    
    private void updatePermissionStatus() {
        // Audio Permission
        if (PermissionManager.hasAudioPermission(this)) {
            binding.audioStatus.setText("✓ Granted");
            binding.grantAudioButton.setEnabled(false);
        } else {
            binding.audioStatus.setText("✗ Not Granted");
            binding.grantAudioButton.setEnabled(true);
        }
        
        // Contacts Permission
        if (PermissionManager.hasContactsPermission(this)) {
            binding.contactsStatus.setText("✓ Granted");
            binding.grantContactsButton.setEnabled(false);
        } else {
            binding.contactsStatus.setText("✗ Not Granted");
            binding.grantContactsButton.setEnabled(true);
        }
        
        // Phone Permission
        if (PermissionManager.hasPhonePermission(this)) {
            binding.phoneStatus.setText("✓ Granted");
            binding.grantPhoneButton.setEnabled(false);
        } else {
            binding.phoneStatus.setText("✗ Not Granted");
            binding.grantPhoneButton.setEnabled(true);
        }
        
        // Accessibility Permission
        if (PermissionManager.isAccessibilityServiceEnabled(this)) {
            binding.accessibilityStatus.setText("✓ Enabled");
            binding.grantAccessibilityButton.setEnabled(false);
        } else {
            binding.accessibilityStatus.setText("✗ Disabled");
            binding.grantAccessibilityButton.setEnabled(true);
        }
    }
}
