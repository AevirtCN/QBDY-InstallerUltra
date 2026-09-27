package com.dykt.installerultra

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import com.rosan.dhizuku.api.Dhizuku
import com.rosan.dhizuku.api.DhizukuRequestPermissionListener
import java.io.File

class MainActivity : Activity() {

    private lateinit var statusText: TextView
    private lateinit var pathText: TextView
    private lateinit var openCheck: CheckBox
    private var selectedPath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val scroll = ScrollView(this)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(60, 60, 60, 60)
        }
        scroll.addView(layout)

        val title = TextView(this).apply {
            text = "Installer Ultra V1.2.0"
            textSize = 26f
            setPadding(0, 0, 0, 40)
        }
        layout.addView(title)

        pathText = TextView(this).apply {
            text = "未选择 APK 文件"
            textSize = 14f
            setPadding(0, 0, 0, 20)
        }
        layout.addView(pathText)

        val pickBtn = Button(this).apply {
            text = "浏览 APK 文件"
            setOnClickListener { showFileBrowser() }
        }
        layout.addView(pickBtn)

        openCheck = CheckBox(this).apply {
            text = "安装后自动打开"
            isChecked = true
        }
        layout.addView(openCheck)

        val installBtn = Button(this).apply {
            text = "静默安装（广播）"
            setOnClickListener { silentInstall() }
        }
        layout.addView(installBtn)

        val dhizukuBtn = Button(this).apply {
            text = "请求 Dhizuku 授权"
            setOnClickListener { requestDhizuku() }
        }
        layout.addView(dhizukuBtn)

        val devBtn = Button(this).apply {
            text = "开启开发者选项"
            setOnClickListener {
                sendBroadcast(Intent("COM.HRA.DEVELOPMENT"))
                statusText.text = "已发送开发者选项广播"
            }
        }
        layout.addView(devBtn)

        statusText = TextView(this).apply {
            textSize = 13f
            setPadding(0, 40, 0, 0)
            text = "等待操作..."
        }
        layout.addView(statusText)

        setContentView(scroll)

        initDhizuku()
        requestAllFilesAccess()
    }

    private fun initDhizuku() {
        if (Dhizuku.init(this)) {
            statusText.text = "Dhizuku 已连接"
        } else {
            statusText.text = "Dhizuku 未激活或不可用"
        }
    }

    private fun requestDhizuku() {
        if (!Dhizuku.init(this)) {
            statusText.text = "Dhizuku 不可用"
            return
        }
        if (Dhizuku.isPermissionGranted()) {
            statusText.text = "Dhizuku 已授权"
            return
        }
        // 只调用公开的 requestPermission(listener)
        val listener = object : DhizukuRequestPermissionListener() {
            override fun onRequestPermission(grantResult: Int) {
                runOnUiThread {
                    if (grantResult == PackageManager.PERMISSION_GRANTED) {
                        statusText.text = "Dhizuku 授权成功"
                    } else {
                        statusText.text = "Dhizuku 授权被拒绝"
                    }
                }
            }
        }
        Dhizuku.requestPermission(listener)
        statusText.text = "已请求 Dhizuku 授权"
    }

    private fun requestAllFilesAccess() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                Toast.makeText(this, "请授予“所有文件访问权限”", Toast.LENGTH_LONG).show()
                try {
                    startActivity(Intent(
                        Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                        Uri.parse("package:$packageName")
                    ))
                } catch (e: Exception) {
                    try {
                        startActivity(Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION))
                    } catch (e2: Exception) {
                        Toast.makeText(this, "请手动去设置开启权限", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun showFileBrowser() {
        browseDirectory(Environment.getExternalStorageDirectory())
    }

    private fun browseDirectory(dir: File) {
        val items = dir.listFiles()
            ?.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
            ?: emptyList()

        if (items.isEmpty()) {
            Toast.makeText(this, "目录为空或无法访问", Toast.LENGTH_SHORT).show()
            return
        }

        val names = items.map {
            if (it.isDirectory) "📁 ${it.name}" else "📄 ${it.name}"
        }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle(dir.absolutePath)
            .setItems(names) { _, which ->
                val chosen = items[which]
                if (chosen.isDirectory) {
                    browseDirectory(chosen)
                } else if (chosen.name.endsWith(".apk", ignoreCase = true)) {
                    selectedPath = chosen.absolutePath
                    pathText.text = "已选择：${chosen.absolutePath}"
                    statusText.text = "文件已就绪，可以安装"
                } else {
                    Toast.makeText(this, "请选择 .apk 文件", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun silentInstall() {
        val path = selectedPath
        if (path == null) {
            Toast.makeText(this, "请先选择 APK 文件", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent("com.hra.Silence.install").apply {
            putExtra("open", openCheck.isChecked)
            putExtra("filePath", path)
        }
        sendBroadcast(intent)

        statusText.text = "已发送安装广播\n路径：$path\n自动打开：${openCheck.isChecked}"
        Toast.makeText(this, "安装广播已发送", Toast.LENGTH_SHORT).show()
    }
}
