package com.dykt.installerultra

import android.app.AlertDialog
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rosan.dhizuku.api.Dhizuku
import java.io.File

class MainActivity : ComponentActivity() {

    private var dpm: DevicePolicyManager? = null
    private var adminName: android.content.ComponentName? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = Color(0xFF64B5F6),
                    secondary = Color(0xFF4DB6AC),
                    background = Color(0xFF0F1115),
                    surface = Color(0xFF1E2128),
                    onSurface = Color(0xFFE0E0E0)
                )
            ) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    AppContent()
                }
            }
        }
        requestAllFilesAccess()
    }

    private fun requestAllFilesAccess() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                try {
                    startActivity(Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, Uri.parse("package:$packageName")))
                } catch (e: Exception) {
                    startActivity(Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION))
                }
            }
        }
    }

    @Composable
    fun AppContent() {
        var selectedTab by remember { mutableStateOf(0) }
        var status by remember { mutableStateOf("就绪") }

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                    Text(
                        text = "Installer Ultra",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = "V1.4.0 · 清北道远定制工具箱",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = { Icon(Icons.Default.InstallMobile, contentDescription = null) },
                        label = { Text("安装") }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = { Icon(Icons.Default.Apps, contentDescription = null) },
                        label = { Text("应用") }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        icon = { Icon(Icons.Default.Build, contentDescription = null) },
                        label = { Text("工具") }
                    )
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF0F1115), Color(0xFF1A1C24))
                        )
                    )
            ) {
                Crossfade(
                    targetState = selectedTab,
                    animationSpec = tween(durationMillis = 300),
                    label = "tab_transition"
                ) { tab ->
                    when (tab) {
                        0 -> InstallTab { status = it }
                        1 -> AppManageTab { status = it }
                        2 -> ToolsTab { status = it }
                    }
                }
            }
        }
    }

    @Composable
    fun InstallTab(onStatus: (String) -> Unit) {
        var selectedPath by remember { mutableStateOf<String?>(null) }
        var openAfter by remember { mutableStateOf(true) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ActionCard(
                icon = Icons.Default.FolderOpen,
                title = "选择 APK 文件",
                subtitle = selectedPath ?: "点击浏览本地文件",
                onClick = {
                    showFileBrowser(this@MainActivity) { path ->
                        selectedPath = path
                        onStatus("已选择：$path")
                    }
                }
            )

            GlassCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Launch, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("安装后自动打开", color = Color.White, modifier = Modifier.weight(1f))
                    Switch(checked = openAfter, onCheckedChange = { openAfter = it })
                }
            }

            ActionCard(
                icon = Icons.Default.Download,
                title = "静默安装（广播）",
                subtitle = "com.hra.Silence.install",
                onClick = {
                    val path = selectedPath
                    if (path == null) onStatus("请先选择 APK")
                    else {
                        val intent = Intent("com.hra.Silence.install").apply {
                            putExtra("open", openAfter)
                            putExtra("filePath", path)
                        }
                        sendBroadcast(intent)
                        onStatus("已发送安装广播：$path")
                    }
                }
            )
        }
    }

    @Composable
    fun AppManageTab(onStatus: (String) -> Unit) {
        var appList by remember { mutableStateOf<List<ApplicationInfo>>(emptyList()) }
        var isDhizukuReady by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            if (Dhizuku.init(this@MainActivity) && Dhizuku.isPermissionGranted()) {
                dpm = Dhizuku.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
                adminName = Dhizuku.getDeviceAdminComponentName()
                isDhizukuReady = true
            }
            appList = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
                .filter { it.packageName != packageName }
                .sortedBy { it.loadLabel(packageManager).toString() }
        }

        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
            if (!isDhizukuReady) {
                Button(
                    onClick = {
                        if (Dhizuku.init(this@MainActivity)) {
                            if (Dhizuku.isPermissionGranted()) {
                                dpm = Dhizuku.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
                                adminName = Dhizuku.getDeviceAdminComponentName()
                                isDhizukuReady = true
                            } else {
                                val listener = object : com.rosan.dhizuku.api.DhizukuRequestPermissionListener() {
                                    override fun onRequestPermission(r: Int) {
                                        if (r == PackageManager.PERMISSION_GRANTED) {
                                            dpm = Dhizuku.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
                                            adminName = Dhizuku.getDeviceAdminComponentName()
                                            isDhizukuReady = true
                                        }
                                    }
                                }
                                Dhizuku.requestPermission(listener)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("请求 Dhizuku 授权")
                }
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(appList) { app ->
                    val label = app.loadLabel(packageManager).toString()
                    GlassCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Android, contentDescription = null, tint = Color.Gray)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(label, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                                Text(app.packageName, color = Color.Gray, fontSize = 11.sp)
                            }
                            if (isDhizukuReady) {
                                TextButton(onClick = {
                                    try {
                                        val isHidden = dpm?.isApplicationHidden(adminName!!, app.packageName) ?: false
                                        dpm?.setApplicationHidden(adminName!!, app.packageName, !isHidden)
                                        onStatus("${if (isHidden) "已恢复" else "已隐藏"}：$label")
                                    } catch (e: Exception) {
                                        onStatus("操作失败：${e.message}")
                                    }
                                }) {
                                    Text(if (dpm?.isApplicationHidden(adminName!!, app.packageName) == true) "恢复" else "隐藏")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun ToolsTab(onStatus: (String) -> Unit) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ActionCard(
                icon = Icons.Default.DeveloperMode,
                title = "开启开发者选项",
                subtitle = "发送 COM.HRA.DEVELOPMENT 广播",
                onClick = {
                    sendBroadcast(Intent("COM.HRA.DEVELOPMENT"))
                    onStatus("已发送开发者选项广播")
                }
            )

            ActionCard(
                icon = Icons.Default.Settings,
                title = "打开开发者设置页",
                subtitle = "系统原生跳转",
                onClick = {
                    startActivity(Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS))
                    onStatus("已跳转")
                }
            )

            ActionCard(
                icon = Icons.Default.Security,
                title = "检查/开启所有文件权限",
                subtitle = "解决无法选文件的问题",
                onClick = {
                    requestAllFilesAccess()
                    onStatus("请在弹出的设置中允许")
                }
            )
        }
    }

    @Composable
    fun ActionCard(
        icon: ImageVector,
        title: String,
        subtitle: String,
        onClick: () -> Unit
    ) {
        GlassCard {
            Row(
                modifier = Modifier.clickable { onClick() }.padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(subtitle, color = Color.Gray, fontSize = 12.sp)
                }
            }
        }
    }

    @Composable
    fun GlassCard(content: @Composable () -> Unit) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White.copy(alpha = 0.05f))
                .padding(16.dp)
        ) {
            content()
        }
    }

    private fun showFileBrowser(activity: ComponentActivity, onSelected: (String) -> Unit) {
        browseDirectory(activity, Environment.getExternalStorageDirectory(), onSelected)
    }

    private fun browseDirectory(activity: ComponentActivity, dir: File, onSelected: (String) -> Unit) {
        val items = dir.listFiles()?.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() })) ?: emptyList()
        if (items.isEmpty()) {
            Toast.makeText(activity, "目录为空", Toast.LENGTH_SHORT).show()
            return
        }
        val names = items.map { if (it.isDirectory) "📁 ${it.name}" else "📄 ${it.name}" }.toTypedArray()
        AlertDialog.Builder(activity)
            .setTitle(dir.absolutePath)
            .setItems(names) { _, which ->
                val chosen = items[which]
                if (chosen.isDirectory) browseDirectory(activity, chosen, onSelected)
                else if (chosen.name.endsWith(".apk", true)) onSelected(chosen.absolutePath)
                else Toast.makeText(activity, "请选择 APK", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("取消", null)
            .show()
    }
}
