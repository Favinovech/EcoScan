package pe.ecoscan.app.core.network

import kotlinx.coroutines.flow.Flow

// Informa si el dispositivo tiene internet. Es una interfaz para poder sustituirla en tests.
interface ConnectivityObserver {
    val isOnline: Flow<Boolean>
}