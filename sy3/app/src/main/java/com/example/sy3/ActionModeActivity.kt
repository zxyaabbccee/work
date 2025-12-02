package com.example.sy3

import android.os.Bundle
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import android.app.Activity

class ActionModeActivity : Activity() {
    private lateinit var listView: ListView
    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var actionModeCallback: ActionMode.Callback
    private var actionMode: ActionMode? = null
    private var selectedPosition = -1
    // 准备数据，使用可变列表以便修改
    private val items = mutableListOf("One", "Two", "Three", "Four", "Five")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.action_mode_activity)
        
        listView = findViewById(R.id.list_view_action_mode)
        
        // 创建适配器
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_activated_1, items)
        
        // 设置适配器
        listView.adapter = adapter
        
        // 设置ActionMode回调
        actionModeCallback = object : ActionMode.Callback {
            override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                mode?.menuInflater?.inflate(R.menu.context_menu, menu)
                return true
            }

            override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                return false
            }

            override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
                return when (item?.itemId) {
                    R.id.action_edit -> {
                        if (selectedPosition != -1) {
                            Toast.makeText(this@ActionModeActivity, "编辑: ${items[selectedPosition]}", Toast.LENGTH_SHORT).show()
                        }
                        mode?.finish()
                        true
                    }
                    R.id.action_delete -> {
                        if (selectedPosition != -1) {
                            // 实际删除数据
                            items.removeAt(selectedPosition)
                            adapter.notifyDataSetChanged()
                            Toast.makeText(this@ActionModeActivity, "已删除选中项", Toast.LENGTH_SHORT).show()
                        }
                        mode?.finish()
                        true
                    }
                    else -> false
                }
            }

            override fun onDestroyActionMode(mode: ActionMode?) {
                // 重置选中状态
                selectedPosition = -1
                listView.setItemChecked(-1, false)
                actionMode = null
            }
        }
        
        // 设置列表项长按事件
        listView.setOnItemLongClickListener { parent, view, position, id ->
            selectedPosition = position
            
            // 如果已经有ActionMode运行，则结束它
            if (actionMode != null) {
                actionMode?.finish()
            }
            
            // 启动新的ActionMode
            actionMode = startActionMode(actionModeCallback)
            view.isSelected = true
            true
        }
    }
}