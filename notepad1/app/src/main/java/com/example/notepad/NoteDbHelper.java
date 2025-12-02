package com.example.notepad;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.notepad.NoteConstants;

/**
 * 数据库帮助类：负责创建表、更新表结构
 */
public class NoteDbHelper extends SQLiteOpenHelper {
    // 建表SQL（含时间戳、代办字段）
    private static final String CREATE_TABLE_NOTES = "CREATE TABLE " + NoteConstants.TABLE_NOTES + " ("
            + NoteConstants._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + NoteConstants.COLUMN_TITLE + " TEXT NOT NULL, "
            + NoteConstants.COLUMN_CONTENT + " TEXT, "
            + NoteConstants.COLUMN_CREATE_TIME + " TEXT NOT NULL, " // 时间戳（字符串格式：yyyy-MM-dd HH:mm:ss）
            + NoteConstants.COLUMN_IS_TODO + " INTEGER DEFAULT 0, " // 0=普通笔记，1=代办
            + NoteConstants.COLUMN_DEADLINE + " TEXT);"; // 截止时间（字符串格式）

    // 删表SQL
    private static final String DROP_TABLE_NOTES = "DROP TABLE IF EXISTS " + NoteConstants.TABLE_NOTES;

    public NoteDbHelper(Context context) {
        super(context, NoteConstants.DATABASE_NAME, null, NoteConstants.DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 创建笔记表
        db.execSQL(CREATE_TABLE_NOTES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 版本更新时删旧表、建新表（实际项目可优化为数据迁移）
        db.execSQL(DROP_TABLE_NOTES);
        onCreate(db);
    }
}