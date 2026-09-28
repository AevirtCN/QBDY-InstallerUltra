package com.dykt.installerultra

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rosan.dhizuku.api.Dhizuku
import java.io.File

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = Color(0xFF2196F3),
                    surface = Color(0xFF1E1E1E),
                    background = Color(0xFF121212)
                )
            ) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    MainScreen()
                }
            }
        }
    }

    @Composable
    fun MainScreen() {
        var status by remember { mutableStateOf("等待操作...") }
        var selectedPath by remember { mutableStateOf<String?>(null) }
        var openAfterInstall by remember { mutableStateOf(true) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 顶部标题
            Text(
                text = "Installer Ultra",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "V1.2.1 · 清北道远专用工具",
                fontSize = 13.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // 文件选择卡片
            ActionCard(
                title = "选择 APK 文件",
                subtitle = selectedPath ?: "未选择文件",
                onClick = {
                    showFileBrowser(this@MainActivity) { path ->
                        selectedPath = path
                        status = "已选择：$path"
                    }
                }
            )

            // 安装选项
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = openAfterInstall,
                        onCheckedChange = { openAfterInstall = it }
                    )
                    Text("安装后自动打开", color = Color.White)
                }
            }

            // 静默安装
            ActionCard(
                title = "静默安装（广播）",
                subtitle = "通过 com.hra.Silence.install 安装",
                onClick = {
                    val path = selectedPath
                    if (path == null) {
                        status = "请先选择 APK 文件"
                    } else {
                        val intent = Intent("com.hra.Silence.install").apply {
                            putExtra("open", openAfterInstall)
                            putExtra("filePath", path)
                        }
                        sendBroadcast(intent)
                        status = "已发送安装广播：$path"
                    }
                }
            )

            // Dhizuku 授权
            ActionCard(
                title = "请求 Dhizuku 授权",
                subtitle = "获得 Device Owner 权限",
                onClick = {
                    if (!Dhizuku.init(this@MainActivity)) {
                        status = "Dhizuku 不可用"
                        return@ActionCard
                    }
                    if (Dhizuku.isPermissionGranted()) {
                        status = "Dhizuku 已授权"
                        return@ActionCard
                    }
                    val listener = object : com.rosan.dhizuku.api.DhizukuRequestPermissionListener() {
                        override fun onRequestPermission(grantResult: Int) {
                            runOnUiThread {
                                status = if (grantResult == android.content.pm.PackageManager.PERMISSION_GRANTED)
                                    "Dhizuku 授权成功" else "Dhizuku 授权被拒绝"
                            }
                        }
                    }
                    Dhizuku.requestPermission(listener)
                }
            )

            // 开发者选项
            ActionCard(
                title = "开启开发者选项",
                subtitle = "发送 COM.HRA.DEVELOPMENT 广播",
                onClick = {
                    sendBroadcast(Intent("COM.HRA.DEVELOPMENT"))
                    status = "已发送开发者选项广播"
                }
            )

            // 状态栏
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = status,
                    color = Color.LightGray,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }

    @Composable
    fun ActionCard(title: String, subtitle: String, onClick: () -> Unit) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp),
            onClick = onClick
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(4.dp))
                Text(subtitle, color = Color.Gray, fontSize = 12.sp)
            }
        }
    }

    // ========== 文件浏览器（沿用旧逻辑） ==========
    private fun showFileBrowser(activity: ComponentActivity, onSelected: (String) -> Unit) {
        browseDirectory(activity, Environment.getExternalStorageDirectory(), onSelected)
    }

    private fun browseDirectory(activity: ComponentActivity, dir: File, onSelected: (String) -> Unit) {
        val items = dir.listFiles()
            ?.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
            ?: emptyList()

        if (items.isEmpty()) {
            Toast.makeText(activity, "目录为空或无法访问", Toast.LENGTH_SHORT).show()
            return
        }

        val names = items.map {
            if (it.isDirectory) "📁 ${it.name}" else "📄 ${it.name}"
        }.toTypedArray()

        AlertDialog.Builder(activity)
            .setTitle(dir.absolutePath)
            .setItems(names) { _, which ->
                val chosen = items[which]
                if (chosen.isDirectory) {
                    browseDirectory(activity, chosen, onSelected)
                } else if (chosen.name.endsWith(".apk", ignoreCase = true)) {
                    onSelected(chosen.absolutePath)
                } else {
                    Toast.makeText(activity, "请选择 .apk 文件", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }
}
