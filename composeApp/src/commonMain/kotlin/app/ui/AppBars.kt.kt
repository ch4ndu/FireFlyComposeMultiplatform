@file:OptIn(ExperimentalMaterial3Api::class)

package app.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.theme.FireflyAppTheme
import fireflycomposemultiplatform.composeapp.generated.resources.Res
import fireflycomposemultiplatform.composeapp.generated.resources.back_navigation_icon
import org.jetbrains.compose.resources.stringResource

@Composable
fun AppBar(
    title: String,
    titleTextStyle: TextStyle = FireflyAppTheme.typography.titleMedium.copy(fontSize = 17.sp),
) {
    TopAppBar(
        modifier = Modifier.shadow(elevation = 6.dp),
        title = {
            Text(
                modifier = Modifier.padding(start = 6.dp),
                text = title,
                style = titleTextStyle
            )
        }
    )
}

@Composable
fun AppBarWithBack(
    title: String,
    titleTextStyle: TextStyle = FireflyAppTheme.typography.titleMedium.copy(fontSize = 17.sp),
//    backgroundColor: Color = FireflyAppTheme.colors.surface,
    backIcon: ImageVector = Icons.AutoMirrored.Filled.ArrowBack,
    arrowTint: Color = LocalContentColor.current,
    backClicked: () -> Unit
) {
    TopAppBar(
        modifier = Modifier.shadow(elevation = 6.dp),
        title = {
            Text(
                text = title,
                style = titleTextStyle
            )
        },

        navigationIcon = {
            IconButton(onClick = backClicked) {
                Icon(
                    backIcon,
                    contentDescription =
                    stringResource(Res.string.back_navigation_icon),
                    tint = arrowTint
                )
            }
        },
//        backgroundColor = backgroundColor,
//        elevation = 6.dp
    )
}

@Composable
fun AppBarWithOptions(
    title: String,
    titleTextStyle: TextStyle = FireflyAppTheme.typography.titleMedium.copy(fontSize = 17.sp),
//    backgroundColor: Color = FireflyAppTheme.colors.surface,
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
//    TopAppBar(
        modifier = Modifier.shadow(elevation = 6.dp),
        title = {
            Text(
                text = title,
                style = titleTextStyle
            )
        },
        actions = actions
    )
}

@Composable
fun AppBarWithBackAndOptions(
    title: String,
    titleTextStyle: TextStyle = FireflyAppTheme.typography.titleMedium.copy(fontSize = 17.sp),
//    backgroundColor: Color = FireflyAppTheme.colors.surface,
    backIcon: ImageVector = Icons.AutoMirrored.Default.ArrowBack,
    arrowTint: Color = LocalContentColor.current,
    backClicked: () -> Unit,
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        modifier = Modifier.shadow(elevation = 6.dp),
        title = {
            Text(
                text = title,
                style = titleTextStyle
            )
        },

        navigationIcon = {
            IconButton(onClick = backClicked) {
                Icon(
                    backIcon,
                    contentDescription =
                    stringResource(Res.string.back_navigation_icon),
                    tint = arrowTint
                )
            }
        },
//        backgroundColor = backgroundColor,
//        elevation = 6.dp,
        actions = actions
    )
}

@Composable
fun TopAppBarIcon(
    painter: Painter,
    description: String = "",
    tint: Color = Color.Unspecified
) {
    Image(
        painter = painter,
        contentDescription = description,
        colorFilter = ColorFilter.tint(tint)
    )
}

@Composable
fun TopAppBarActionButton(
    imageVector: ImageVector,
    description: String,
    onClick: () -> Unit
) {
    IconButton(onClick = {
        onClick()
    }) {
        Icon(imageVector = imageVector, contentDescription = description)
    }
}

@Composable
fun TopAppBarActionButton(
    painter: Painter,
    description: String,
    onClick: () -> Unit
) {
    IconButton(onClick = {
        onClick()
    }) {
        Icon(painter = painter, contentDescription = description)
    }
}