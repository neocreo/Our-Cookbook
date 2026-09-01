@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.ourcookbook.ui.screens.cookbook

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.graphics.drawable.toBitmap
import com.ourcookbook.R
import com.ourcookbook.domain.model.Cookbook
import com.ourcookbook.ui.components.CookbookPrimaryButton
import com.ourcookbook.ui.components.CookbookSecondaryButton
import com.ourcookbook.ui.components.CookbookTextField
import com.ourcookbook.ui.theme.CookbookSpacing
import com.ourcookbook.ui.theme.CookbookTypography
import com.ourcookbook.ui.viewmodel.CookbookSharingInfo
import com.ourcookbook.ui.viewmodel.ExportFormat
import com.ourcookbook.ui.viewmodel.Permission
import com.ourcookbook.ui.viewmodel.SharedUserInfo
import java.time.Instant

/**
 * Cookbook Creation/Editing Dialog
 * Task 2.1.07: Cookbook Management Screen Implementation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CookbookCreationDialog(
    onDismiss: () -> Unit,
    onCreate: (name: String, description: String, imageUri: String?) -> Unit,
    defaultName: String = "",
    defaultDescription: String = ""
) {
    var name by remember { mutableStateOf(defaultName) }
    var description by remember { mutableStateOf(defaultDescription) }
    var imageUri by remember { mutableStateOf<String?>(null) }
    var nameError by remember { mutableStateOf<String?>(null) }
    
    val context = LocalContext.current
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { imageUri = it.toString() }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        ElevatedCard(
            modifier = Modifier
                .padding(CookbookSpacing.medium)
                .fillMaxWidth(0.9f),
            shape = MaterialTheme.shapes.large,
            colors = androidx.compose.material3.CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(CookbookSpacing.large)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(CookbookSpacing.medium)
            ) {
                Text(
                    text = "Create New Cookbook",
                    style = CookbookTypography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Image selection
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(CookbookSpacing.medium)
                        )
                        .border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(CookbookSpacing.medium)
                        )
                        .clickable { imagePickerLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (imageUri != null) {
                        // In production, this would display the actual image
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = "Cookbook Image",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(CookbookSpacing.small)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddAPhoto,
                                contentDescription = "Add Image",
                                modifier = Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Text(
                                text = "Add Cover Image",
                                style = CookbookTypography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }

                // Name field
                CookbookTextField(
                    value = name,
                    onValueChange = { 
                        name = it
                        nameError = if (it.isBlank()) "Cookbook name is required" else null
                    },
                    label = "Cookbook Name",
                    placeholder = "Enter cookbook name",
                    isError = nameError != null,
                    errorMessage = nameError,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next
                    )
                )

                // Description field
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    placeholder = { Text("Enter cookbook description (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done
                    ),
                    shape = MaterialTheme.shapes.small,
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                Spacer(modifier = Modifier.height(CookbookSpacing.medium))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(
                            text = "CANCEL",
                            style = CookbookTypography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }

                    Spacer(modifier = Modifier.width(CookbookSpacing.medium))

                    Button(
                        onClick = {
                            if (name.isBlank()) {
                                nameError = "Cookbook name is required"
                            } else {
                                onCreate(name, description, imageUri)
                            }
                        },
                        enabled = name.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)
                        ),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(
                            text = "CREATE",
                            style = CookbookTypography.labelLarge
                        )
                    }
                }
            }
        }
    }
}

/**
 * Cookbook Editing Dialog
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CookbookEditingDialog(
    cookbook: Cookbook,
    onDismiss: () -> Unit,
    onSave: (name: String, description: String, imageUri: String?) -> Unit
) {
    var name by remember { mutableStateOf(cookbook.name) }
    var description by remember { mutableStateOf(cookbook.description ?: "") }
    var imageUri by remember { mutableStateOf<String?>(null) }
    var nameError by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { imageUri = it.toString() }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        ElevatedCard(
            modifier = Modifier
                .padding(CookbookSpacing.medium)
                .fillMaxWidth(0.9f),
            shape = MaterialTheme.shapes.large,
            colors = androidx.compose.material3.CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(CookbookSpacing.large)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(CookbookSpacing.medium)
            ) {
                Text(
                    text = "Edit Cookbook",
                    style = CookbookTypography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Image selection
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(CookbookSpacing.medium)
                        )
                        .border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(CookbookSpacing.medium)
                        )
                        .clickable { imagePickerLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (imageUri != null) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = "Cookbook Image",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(CookbookSpacing.small)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddAPhoto,
                                contentDescription = "Add Image",
                                modifier = Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Text(
                                text = "Change Cover Image",
                                style = CookbookTypography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }

                // Name field
                CookbookTextField(
                    value = name,
                    onValueChange = { 
                        name = it
                        nameError = if (it.isBlank()) "Cookbook name is required" else null
                    },
                    label = "Cookbook Name",
                    placeholder = "Enter cookbook name",
                    isError = nameError != null,
                    errorMessage = nameError,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next
                    )
                )

                // Description field
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    placeholder = { Text("Enter cookbook description (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done
                    ),
                    shape = MaterialTheme.shapes.small,
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                Spacer(modifier = Modifier.height(CookbookSpacing.medium))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(
                            text = "CANCEL",
                            style = CookbookTypography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }

                    Spacer(modifier = Modifier.width(CookbookSpacing.medium))

                    Button(
                        onClick = {
                            if (name.isBlank()) {
                                nameError = "Cookbook name is required"
                            } else {
                                onSave(name, description, imageUri)
                            }
                        },
                        enabled = name.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)
                        ),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(
                            text = "SAVE",
                            style = CookbookTypography.labelLarge
                        )
                    }
                }
            }
        }
    }
}

/**
 * Delete Confirmation Dialog
 */
@Composable
fun CookbookDeleteConfirmationDialog(
    cookbookName: String,
    hasRecipes: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(CookbookSpacing.small))
                Text(
                    text = "Delete Cookbook",
                    style = CookbookTypography.headlineSmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(CookbookSpacing.small)
            ) {
                Text(
                    text = "Are you sure you want to delete \"$cookbookName\"?",
                    style = CookbookTypography.bodyMedium
                )
                
                if (hasRecipes) {
                    Text(
                        text = "This cookbook contains recipes. Deleting it will remove all recipes from this collection.",
                        style = CookbookTypography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                
                Text(
                    text = "This action cannot be undone.",
                    style = CookbookTypography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(
                    text = "DELETE",
                    style = CookbookTypography.labelLarge
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "CANCEL",
                    style = CookbookTypography.labelLarge
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.large
    )
}

/**
 * Bulk Delete Confirmation Dialog
 */
@Composable
fun BulkDeleteConfirmationDialog(
    cookbookCount: Int,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(CookbookSpacing.small))
                Text(
                    text = "Delete Multiple Cookbooks",
                    style = CookbookTypography.headlineSmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        text = {
            Text(
                text = "Are you sure you want to delete $cookbookCount cookbooks? This action cannot be undone.",
                style = CookbookTypography.bodyMedium
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(
                    text = "DELETE ALL",
                    style = CookbookTypography.labelLarge
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "CANCEL",
                    style = CookbookTypography.labelLarge
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.large
    )
}

/**
 * Share Cookbook Dialog
 */
@Composable
fun ShareCookbookDialog(
    cookbook: Cookbook,
    onDismiss: () -> Unit,
    onShareWithUsers: (userIds: List<String>, permissions: Set<Permission>) -> Unit,
    onGenerateLink: () -> Unit
) {
    var selectedPermissions by remember { mutableStateOf(setOf(Permission.VIEW)) }
    var userIds by remember { mutableStateOf(listOf<String>()) }
    var newUserId by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        ElevatedCard(
            modifier = Modifier
                .padding(CookbookSpacing.medium)
                .fillMaxWidth(0.9f),
            shape = MaterialTheme.shapes.large,
            colors = androidx.compose.material3.CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(CookbookSpacing.large)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(CookbookSpacing.medium)
            ) {
                Text(
                    text = "Share Cookbook",
                    style = CookbookTypography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "Share \"${cookbook.name}\" with other users or generate a sharing link.",
                    style = CookbookTypography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )

                // Permission selection
                Text(
                    text = "Permissions",
                    style = CookbookTypography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(CookbookSpacing.medium)
                ) {
                    PermissionChip(
                        permission = Permission.VIEW,
                        isSelected = Permission.VIEW in selectedPermissions,
                        onSelected = { 
                            selectedPermissions = if (Permission.VIEW in selectedPermissions) {
                                selectedPermissions - Permission.VIEW
                            } else {
                                selectedPermissions + Permission.VIEW
                            }
                        }
                    )
                    
                    PermissionChip(
                        permission = Permission.EDIT,
                        isSelected = Permission.EDIT in selectedPermissions,
                        onSelected = { 
                            selectedPermissions = if (Permission.EDIT in selectedPermissions) {
                                selectedPermissions - Permission.EDIT
                            } else {
                                selectedPermissions + Permission.EDIT
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(CookbookSpacing.medium))

                // User selection (simplified for now)
                Text(
                    text = "Share with users",
                    style = CookbookTypography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                OutlinedTextField(
                    value = newUserId,
                    onValueChange = { newUserId = it },
                    label = { Text("User ID or Email") },
                    placeholder = { Text("Enter user ID or email") },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        if (newUserId.isNotBlank()) {
                            IconButton(onClick = { 
                                if (newUserId.isNotBlank() && !userIds.contains(newUserId)) {
                                    userIds = userIds + newUserId
                                    newUserId = ""
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add User"
                                )
                            }
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done
                    ),
                    shape = MaterialTheme.shapes.small
                )

                // Selected users
                if (userIds.isNotEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(CookbookSpacing.small)
                    ) {
                        Text(
                            text = "Selected Users:",
                            style = CookbookTypography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        
                        userIds.forEach { userId ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = userId,
                                    style = CookbookTypography.bodyMedium
                                )
                                IconButton(onClick = { userIds = userIds - userId }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Remove User",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(CookbookSpacing.medium))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(CookbookSpacing.medium)
                ) {
                    OutlinedButton(
                        onClick = { 
                            if (userIds.isNotEmpty()) {
                                onShareWithUsers(userIds, selectedPermissions)
                            }
                        },
                        enabled = userIds.isNotEmpty(),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Share with Users",
                            style = CookbookTypography.labelLarge
                        )
                    }

                    Button(
                        onClick = onGenerateLink,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Link,
                            contentDescription = "Generate Link",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(CookbookSpacing.xSmall))
                        Text(
                            text = "Generate Link",
                            style = CookbookTypography.labelLarge
                        )
                    }
                }

                Spacer(modifier = Modifier.height(CookbookSpacing.small))

                TextButton(onClick = onDismiss) {
                    Text(
                        text = "CANCEL",
                        style = CookbookTypography.labelLarge
                    )
                }
            }
        }
    }
}

/**
 * Permission chip for sharing dialog
 */
@Composable
fun PermissionChip(
    permission: Permission,
    isSelected: Boolean,
    onSelected: () -> Unit
) {
    val icon = when (permission) {
        Permission.VIEW -> Icons.Default.Info
        Permission.EDIT -> Icons.Default.Edit
    }
    
    val label = when (permission) {
        Permission.VIEW -> "View"
        Permission.EDIT -> "Edit"
    }

    OutlinedButton(
        onClick = onSelected,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
        ),
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(
                width = 2.dp,
                color = MaterialTheme.colorScheme.primary
            )
        } else {
            androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline
            )
        }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(CookbookSpacing.xSmall))
        Text(
            text = label,
            style = CookbookTypography.labelMedium
        )
    }
}

/**
 * Sharing Link Dialog with QR Code
 */
@Composable
fun SharingLinkDialog(
    sharingLink: String,
    qrCodeData: String?,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        ElevatedCard(
            modifier = Modifier
                .padding(CookbookSpacing.medium)
                .fillMaxWidth(0.9f),
            shape = MaterialTheme.shapes.large,
            colors = androidx.compose.material3.CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(CookbookSpacing.large)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(CookbookSpacing.medium),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Share Cookbook",
                    style = CookbookTypography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "Anyone with this link can access your cookbook",
                    style = CookbookTypography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )

                // QR Code placeholder
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(CookbookSpacing.medium)
                        )
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(CookbookSpacing.medium)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCode,
                        contentDescription = "QR Code",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Text(
                    text = "Scan QR Code or share the link below",
                    style = CookbookTypography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                // Link display
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = androidx.compose.material3.CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(CookbookSpacing.medium),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = sharingLink,
                            style = CookbookTypography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1
                        )
                        IconButton(onClick = { /* Copy to clipboard */ }) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share Link"
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(CookbookSpacing.medium))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "DONE",
                        style = CookbookTypography.labelLarge
                    )
                }
            }
        }
    }
}

/**
 * Export Cookbook Dialog
 */
@Composable
fun ExportCookbookDialog(
    cookbook: Cookbook,
    onDismiss: () -> Unit,
    onExport: (format: ExportFormat) -> Unit
) {
    var selectedFormat by remember { mutableStateOf<ExportFormat?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        ElevatedCard(
            modifier = Modifier
                .padding(CookbookSpacing.medium)
                .fillMaxWidth(0.9f),
            shape = MaterialTheme.shapes.large,
            colors = androidx.compose.material3.CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(CookbookSpacing.large)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(CookbookSpacing.medium)
            ) {
                Text(
                    text = "Export Cookbook",
                    style = CookbookTypography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "Export \"${cookbook.name}\" to a file that can be shared or backed up.",
                    style = CookbookTypography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )

                Text(
                    text = "Select export format:",
                    style = CookbookTypography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(CookbookSpacing.small)
                ) {
                    ExportFormatOption(
                        format = ExportFormat.JSON,
                        isSelected = selectedFormat == ExportFormat.JSON,
                        onSelected = { selectedFormat = ExportFormat.JSON },
                        description = "Structured data format, easily importable"
                    )
                    
                    ExportFormatOption(
                        format = ExportFormat.MARKDOWN,
                        isSelected = selectedFormat == ExportFormat.MARKDOWN,
                        onSelected = { selectedFormat = ExportFormat.MARKDOWN },
                        description = "Human-readable format for documentation"
                    )
                    
                    ExportFormatOption(
                        format = ExportFormat.PDF,
                        isSelected = selectedFormat == ExportFormat.PDF,
                        onSelected = { selectedFormat = ExportFormat.PDF },
                        description = "Printable format with formatting"
                    )
                }

                Spacer(modifier = Modifier.height(CookbookSpacing.medium))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(
                            text = "CANCEL",
                            style = CookbookTypography.labelLarge
                        )
                    }

                    Spacer(modifier = Modifier.width(CookbookSpacing.medium))

                    Button(
                        onClick = { 
                            selectedFormat?.let { onExport(it) }
                        },
                        enabled = selectedFormat != null,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text(
                            text = "EXPORT",
                            style = CookbookTypography.labelLarge
                        )
                    }
                }
            }
        }
    }
}

/**
 * Export format option
 */
@Composable
fun ExportFormatOption(
    format: ExportFormat,
    isSelected: Boolean,
    onSelected: () -> Unit,
    description: String
) {
    val icon = when (format) {
        ExportFormat.JSON -> Icons.Default.Code
        ExportFormat.MARKDOWN -> Icons.Default.TextFields
        ExportFormat.PDF -> Icons.Default.PictureAsPdf
    }
    
    val label = when (format) {
        ExportFormat.JSON -> "JSON"
        ExportFormat.MARKDOWN -> "Markdown"
        ExportFormat.PDF -> "PDF"
    }

    Card(
        onClick = onSelected,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CookbookSpacing.medium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(CookbookSpacing.medium)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
                
                Column(
                    verticalArrangement = Arrangement.spacedBy(CookbookSpacing.xSmall)
                ) {
                    Text(
                        text = label,
                        style = CookbookTypography.bodyLarge,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = description,
                        style = CookbookTypography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/**
 * Import Cookbook Dialog
 */
@Composable
fun ImportCookbookDialog(
    supportedFormats: List<ExportFormat>,
    onDismiss: () -> Unit,
    onImport: (file: File, format: ExportFormat) -> Unit
) {
    var selectedFormat by remember { mutableStateOf<ExportFormat?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        ElevatedCard(
            modifier = Modifier
                .padding(CookbookSpacing.medium)
                .fillMaxWidth(0.9f),
            shape = MaterialTheme.shapes.large,
            colors = androidx.compose.material3.CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(CookbookSpacing.large)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(CookbookSpacing.medium)
            ) {
                Text(
                    text = "Import Cookbook",
                    style = CookbookTypography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "Import a cookbook from a file to add it to your collection.",
                    style = CookbookTypography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )

                Text(
                    text = "Select import format:",
                    style = CookbookTypography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(CookbookSpacing.small)
                ) {
                    supportedFormats.forEach { format ->
                        ExportFormatOption(
                            format = format,
                            isSelected = selectedFormat == format,
                            onSelected = { selectedFormat = format },
                            description = when (format) {
                                ExportFormat.JSON -> "Structured data format"
                                ExportFormat.MARKDOWN -> "Human-readable format"
                                ExportFormat.PDF -> "Printable format"
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(CookbookSpacing.medium))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(
                            text = "CANCEL",
                            style = CookbookTypography.labelLarge
                        )
                    }

                    Spacer(modifier = Modifier.width(CookbookSpacing.medium))

                    Button(
                        onClick = { 
                            selectedFormat?.let { 
                                // In production, this would open a file picker
                                // For now, just call the callback
                                onImport(File("selected_file.${selectedFormat.name.lowercase()}"), selectedFormat)
                            }
                        },
                        enabled = selectedFormat != null,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text(
                            text = "IMPORT",
                            style = CookbookTypography.labelLarge
                        )
                    }
                }
            }
        }
    }
}

/**
 * Progress Dialog for Export/Import operations
 */
@Composable
fun ProgressDialog(
    title: String,
    message: String,
    progress: Int, // 0-100
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        ElevatedCard(
            modifier = Modifier
                .padding(CookbookSpacing.medium)
                .fillMaxWidth(0.8f),
            shape = MaterialTheme.shapes.large,
            colors = androidx.compose.material3.CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(CookbookSpacing.large)
            ) {
                CircularProgressIndicator(
                    progress = { progress / 100f },
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 4.dp,
                    modifier = Modifier.size(64.dp)
                )

                Spacer(modifier = Modifier.height(CookbookSpacing.medium))

                Text(
                    text = title,
                    style = CookbookTypography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(CookbookSpacing.small))

                Text(
                    text = "$message ($progress%)",
                    style = CookbookTypography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * Success Dialog for Export/Import operations
 */
@Composable
fun SuccessDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        ElevatedCard(
            modifier = Modifier
                .padding(CookbookSpacing.medium)
                .fillMaxWidth(0.8f),
            shape = MaterialTheme.shapes.large,
            colors = androidx.compose.material3.CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(CookbookSpacing.large)
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Success",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(64.dp)
                )

                Spacer(modifier = Modifier.height(CookbookSpacing.medium))

                Text(
                    text = title,
                    style = CookbookTypography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(CookbookSpacing.small))

                Text(
                    text = message,
                    style = CookbookTypography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(CookbookSpacing.medium))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "OK",
                        style = CookbookTypography.labelLarge
                    )
                }
            }
        }
    }
}

/**
 * Preview for dialog components
 */
@Preview(showBackground = true)
@Composable
fun CookbookDialogsPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier.padding(CookbookSpacing.medium),
            verticalArrangement = Arrangement.spacedBy(CookbookSpacing.medium)
        ) {
            Text(
                text = "Cookbook Dialogs Preview",
                style = CookbookTypography.headlineMedium
            )
            
            Text(
                text = "Dialogs are best previewed in actual usage context",
                style = CookbookTypography.bodyMedium
            )
        }
    }
}