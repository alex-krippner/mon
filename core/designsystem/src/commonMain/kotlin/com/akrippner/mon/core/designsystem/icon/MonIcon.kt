package com.akrippner.mon.core.designsystem.icon

import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.akrippner.mon.core.designsystem.theme.MonTheme

enum class IconSize {
    ExtraSmall,
    Small,
    Medium,
    Large,
    ExtraLarge
}


@Composable
fun MonIcon(
    id: IconId,
    primaryColor: Color,
    modifier: Modifier = Modifier,
    secondaryColor: Color = Color.Unspecified,
    requiredSize: Dp,
    contentDescription: String? = null
) {
    val catalog = LocalIconCatalog.current
    val imageVector = catalog.get(id, primaryColor, secondaryColor)

    Icon(
        imageVector = imageVector,
        contentDescription = contentDescription,
        modifier = modifier.requiredSize(requiredSize),
        tint = Color.Unspecified
    )
}

@Composable
fun MonIcon(
    id: IconId,
    primaryColor: Color,
    modifier: Modifier = Modifier,
    secondaryColor: Color = Color.Unspecified,
    size: IconSize,
    contentDescription: String? = null
) {
    val size = when (size) {
        IconSize.ExtraSmall -> MonTheme.sizes.iconExtraSmall
        IconSize.Small -> MonTheme.sizes.iconSmall
        IconSize.Medium -> MonTheme.sizes.iconMedium
        IconSize.Large -> MonTheme.sizes.iconLarge
        IconSize.ExtraLarge -> MonTheme.sizes.iconExtraLarge
    }

    MonIcon(
        id = id,
        primaryColor = primaryColor,
        secondaryColor = secondaryColor,
        requiredSize = size,
        modifier = modifier,
        contentDescription = contentDescription
    )
}
