package com.example.notepad;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 数据库帮助类：创建和管理SQLite数据库
 */
public class NotePadDbHelper extends SQLiteOpenHelper {
    // SQL语句：创建笔记表
    private static final String SQL_CREATE_TABLE = "CREATE TABLE " + NoteConstants.TABLE_NOTES + " (" +
            NoteConstants._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            NoteConstants.COLUMN_TITLE + " TEXT NOT NULL, " +
            NoteConstants.COLUMN_CONTENT + " TEXT, " +
            NoteConstants.COLUMN_CREATE_TIME + " TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
            NoteConstants.COLUMN_IS_TODO + " INTEGER DEFAULT 0, " +
            NoteConstants.COLUMN_DEADLINE + " TEXT);";

    // SQL语句：删除笔记表
    private static final String SQL_DELETE_TABLE = "DROP TABLE IF EXISTS " + NoteConstants.TABLE_NOTES + ";";

    public NotePadDbHelper(Context context) {
        super(context, NoteConstants.DATABASE_NAME, null, NoteConstants.DATABASE_VERSION);
    }

    /**
     * 创建数据库表
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLE);
    }

    /**
     * 升级数据库（版本变更时）
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 简单处理：删除旧表，创建新表
        db.execSQL(SQL_DELETE_TABLE);
        onCreate(db);
    }
}