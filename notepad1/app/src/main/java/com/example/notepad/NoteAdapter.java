package com.example.notepad;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;
import com.example.notepad.NoteConstants;

/**
 * 列表适配器：绑定笔记数据到列表项（显示标题、时间戳、代办标记）
 */
public class NoteAdapter extends CursorAdapter {
    // 字段索引（避免重复查询getColumnIndex）
    private int mTitleIndex;
    private int mTimeIndex;
    private int mTodoIndex;
    private int mDeadlineIndex;

    public NoteAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        // 初始化字段索引（Cursor非空时）
        if (c != null) {
            mTitleIndex = c.getColumnIndex(NoteConstants.COLUMN_TITLE);
            mTimeIndex = c.getColumnIndex(NoteConstants.COLUMN_CREATE_TIME);
            mTodoIndex = c.getColumnIndex(NoteConstants.COLUMN_IS_TODO);
            mDeadlineIndex = c.getColumnIndex(NoteConstants.COLUMN_DEADLINE);
        }
    }

    /**
     * 创建空的列表项视图
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.note_item, parent, false);
    }

    /**
     * 绑定数据到列表项视图
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // 1. 获取视图控件
        TextView tvTitle = view.findViewById(R.id.tv_note_title);
        TextView tvTime = view.findViewById(R.id.tv_note_time);
        TextView tvTodoTag = view.findViewById(R.id.tv_todo_tag);
        TextView tvCategoryTag = view.findViewById(R.id.tv_category_tag);

        // 2. 读取数据
        String title = cursor.getString(mTitleIndex);
        String time = cursor.getString(mTimeIndex);
        int isTodo = cursor.getInt(mTodoIndex);
        String deadline = cursor.getString(mDeadlineIndex);
        String category = cursor.getString(cursor.getColumnIndex(NoteConstants.COLUMN_CATEGORY));

        // 3. 设置标题（超长时省略）
        tvTitle.setText(title);
        tvTitle.setEllipsize(android.text.TextUtils.TruncateAt.END);
        tvTitle.setSingleLine(true);

        // 4. 设置时间戳（显示相对时间，更友好）
        tvTime.setText(formatRelativeTime(time, context));
        
        // 5. 设置分类标签
        if (category != null && !category.isEmpty()) {
            tvCategoryTag.setVisibility(View.VISIBLE);
            tvCategoryTag.setText(category);
            // 根据分类设置不同的背景色
            tvCategoryTag.setBackgroundColor(getCategoryColor(category, context));
        } else {
            tvCategoryTag.setVisibility(View.GONE);
        }

        // 6. 设置代办标记（有截止时间则显示，否则隐藏）
        if (isTodo == 1 && !android.text.TextUtils.isEmpty(deadline)) {
            tvTodoTag.setVisibility(View.VISIBLE);
            tvTodoTag.setText("代办：" + deadline);
            // 检查是否过期（标红提醒）
            try {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(
                        "yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()
                );
                java.util.Date deadlineDate = sdf.parse(deadline);
                java.util.Date currentDate = new java.util.Date();
                if (deadlineDate.before(currentDate)) {
                    tvTodoTag.setTextColor(context.getResources().getColor(R.color.red));
                } else {
                    tvTodoTag.setTextColor(context.getResources().getColor(R.color.blue));
                }
            } catch (Exception e) {
                tvTodoTag.setTextColor(context.getResources().getColor(R.color.blue));
            }
        } else {
            tvTodoTag.setVisibility(View.GONE);
        }
    }
    
    /**
     * 根据分类返回对应的颜色
     */
    private int getCategoryColor(String category, Context context) {
        switch (category) {
            case "工作":
                return android.graphics.Color.parseColor("#4285F4"); // 蓝色
            case "学习":
                return android.graphics.Color.parseColor("#34A853"); // 绿色
            case "生活":
                return android.graphics.Color.parseColor("#EA4335"); // 红色
            case "备忘录":
                return android.graphics.Color.parseColor("#FBBC05"); // 黄色
            case "灵感":
                return android.graphics.Color.parseColor("#9C27B0"); // 紫色
            default:
                return android.graphics.Color.parseColor("#757575"); // 灰色（默认分类）
        }
    }

    /**
     * 重新查询时更新字段索引
     */
    @Override
    public void changeCursor(Cursor cursor) {
        super.changeCursor(cursor);
        if (cursor != null) {
            mTitleIndex = cursor.getColumnIndex(NoteConstants.COLUMN_TITLE);
            mTimeIndex = cursor.getColumnIndex(NoteConstants.COLUMN_CREATE_TIME);
            mTodoIndex = cursor.getColumnIndex(NoteConstants.COLUMN_IS_TODO);
            mDeadlineIndex = cursor.getColumnIndex(NoteConstants.COLUMN_DEADLINE);
        }
    }

    /**
     * 格式化时间戳为相对时间（更友好的显示）
     */
    private String formatRelativeTime(String timeString, Context context) {
        try {
            // 解析时间字符串
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault());
            java.util.Date createDate = sdf.parse(timeString);
            java.util.Date currentDate = new java.util.Date();
            
            // 计算时间差（毫秒）
            long diffMillis = currentDate.getTime() - createDate.getTime();
            
            // 转换为不同的时间单位
            long diffSeconds = diffMillis / 1000;
            long diffMinutes = diffSeconds / 60;
            long diffHours = diffMinutes / 60;
            long diffDays = diffHours / 24;
            
            // 根据时间差返回相对时间描述
            if (diffSeconds < 60) {
                return "刚刚";
            } else if (diffMinutes < 60) {
                return diffMinutes + "分钟前";
            } else if (diffHours < 24) {
                return diffHours + "小时前";
            } else if (diffDays < 7) {
                return diffDays + "天前";
            } else {
                // 超过7天则显示具体日期
                return new java.text.SimpleDateFormat("MM-dd HH:mm", 
                        java.util.Locale.getDefault()).format(createDate);
            }
        } catch (Exception e) {
            // 解析失败则返回原始时间
            return timeString;
        }
    }
}