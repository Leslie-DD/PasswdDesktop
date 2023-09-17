package ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowScope

@Composable
private fun WindowScope.AppWindowTitleBar(
    onCloseClick: () -> Unit
) = WindowDraggableArea {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Spacer(
                modifier = Modifier.width(34.dp)
            )
            Text(
                modifier = Modifier.padding(start = 10.dp, bottom = 4.dp, top = 4.dp),
                text = "Passwd",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            CloseButton { onCloseClick() }
        }
        Divider(
            Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun CloseButton(onClick: () -> Unit) {
    Box(modifier = Modifier.padding(horizontal = 10.dp)) {
        val interactionSource = remember { MutableInteractionSource() }
        val isHovered by interactionSource.collectIsHoveredAsState()
        Icon(
            modifier = Modifier.size(24.dp)
                .background(if (isHovered) Color.LightGray else MaterialTheme.colorScheme.onPrimary)
                .hoverable(interactionSource)
                .clickable(onClick = onClick),
            imageVector = Icons.Default.Close,
            contentDescription = "Close",
            tint = Color.Red
        )
    }
}