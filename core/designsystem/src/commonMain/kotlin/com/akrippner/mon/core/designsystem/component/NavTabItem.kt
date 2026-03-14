package com.akrippner.mon.core.designsystem.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.akrippner.mon.core.designsystem.NavTab
import com.akrippner.mon.core.designsystem.theme.YomuColors
import com.akrippner.mon.core.designsystem.theme.YomuType

@Composable
fun YomuNavTabItem(
    tab: NavTab,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val contentColor by animateColorAsState(
        targetValue = if (selected) YomuColors.Vermilion else YomuColors.InkSubtle,
        animationSpec = tween(200),
        label = "navTabColor"
    )
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = false, radius = 32.dp),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center

    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(width = 56.dp, height = 32.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (selected) YomuColors.Vermilion.copy(alpha = 0.10f)
                        else Color.Transparent
                    ),
                contentAlignment = Alignment.Center


            ) {
                Text(
                    text = tab.kanjiLabel,
                    style = YomuType.NavTitle.copy(
                        fontSize = YomuType.NavTitle.fontSize,
                        color = contentColor,
                    ),
                )
            }
            Spacer(Modifier.height(2.dp))
            Text(
                text = tab.latinLabel,
                style = YomuType.NavLabel.copy(
                    color = contentColor,
                    fontWeight = if (selected) FontWeight.Bold
                    else FontWeight.Normal
                )
            )
        }
    }
}