package com.example.ramu.service;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

/**
 * JobService to ensure VoiceListeningService stays alive
 * This is an additional layer of protection beyond WakeLock and START_STICKY
 * Similar to how Google Assistant/Alexa stay always-on
 */
public class ServiceRestartJob extends JobService {
    
    private static final String TAG = "ServiceRestartJob";
    private static final int JOB_ID = 1001;
    
    @Override
    public boolean onStartJob(JobParameters params) {
        Log.i(TAG, "🔄 JobScheduler triggered - Checking service status");
        
        // Check if service should be running
        if (VoiceListeningService.isRunning) {
            Log.i(TAG, "✅ Service should be running - Ensuring it's alive");
            
            // Start service if not running
            Intent serviceIntent = new Intent(getApplicationContext(), VoiceListeningService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                getApplicationContext().startForegroundService(serviceIntent);
            } else {
                getApplicationContext().startService(serviceIntent);
            }
            
            Log.i(TAG, "🚀 Service restart triggered by JobScheduler");
        } else {
            Log.i(TAG, "⏸️ Service stopped by user - Not restarting");
        }
        
        // DO NOT reschedule here - periodic job automatically reschedules itself!
        // Removing this line fixes the infinite loop issue
        
        // Job finished
        return false;
    }
    
    @Override
    public boolean onStopJob(JobParameters params) {
        Log.w(TAG, "⚠️ Job stopped - Will automatically reschedule (periodic job)");
        // Periodic jobs automatically reschedule - no need to manually reschedule
        return true; // Return true to reschedule if job is stopped prematurely
    }
    
    /**
     * Schedule periodic job to monitor and restart service
     * Runs every 15 minutes to ensure service is alive
     */
    public static void scheduleJob(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
            
            if (jobScheduler != null) {
                // Check if job is already scheduled to avoid duplicate scheduling
                if (isJobScheduled(jobScheduler)) {
                    Log.i(TAG, "⏭️ JobScheduler already scheduled - Skipping");
                    return;
                }
                
                ComponentName componentName = new ComponentName(context, ServiceRestartJob.class);
                
                JobInfo.Builder builder = new JobInfo.Builder(JOB_ID, componentName)
                        .setRequiredNetworkType(JobInfo.NETWORK_TYPE_NONE) // No network required
                        .setPersisted(true) // Persist across reboots
                        .setPeriodic(15 * 60 * 1000); // Every 15 minutes
                
                // NOTE: Cannot use setMinimumLatency() with periodic jobs
                // Periodic jobs run at the specified interval automatically
                
                int result = jobScheduler.schedule(builder.build());
                
                if (result == JobScheduler.RESULT_SUCCESS) {
                    Log.i(TAG, "✅ JobScheduler scheduled successfully - Will monitor service every 15 minutes");
                } else {
                    Log.e(TAG, "❌ JobScheduler scheduling failed");
                }
            }
        }
    }
    
    /**
     * Check if job is already scheduled
     */
    private static boolean isJobScheduled(JobScheduler jobScheduler) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            for (JobInfo jobInfo : jobScheduler.getAllPendingJobs()) {
                if (jobInfo.getId() == JOB_ID) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Cancel scheduled job
     */
    public static void cancelJob(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
            if (jobScheduler != null) {
                jobScheduler.cancel(JOB_ID);
                Log.i(TAG, "🛑 JobScheduler cancelled");
            }
        }
    }
}
