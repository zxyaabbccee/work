package com.example.sy3

import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.*
import android.app.Activity
import android.app.PendingIntent

class MainActivity : Activity() {
    private val CHANNEL_ID = "animal_notification_channel"
    private val notificationId = 1
    private val NOTIFICATION_PERMISSION_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // 创建通知渠道（Android 8.0+）
        createNotificationChannel()
        
        // 请求通知权限（Android 13+）
        requestNotificationPermission()
        
        // 初始化ListView
        initListView()
        
        // 设置自定义对话框按钮点击事件
        findViewById<Button>(R.id.button_custom_dialog).setOnClickListener {
            showCustomDialog()
        }
        
        // 设置菜单Activity跳转按钮点击事件
        findViewById<Button>(R.id.button_menu_activity).setOnClickListener {
            startActivity(Intent(this, MenuActivity::class.java))
        }
        
        // 设置ActionMode列表跳转按钮点击事件
        findViewById<Button>(R.id.button_action_mode).setOnClickListener {
            startActivity(Intent(this, ActionModeActivity::class.java))
        }
    }
    
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), NOTIFICATION_PERMISSION_CODE)
            }
        }
    }
    
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "需要通知权限才能发送通知", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun initListView() {
        val listView = findViewById<ListView>(R.id.list_view)
        
        // 准备数据
        val data = ArrayList<HashMap<String, Any>>()
        val animals = arrayOf("Lion", "Tiger", "Monkey", "Dog", "Cat", "Elephant")
        val icons = arrayOf(
            R.drawable.lion,
            R.drawable.tiger,
            R.drawable.monkey,
            R.drawable.dog,
            R.drawable.cat,
            R.drawable.elephant
        )
        
        for (i in animals.indices) {
            val map = HashMap<String, Any>()
            map["name"] = animals[i]
            map["icon"] = icons[i]
            data.add(map)
        }
        
        // 创建SimpleAdapter
        val adapter = SimpleAdapter(
            this,
            data,
            R.layout.list_item,
            arrayOf("name", "icon"),
            intArrayOf(R.id.item_name, R.id.item_image)
        )
        
        // 设置适配器
        listView.adapter = adapter
        
        // 设置点击事件
        listView.setOnItemClickListener { parent, view, position, id ->
            val animalName = animals[position]
            
            // 显示Toast
            Toast.makeText(this, "选中了: $animalName", Toast.LENGTH_SHORT).show()
            
            // 发送通知
            sendNotification(animalName)
        }
    }
    
    private fun showCustomDialog() {
        // 创建AlertDialog.Builder
        val builder = AlertDialog.Builder(this)
        
        // 加载自定义布局
        val dialogView = layoutInflater.inflate(R.layout.dialog_login, null)
        builder.setView(dialogView)
        
        // 获取对话框中的控件
        val editUsername = dialogView.findViewById<EditText>(R.id.edit_username)
        val editPassword = dialogView.findViewById<EditText>(R.id.edit_password)
        val buttonCancel = dialogView.findViewById<Button>(R.id.button_cancel)
        val buttonSignIn = dialogView.findViewById<Button>(R.id.button_sign_in)
        
        // 创建对话框
        val dialog = builder.create()
        
        // 设置按钮点击事件
        buttonCancel.setOnClickListener {
            dialog.dismiss()
        }
        
        buttonSignIn.setOnClickListener {
            val username = editUsername.text.toString()
            val password = editPassword.text.toString()
            Toast.makeText(this, "登录信息: 用户名=$username, 密码=$password", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
        
        // 显示对话框
        dialog.show()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "动物通知"
            val descriptionText = "显示选中的动物信息"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // 注册通知渠道
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun sendNotification(animalName: String) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        
        // 检查权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        
        // 创建通知构建器
        val notificationBuilder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "动物通知", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
            android.app.Notification.Builder(this, CHANNEL_ID)
        } else {
            @Suppress("DEPRECATION")
            android.app.Notification.Builder(this)
        }
        
        // 设置通知内容
        val notification = notificationBuilder
            .setSmallIcon(R.drawable.cat)
            .setContentTitle(animalName)
            .setContentText("您选择了" + animalName + "作为您喜欢的动物")
            .setAutoCancel(true)
            .build()
        
        // 发送通知
        notificationManager.notify(notificationId, notification)
    }
}