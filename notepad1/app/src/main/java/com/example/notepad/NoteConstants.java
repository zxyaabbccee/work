package com.example.notepad;

import android.net.Uri;

/**
 * 常量类：定义表名、字段名、数据库版本等
 */
public class NoteConstants {
    // 数据库信息
    public static final String DATABASE_NAME = "NotePad.db";
    public static final int DATABASE_VERSION = 3; // 版本3：新增分类功能

    // 笔记表信息
    public static final String TABLE_NOTES = "notes";
    public static final String _ID = "_id"; // 必须含此字段（ContentProvider要求）
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_CONTENT = "content";
    public static final String COLUMN_CREATE_TIME = "create_time"; // 新增时间戳字段
    public static final String COLUMN_IS_TODO = "is_todo"; // 代办标记（附加功能）
    public static final String COLUMN_DEADLINE = "deadline"; // 截止时间（附加功能）
    // 新增分类字段
    public static final String COLUMN_CATEGORY = "category";

    // 内容提供者URI配置
    public static final String AUTHORITY = "com.example.notepad.NotePadProvider";
    public static final String PATH_NOTES = "notes";
    public static final android.net.Uri CONTENT_URI = android.net.Uri.parse(
            "content://" + AUTHORITY + "/" + PATH_NOTES
    );

    // 排序方式
    public static final String SORT_ORDER = COLUMN_CREATE_TIME + " DESC";
    
    // 默认分类
    public static final String DEFAULT_CATEGORY = "默认";
    // 常用分类列表
    public static final String[] DEFAULT_CATEGORIES = {
        DEFAULT_CATEGORY,
        "工作",
        "学习",
        "生活",
        "备忘录",
        "灵感"
    };
}