package com.example.wearfitness.presentation

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.example.stepcounter.presentation.HeartRateSensorManager
import com.example.stepcounter.presentation.WearFitnessApp
import com.example.stepcounter.presentation.createNotificationChannel
import com.example.wearfitness.presentation.theme.WearFitnessTheme
import com.google.android.gms.wearable.Wearable
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import com.example.wearfitness.presentation.theme.WearFitnessTheme
import com.example.wearfitness.shared.data.FirebaseRepository
import com.google.firebase.firestore.ListenerRegistration


class MainActivity : ComponentActivity() {
    private var heartRate by mutableIntStateOf(72)
    private var stepsGoal by mutableIntStateOf(10000)
    private lateinit var wearDataListener: WearDataListener
    private lateinit var heartRateSensorManager: HeartRateSensorManager

    private lateinit var repository: FirebaseRepository
    private lateinit var firebaseListener: ListenerRegistration? = null
    private val heartRatePermissionLauncher = registerForActivityResult(
        ActivityResultContracts
        .RequestPermission()) { isGranted ->

        if(isGranted) {
            heartRateSensorManager.startListening()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repository = FirebaseRepository()


        createNotificationChannel(this)
        heartRateSensorManager = HeartRateSensorManager(context = this, onHeartRateChanged = {
                newHeartRate -> heartRate = newHeartRate
        }
        )
        if (heartRateSensorManager.hasHeartRateSensor && !heartRateSensorManager.hasPermission()
        ) {
            heartRatePermissionLauncher.launch(heartRateSensorManager.requiredPermission)
        }

        wearDataListener = WearDataListener(onStepsGoalChanged = {newGoal ->
            runOnUiThread { stepsGoal = newGoal }
        })

        setContent {
            WearFitnessTheme {
                WearFitnessApp(
                    heartRateSensorValue = heartRate,
                    hasHeartRateSensor = heartRateSensorManager.hasHeartRateSensor,
                    stepsGoalFromPhone = stepsGoal
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (::heartRateSensorManager.isInitialized) {
            heartRateSensorManager.startListening()
        }
        if(::wearDataListener.isInitialized) {
            Wearable.getDataClient(this).addListener ( wearDataListener )
        }
        if (:: repository.isInitialized) {

        }
            startFirebaseListener()

    }

    override fun onPause() {
        super.onPause()
        if (::heartRateSensorManager.isInitialized) {
            heartRateSensorManager.stopListening()
        }
        if(::wearDataListener.isInitialized) {
            Wearable.getDataClient(this).removeListener ( wearDataListener )
        }
        stopFirebaseListener()
    }

    private fun startFirebaseListener() {
        if (firebaseListener != null){
            return
        }
        firebaseListener = repository.listenToFitnessData(
            onDataChanged = { fitnessData -> runOnUiThread { stepsGoal = fitnessData.dailyGoal.toInt()}},
            onError = {exception -> Log.e("SharedFirebaseWear", "Firebase listener error", exception)}
        )
    }
    private fun stopFirebaseListener() {
        firebaseListener?.remove()
        firebaseListener = null
    }
}