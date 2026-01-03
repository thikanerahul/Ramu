package com.example.ramu.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AppCacheManager {

    private static AppCacheManager instance;
    private Context context;
    private Map<String, String> appMap; // Name -> PackageName
    private boolean isLoaded = false;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private AppCacheManager(Context context) {
        this.context = context.getApplicationContext();
        this.appMap = new HashMap<>();
        refreshAppCache();
    }

    public static synchronized AppCacheManager getInstance(Context context) {
        if (instance == null) {
            instance = new AppCacheManager(context);
        }
        return instance;
    }

    public void refreshAppCache() {
        executor.execute(() -> {
            PackageManager pm = context.getPackageManager();
            List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
            Map<String, String> newMap = new HashMap<>();

            for (ApplicationInfo packageInfo : packages) {
                try {
                    // Only include apps that have a launch intent (are launchable)
                    if (pm.getLaunchIntentForPackage(packageInfo.packageName) != null) {
                        String label = pm.getApplicationLabel(packageInfo).toString().toLowerCase().trim();
                        newMap.put(label, packageInfo.packageName);
                    }
                } catch (Exception e) {
                    // Ignore errors for individual apps
                }
            }

            synchronized (this) {
                appMap = newMap;
                isLoaded = true;
            }
            // System.out.println("Ramu: App cache loaded with " + newMap.size() + " apps");
        });
    }

    public String findPackageName(String appName) {
        if (!isLoaded) {
            return null; // Cache not ready
        }

        String lowerQuery = appName.toLowerCase().trim();

        synchronized (this) {
            // 1. Exact match
            if (appMap.containsKey(lowerQuery)) {
                return appMap.get(lowerQuery);
            }

            // 2. Fuzzy match (contains)
            // This is slower as O(N), but necessary for partial names
            // Prioritize shortest match that contains query? Or startswith?
            for (Map.Entry<String, String> entry : appMap.entrySet()) {
                if (entry.getKey().contains(lowerQuery)) {
                    return entry.getValue();
                }
            }
        }
        return null;
    }
}
