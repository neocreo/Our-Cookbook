package com.ourcookbook.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ourcookbook.ui.theme.CookbookSpacing
import com.ourcookbook.ui.theme.CookbookTypography

/**
 * Base text field component with consistent styling
 */
@Composable
fun CookbookTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    trailingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    maxLines: Int = 1,
    readOnly: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    onClearClick: (() -> Unit)? = null,
    imeAction: ImeAction = ImeAction.Next,
    onNext: (() -> Unit)? = null
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            placeholder = placeholder?.let { { Text(it) } },
            leadingIcon = leadingIcon?.let { 
                { 
                    Icon(
                        imageVector = it, 
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
            },
            trailingIcon = {
                if (onClearClick != null && value.isNotEmpty()) {
                    IconButton(onClick = onClearClick) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                } else if (trailingIcon != null) {
                    Icon(
                        imageVector = trailingIcon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
            },
            isError = isError,
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = imeAction
            ),
            keyboardActions = KeyboardActions(
                onNext = { onNext?.invoke() }
            ),
            maxLines = maxLines,
            readOnly = readOnly,
            visualTransformation = visualTransformation,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                errorBorderColor = MaterialTheme.colorScheme.error,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                errorLabelColor = MaterialTheme.colorScheme.error,
                focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
                unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            ),
            shape = MaterialTheme.shapes.small
        )

        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = CookbookTypography.labelSmall,
                modifier = Modifier.padding(start = CookbookSpacing.small, top = CookbookSpacing.xSmall)
            )
        }
    }
}

/**
 * Multiline text field for descriptions, notes, etc.
 */
@Composable
fun CookbookMultilineTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    minLines: Int = 3,
    maxLines: Int = Int.MAX_VALUE
) {
    CookbookTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        modifier = modifier,
        placeholder = placeholder,
        isError = isError,
        errorMessage = errorMessage,
        maxLines = maxLines,
        keyboardType = KeyboardType.Text,
        readOnly = false,
        imeAction = ImeAction.Default,
        minLines = minLines
    )
}

/**
 * Number input field with validation
 */
@Composable
fun CookbookNumberField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    suffix: String? = null,
    allowDecimals: Boolean = false
) {
    CookbookTextField(
        value = value,
        onValueChange = { newValue ->
            if (allowDecimals) {
                if (newValue.all { it.isDigit() || it == '.' }) {
                    onValueChange(newValue)
                }
            } else {
                if (newValue.all { it.isDigit() }) {
                    onValueChange(newValue)
                }
            }
        },
        label = label,
        modifier = modifier,
        placeholder = placeholder,
        isError = isError,
        errorMessage = errorMessage,
        keyboardType = KeyboardType.Number,
        trailingIcon = if (suffix != null) Icons.Default.Info else null,
        imeAction = ImeAction.Next
    )
}

/**
 * Search field with search icon and clear functionality
 */
@Composable
fun CookbookSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search recipes...",
    onSearch: (() -> Unit)? = null,
    onClear: (() -> Unit)? = null
) {
    CookbookTextField(
        value = value,
        onValueChange = onValueChange,
        label = "Search",
        modifier = modifier,
        placeholder = placeholder,
        leadingIcon = Icons.Default.Search,
        onClearClick = { 
            onValueChange("")
            onClear?.invoke()
        },
        keyboardType = KeyboardType.Text,
        imeAction = ImeAction.Search,
        onNext = { onSearch?.invoke() }
    )
}

/**
 * Password field with hidden input
 */
@Composable
fun CookbookPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    isError: Boolean = false,
    errorMessage: String? = null
) {
    CookbookTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        modifier = modifier,
        placeholder = placeholder,
        isError = isError,
        errorMessage = errorMessage,
        keyboardType = KeyboardType.Password,
        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation()
    )
}

/**
 * Email field with email validation
 */
@Composable
fun CookbookEmailField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    isError: Boolean = false,
    errorMessage: String? = null
) {
    CookbookTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        modifier = modifier,
        placeholder = placeholder,
        isError = isError,
        errorMessage = errorMessage,
        keyboardType = KeyboardType.Email
    )
}

/**
 * Preview for input field components
 */
@Preview(showBackground = true)
@Composable
fun InputFieldsPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier.padding(CookbookSpacing.medium),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(CookbookSpacing.medium)
        ) {
            CookbookTextField(
                value = "Spaghetti Carbonara",
                onValueChange = {},
                label = "Recipe Title",
                placeholder = "Enter recipe title"
            )
            
            CookbookMultilineTextField(
                value = "Classic Italian pasta dish with eggs, cheese, pancetta, and black pepper",
                onValueChange = {},
                label = "Description",
                placeholder = "Enter recipe description"
            )
            
            CookbookNumberField(
                value = "30",
                onValueChange = {},
                label = "Cook Time",
                suffix = "minutes"
            )
            
            CookbookSearchField(
                value = "pasta",
                onValueChange = {},
                placeholder = "Search recipes..."
            )
            
            CookbookTextField(
                value = "error@example.com",
                onValueChange = {},
                label = "Email",
                isError = true,
                errorMessage = "Please enter a valid email address"
            )
        }
    }
}
