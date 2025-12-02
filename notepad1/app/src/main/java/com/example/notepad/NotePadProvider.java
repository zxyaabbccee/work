package com.example.notepad;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.text.TextUtils;
import com.example.notepad.NoteConstants;

/**
 * 内容提供者：统一管理数据库增删改查（支持查询、时间戳存储）
 */
public class NotePadProvider extends ContentProvider {
    // URI匹配器（区分“所有笔记”和“单条笔记”）
    private static final UriMatcher sUriMatcher;
    private static final int NOTES = 1; // 所有笔记
    private static final int NOTE_ID = 2; // 单条笔记

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(NoteConstants.AUTHORITY, NoteConstants.PATH_NOTES, NOTES);
        sUriMatcher.addURI(NoteConstants.AUTHORITY, NoteConstants.PATH_NOTES + "/#", NOTE_ID);
    }

    private NotePadDbHelper mDbHelper; // 数据库帮助类实例

    @Override
    public boolean onCreate() {
        // 初始化数据库帮助类
        mDbHelper = new NotePadDbHelper(getContext());
        return true;
    }
    
    /**
     * 数据库帮助类：添加分类字段支持
     */
    private static class NotePadDbHelper extends SQLiteOpenHelper {
        
        public NotePadDbHelper(Context context) {
            super(context, NoteConstants.DATABASE_NAME, null, NoteConstants.DATABASE_VERSION);
        }
        
        @Override
        public void onCreate(SQLiteDatabase db) {
            // 创建表的SQL语句（版本3：添加category字段）
            String CREATE_TABLE = "CREATE TABLE " + NoteConstants.TABLE_NOTES + " (" +
                    NoteConstants._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    NoteConstants.COLUMN_TITLE + " TEXT NOT NULL, " +
                    NoteConstants.COLUMN_CONTENT + " TEXT, " +
                    NoteConstants.COLUMN_CREATE_TIME + " TEXT NOT NULL, " +
                    NoteConstants.COLUMN_IS_TODO + " INTEGER DEFAULT 0, " +
                    NoteConstants.COLUMN_DEADLINE + " TEXT, " +
                    NoteConstants.COLUMN_CATEGORY + " TEXT DEFAULT '" + NoteConstants.DEFAULT_CATEGORY + "');";
            db.execSQL(CREATE_TABLE);
        }
        
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (oldVersion < 2) {
                // 从版本1升级到版本2：添加时间戳字段
                db.execSQL("ALTER TABLE " + NoteConstants.TABLE_NOTES + " ADD COLUMN " + 
                           NoteConstants.COLUMN_CREATE_TIME + " TEXT NOT NULL DEFAULT datetime('now', 'localtime')");
            }
            if (oldVersion < 3) {
                // 从版本2升级到版本3：添加分类字段
                db.execSQL("ALTER TABLE " + NoteConstants.TABLE_NOTES + " ADD COLUMN " + 
                           NoteConstants.COLUMN_CATEGORY + " TEXT DEFAULT '" + NoteConstants.DEFAULT_CATEGORY + "'");
            }
        }
    }

    /**
     * 查询：支持按标题/内容/分类模糊查询
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor cursor;

        switch (sUriMatcher.match(uri)) {
            case NOTES:
                // 处理查询：若有selection则按条件模糊查询，否则查全部
                if (!TextUtils.isEmpty(selection)) {
                    // 拼接模糊查询条件（标题、内容或分类包含关键词）
                    selection = "(" + NoteConstants.COLUMN_TITLE + " LIKE ?) OR (" +
                            NoteConstants.COLUMN_CONTENT + " LIKE ?) OR (" +
                            NoteConstants.COLUMN_CATEGORY + " LIKE ?)";
                    // 关键词前后加%（支持中间匹配）
                    selectionArgs = new String[]{"%" + selectionArgs[0] + "%", "%" + selectionArgs[0] + "%", "%" + selectionArgs[0] + "%"};
                }
                cursor = db.query(NoteConstants.TABLE_NOTES, projection, selection,
                        selectionArgs, null, null,
                        TextUtils.isEmpty(sortOrder) ? NoteConstants.SORT_ORDER : sortOrder);
                break;
            case NOTE_ID:
                // 按ID查询单条笔记
                long id = ContentUris.parseId(uri);
                selection = NoteConstants._ID + "=" + id;
                cursor = db.query(NoteConstants.TABLE_NOTES, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        // 设置URI通知：数据变化时更新UI
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        // 返回MIME类型（ContentProvider标准要求）
        switch (sUriMatcher.match(uri)) {
            case NOTES:
                return "vnd.android.cursor.dir/vnd.com.example.notepad.notes";
            case NOTE_ID:
                return "vnd.android.cursor.item/vnd.com.example.notepad.notes";
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    /**
     * 插入：自动添加当前时间戳和默认分类
     */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        if (sUriMatcher.match(uri) != NOTES) {
            throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        // 自动添加当前时间戳（若未传入）
        if (!values.containsKey(NoteConstants.COLUMN_CREATE_TIME)) {
            String currentTime = new java.text.SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()
            ).format(new java.util.Date());
            values.put(NoteConstants.COLUMN_CREATE_TIME, currentTime);
        }
        
        // 自动添加默认分类（若未传入）
        if (!values.containsKey(NoteConstants.COLUMN_CATEGORY)) {
            values.put(NoteConstants.COLUMN_CATEGORY, NoteConstants.DEFAULT_CATEGORY);
        }

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        // 插入数据并获取新记录ID
        long rowId = db.insert(NoteConstants.TABLE_NOTES, null, values);
        if (rowId > 0) {
            // 构建新记录的URI并通知数据变化
            Uri noteUri = ContentUris.withAppendedId(NoteConstants.CONTENT_URI, rowId);
            getContext().getContentResolver().notifyChange(noteUri, null);
            return noteUri;
        }

        throw new android.database.SQLException("Failed to insert row into " + uri);
    }

    /**
     * 删除：按ID或条件删除
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int rowsDeleted;

        switch (sUriMatcher.match(uri)) {
            case NOTES:
                rowsDeleted = db.delete(NoteConstants.TABLE_NOTES, selection, selectionArgs);
                break;
            case NOTE_ID:
                long id = ContentUris.parseId(uri);
                String whereClause = NoteConstants._ID + "=" + id;
                if (!TextUtils.isEmpty(selection)) {
                    whereClause += " AND " + selection;
                }
                rowsDeleted = db.delete(NoteConstants.TABLE_NOTES, whereClause, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        // 通知数据变化
        if (rowsDeleted > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    /**
     * 更新：支持修改笔记内容、代办状态等
     */
    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int rowsUpdated;

        switch (sUriMatcher.match(uri)) {
            case NOTES:
                rowsUpdated = db.update(NoteConstants.TABLE_NOTES, values, selection, selectionArgs);
                break;
            case NOTE_ID:
                long id = ContentUris.parseId(uri);
                String whereClause = NoteConstants._ID + "=" + id;
                if (!TextUtils.isEmpty(selection)) {
                    whereClause += " AND " + selection;
                }
                rowsUpdated = db.update(NoteConstants.TABLE_NOTES, values, whereClause, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        // 通知数据变化
        if (rowsUpdated > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }
}