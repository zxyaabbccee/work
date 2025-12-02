package com.example.notepad;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.notepad.NoteConstants;
import java.util.Calendar;

/**
 * 笔记编辑界面：实现新增/修改笔记、设置代办等功能
 */
public class NoteEditor extends AppCompatActivity {
    private EditText mEtTitle; // 标题输入框
    private EditText mEtContent; // 内容输入框
    private CheckBox mCbTodo; // 代办标记复选框
    private TextView mTvDeadline; // 截止时间文本
    private Spinner mCategorySpinner; // 分类选择器
    private String mCurrentCategory = NoteConstants.DEFAULT_CATEGORY; // 当前分类
    private Uri mNoteUri; // 当前笔记URI（null表示新增）
    private String mDeadline; // 截止时间（字符串格式）

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_editor);

        // 初始化控件
        initViews();

        // 获取Intent数据（判断是新增还是修改）
        Intent intent = getIntent();
        mNoteUri = intent.getData();
        if (mNoteUri != null) {
            // 修改笔记：加载已有数据
            loadNoteData();
        } else {
            // 新增笔记：默认隐藏截止时间
            mTvDeadline.setVisibility(View.GONE);
        }
    }

    /**
     * 初始化控件
     */
    private void initViews() {
        mEtTitle = findViewById(R.id.et_note_title);
        mEtContent = findViewById(R.id.et_note_content);
        mCbTodo = findViewById(R.id.cb_todo);
        mTvDeadline = findViewById(R.id.tv_deadline);
        mCategorySpinner = findViewById(R.id.spinner_category);
        
        // 初始化分类选择器
        initCategorySpinner();

        // 代办复选框监听：勾选则显示截止时间选择，取消则隐藏
        mCbTodo.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                mTvDeadline.setVisibility(View.VISIBLE);
                // 默认显示当前时间（可修改）
                setDefaultDeadline();
            } else {
                mTvDeadline.setVisibility(View.GONE);
                mDeadline = null;
            }
        });

        // 截止时间文本点击：弹出时间选择器
        mTvDeadline.setOnClickListener(v -> showDateTimePicker());
        
        // 设置分类选择监听
        mCategorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mCurrentCategory = parent.getItemAtPosition(position).toString();
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mCurrentCategory = NoteConstants.DEFAULT_CATEGORY;
            }
        });
    }

    /**
     * 加载已有笔记数据（修改时）
     */
    /**
     * 初始化分类选择器
     */
    private void initCategorySpinner() {
        // 设置分类适配器
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, NoteConstants.DEFAULT_CATEGORIES);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mCategorySpinner.setAdapter(adapter);
        
        // 默认选择默认分类
        for (int i = 0; i < NoteConstants.DEFAULT_CATEGORIES.length; i++) {
            if (NoteConstants.DEFAULT_CATEGORIES[i].equals(NoteConstants.DEFAULT_CATEGORY)) {
                mCategorySpinner.setSelection(i);
                break;
            }
        }
    }
    
    /**
     * 加载笔记数据（编辑模式）
     */
    private void loadNoteData() {
        // 查询笔记完整数据
        String[] projection = {
                NoteConstants.COLUMN_TITLE,
                NoteConstants.COLUMN_CONTENT,
                NoteConstants.COLUMN_IS_TODO,
                NoteConstants.COLUMN_DEADLINE,
                NoteConstants.COLUMN_CATEGORY
        };
        android.database.Cursor cursor = getContentResolver().query(
                mNoteUri,
                projection,
                null,
                null,
                null
        );

        if (cursor != null && cursor.moveToFirst()) {
            // 填充数据到控件
            mEtTitle.setText(cursor.getString(0));
            mEtContent.setText(cursor.getString(1));
            int isTodo = cursor.getInt(2);
            mDeadline = cursor.getString(3);

            // 设置代办状态
            mCbTodo.setChecked(isTodo == 1);
            if (isTodo == 1 && !android.text.TextUtils.isEmpty(mDeadline)) {
                mTvDeadline.setVisibility(View.VISIBLE);
                mTvDeadline.setText("截止时间：" + mDeadline);
            } else {
                mTvDeadline.setVisibility(View.GONE);
            }
            
            // 加载分类信息
            String category = cursor.getString(4);
            if (category != null) {
                mCurrentCategory = category;
                // 在分类选择器中查找并选择对应的分类
                for (int i = 0; i < NoteConstants.DEFAULT_CATEGORIES.length; i++) {
                    if (NoteConstants.DEFAULT_CATEGORIES[i].equals(category)) {
                        mCategorySpinner.setSelection(i);
                        break;
                    }
                }
            }

            cursor.close();
        }
    }

    /**
     * 设置默认截止时间（当前时间+1小时）
     */
    private void setDefaultDeadline() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, 1); // 加1小时
        mDeadline = new java.text.SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()
        ).format(calendar.getTime());
        mTvDeadline.setText("截止时间：" + mDeadline);
    }

    /**
     * 显示日期+时间选择器
     */
    private void showDateTimePicker() {
        Calendar calendar = Calendar.getInstance();
        // 1. 显示日期选择器
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            // 2. 日期选择后显示时间选择器
            new TimePickerDialog(this, (view1, hourOfDay, minute) -> {
                // 3. 拼接时间字符串
                calendar.set(year, month, dayOfMonth, hourOfDay, minute, 0);
                mDeadline = new java.text.SimpleDateFormat(
                        "yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()
                ).format(calendar.getTime());
                mTvDeadline.setText("截止时间：" + mDeadline);
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    /**
     * 保存笔记（新增或修改）
     */
    private void saveNote() {
        // 获取输入内容
        String title = mEtTitle.getText().toString().trim();
        String content = mEtContent.getText().toString().trim();

        // 校验标题（不能为空）
        if (android.text.TextUtils.isEmpty(title)) {
            Toast.makeText(this, "标题不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        // 封装数据
        ContentValues values = new ContentValues();
        values.put(NoteConstants.COLUMN_TITLE, title);
        values.put(NoteConstants.COLUMN_CONTENT, content);
        values.put(NoteConstants.COLUMN_CATEGORY, mCurrentCategory); // 添加分类信息
        values.put(NoteConstants.COLUMN_IS_TODO, mCbTodo.isChecked() ? 1 : 0);
        values.put(NoteConstants.COLUMN_DEADLINE, mDeadline);

        if (mNoteUri == null) {
            // 新增笔记：调用ContentProvider插入
            Uri newUri = getContentResolver().insert(NoteConstants.CONTENT_URI, values);
            if (newUri != null) {
                Toast.makeText(this, "笔记已保存", Toast.LENGTH_SHORT).show();
                finish(); // 关闭编辑界面，返回列表
            } else {
                Toast.makeText(this, "保存失败", Toast.LENGTH_SHORT).show();
            }
        } else {
            // 修改笔记：调用ContentProvider更新
            int rowsUpdated = getContentResolver().update(mNoteUri, values, null, null);
            if (rowsUpdated > 0) {
                Toast.makeText(this, "笔记已更新", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "更新失败", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 创建编辑菜单（保存、删除）
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.note_editor_menu, menu);
        // 新增笔记时隐藏“删除”菜单
        if (mNoteUri == null) {
            menu.findItem(R.id.menu_delete).setVisible(false);
        }
        return true;
    }

    /**
     * 编辑菜单点击事件
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_save) {
            // 保存笔记
            saveNote();
            return true;
        } else if (item.getItemId() == R.id.menu_delete) {
            // 删除笔记（仅修改时可用）
            deleteNote();
            return true;
        } else if (item.getItemId() == android.R.id.home) {
            // 返回按钮：关闭界面
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 删除当前笔记（修改时）
     */
    private void deleteNote() {
        new android.app.AlertDialog.Builder(this)
                .setTitle("确认删除")
                .setMessage("是否删除当前笔记？")
                .setPositiveButton("删除", (dialog, which) -> {
                    int rowsDeleted = getContentResolver().delete(mNoteUri, null, null);
                    if (rowsDeleted > 0) {
                        Toast.makeText(this, "笔记已删除", Toast.LENGTH_SHORT).show();
                        finish(); // 关闭界面，返回列表
                    } else {
                        Toast.makeText(this, "删除失败", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }
}