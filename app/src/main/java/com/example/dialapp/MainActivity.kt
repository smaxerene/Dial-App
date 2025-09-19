package com.example.dialapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.compose.ui.tooling.preview.Preview
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.dialapp.ui.theme.DialAppTheme
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalConfiguration
import android.content.res.Configuration
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.core.net.toUri

// Colors used throughout the UI
val Background = Color(0xFFDEEFFF)
val DialPadColor = Color(0xFF5A5ADC)
val CallButtonColor = Color(0xFF288229)
val TextColor = Color(0xFFFFFFFF)

class MainActivity : ComponentActivity() {

    // Mutable state holding the current phone number as a string
    private var currentNumber by mutableStateOf("")

    // Register permission launcher to request CALL_PHONE permission at runtime
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // If the permission is granted, start the phone call with current number
            makePhoneCall(currentNumber)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Compose UI setup using setContent
        setContent {
            DialAppTheme {
                // Saves number when the layout is rotated (prevents number to disappear)
                var currentNumber by rememberSaveable { mutableStateOf("") }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Background
                ) {
                    DialApp(
                        phoneNumber = currentNumber,
                        onNumberChanged = { currentNumber = it },
                        onCallClicked = { checkPermissionsAndCall(currentNumber) }
                    )
                }
            }
        }
        handleIntent(intent) // Process any intent
    }

    // Handle the new intent
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    // Processes incoming DIAL intents
    private fun handleIntent(intent: Intent) {
        if (intent.action == Intent.ACTION_DIAL) {
            val uri = intent.data
            uri?.let {
                // Check that URI scheme is "tel"
                if (it.scheme == "tel") {
                    currentNumber = it.schemeSpecificPart
                }
            }
        }
    }

    // Check if CALL_PHONE permission is granted before making a call
    private fun checkPermissionsAndCall(number: String) {
        if (number.isEmpty()) return

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CALL_PHONE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // If the permission is granted, proceed with call
            makePhoneCall(number)
        } else {
            // Request CALL_PHONE permission from the user
            requestPermissionLauncher.launch(Manifest.permission.CALL_PHONE)
        }
    }

    // Starts an intent to place a phone call to the chosen number
    private fun makePhoneCall(number: String) {
        try {
            val intent = Intent(Intent.ACTION_CALL).apply {
                data = "tel:$number".toUri()
            }
            startActivity(intent) // Launch the call activity
        } catch (e: Exception) {
            // Catch any exceptions
            e.printStackTrace()
        }
    }
}


@Composable
fun DialApp(
    phoneNumber: String,                 // Current phone number displayed
    onNumberChanged: (String) -> Unit,  // Callback when number changes (digit/s added/removed)
    onCallClicked: () -> Unit            // Callback when Call button is pressed
) {
    var statusMessage by remember { mutableStateOf("") }
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE // Checks Orientation

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Background
    ) {
        Column(
            modifier = Modifier
                .padding(if (isLandscape) 8.dp else 16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Top horizontal divider only in portrait
            if (!isLandscape) {
                Spacer(modifier = Modifier.height(30.dp))
                HorizontalDivider(
                    color = Color.Gray,
                    thickness = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Phone number display and backspace button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = if (isLandscape) 8.dp else 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = if (isLandscape) Arrangement.Center else Arrangement.SpaceBetween
            ) {
                if (!isLandscape) {
                    // Large number display and backspace for portrait
                    Text(
                        text = phoneNumber.ifEmpty { "" },
                        fontSize = 45.sp,
                        modifier = Modifier.weight(1f)
                    )
                    // Backspace button to delete last digit
                    TextButton(onClick = {
                        if (phoneNumber.isNotEmpty()) {
                            onNumberChanged(phoneNumber.dropLast(1))
                        }

                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.backspace),
                            contentDescription = "Voicemail",
                            modifier = Modifier.size(40.dp)
                        )

                    }
                } else {
                    // Smaller number text and backspace for landscape
                    Text(
                        text = phoneNumber.ifEmpty { "" },
                        fontSize = 32.sp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    // Backspace button with text symbol
                    TextButton(onClick = {
                        if (phoneNumber.isNotEmpty()) {
                            onNumberChanged(phoneNumber.dropLast(1))
                        }
                    }) {
                        Text("⌫", fontSize = 28.sp)
                    }
                }
            }

            // Bottom horizontal divider only in portrait mode
            if (!isLandscape) {
                HorizontalDivider(
                    color = Color.Gray,
                    thickness = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Dial pad
            DialPad(onDigitPressed = { digit ->
                onNumberChanged(phoneNumber + digit)
            })

            // Call button
            Button(
                onClick = {
                    if (phoneNumber.isNotEmpty()) {
                        onCallClicked() // Invoke call action
                        statusMessage = "Calling $phoneNumber..."
                    } else {
                        statusMessage = "Enter a number first"
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = CallButtonColor,
                    contentColor = TextColor
                ),
                shape = CircleShape,
                modifier = Modifier
                    .padding(top = if (isLandscape) 8.dp else 16.dp)
                    .height(if (isLandscape) 80.dp else 110.dp)
                    .width(if (isLandscape) 140.dp else 170.dp)
            ) {
                // Call button icon and text
                Icon(
                    painter = painterResource(id = R.drawable.phone),
                    contentDescription = "Call",
                    tint = TextColor,
                    modifier = Modifier.size(if (isLandscape) 24.dp else 30.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Call", fontSize = if (isLandscape) 24.sp else 35.sp)
            }

            // Extra space at the bottom if it's in portrait
            if (!isLandscape) Spacer(modifier = Modifier.height(5.dp))
        }
    }
}

// UI for dial pad
@Composable
fun DialPad(onDigitPressed: (String) -> Unit) {
    val configuration = LocalConfiguration.current

    // Checks if device is in landscape orientation
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    // Dial pad rows with digit and associated letters
    val buttons = listOf(
        listOf(Pair("1",""), Pair("2","ABC"), Pair("3","DEF")),
        listOf(Pair("4","GHI"), Pair("5","JKL"), Pair("6","MNO")),
        listOf(Pair("7","PQRS"), Pair("8","TUV"), Pair("9","WXYZ")),
        listOf(Pair("*", ""), Pair("0","+"), Pair("#",""))
    )

    // Dial pad layout
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(if (isLandscape) 4.dp else 8.dp)
    ) {
        buttons.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                row.forEach { (digit, letters) ->
                    Button(
                        onClick = { onDigitPressed(digit) },
                        shape = if (isLandscape) {
                            MaterialTheme.shapes.medium // Rounded rectangle buttons in landscape
                        } else {
                            CircleShape // Circular buttons in portrait
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DialPadColor,
                            contentColor = TextColor
                        ),
                        modifier = Modifier
                            .width(if (isLandscape) 120.dp else 120.dp)
                            .height(if (isLandscape) 60.dp else 120.dp)
                            .padding(if (isLandscape) 1.dp else 4.dp)
                    ) {
                        if (isLandscape) {
                            // Digit and letters side by side horizontally in landscape mode
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = digit,
                                    fontSize = 24.sp,
                                    modifier = Modifier.padding(end = 4.dp)
                                )
                                // Show voicemail icon under "1" digit
                                if (digit == "1") {
                                    Icon(
                                        painter = painterResource(id = R.drawable.voicemail),
                                        contentDescription = "Voicemail",
                                        modifier = Modifier.size(16.dp)
                                    )
                                } else if (letters.isNotEmpty()) {
                                    // Show associated letters under digit
                                    Text(
                                        text = letters,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        } else {
                            // Digit above letters vertically in portrait mode
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = digit,
                                    fontSize = 40.sp
                                )
                                if (digit == "1") {
                                    // Voicemail icon below digit
                                    Icon(
                                        painter = painterResource(id = R.drawable.voicemail),
                                        contentDescription = "Voicemail",
                                        modifier = Modifier.size(20.dp)
                                    )
                                } else if (letters.isNotEmpty()) {
                                    // Display letters below digit
                                    Text(
                                        text = letters,
                                        fontSize = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
            // Spacing between rows only in portrait mode
            if (!isLandscape) Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Preview(showBackground = true,
    widthDp = 420,
    heightDp = 900)
@Composable
fun DialAppPreview() {
    DialAppTheme {
        DialApp(
            phoneNumber = "1234567890",
            onNumberChanged = {},
            onCallClicked = {}
        )
    }
}
