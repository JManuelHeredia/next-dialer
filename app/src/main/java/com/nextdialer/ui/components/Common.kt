package com.nextdialer.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallMade
import androidx.compose.material.icons.filled.CallMissed
import androidx.compose.material.icons.filled.CallReceived
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Voicemail
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nextdialer.data.CallKind
import com.nextdialer.ui.theme.DialerTheme
import com.nextdialer.ui.theme.LocalTextScale

@Composable
fun SearchHeader(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    theme: DialerTheme,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        shape = RoundedCornerShape(26.dp),
        color = theme.searchField,
        border = if (theme.useWallpaper) BorderStroke(1.dp, theme.divider) else null,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .padding(start = 16.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = null,
                tint = theme.textSecondary,
                modifier = Modifier.size(22.dp),
            )
            Spacer(Modifier.width(12.dp))
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.weight(1f),
                singleLine = true,
                textStyle = TextStyle(
                    color = theme.textPrimary,
                    fontSize = 16.sp,
                ),
                cursorBrush = SolidColor(theme.accent),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                decorationBox = { innerTextField ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (query.isEmpty()) {
                            Text(
                                text = placeholder,
                                color = theme.textSecondary,
                                fontSize = 16.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        innerTextField()
                    }
                },
            )
            if (query.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clickable { onQueryChange("") },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = "Clear search",
                        tint = theme.textSecondary,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
    }
}

@Composable
fun PermissionBanner(
    message: String,
    theme: DialerTheme,
    onGrant: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        color = theme.accent.copy(alpha = 0.14f),
    ) {
        Row(
            modifier = Modifier.padding(start = 14.dp, end = 4.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Filled.Info,
                contentDescription = null,
                tint = theme.accent,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = message,
                color = theme.textPrimary,
                fontSize = 13.sp,
                lineHeight = 17.sp,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = onGrant) {
                Text("Grant", color = theme.accent, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    message: String,
    theme: DialerTheme,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(76.dp)
                .background(theme.surface.copy(alpha = 0.65f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = theme.textSecondary,
                modifier = Modifier.size(36.dp),
            )
        }
        Spacer(Modifier.height(18.dp))
        Text(
            text = title,
            color = theme.textPrimary,
            fontSize = 19.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = message,
            color = theme.textSecondary,
            fontSize = 14.sp,
            lineHeight = 19.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(12.dp))
            TextButton(onClick = onAction) {
                Text(actionLabel, color = theme.accent, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun DayHeader(label: String, theme: DialerTheme) {
    Text(
        text = label.uppercase(),
        color = theme.textSecondary,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.4.sp,
        modifier = Modifier.padding(start = 22.dp, end = 22.dp, top = 18.dp, bottom = 6.dp),
    )
}

@Composable
fun SettingsPage(
    title: String,
    theme: DialerTheme,
    onBack: () -> Unit,
    content: LazyListScope.() -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.settingsBackground)
            .navigationBarsPadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 6.dp, end = 16.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = theme.textPrimary,
                )
            }
            Text(
                text = title,
                color = theme.textPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 36.dp),
            content = content,
        )
    }
}

@Composable
fun SettingsSection(title: String, theme: DialerTheme) {
    Text(
        text = title.uppercase(),
        color = theme.accent,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.3.sp,
        modifier = Modifier.padding(start = 22.dp, end = 22.dp, top = 22.dp, bottom = 8.dp),
    )
}

@Composable
fun SettingsNote(text: String, theme: DialerTheme) {
    Text(
        text = text,
        color = theme.textSecondary,
        fontSize = 12.sp,
        lineHeight = 17.sp,
        modifier = Modifier.padding(start = 22.dp, end = 22.dp, top = 4.dp, bottom = 12.dp),
    )
}

@Composable
fun SettingsRow(
    title: String,
    theme: DialerTheme,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
    trailing: @Composable () -> Unit = {},
) {
    val scale = LocalTextScale.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) Modifier.clickable { onClick() } else Modifier,
            )
            .padding(horizontal = 22.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                color = theme.textPrimary,
                fontSize = (16f * scale).sp,
            )
            if (subtitle != null) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    color = theme.textSecondary,
                    fontSize = (12.5f * scale).sp,
                    lineHeight = (17f * scale).sp,
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        trailing()
    }
}

@Composable
fun SettingsSwitchRow(
    title: String,
    subtitle: String?,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    theme: DialerTheme,
    enabled: Boolean = true,
) {
    SettingsRow(
        title = title,
        subtitle = subtitle,
        theme = theme,
        onClick = if (enabled) {
            { onCheckedChange(!checked) }
        } else {
            null
        },
        trailing = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                enabled = enabled,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = theme.accentOn,
                    checkedTrackColor = theme.accent,
                    uncheckedThumbColor = theme.textSecondary,
                    uncheckedTrackColor = theme.divider,
                ),
            )
        },
    )
}

@Composable
fun SettingsValueRow(
    title: String,
    value: String,
    theme: DialerTheme,
    subtitle: String? = null,
    onClick: () -> Unit,
) {
    SettingsRow(
        title = title,
        subtitle = subtitle,
        theme = theme,
        onClick = onClick,
        trailing = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = value,
                    color = theme.textSecondary,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.width(4.dp))
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = theme.textSecondary,
                    modifier = Modifier.size(20.dp),
                )
            }
        },
    )
}

@Composable
fun SettingsSliderRow(
    title: String,
    valueLabel: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    theme: DialerTheme,
    onValueChange: (Float) -> Unit,
    enabled: Boolean = true,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp, vertical = 6.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                color = if (enabled) theme.textPrimary else theme.textSecondary,
                fontSize = 16.sp,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = valueLabel,
                color = theme.accent,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            enabled = enabled,
            colors = SliderDefaults.colors(
                thumbColor = theme.accent,
                activeTrackColor = theme.accent,
                inactiveTrackColor = theme.divider,
            ),
        )
    }
}

@Composable
fun SettingsDivider(theme: DialerTheme) {
    HorizontalDivider(
        color = theme.divider,
        thickness = 0.6.dp,
        modifier = Modifier.padding(start = 22.dp, end = 22.dp),
    )
}

@Composable
fun <T> ChoiceDialog(
    title: String,
    options: List<T>,
    selected: T,
    label: (T) -> String,
    theme: DialerTheme,
    onSelect: (T) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = theme.surfaceStrong,
        titleContentColor = theme.textPrimary,
        textContentColor = theme.textSecondary,
        title = { Text(title) },
        text = {
            Column {
                options.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(option) }
                            .padding(vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = option == selected,
                            onClick = { onSelect(option) },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = theme.accent,
                                unselectedColor = theme.textSecondary,
                            ),
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = label(option),
                            color = theme.textPrimary,
                            fontSize = 16.sp,
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = theme.accent)
            }
        },
    )
}

@Composable
fun CallTypeIcon(
    kind: CallKind,
    color: Color,
    modifier: Modifier = Modifier,
) {
    val icon = when (kind) {
        CallKind.INCOMING -> Icons.Filled.CallReceived
        CallKind.OUTGOING -> Icons.Filled.CallMade
        CallKind.MISSED -> Icons.Filled.CallMissed
        CallKind.REJECTED, CallKind.BLOCKED -> Icons.Filled.Block
        CallKind.VOICEMAIL -> Icons.Filled.Voicemail
        CallKind.UNKNOWN -> Icons.Filled.Call
    }
    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = color,
        modifier = modifier.size(16.dp),
    )
}

fun callKindColor(kind: CallKind, theme: DialerTheme, colorCalls: Boolean): Color = when {
    kind == CallKind.MISSED -> theme.missedRed
    !colorCalls -> theme.textSecondary
    kind == CallKind.INCOMING -> theme.incoming
    kind == CallKind.OUTGOING -> theme.outgoing
    kind == CallKind.VOICEMAIL -> theme.accent
    else -> theme.textSecondary
}

/**
 * Background for list rows (call log, contacts and dialpad search). The slider is a percentage
 * of the theme's current opacity, so 100 % keeps the existing look and 0 % is fully transparent.
 */
fun rowSurfaceColor(theme: DialerTheme, opacityPercent: Int): Color {
    val base = if (theme.useWallpaper) 0.62f else 1f
    val factor = opacityPercent.coerceIn(0, 100) / 100f
    return theme.surface.copy(alpha = base * factor)
}

fun callKindLabel(kind: CallKind): String = when (kind) {
    CallKind.INCOMING -> "Incoming"
    CallKind.OUTGOING -> "Outgoing"
    CallKind.MISSED -> "Missed"
    CallKind.REJECTED -> "Rejected"
    CallKind.BLOCKED -> "Blocked"
    CallKind.VOICEMAIL -> "Voicemail"
    CallKind.UNKNOWN -> "Call"
}

@Composable
fun SettingsHintCard(text: String, theme: DialerTheme) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        color = theme.surface.copy(alpha = if (theme.useWallpaper) 0.55f else 1f),
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.Start,
        ) {
            Icon(
                Icons.Filled.Info,
                contentDescription = null,
                tint = theme.accent,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = text,
                color = theme.textSecondary,
                fontSize = 12.5.sp,
                lineHeight = 17.sp,
            )
        }
    }
}
