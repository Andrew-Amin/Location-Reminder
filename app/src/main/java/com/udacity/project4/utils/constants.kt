package com.udacity.project4.utils

const val LOCATION_PERMISSION_REQUEST_CODE = 1


object GeofencingConstants {

    /**
     * Used to set an expiration time for a geofence. After this amount of time, Location services
     * stops tracking the geofence. For this sample, geofences expire after one hour.
     */
    const val GEOFENCE_RADIUS_IN_METERS = 100f
    const val ACTION_GEOFENCE_EVENT =
        "RemindersActivity.SaveReminderFragment.action.ACTION_GEOFENCE_EVENT"
}