package com.example.wearfitness

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.focusable
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
import androidx.window.core.layout.WindowSizeClass
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.SupportingPaneScaffold
import androidx.compose.material3.adaptive.navigation.rememberSupportingPaneScaffoldNavigator
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.newFixedThreadPoolContext
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isAltPressed
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
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
fun PhoneCompanionApp(
    repository: FirebaseRepository
) {
    val context = LocalContext.current
    var stepsGoal by remember {
        mutableIntStateOf(10000)
    }

    var sendStatus by remember {
        mutableStateOf("Not sent")
    }
    DisposableEffect(repository) {
        val listenerRegistration = repository.listenToFitnessData(
            onDataChanged = { fitnessData ->
                stepsGoal = fitnessData.dailyGoal.toInt()
                sendStatus = "Goal received from Firebase: $stepsGoal"
            },
            onError = { exception ->
                sendStatus = "Firebase listener error: " + (exception.message ?: "Unknown error")
            }
        )
        onDispose { listenerRegistration.remove() }
    }

    val navigator = rememberSupportingPaneScaffoldNavigator()

    SupportingPaneScaffold(
        directive = navigator.scaffoldDirective,
        value = navigator.scaffoldValue,
        mainPane = {
            AnimatedPane() {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    GoalControlSection(
                        stepsGoal = stepsGoal,
                        onIncrease = { stepsGoal += 500 },
                        onDecrease = { if (stepsGoal > 500) stepsGoal -= 500 }
                    )
                }

            }


        },
        supportingPane = {
            AnimatedPane() {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ActionsSection(
                        sendStatus = sendStatus,
                        onSendToWatch = {
                            sendStatus = "Sending..."
                            sendStepsGoalToWatch(
                                context = context,
                                stepsGoal = stepsGoal,
                                onSuccess = {sendStatus = "Sent $stepsGoal to the watch"},
                                onError = { errorMessage -> sendStatus = "Error $errorMessage"}
                            )
                        },
                        onSaveToFirebase =  {
                            sendStatus = "Sending to Firebase..."
                            repository.updateDailyGoal(
                                dailyGoal = stepsGoal.toLong(),
                                onSuccess = {sendStatus = "Sent $stepsGoal to Firebase"},
                                onError = { exception ->
                                    sendStatus = "Firebase error: " + (exception.message ?: "Unknown Error")
                                }
                            )
                    }
                    )
                    }
                }

            }

    )


    /*val isExpanded = windowSizeClass.isWidthAtLeastBreakpoint(
    windowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND
)*/

    /*if (isExpanded) {
    TabletLayout(
        stepsGoal = stepsGoal,
        sendStatus = sendStatus,
        onIncrease = { stepsGoal += 500 },
        onDecrease = { if (stepsGoal > 500) stepsGoal -= 500 },
        onSendWatch = {
            sendStatus = "Sending..."
            sendStepsGoalToWatch(
                context = context,
                stepsGoal = stepsGoal,
                onSuccess = { sendStatus = "Sent $stepsGoal to the watch " },
                onError = { errorMessage -> sendStatus = "Error: $errorMessage" }
            )
        },
        onSaveToFirebase = {
            sendStatus = "Saving to Firebase..."
            repository.updateDailyGoal(
                dailyGoal = stepsGoal.toLong(),
                onSuccess = { sendStatus = "Saved $stepsGoal in Firebase" },
                onError = { exception ->
                    sendStatus = "Firebase error: " + (exception.message ?: "Unknown error")
                }
            )
        }
    )
} else {
    PhoneLayout(
        stepsGoal = stepsGoal,
        sendStatus = sendStatus,
        onIncrease = { stepsGoal += 500 },
        onDecrease = { if (stepsGoal > 500) stepsGoal -= 500 },
        onSendToWatch = {
            sendStatus = "Sending..."
            sendStepsGoalToWatch(
                context = context,
                stepsGoal = stepsGoal,
                onSuccess = { sendStatus = "Sent $stepsGoal to the watch" },
                onError = { errorMessage -> sendStatus = "Error: $errorMessage" }
            )
        },
        onSaveToFirebase = {
            sendStatus = "Saving to Firebase..."
            repository.updateDailyGoal(
                dailyGoal = stepsGoal.toLong(),
                onSuccess = { sendStatus = "Saved $stepsGoal in Firebase" },
                onError = { exception ->
                    sendStatus = "Firebase error: " + (exception.message ?: "Unknown error")
                }
            )
        }

    )
}*/

}

@Composable
fun PhoneLayout(
    stepsGoal: Int,
    sendStatus: String,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onSendToWatch: () -> Unit,
    onSaveToFirebase: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        GoalControlSection(
            stepsGoal = stepsGoal,
            onIncrease = onIncrease,
            onDecrease = onDecrease
        )

        Spacer(modifier = Modifier.height(24.dp))

        ActionsSection(
            sendStatus = sendStatus,
            onSendToWatch = onSendToWatch,
            onSaveToFirebase = onSaveToFirebase
        )
    }
}

@Composable
fun TabletLayout(
    stepsGoal: Int,
    sendStatus: String,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onSendToWatch: () -> Unit,
    onSaveToFirebase: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalArrangement = Arrangement.spacedBy(32.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            GoalControlSection(
                stepsGoal = stepsGoal,
                onIncrease = onIncrease,
                onDecrease = onDecrease
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            ActionsSection(
                sendStatus = sendStatus,
                onSendToWatch = onSendToWatch,
                onSaveToFirebase = onSaveToFirebase
            )
        }
    }
}

@Composable
fun GoalControlSection(
    stepsGoal: Int,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit
) {

    val focusRequester = remember{ FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column(
        modifier = Modifier
            .focusRequester(focusRequester)
            .focusable()
            .onPreviewKeyEvent{keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown && keyEvent.isShiftPressed ) {
                    when (keyEvent.key) {
                        Key.Equals, Key.NumPadAdd -> {
                            onIncrease()
                            true
                        }
                        Key.Minus, Key.NumPadSubtract -> {
                        onIncrease()
                            true

                    }
                        else -> false

                    }
                } else {
                    false
                }
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
    Text(
        text = "Wear Fitness",
        style = MaterialTheme.typography.headlineMedium
    )

    Spacer(modifier = Modifier.height(24.dp))

    Text(
        text = "Steps Goal",
        style = MaterialTheme.typography.titleMedium
    )

    Spacer(modifier = Modifier.height(4.dp))

    Text(
        text = "Tip: Ctrl + + / Ctrl + - to adjust with keyboard",
        style = MaterialTheme.typography.labelSmall
    )

    Spacer(modifier = Modifier.height(12.dp))

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Button(onClick = onDecrease) {
            Text("_")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stepsGoal.toString(),
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onIncrease) {
            Text("_")
        }
    }
}

@Composable
fun ActionsSection(
    sendStatus: String,
    onSendToWatch: () -> Unit,
    onSaveToFirebase: () -> Unit
) {
    Text(
        text = "Actions",
        style = MaterialTheme.typography.titleMedium
    )

    Spacer(modifier = Modifier.height(12.dp))

    Button(onClick = onSendToWatch) {
        Text("Send to watch")
    }

    Spacer(modifier = Modifier.height(16.dp))

    Text(text = "Status: $sendStatus")
}
