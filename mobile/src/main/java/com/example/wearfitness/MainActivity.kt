package com.example.wearfitness

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.example.stepcounter.sendStepsGoalToWatch
import com.example.wearfitness.shared.data.FirebaseRepository

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repository = FirebaseRepository()

        setContent {
            MaterialTheme {
                PhoneCompanionApp(repository = repository)
            }
        }
    }
}

@Composable
fun PhoneCompanionApp(repository: FirebaseRepository) {
    val context = LocalContext.current
    var stepsGoal by remember {
        mutableIntStateOf(10000)
    }

    var sendStatus by remember {
        mutableStateOf("Not sent")
    }
    DisposableEffect(repository) {
        val listenerRegistration = repository.listenToFitnessData(
            onDataChanged = {fitnessData ->
                stepsGoal = fitnessData.dailyGoal.toInt()
                sendStatus = "Goal received from Firebase: $stepsGoal"
            },
            onError = {exception -> sendStatus = "Firebase listener error: " + (exception.message ?: "Unkown error")}
        )
        onDispose { listenerRegistration.remove() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement =
            Arrangement.Center,
        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {
        Text(
            text = "Wear Fitness",
            style =
                MaterialTheme.typography.headlineMedium
        )

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        Text(
            text = "Steps Goal",
            style =
                MaterialTheme.typography.titleMedium
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        Row(
            verticalAlignment =
                Alignment.CenterVertically,
            horizontalArrangement =
                Arrangement.Center
        ) {
            Button(
                onClick = {
                    if (stepsGoal > 500) {
                        stepsGoal -= 500
                    }
                }
            ) {
                Text("-")
            }

            Spacer(
                modifier = Modifier.width(16.dp)
            )

            Text(
                text = stepsGoal.toString(),
                style =
                    MaterialTheme.typography.headlineSmall
            )

            Spacer(
                modifier = Modifier.width(16.dp)
            )

            Button(
                onClick = {
                    stepsGoal += 500
                }
            ) {
                Text("+")
            }
        }

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        Button(
            onClick = {
                sendStatus =
                    "Sending..."

                sendStepsGoalToWatch(context = context,
                    stepsGoal = stepsGoal,
                    onSuccess = {
                        sendStatus = "Sent $stepsGoal to the watch"
                    },
                    onError = {errorMessage -> sendStatus = "Error: $errorMessage "}
                )
            }
        ) {
            Text("Send to Watch")
        }
        Spacer(modifier = Modifier.height(12.dp))

        Button(onClick = {
            sendStatus = "Saving to Firebase..."
            repository.updateDailyGoal(
                dailyGoal = stepsGoal.toLong(),
                onSuccess = {sendStatus = "Saved $stepsGoal in Firebase"},
                onError = {exception -> sendStatus = "Firebase error: " + (exception.message ?: "Unknown error")}
            )

        }) {
            Text ("Save to Firebase")
        }

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        Text(
            text = "Status: $sendStatus"
        )
    }
}
