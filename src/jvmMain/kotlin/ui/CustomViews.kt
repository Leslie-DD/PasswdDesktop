package ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberDialogState
import kotlinx.coroutines.launch
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomOutlinedTextField(
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    maxLines: Int = 1,
    leadingIcon: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    colors: TextFieldColors = TextFieldDefaults.outlinedTextFieldColors(
        textColor = MaterialTheme.colorScheme.onPrimaryContainer,
        cursorColor = MaterialTheme.colorScheme.onPrimaryContainer,
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        focusedBorderColor = MaterialTheme.colorScheme.secondary,
        unfocusedBorderColor = MaterialTheme.colorScheme.onSecondaryContainer,
        placeholderColor = MaterialTheme.colorScheme.outline
    ),
    contentPadding: PaddingValues = PaddingValues(horizontal = 10.dp)
) {
    val coroutineScope = rememberCoroutineScope()
    var text by rememberSaveable { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    BasicTextField(
        value = text,
        modifier = modifier
            .focusRequester(focusRequester)
            .onFocusChanged {
                text = ""
                onValueChange("")
            },
        onValueChange = {
            text = it
            onValueChange(it)
        },
        textStyle = LocalTextStyle.current.merge(TextStyle(color = textColor().value)),
        cursorBrush = SolidColor(cursorColor().value),
        interactionSource = interactionSource,
        singleLine = singleLine,
        maxLines = maxLines,
        decorationBox = @Composable { innerTextField ->
            TextFieldDefaults.OutlinedTextFieldDecorationBox(
                value = text,
                visualTransformation = VisualTransformation.None,
                innerTextField = innerTextField,
                placeholder = placeholder,
                leadingIcon = leadingIcon,
                enabled = true,
                singleLine = singleLine,
                interactionSource = interactionSource,
                colors = colors,
                contentPadding = contentPadding,
                container = {
                    TextFieldDefaults.OutlinedBorderContainerBox(
                        enabled = true,
                        isError = false,
                        interactionSource,
                        colors,
                        RoundedCornerShape(30)
                    )
                }
            )
        }
    )

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            focusRequester.requestFocus()
        }
    }
}

@Composable
private fun textColor(): State<Color> {
    return rememberUpdatedState(MaterialTheme.colorScheme.onPrimaryContainer)
}

@Composable
private fun cursorColor(): State<Color> {
    return rememberUpdatedState(MaterialTheme.colorScheme.onPrimaryContainer)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnabledOutlinedTextField(
    enabled: Boolean = true,
    modifier: Modifier = Modifier.width(300.dp),
    value: String,
    labelValue: String,
    maxLines: Int = 1,
    singleLine: Boolean = true,
    leadingIconImageVector: ImageVector? = null,
    disableContentEncrypt: Boolean = true,
    colors: TextFieldColors = TextFieldDefaults.outlinedTextFieldColors(
        textColor = MaterialTheme.colorScheme.onPrimaryContainer,
        focusedBorderColor = MaterialTheme.colorScheme.secondary,
        unfocusedBorderColor = MaterialTheme.colorScheme.onSecondaryContainer,
        focusedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
        cursorColor = MaterialTheme.colorScheme.onPrimaryContainer,
        selectionColors = TextSelectionColors(
            handleColor = Color.White,
            backgroundColor = Color.Blue
        )
    ),
    onValueChange: (String) -> Unit
) {
    val text = remember { mutableStateOf(value) }
    var contentVisible by remember { mutableStateOf(disableContentEncrypt) }

    OutlinedTextField(
        modifier = modifier,
        enabled = enabled,
        label = {
            Text(
                text = labelValue,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        leadingIcon = {
            leadingIconImageVector?.let {
                Icon(imageVector = leadingIconImageVector, contentDescription = null)
            }
        },
        visualTransformation = if (contentVisible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = if (disableContentEncrypt) {
            null
        } else {
            {
                Box(
                    modifier = Modifier
                        .wrapContentSize()
                        .padding(end = 10.dp)
                        .focusProperties { canFocus = false }
                ) {
                    IconButton(
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        onClick = {
                            contentVisible = !contentVisible
                        }
                    ) {
                        Icon(
                            imageVector = if (contentVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                            contentDescription = if (contentVisible) "Hide content" else "Show content"
                        )
                    }
                }
            }
        },
        value = text.value,
        maxLines = maxLines,
        singleLine = singleLine,
        onValueChange = {
            text.value = it
            onValueChange(it)
        },
        colors = colors
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadableTextField(
    modifier: Modifier = Modifier.fillMaxWidth(),
    label: String,
    leadingIcon: ImageVector,
    value: String,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    TextField(
        enabled = false,
        modifier = modifier,
        label = { Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        leadingIcon = {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        },
        value = value,
        maxLines = 1,
        onValueChange = { },
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        trailingIcon = trailingIcon,
        colors = TextFieldDefaults.textFieldColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            disabledTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
            disabledLabelColor = MaterialTheme.colorScheme.outline,
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadableOutlinedTextField(
    modifier: Modifier = Modifier.fillMaxWidth(),
    label: String,
    leadingIcon: ImageVector,
    value: String,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    OutlinedTextField(
        enabled = false,
        modifier = modifier,
        label = { Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        leadingIcon = {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        },
        value = value,
        maxLines = 1,
        onValueChange = { },
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        trailingIcon = trailingIcon,
        colors = TextFieldDefaults.outlinedTextFieldColors(
            disabledTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
            disabledBorderColor = MaterialTheme.colorScheme.secondary,
            disabledLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
            cursorColor = MaterialTheme.colorScheme.onPrimaryContainer,
            selectionColors = TextSelectionColors(
                handleColor = Color.White,
                backgroundColor = Color.Blue
            )
        ),
    )
}

@Composable
fun RowSpacer() {
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(16.dp)
            .background(color = MaterialTheme.colorScheme.background)
            .border(width = 1.dp, color = MaterialTheme.colorScheme.onBackground)
    )
}

fun String.copyToClipboard() = try {
    Toolkit.getDefaultToolkit()
        .systemClipboard
        .setContents(StringSelection(this), null)
} catch (throwable: Throwable) {
    throwable.printStackTrace()
}

@Composable
fun GlobalDialog(
    title: String,
    position: WindowPosition = WindowPosition(Alignment.Center),
    size: DpSize = DpSize(500.dp, 300.dp),
    onCloseClick: () -> Unit,
    onConfirmClick: ((Any) -> Unit)? = null,
    onCancelClick: (() -> Unit)? = null,
    confirmString: String = "Confirm",
    cancelString: String = "Cancel",
    content: @Composable BoxScope.() -> Unit
) {

    Dialog(
        title = title,
        onCloseRequest = { onCloseClick() },
        state = rememberDialogState(position, size)
    ) {
        window.rootPane.apply {
            rootPane.putClientProperty("apple.awt.fullWindowContent", true)
            rootPane.putClientProperty("apple.awt.transparentTitleBar", true)
            rootPane.putClientProperty("apple.awt.windowTitleVisible", false)
        }

        val enable = remember { mutableStateOf(true) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(horizontal = 100.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(28.dp))
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontSize = 18.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(16.dp))

            Box(content = content)

            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                onConfirmClick?.let {
                    Button(
                        colors = ButtonDefaults.buttonColors(
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        ),
                        enabled = enable.value,
                        onClick = {
                            enable.value = false
                            onConfirmClick(true)
                        }
                    ) {
                        Text(
                            text = confirmString,
                            maxLines = 1,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.width(20.dp))
                }
                onCancelClick?.let {
                    Button(
                        colors = ButtonDefaults.buttonColors(
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        ),
                        enabled = enable.value,
                        onClick = {
                            enable.value = false
                            onCancelClick()
                        }
                    ) {
                        Text(
                            text = cancelString,
                            maxLines = 1,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

            }
        }
    }
}