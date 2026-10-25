package com.nextdialer.ui.components

import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nextdialer.data.SwipeAction
import com.nextdialer.ui.theme.DialerTheme
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

fun SwipeAction.label(): String = when (this) {
    SwipeAction.CALL -> "Call"
    SwipeAction.SMS -> "SMS"
    SwipeAction.VIEW -> "View"
    SwipeAction.VOIP -> "VoIP"
    SwipeAction.DELETE -> "Delete"
    SwipeAction.NONE -> "None"
}

fun SwipeAction.icon(): ImageVector = when (this) {
    SwipeAction.CALL -> Icons.Filled.Phone
    SwipeAction.SMS -> Icons.Filled.Message
    SwipeAction.VIEW -> Icons.Filled.Person
    SwipeAction.VOIP -> Icons.Filled.PhoneInTalk
    SwipeAction.DELETE -> Icons.Filled.Delete
    SwipeAction.NONE -> Icons.Filled.Block
}

fun SwipeAction.color(theme: DialerTheme): Color = when (this) {
    SwipeAction.CALL -> theme.callGreen
    SwipeAction.SMS -> Color(0xFF2196F3)
    SwipeAction.VIEW -> Color(0xFF607D8B)
    SwipeAction.VOIP -> Color(0xFF7E57C2)
    SwipeAction.DELETE -> Color(0xFFD32F2F)
    SwipeAction.NONE -> Color.Transparent
}

/**
 * A row that can be dragged left or right to trigger an action. The action backgrounds are
 * revealed under the row; passing the threshold and releasing invokes the action.
 */
@Composable
fun SwipeActionRow(
    leftAction: SwipeAction,
    rightAction: SwipeAction,
    enabled: Boolean,
    theme: DialerTheme,
    onAction: (SwipeAction) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val maxRevealPx = with(density) { 132.dp.toPx() }
    val thresholdPx = with(density) { 64.dp.toPx() }
    var offset by remember { mutableFloatStateOf(0f) }
    var busy by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxWidth()) {
        val revealedLeft = (-offset).coerceAtLeast(0f)
        val revealedRight = offset.coerceAtLeast(0f)
        val labelThreshold = with(density) { 48.dp.toPx() }

        if (leftAction != SwipeAction.NONE && revealedLeft > 0f) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(18.dp)),
            ) {
                SwipeReveal(
                    action = leftAction,
                    theme = theme,
                    showLabel = revealedLeft >= labelThreshold,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .width(with(density) { revealedLeft.toDp() })
                        .fillMaxHeight(),
                )
            }
        }

        if (rightAction != SwipeAction.NONE && revealedRight > 0f) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(18.dp)),
            ) {
                SwipeReveal(
                    action = rightAction,
                    theme = theme,
                    showLabel = revealedRight >= labelThreshold,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .width(with(density) { revealedRight.toDp() })
                        .fillMaxHeight(),
                )
            }
        }

        Box(
            modifier = Modifier
                .offset { IntOffset(offset.roundToInt(), 0) }
                .pointerInput(enabled, leftAction, rightAction, thresholdPx, maxRevealPx) {
                    if (!enabled) return@pointerInput
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            if (!busy) {
                                val swipedLeft = offset < 0f
                                val action = when {
                                    swipedLeft && offset <= -thresholdPx &&
                                        leftAction != SwipeAction.NONE -> leftAction
                                    !swipedLeft && offset >= thresholdPx &&
                                        rightAction != SwipeAction.NONE -> rightAction
                                    else -> null
                                }
                                if (action != null) {
                                    busy = true
                                    val target = if (swipedLeft) -maxRevealPx else maxRevealPx
                                    scope.launch {
                                        animate(offset, target, animationSpec = tween(110)) { value, _ ->
                                            offset = value
                                        }
                                        offset = 0f
                                        busy = false
                                        onAction(action)
                                    }
                                } else {
                                    scope.launch {
                                        animate(offset, 0f, animationSpec = tween(160)) { value, _ ->
                                            offset = value
                                        }
                                    }
                                }
                            }
                        },
                        onDragCancel = {
                            scope.launch {
                                animate(offset, 0f, animationSpec = tween(160)) { value, _ ->
                                    offset = value
                                }
                            }
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            if (!busy) {
                                offset = (offset + dragAmount).coerceIn(
                                    -maxRevealPx,
                                    maxRevealPx,
                                )
                            }
                        },
                    )
                },
        ) {
            content()
        }
    }
}

@Composable
private fun SwipeReveal(
    action: SwipeAction,
    theme: DialerTheme,
    showLabel: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(action.color(theme))
            .padding(horizontal = 4.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = action.icon(),
            contentDescription = action.label(),
            tint = Color.White,
            modifier = Modifier.size(22.dp),
        )
        if (showLabel) {
            Spacer(Modifier.height(3.dp))
            Text(
                text = action.label(),
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Clip,
                textAlign = TextAlign.Center,
            )
        }
    }
}
