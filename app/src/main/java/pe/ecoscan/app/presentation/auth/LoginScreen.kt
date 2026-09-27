package pe.ecoscan.app.presentation.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.ecoscan.app.core.designsystem.component.EcoScanButton
import pe.ecoscan.app.core.designsystem.component.EcoScanButtonVariant
import pe.ecoscan.app.core.designsystem.component.EcoScanTopBar

@Suppress("DEPRECATION")
@Composable
fun LoginRoute(
    onLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onLoginSuccess()
        }
    }

    LaunchedEffect(uiState.errorMessage, uiState.infoMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
        uiState.infoMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    LoginScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onSignIn = viewModel::signIn,
        onSignUp = viewModel::signUp,
        onForgotPassword = viewModel::sendPasswordReset,
        modifier = modifier
    )
}

@Composable
fun LoginScreen(
    uiState: AuthUiState,
    snackbarHostState: SnackbarHostState,
    onSignIn: (String, String) -> Unit,
    onSignUp: (String, String) -> Unit,
    onForgotPassword: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isRegisterMode by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Scaffold(
        modifier = modifier,
        topBar = {
            EcoScanTopBar(
                title = if (isRegisterMode) "Registro EcoScan" else "Iniciar Sesión"
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "EcoScan",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = if (isRegisterMode) "Crea una cuenta para comenzar a reciclar" else "Ingresa para gestionar tus EcoPuntos",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo electrónico") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            EcoScanButton(
                text = if (isRegisterMode) "Registrarse" else "Ingresar",
                onClick = {
                    if (isRegisterMode) {
                        onSignUp(email, password)
                    } else {
                        onSignIn(email, password)
                    }
                },
                isLoading = uiState.isLoading,
                modifier = Modifier.fillMaxWidth()
            )

            if (!isRegisterMode) {
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = { onForgotPassword(email) }) {
                    Text("¿Olvidaste tu contraseña?")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            EcoScanButton(
                text = if (isRegisterMode) "¿Ya tienes cuenta? Inicia sesión" else "¿No tienes cuenta? Regístrate aquí",
                onClick = { isRegisterMode = !isRegisterMode },
                variant = EcoScanButtonVariant.TEXT
            )
        }
    }
}