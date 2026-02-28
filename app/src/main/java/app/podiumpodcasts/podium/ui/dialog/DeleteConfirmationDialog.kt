package app.podiumpodcasts.podium.ui.dialog

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import app.podiumpodcasts.podium.R

@Composable
fun DeleteConfirmationDialog(
    onDismiss: () -> Unit,
    itemName: String,
    additionalText: String = "",
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {
            onDismiss()
        },
        icon = {
            Icon(
                Icons.Rounded.Warning,
                contentDescription = stringResource(R.string.common_confirmation)
            )
        },
        title = {
            Text(stringResource(R.string.common_are_you_sure))
        },
        text = {
            Text(
                text = stringResource(
                    R.string.dialog_delete_confirmation_text,
                    itemName
                ) + " $additionalText"
            )
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismiss()
                },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Text(stringResource(R.string.common_action_abort))
            }
        },
        containerColor = MaterialTheme.colorScheme.errorContainer,
        iconContentColor = MaterialTheme.colorScheme.error,
        titleContentColor = MaterialTheme.colorScheme.onErrorContainer,
        textContentColor = MaterialTheme.colorScheme.onErrorContainer,
        confirmButton = {
            Button(
                onClick = {
                    onDismiss()
                    onConfirm()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) {
                Text(stringResource(R.string.common_action_yes))
            }
        }
    )
}