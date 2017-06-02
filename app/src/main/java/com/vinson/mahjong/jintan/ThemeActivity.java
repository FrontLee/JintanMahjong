package com.vinson.mahjong.jintan;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

public class ThemeActivity extends Activity {
    
    private TextView txt_theme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);// 隐藏标题
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);// 设置全屏
        setContentView(R.layout.activity_theme);
        txt_theme = (TextView) findViewById(R.id.txt_theme);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 0, 0, "金坛麻将规则");
        menu.add(0, 1, 1, "国际麻将规则");
        return true;
    }
    
    @Override
    // 菜单被选中时触发的事件
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 0) {
            txt_theme.setText(R.string.JTTheme);
            
        } else if (item.getItemId() == 1) {
            txt_theme.setText(R.string.theme);
            
        }
        return true;
    }

}
