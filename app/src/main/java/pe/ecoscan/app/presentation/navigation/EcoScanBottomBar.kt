package pe.ecoscan.app.presentation.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import pe.ecoscan.app.core.designsystem.theme.EcoScanTheme

@Composable
fun EcoScanBottomBar(
    currentRoute: String?,
    onDestinationSelected: (EcoScanDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(modifier = modifier) {
        EcoScanDestination.bottomBarDestinations.forEach { destination ->
            val isSelected = currentRoute == destination.route
            NavigationBarItem(
                selected = isSelected,
                onClick = { onDestinationSelected(destination) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) destination.selectedIcon else destination.unselectedIcon,
                        contentDescription = destination.label
                    )
                },
                label = { Text(text = destination.label) }
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun EcoScanBottomBarPreview() {
    EcoScanTheme {
        EcoScanBottomBar(
            currentRoute = EcoScanDestination.Scan.route,
            onDestinationSelected = {}
        )
    }
}
