package com.anix.studybuddy

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.anix.studybuddy.ui.*
import com.anix.studybuddy.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val vm: MainViewModel by viewModels()

    private val notifPermLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* handled silently */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request notification permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notifPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        // Request exact alarm permission on Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val am = getSystemService(android.app.AlarmManager::class.java)
            if (!am.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                    Uri.parse("package:$packageName"))
                startActivity(intent)
            }
        }

        // Seed DB and check auto-activation
        vm.seedIfEmpty()
        vm.checkAndActivateExams()

        setContent {
            AnixTheme {
                AnixApp(vm)
            }
        }
    }
}

sealed class NavItem(val route: String, val icon: ImageVector, val label: String) {
    object Home     : NavItem("home",      Icons.Default.Home,         "Home")
    object Exams    : NavItem("exams",     Icons.Default.DateRange,    "Exams")
    object Alarms   : NavItem("alarms",    Icons.Default.Alarm,        "Alarms")
    object Chapters : NavItem("chapters",  Icons.Default.MenuBook,     "Chapters")
    object Timetable: NavItem("timetable", Icons.Default.CalendarMonth,"Timetable")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnixApp(vm: MainViewModel) {
    var currentRoute by remember { mutableStateOf("home") }

    val navItems = listOf(
        NavItem.Home, NavItem.Exams, NavItem.Alarms,
        NavItem.Chapters, NavItem.Timetable
    )

    val exams     by vm.allExams.collectAsState()
    val reminders by vm.allReminders.collectAsState()
    val chapters  by vm.allChapters.collectAsState()
    val slots     by vm.allSlots.collectAsState()
    val examUiList by vm.examUiList.collectAsState()

    Scaffold(
        containerColor = Black,
        bottomBar = {
            NavigationBar(
                containerColor = Surface1,
                tonalElevation = 0.dp
            ) {
                navItems.forEach { item ->
                    NavigationBarItem(
                        selected = currentRoute == item.route,
                        onClick  = { currentRoute = item.route },
                        icon = { Icon(item.icon, item.label) },
                        label = { Text(item.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Black,
                            selectedTextColor = NeonGreen,
                            unselectedIconColor = OnSurfaceDim,
                            unselectedTextColor = OnSurfaceDim,
                            indicatorColor = NeonGreen
                        )
                    )
                }
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize().background(Black)) {
            when (currentRoute) {
                "home"      -> HomeScreen(examUiList, slots)
                "exams"     -> ExamsScreen(examUiList, onDelete = { vm.deleteExam(it) })
                "alarms"    -> RemindersScreen(
                    reminders = reminders,
                    exams = exams,
                    onAdd    = { vm.addReminder(it) },
                    onToggle = { vm.toggleReminder(it) },
                    onDelete = { vm.deleteReminder(it) }
                )
                "chapters"  -> ChaptersScreen(
                    chapters = chapters,
                    exams    = exams,
                    onAddChapter    = { vm.addChapter(it) },
                    onDeleteChapter = { vm.deleteChapter(it) },
                    onToggleTopic   = { ch, topic -> vm.toggleTopic(ch, topic) }
                )
                "timetable" -> TimetableScreen(
                    slots    = slots,
                    exams    = exams,
                    onAddSlot    = { vm.addSlot(it) },
                    onDeleteSlot = { vm.deleteSlot(it) }
                )
            }
        }
    }
}
