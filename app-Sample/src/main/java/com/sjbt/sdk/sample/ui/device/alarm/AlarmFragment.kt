package com.sjbt.sdk.sample.ui.device.alarm

import android.os.Bundle
import androidx.navigation.fragment.NavHostFragment
import com.sjbt.sdk.sample.R

/**
 *
 * ***Description**
 * Display and modify alarms
 *
 * **Usage**
 * 1. [AlarmListFragment]
 * Display all alarms and sort it.
 * [AlarmHelper] [AlarmListAdapter]
 *
 * 2. [AlarmViewModel]
 * Show how to request alarms and set alarms
 * [FcSettingsFeature.requestAlarms] [FcSettingsFeature.setAlarms]
 *
 * 3.[AlarmDetailFragment]
 * Show how to modify the properties of Alarm
 * [AlarmLabelDialogFragment] [AlarmRepeatDialogFragment]
 */
class AlarmFragment : NavHostFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        navController.setGraph(R.navigation.alarm_nav_graph)
    }

}