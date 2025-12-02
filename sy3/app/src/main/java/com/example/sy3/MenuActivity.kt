package com.example.sy3

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import android.app.Activity


class MenuActivity : Activity() {
    private lateinit var textTest: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.menu_activity)
        textTest = findViewById(R.id.text_test)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // 加载XML菜单资源
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            // 字体大小设置
            R.id.menu_font_small -> {
                textTest.textSize = 10f
                true
            }
            R.id.menu_font_medium -> {
                textTest.textSize = 16f
                true
            }
            R.id.menu_font_large -> {
                textTest.textSize = 20f
                true
            }
            // 字体颜色设置
            R.id.menu_color_red -> {
                textTest.setTextColor(resources.getColor(android.R.color.holo_red_dark))
                true
            }
            R.id.menu_color_black -> {
                textTest.setTextColor(resources.getColor(android.R.color.black))
                true
            }
            // 普通菜单项
            R.id.menu_normal_item -> {
                Toast.makeText(this, "点击了普通菜单项", Toast.LENGTH_SHORT).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}