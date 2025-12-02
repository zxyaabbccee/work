package com.example.notepad;

import android.app.Application;

/**
 * 应用程序类，用于初始化应用级别的组件
 */
public class NoteApplication extends Application {
    
    @Override
    public void onCreate() {
        super.onCreate();
        // 初始化应用级别的配置
        // 这里可以添加数据库初始化、全局配置等
    }
}