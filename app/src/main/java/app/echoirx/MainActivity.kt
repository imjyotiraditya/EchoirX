package app.echoirx

import android.os.Build
import android.os.Bundle
import android.os.Environment
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.compose.rememberNavController
import app.echoirx.data.permission.PermissionsManager
import app.echoirx.presentation.screens.MainScreen
import app.echoirx.presentation.theme.EchoirTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var permissionsManager: PermissionsManager

    private val permissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        checkAndRequestAllFilesAccess()
    }

    private val allFilesAccessLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        checkAndRequestNotificationListenerAccess()
    }

    private val notificationListenerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        requestPermissions()

        setContent {
            val navController = rememberNavController()

            EchoirTheme {
                MainScreen(navController = navController)
            }
        }
    }

    private fun requestPermissions() {
        if (!permissionsManager.arePermissionsGranted()) {
            permissionsLauncher.launch(permissionsManager.requiredPermissions)
        } else {
            checkAndRequestAllFilesAccess()
        }
    }

    private fun checkAndRequestAllFilesAccess() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
            permissionsManager.getAllFilesAccessIntent()?.let { intent ->
                allFilesAccessLauncher.launch(intent)
            }
        } else {
            checkAndRequestNotificationListenerAccess()
        }
    }

    private fun checkAndRequestNotificationListenerAccess() {
        if (!permissionsManager.hasNotificationListenerPermission()) {
            val intent = permissionsManager.getNotificationListenerSettingsIntent()
            notificationListenerLauncher.launch(intent)
        }
    }
}