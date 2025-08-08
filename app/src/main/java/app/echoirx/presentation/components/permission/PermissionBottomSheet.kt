package app.echoirx.presentation.components.permission

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.echoirx.R
import app.echoirx.data.permission.PermissionType

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PermissionBottomSheet(
    permissionType: PermissionType,
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState()

    val permissionInfo = when (permissionType) {
        PermissionType.STORAGE_LEGACY -> PermissionInfo(
            title = stringResource(R.string.permission_storage_title),
            description = stringResource(R.string.permission_storage_description),
            primaryButtonText = stringResource(R.string.action_grant_permission),
            secondaryButtonText = stringResource(R.string.action_skip)
        )

        PermissionType.MANAGE_EXTERNAL_STORAGE -> PermissionInfo(
            title = stringResource(R.string.permission_manage_storage_title),
            description = stringResource(R.string.permission_manage_storage_description),
            primaryButtonText = stringResource(R.string.action_open_settings),
            secondaryButtonText = stringResource(R.string.action_skip)
        )

        PermissionType.NOTIFICATION_LISTENER -> PermissionInfo(
            title = stringResource(R.string.permission_notification_listener_title),
            description = stringResource(R.string.permission_notification_listener_description),
            primaryButtonText = stringResource(R.string.action_open_settings),
            secondaryButtonText = stringResource(R.string.action_skip)
        )

        PermissionType.POST_NOTIFICATIONS -> PermissionInfo(
            title = stringResource(R.string.permission_post_notifications_title),
            description = stringResource(R.string.permission_post_notifications_description),
            primaryButtonText = stringResource(R.string.action_grant_permission),
            secondaryButtonText = stringResource(R.string.action_skip)
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = permissionInfo.title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
            }

            Text(
                text = permissionInfo.description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            HorizontalDivider()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledTonalButton(
                    onClick = onDismiss,
                    shapes = ButtonDefaults.shapes()
                ) {
                    Text(permissionInfo.secondaryButtonText)
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        when (permissionType) {
                            PermissionType.STORAGE_LEGACY,
                            PermissionType.POST_NOTIFICATIONS -> onRequestPermission()

                            PermissionType.MANAGE_EXTERNAL_STORAGE,
                            PermissionType.NOTIFICATION_LISTENER -> onOpenSettings()
                        }
                    },
                    shapes = ButtonDefaults.shapes()
                ) {
                    Text(permissionInfo.primaryButtonText)
                }
            }
        }
    }
}