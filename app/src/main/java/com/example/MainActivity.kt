package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Checkroom
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material3.IconButton
import com.example.ui.screens.CalendarEventsScreen
import com.example.ui.screens.LookbookScreen
import com.example.ui.screens.StyleGuideScreen
import com.example.ui.screens.StyleProfileScreen
import com.example.ui.screens.StylistHomeScreen
import com.example.ui.screens.WardrobeClosetScreen
import com.example.ui.theme.OutfitStylistTheme
import com.example.ui.viewmodel.StylistViewModel

data class NavItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

class MainActivity : ComponentActivity() {

    private val viewModel: StylistViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            OutfitStylistTheme {
                val context = LocalContext.current
                val uiState by viewModel.uiState.collectAsState()
                var selectedTab by remember { mutableIntStateOf(0) }
                val snackbarHostState = remember { SnackbarHostState() }

                LaunchedEffect(intent?.getStringExtra("navigate_to")) {
                    when (intent?.getStringExtra("navigate_to")) {
                        "lookbook" -> selectedTab = 3
                        "stylist" -> selectedTab = 0
                    }
                }

                val navItems = listOf(
                    NavItem("Stylist AI", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome, "nav_stylist_ai"),
                    NavItem("Style Guide", Icons.Filled.MenuBook, Icons.Outlined.MenuBook, "nav_style_guide"),
                    NavItem("Events", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth, "nav_events"),
                    NavItem("My Closet", Icons.Filled.Checkroom, Icons.Outlined.Checkroom, "nav_closet"),
                    NavItem("Lookbook", Icons.Filled.Bookmark, Icons.Outlined.BookmarkBorder, "nav_lookbook"),
                    NavItem("Profile", Icons.Filled.Person, Icons.Outlined.Person, "nav_profile")
                )

                // Show toast when message is triggered
                LaunchedEffect(uiState.userMessageToast) {
                    uiState.userMessageToast?.let { msg ->
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        viewModel.clearToast()
                    }
                }

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    text = navItems[selectedTab].title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                )
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.background,
                                titleContentColor = MaterialTheme.colorScheme.onBackground
                            )
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                        ) {
                            navItems.forEachIndexed { index, item ->
                                val isSelected = selectedTab == index
                                NavigationBarItem(
                                    selected = isSelected,
                                    onClick = { selectedTab = index },
                                    label = {
                                        Text(
                                            text = item.title,
                                            fontSize = 10.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            maxLines = 1
                                        )
                                    },
                                    icon = {
                                        Icon(
                                            imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                            contentDescription = item.title
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.primary,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                    ),
                                    modifier = Modifier.testTag(item.testTag)
                                )
                            }
                        }
                    },
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    val screenModifier = Modifier.padding(innerPadding)

                    when (selectedTab) {
                        0 -> StylistHomeScreen(viewModel = viewModel, modifier = screenModifier)
                        1 -> StyleGuideScreen(
                            viewModel = viewModel,
                            onApplyDressCodeToStylist = { dressCode ->
                                viewModel.updatePromptInput("Suggest a $dressCode outfit")
                                selectedTab = 0
                            },
                            modifier = screenModifier
                        )
                        2 -> CalendarEventsScreen(
                            viewModel = viewModel,
                            onNavigateToStylistWithEvent = { selectedTab = 0 },
                            modifier = screenModifier
                        )
                        3 -> WardrobeClosetScreen(viewModel = viewModel, modifier = screenModifier)
                        4 -> LookbookScreen(viewModel = viewModel, modifier = screenModifier)
                        5 -> StyleProfileScreen(viewModel = viewModel, modifier = screenModifier)
                    }
                }
            }
        }
    }
}
