package com.sanjangeet.coop1

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.sanjangeet.coop1.db.AppDatabase
import com.sanjangeet.coop1.db.Notes
import com.sanjangeet.coop1.ui.theme.Coop1Theme
import kotlinx.coroutines.launch
import kotlin.system.exitProcess

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Coop1Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    BiometricAuthenticator(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Root(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    var notes by remember { mutableStateOf(listOf<Notes>()) }
    var note by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        notes = db.notesDao().getAll()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            val scrollState = rememberScrollState()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(scrollState)
            ) {
                notes.forEach { note ->
                    Row {
                        Button(
                            modifier = Modifier
                                .size(49.dp)
                                .align(Alignment.CenterVertically)
                                .padding(vertical = 4.dp),
                            onClick = {
                                scope.launch {
                                    db.notesDao().delete(note)
                                    notes = db.notesDao().getAll()
                                }
                            }
                        ) {
                            Text("-")
                        }
                        Spacer(modifier = Modifier.padding(4.dp))
                        Text(
                            text = note.content,
                            modifier = Modifier
                                .padding(vertical = 4.dp)
                                .align(Alignment.CenterVertically)
                        )
                    }
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Note") },
                modifier = Modifier
                    .weight(1f)
            )
            Spacer(modifier = Modifier.padding(8.dp))
            Button(
                modifier = Modifier
                    .size(56.dp)
                    .align(Alignment.CenterVertically),
                onClick = {
                    scope.launch {
                        if (note.isNotEmpty()) {
                            db.notesDao().insert(Notes(0, note))
                            notes = db.notesDao().getAll()
                            note = ""
                        }
                    }
                }
            ) {
                Text("+")
            }
        }
    }
}

@Composable
fun BiometricAuthenticator(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var activity = context as FragmentActivity
    var authenticated by remember { mutableStateOf(false) }
    val executor = ContextCompat.getMainExecutor(context)

    fun showBiometricPrompt() {
        BiometricPrompt(activity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    authenticated = true
                }
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    exitProcess(0)
                }
                override fun onAuthenticationFailed() {  }
            }
        ).authenticate(
            BiometricPrompt.PromptInfo.Builder()
                .setTitle("Notes App")
                .setDescription("Please authenticate to continue")
                .setAllowedAuthenticators(BiometricManager.Authenticators.DEVICE_CREDENTIAL or BiometricManager.Authenticators.BIOMETRIC_WEAK)
                .build()
        )
    }

    LaunchedEffect(Unit) {
        showBiometricPrompt()
    }

    if (authenticated) {
        Root(
            modifier = modifier
        )
    }
}
