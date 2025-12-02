package com.example.notepad;

import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.content.ContentUris;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.notepad.NoteConstants;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

/**
 * 笔记列表界面：实现时间戳显示、搜索、导出、跳转编辑等功能
 */
public class NoteList extends AppCompatActivity {
    private NoteAdapter mAdapter;
    private ListView mListView;
    private Cursor mCursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_list);

        // 初始化列表视图
        initListView();
        // 初始化搜索框
        initSearchView();
    }

    /**
     * 初始化列表视图（加载所有笔记）
     */
    private void initListView() {
        mListView = findViewById(R.id.lv_notes);
        // 查询所有笔记（投影：只获取需要的字段）
        String[] projection = {
                NoteConstants._ID,
                NoteConstants.COLUMN_TITLE,
                NoteConstants.COLUMN_CREATE_TIME,
                NoteConstants.COLUMN_IS_TODO,
                NoteConstants.COLUMN_DEADLINE
        };
        mCursor = getContentResolver().query(
                NoteConstants.CONTENT_URI,
                projection,
                null,
                null,
                NoteConstants.SORT_ORDER
        );

        // 设置适配器
        mAdapter = new NoteAdapter(this, mCursor, 0);
        mListView.setAdapter(mAdapter);

        // 列表项点击：跳转编辑界面（修改笔记）
        mListView.setOnItemClickListener((parent, view, position, id) -> {
            Uri noteUri = ContentUris.withAppendedId(NoteConstants.CONTENT_URI, id);
            Intent intent = new Intent(NoteList.this, NoteEditor.class);
            intent.setData(noteUri); // 传入笔记ID
            startActivity(intent);
        });

        // 列表项长按：弹出菜单（导出/删除）
        mListView.setOnItemLongClickListener((parent, view, position, id) -> {
            showLongClickMenu(id);
            return true;
        });
    }

    private android.os.Handler mSearchHandler; // 搜索延迟处理
    private Runnable mSearchRunnable; // 搜索任务
    
    /**
     * 初始化搜索框（按标题/内容查询）
     */
    private void initSearchView() {
        SearchView searchView = findViewById(R.id.search_view);
        searchView.setQueryHint("搜索标题或内容...");
        
        // 初始化搜索延迟处理
        mSearchHandler = new android.os.Handler();
        
        // 搜索文本变化监听
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // 提交搜索：直接查询关键词
                performSearch(query);
                searchView.clearFocus(); // 隐藏软键盘
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // 文本变化：延迟搜索（避免频繁查询）
                debounceSearch(newText);
                return true;
            }
        });
    }
    
    /**
     * 延迟搜索（防抖）
     */
    private void debounceSearch(final String keyword) {
        // 移除之前的搜索任务
        if (mSearchRunnable != null) {
            mSearchHandler.removeCallbacks(mSearchRunnable);
        }
        
        // 创建新的搜索任务
        mSearchRunnable = new Runnable() {
            @Override
            public void run() {
                performSearch(keyword);
            }
        };
        
        // 延迟300ms执行（用户停止输入后再搜索）
        mSearchHandler.postDelayed(mSearchRunnable, 300);
    }
    
    /**
     * 执行搜索
     */
    private void performSearch(String keyword) {
        queryNotes(keyword);
    }

    // 当前选中的分类（空表示全部）
    private String mSelectedCategory = "";
    
    /**
     * 执行笔记查询，支持关键词搜索和分类过滤
     */
    private void queryNotes(String keyword) {
        // 关闭旧Cursor（避免内存泄漏）
        if (mCursor != null && !mCursor.isClosed()) {
            mCursor.close();
        }

        String[] projection = {
                NoteConstants._ID,
                NoteConstants.COLUMN_TITLE,
                NoteConstants.COLUMN_CREATE_TIME,
                NoteConstants.COLUMN_IS_TODO,
                NoteConstants.COLUMN_DEADLINE,
                NoteConstants.COLUMN_CATEGORY // 新增分类字段
        };

        String selection = null;
        String[] selectionArgs = null;

        // 构建查询条件：支持关键词搜索和分类过滤
        if (!android.text.TextUtils.isEmpty(keyword)) {
            // 有关键词，使用占位符（在Provider中会被替换为模糊查询）
            selection = "1=1";
            selectionArgs = new String[]{keyword};
        } else if (!android.text.TextUtils.isEmpty(mSelectedCategory)) {
            // 无关键词但有分类过滤
            selection = NoteConstants.COLUMN_CATEGORY + "=?";
            selectionArgs = new String[]{mSelectedCategory};
        }

        // 执行查询
        mCursor = getContentResolver().query(
                NoteConstants.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                NoteConstants.SORT_ORDER
        );

        // 安全检查
        if (mCursor == null) {
            Toast.makeText(this, "查询失败，请稍后重试", Toast.LENGTH_SHORT).show();
            return;
        }

        // 更新适配器
        mAdapter.changeCursor(mCursor);
        mAdapter.notifyDataSetChanged();

        // 统计并提示查询结果
        int count = mCursor.getCount();
        StringBuilder toastMsg = new StringBuilder();
        
        if (count == 0) {
            if (!android.text.TextUtils.isEmpty(keyword)) {
                toastMsg.append("未找到包含'").append(keyword).append("'的笔记");
            } else if (!android.text.TextUtils.isEmpty(mSelectedCategory)) {
                toastMsg.append("分类'").append(mSelectedCategory).append("'下暂无笔记");
            } else {
                toastMsg.append("暂无笔记，点击右上角添加");
            }
        } else {
            toastMsg.append("找到 ").append(count).append(" 条笔记");
            if (!android.text.TextUtils.isEmpty(mSelectedCategory)) {
                toastMsg.append(" (分类: '").append(mSelectedCategory).append("')");
            }
        }
        
        Toast.makeText(this, toastMsg.toString(), Toast.LENGTH_SHORT).show();
    }
    
    /**
     * 打开分类选择菜单
     */
    private void showCategoryMenu() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("选择分类");
        
        // 准备选项，第一个是"全部"选项
        final String[] categories = new String[NoteConstants.DEFAULT_CATEGORIES.length + 1];
        categories[0] = "全部";
        System.arraycopy(NoteConstants.DEFAULT_CATEGORIES, 0, categories, 1, NoteConstants.DEFAULT_CATEGORIES.length);
        
        // 找到当前选中的索引
        int selectedIndex = 0;
        if (!android.text.TextUtils.isEmpty(mSelectedCategory)) {
            for (int i = 1; i < categories.length; i++) {
                if (categories[i].equals(mSelectedCategory)) {
                    selectedIndex = i;
                    break;
                }
            }
        }
        
        builder.setSingleChoiceItems(categories, selectedIndex, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 更新选中的分类
                if (which == 0) {
                    mSelectedCategory = ""; // "全部"选项
                } else {
                    mSelectedCategory = categories[which];
                }
                
                // 关闭对话框并重新查询
                dialog.dismiss();
                queryNotes(""); // 清除搜索关键词，按分类过滤
            }
        });
        
        builder.show();
    }

    /**
     * 列表项长按菜单（导出/删除）
     */
    private void showLongClickMenu(long noteId) {
        String[] items = {"导出笔记", "删除笔记"};
        new AlertDialog.Builder(this)
                .setItems(items, (dialog, which) -> {
                    Uri noteUri = ContentUris.withAppendedId(NoteConstants.CONTENT_URI, noteId);
                    if (which == 0) {
                        // 导出笔记
                        exportNote(noteUri);
                    } else {
                        // 删除笔记
                        deleteNote(noteUri);
                    }
                })
                .show();
    }

    /**
     * 导出笔记为TXT文件
     */
    private void exportNote(Uri noteUri) {
        // 查询笔记完整内容
        String[] projection = {
                NoteConstants.COLUMN_TITLE,
                NoteConstants.COLUMN_CONTENT,
                NoteConstants.COLUMN_CREATE_TIME
        };
        Cursor cursor = getContentResolver().query(
                noteUri,
                projection,
                null,
                null,
                null
        );

        if (cursor != null && cursor.moveToFirst()) {
            String title = cursor.getString(0);
            String content = cursor.getString(1);
            String time = cursor.getString(2);
            cursor.close();

            // 拼接TXT内容
            String noteText = "标题：" + title + "\n"
                    + "创建时间：" + time + "\n"
                    + "-------------------------\n"
                    + content;

            try {
                // 保存路径：外部存储/NotePad/导出笔记_标题.txt
                File dir = new File(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOCUMENTS), "NotePad");
                if (!dir.exists()) {
                    dir.mkdirs(); // 创建目录
                }
                File file = new File(dir, "导出笔记_" + title.replace("/", "_") + ".txt");
                FileOutputStream fos = new FileOutputStream(file);
                OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
                osw.write(noteText);
                osw.close();
                fos.close();

                // 提示导出成功并跳转文件位置
                Toast.makeText(this, "导出成功：" + file.getPath(), Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(file), "text/plain");
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "导出失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 删除笔记
     */
    private void deleteNote(Uri noteUri) {
        int rowsDeleted = getContentResolver().delete(noteUri, null, null);
        if (rowsDeleted > 0) {
            Toast.makeText(this, "笔记已删除", Toast.LENGTH_SHORT).show();
            // 刷新列表
            queryNotes(null);
        } else {
            Toast.makeText(this, "删除失败", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 创建菜单（新增笔记、刷新、分类筛选）
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.note_list_menu, menu);
        return true;
    }

    /**
     * 菜单点击事件
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_add_note) {
            // 新增笔记：跳转编辑界面（无ID）
            Intent intent = new Intent(this, NoteEditor.class);
            startActivity(intent);
            return true;
        } else if (item.getItemId() == R.id.menu_refresh) {
            // 刷新列表
            queryNotes(null);
            Toast.makeText(this, "已刷新", Toast.LENGTH_SHORT).show();
            return true;
        } else if (item.getItemId() == R.id.menu_category) {
            // 分类筛选
            showCategoryMenu();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 回到界面时刷新列表（处理编辑/新增后的变化）
     */
    @Override
    protected void onResume() {
        super.onResume();
        queryNotes(null);
    }

    /**
     * 关闭Cursor（避免内存泄漏）
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCursor != null && !mCursor.isClosed()) {
            mCursor.close();
        }
    }
}