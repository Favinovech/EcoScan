package pe.ecoscan.app.presentation.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import pe.ecoscan.app.R
import pe.ecoscan.app.core.designsystem.component.EcoScanButton
import pe.ecoscan.app.core.designsystem.component.EcoScanButtonVariant
import pe.ecoscan.app.core.designsystem.component.EcoScanCard
import pe.ecoscan.app.core.designsystem.component.EcoScanTopBar
import pe.ecoscan.app.core.designsystem.theme.EcoScanTheme
import pe.ecoscan.app.domain.model.UserProfile
import java.io.File
import kotlin.math.roundToInt

// Punto de entrada con Hilt. Aquí viven los launchers de cámara/galería y las reacciones
// a mensajes y cierre de sesión; ProfileScreen queda sin dependencias para previsualizarla.
// hiltViewModel() de androidx.hilt.navigation.compose está deprecado a favor de
// androidx.hilt.lifecycle.viewmodel.compose, pero ese artefacto sigue en alpha/beta;
// nos quedamos en la versión estable hasta que haya un release estable.
@Suppress("DEPRECATION")
@Composable
fun ProfileRoute(
    onSignedOut: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var cameraUri by rememberSaveable { mutableStateOf<Uri?>(null) }

    // Galería: el selector de fotos del sistema no necesita permisos.
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) viewModel.onPhotoSelected(uri.toString())
    }

    // Cámara: la app de cámara del sistema guarda la foto en el Uri que le pasamos.
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val uri = cameraUri
        if (success && uri != null) viewModel.onPhotoSelected(uri.toString())
    }

    LaunchedEffect(uiState.isSignedOut) {
        if (uiState.isSignedOut) onSignedOut()
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(uiState.infoMessage) {
        uiState.infoMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    ProfileScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onDisplayNameChange = viewModel::onDisplayNameChange,
        onDistrictChange = viewModel::onDistrictChange,
        onNotificationsChange = viewModel::onNotificationsChange,
        onWeeklyGoalChange = viewModel::onWeeklyGoalChange,
        onTakePhoto = {
            val imagesDir = File(context.cacheDir, "images").apply { mkdirs() }
            val photoFile = File.createTempFile("profile_", ".jpg", imagesDir)
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                photoFile
            )
            cameraUri = uri
            cameraLauncher.launch(uri)
        },
        onPickFromGallery = {
            galleryLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        },
        onSave = viewModel::saveProfile,
        onSignOut = viewModel::signOut,
        onDeleteAccount = viewModel::deleteAccount,
        modifier = modifier
    )
}

@Composable
fun ProfileScreen(
    uiState: ProfileUiState,
    snackbarHostState: SnackbarHostState,
    onDisplayNameChange: (String) -> Unit,
    onDistrictChange: (String) -> Unit,
    onNotificationsChange: (Boolean) -> Unit,
    onWeeklyGoalChange: (Int) -> Unit,
    onTakePhoto: () -> Unit,
    onPickFromGallery: () -> Unit,
    onSave: () -> Unit,
    onSignOut: () -> Unit,
    onDeleteAccount: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showPhotoDialog by remember { mutableStateOf(false) }
    var showSignOutDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Cierra el diálogo de eliminar cuando termina (bien o mal); si falló, el error sale en el Snackbar.
    LaunchedEffect(uiState.isDeleting) {
        if (!uiState.isDeleting) showDeleteDialog = false
    }

    Scaffold(
        modifier = modifier,
        topBar = { EcoScanTopBar(title = stringResource(R.string.profile_title)) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (uiState.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ProfileAvatar(
                    photoModel = uiState.pendingPhotoUri ?: uiState.photoUrl,
                    onClick = { showPhotoDialog = true }
                )
                TextButton(onClick = { showPhotoDialog = true }) {
                    Text(stringResource(R.string.profile_change_photo))
                }
                Text(
                    text = uiState.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (!uiState.isSynced) {
                    Text(
                        text = stringResource(R.string.profile_pending_sync),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }

                EcoScanCard {
                    Text(
                        text = stringResource(R.string.profile_section_personal),
                        style = MaterialTheme.typography.titleMedium
                    )
                    OutlinedTextField(
                        value = uiState.displayName,
                        onValueChange = onDisplayNameChange,
                        label = { Text(stringResource(R.string.profile_name_label)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Next
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                    )
                    OutlinedTextField(
                        value = uiState.district,
                        onValueChange = onDistrictChange,
                        label = { Text(stringResource(R.string.profile_district_label)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Done
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                    )
                }

                EcoScanCard {
                    Text(
                        text = stringResource(R.string.profile_section_preferences),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.profile_notifications_label),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = uiState.notificationsEnabled,
                            onCheckedChange = onNotificationsChange
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.profile_weekly_goal_format, uiState.weeklyGoalKg),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Slider(
                        value = uiState.weeklyGoalKg.toFloat(),
                        onValueChange = { onWeeklyGoalChange(it.roundToInt()) },
                        valueRange = UserProfile.MIN_WEEKLY_GOAL_KG.toFloat()..
                                UserProfile.MAX_WEEKLY_GOAL_KG.toFloat(),
                        steps = UserProfile.MAX_WEEKLY_GOAL_KG - UserProfile.MIN_WEEKLY_GOAL_KG - 1
                    )
                }

                EcoScanButton(
                    text = stringResource(R.string.profile_save),
                    onClick = onSave,
                    isLoading = uiState.isSaving,
                    enabled = uiState.hasUnsavedChanges || !uiState.isSynced,
                    modifier = Modifier.fillMaxWidth()
                )
                EcoScanButton(
                    text = stringResource(R.string.profile_sign_out),
                    onClick = { showSignOutDialog = true },
                    variant = EcoScanButtonVariant.TONAL,
                    modifier = Modifier.fillMaxWidth()
                )
                TextButton(
                    onClick = { showDeleteDialog = true },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.profile_delete_account))
                }
            }
        }
    }

    if (showPhotoDialog) {
        PhotoSourceDialog(
            onTakePhoto = {
                showPhotoDialog = false
                onTakePhoto()
            },
            onPickFromGallery = {
                showPhotoDialog = false
                onPickFromGallery()
            },
            onDismiss = { showPhotoDialog = false }
        )
    }
    if (showSignOutDialog) {
        SignOutDialog(
            onConfirm = {
                showSignOutDialog = false
                onSignOut()
            },
            onDismiss = { showSignOutDialog = false }
        )
    }
    if (showDeleteDialog) {
        DeleteAccountDialog(
            isDeleting = uiState.isDeleting,
            onConfirm = onDeleteAccount,
            onDismiss = { showDeleteDialog = false }
        )
    }
}

@Composable
private fun ProfileAvatar(
    photoModel: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(120.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (photoModel != null) {
            AsyncImage(
                model = photoModel,
                contentDescription = stringResource(R.string.profile_photo_description),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = stringResource(R.string.profile_photo_description),
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun ProfileScreenPreview() {
    EcoScanTheme {
        ProfileScreen(
            uiState = ProfileUiState(
                email = "francis@ecoscan.pe",
                displayName = "Francis",
                district = "Miraflores",
                weeklyGoalKg = 10,
                hasUnsavedChanges = true
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onDisplayNameChange = {},
            onDistrictChange = {},
            onNotificationsChange = {},
            onWeeklyGoalChange = {},
            onTakePhoto = {},
            onPickFromGallery = {},
            onSave = {},
            onSignOut = {},
            onDeleteAccount = {}
        )
    }
}