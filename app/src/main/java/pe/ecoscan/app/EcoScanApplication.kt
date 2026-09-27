package pe.ecoscan.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

// Clase Application que habilita el contenedor de dependencias de Hilt para toda la app.
@HiltAndroidApp
class EcoScanApplication : Application()
