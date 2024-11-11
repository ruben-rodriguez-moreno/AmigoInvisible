package com.example.amigoinvisible

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.key
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.Firebase
import kotlinx.coroutines.delay
import kotlin.collections.isNotEmpty
import kotlin.collections.mapNotNull
import kotlin.collections.random
import kotlin.collections.remove
import kotlin.collections.toMutableList

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AmigoInvisibleApp()
        }
    }
}

@Composable
fun AmigoInvisibleApp() {
    var showMainScreen by remember { mutableStateOf(false) }

    if (showMainScreen) {
        SecretSantaApp()
    } else {
        StartScreen { showMainScreen = true }
    }
}

@Composable
fun StartScreen(onStartClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE0F2F7)) // Light blue background
            .padding(16.dp)
            .wrapContentSize(Alignment.Center), // Wrap content and center
        horizontalAlignment = Alignment.CenterHorizontally, // Center horizontally
        verticalArrangement = Arrangement.Center // Center vertically
    ) {
        // Title with styling
        Text(
            text = "Amigo Invisible",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                color = Color(0xFF00BCD4) // Cyan color
            )
        )
        Spacer(modifier = Modifier.height(32.dp))

        // Start button with styling
        Button(
            onClick = onStartClick,
            modifier = Modifier
                .padding(horizontal = 32.dp), // Add horizontal padding
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF00BCD4) // Cyan color
            )
        ) {
            Text("Start", color = Color.White, fontSize = 18.sp)
        }
        Spacer(modifier = Modifier.height(24.dp))

        // Credit text with styling
        Text(
            text = "Made by Ruben",
            style = MaterialTheme.typography.bodySmall.copy(
                color = Color.Gray
            ),
            modifier = Modifier.padding(bottom = 16.dp)
        )
    }
}

data class SecretSantaState(
    val participants: List<String> = listOf("Susana" , "Estrelli", "Pepi", "Nati", "Lucy" , "Inma" , "Elena" , "Pilar" , "Mari Carmen" , "Isabel" , "Palmy"),
    val currentUser: String = "", // Replace with actual current user
    val secretSanta: String? = null,
    val isRotating: Boolean = false
)

class SecretSantaLogic {
    fun selectSecretSanta(currentUser: String, participants: List<String>): String? {
        return participants.filter { it != currentUser }.randomOrNull()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecretSantaApp() {
    var state by remember { mutableStateOf(SecretSantaState()) }
    val logic = remember { SecretSantaLogic() }
    var expanded by remember { mutableStateOf(false) }
    var selectedParticipantIndex by remember { mutableStateOf(0) }
    var drawnParticipants by remember { mutableStateOf(emptySet<String>()) }
    var showResult by remember { mutableStateOf(false) } // State to control result

    val rotation by animateFloatAsState(
        targetValue = if (state.isRotating) 360f else 0f,
        animationSpec = tween(durationMillis = 1000),
        finishedListener = { state = state.copy(isRotating = false) }
    )

    // Use a Card to wrap the content
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFBBDEFB)) // Light Blue Card
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Amigo Invisible",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0D47A1) // Dark Blue Title
                )
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Dropdown menu (using ExposedDropdownMenuBox)
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                TextField(
                    value = state.participants[selectedParticipantIndex],
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    colors = ExposedDropdownMenuDefaults.textFieldColors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    textStyle = LocalTextStyle.current.copy(color = Color.Black)
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    state.participants.forEachIndexed { index, participant ->
                        DropdownMenuItem(
                            text = { Text(participant) },
                            onClick = {
                                selectedParticipantIndex = index
                                state = state.copy(currentUser = participant)
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (drawnParticipants.size < state.participants.size) {
                        var secretSanta: String?
                        do {
                            secretSanta = logic.selectSecretSanta(state.currentUser, state.participants)
                        } while (secretSanta != null && secretSanta in drawnParticipants)

                        if (secretSanta != null) {
                            state = state.copy(isRotating = true, secretSanta = secretSanta)
                            drawnParticipants = drawnParticipants + secretSanta
                        }
                    } else {
                        state = state.copy(secretSanta = "Todos los participantes han sido sorteados")
                    }
                    showResult = true // Show the result after drawing
                },
                enabled = !state.isRotating,
                modifier = Modifier
                    .graphicsLayer { rotationZ = rotation }
                    .padding(horizontal = 32.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D47A1)) // Dark Blue Button
            ) {
                Text("Sortear!", color = Color.White)
            }
            if (showResult) { // Show Next button only after drawing
                Button(
                    onClick = {
                        showResult = false // Hide the result
                        // Move to the next participant (if any)
                        if (selectedParticipantIndex < state.participants.size - 1) {
                            selectedParticipantIndex++
                            state = state.copy(currentUser = state.participants[selectedParticipantIndex])
                        } else {
                            // All participants have drawn, reset to the beginning
                            selectedParticipantIndex = 0
                            state = state.copy(currentUser = state.participants[selectedParticipantIndex])
                            drawnParticipants = emptySet() // Reset drawn participants
                        }
                    }
                ) {
                    Text("Siguiente (Dale al aprender tu amigo invisible)")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Display result conditionally
            if (showResult && state.secretSanta != null) {
                Text(
                    text = "Tu amigo invisible es: ${state.secretSanta}",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD32828) // Red Result
                    )
                )
            }
        }
    }
}
