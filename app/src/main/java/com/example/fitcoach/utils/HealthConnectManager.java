package com.example.fitcoach.utils;

import android.content.Context;
import android.util.Log;

import androidx.health.connect.client.HealthConnectClient;
import androidx.health.connect.client.permission.HealthPermission;
import androidx.health.connect.client.records.StepsRecord;
import androidx.health.connect.client.records.metadata.DataOrigin;
import androidx.health.connect.client.request.AggregateRequest;
import androidx.health.connect.client.time.TimeRangeFilter;
import androidx.health.connect.client.aggregate.AggregationResult;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import kotlin.coroutines.Continuation;
import kotlin.coroutines.EmptyCoroutineContext;
import kotlin.jvm.JvmClassMappingKt;
import kotlinx.coroutines.BuildersKt;
import kotlinx.coroutines.Dispatchers;

/**
 * Manages interaction with Health Connect SDK.
 */
public class HealthConnectManager {

    private static final String TAG = "HealthConnectManager";
    private final HealthConnectClient healthConnectClient;
    private final Set<String> permissions;

    public HealthConnectManager(Context context) {
        if (isHealthConnectAvailable(context)) {
            this.healthConnectClient = HealthConnectClient.getOrCreate(context);
        } else {
            this.healthConnectClient = null;
        }
        
        this.permissions = Collections.singleton(
                HealthPermission.getReadPermission(JvmClassMappingKt.getKotlinClass(StepsRecord.class))
        );
    }

    /**
     * Checks if Health Connect SDK is available on the device.
     */
    public static boolean isHealthConnectAvailable(Context context) {
        int availabilityStatus = HealthConnectClient.getSdkStatus(context);
        Log.d(TAG, "Health Connect availability status: " + availabilityStatus);
        
        if (availabilityStatus == HealthConnectClient.SDK_AVAILABLE) {
            return true;
        }
        
        if (availabilityStatus == HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED) {
            Log.w(TAG, "Health Connect update required");
        }
        return false;
    }

    public HealthConnectClient getHealthConnectClient() {
        return healthConnectClient;
    }

    public Set<String> getPermissions() {
        return permissions;
    }

    public void checkPermissions(PermissionCallback callback) {
        if (healthConnectClient == null) {
            Log.e(TAG, "HealthConnectClient is null, cannot check permissions");
            callback.onResult(false);
            return;
        }

        new Thread(() -> {
            try {
                Object result = BuildersKt.runBlocking(EmptyCoroutineContext.INSTANCE, (scope, continuation) -> 
                    healthConnectClient.getPermissionController().getGrantedPermissions((Continuation<? super Set<String>>) continuation)
                );
                if (result instanceof Set) {
                    @SuppressWarnings("unchecked")
                    Set<String> granted = (Set<String>) result;
                    boolean allGranted = granted.containsAll(permissions);
                    Log.d(TAG, "Permissions granted status: " + allGranted);
                    callback.onResult(allGranted);
                } else {
                    callback.onResult(false);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error checking permissions", e);
                callback.onResult(false);
            }
        }).start();
    }

    public interface PermissionCallback {
        void onResult(boolean granted);
    }

    public Long getTodaySteps() {
        if (healthConnectClient == null) return null;

        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();

            AggregateRequest request = new AggregateRequest(
                    Collections.singleton(StepsRecord.COUNT_TOTAL),
                    TimeRangeFilter.between(
                            startOfDay.atZone(ZoneId.systemDefault()).toInstant(),
                            now.atZone(ZoneId.systemDefault()).toInstant()
                    ),
                    new HashSet<DataOrigin>() 
            );

            Object resultObj = BuildersKt.runBlocking(Dispatchers.getIO(), (scope, continuation) -> 
                healthConnectClient.aggregate(request, (Continuation<? super AggregationResult>) continuation)
            );

            if (resultObj instanceof AggregationResult) {
                AggregationResult aggregationResult = (AggregationResult) resultObj;
                return aggregationResult.get(StepsRecord.COUNT_TOTAL);
            }
            return null;
        } catch (Exception e) {
            Log.e(TAG, "Error fetching steps", e);
            return null;
        }
    }
}
