package com.vinson.mahjong.jintan;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.vinson.mahjong.base.Constant;
import com.vinson.mahjong.mahjong.GameImage;

public class ModeSelectorActivity extends Activity implements CompoundButton.OnCheckedChangeListener {
    private EditText txt_YourName;
    private ImageView head_selector;
    private CheckBox chk_chow;
    private LinearLayout theme_option;
    private int headNum;
    private long exitTime = 0;
    private boolean canChow = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);// 隐藏标题
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);// 设置全屏

        setContentView(R.layout.activity_mode_selector);

        txt_YourName = (EditText) findViewById(R.id.yourName);
        head_selector = (ImageView) findViewById(R.id.head_selector);
        theme_option = (LinearLayout) findViewById(R.id.theme_option);
        chk_chow = (CheckBox) findViewById(R.id.chk_chow);

        chk_chow.setOnCheckedChangeListener(this);
        chk_chow.setChecked(true);
        txt_YourName.setText(utils.randomName(3));
        headNum = utils.randomHead();

        setHead();
    }

    private void setHead(){
        Bitmap head = GameImage.getHeadImage(headNum);
        head_selector.setImageBitmap(head);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_DOWN) {

            if ((System.currentTimeMillis() - exitTime) > 2000) {
                Toast.makeText(getApplicationContext(), "再按一次退出游戏",
                        Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                finish();
                System.exit(0);
            }
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    public void StandAloneOnClick(View v) {
        if (txt_YourName.getText().toString().equalsIgnoreCase("")) {
            Toast.makeText(this, "给自己起个萌萌的昵称再开始游戏吧!", Toast.LENGTH_LONG).show();
            return;
        }
        Intent intent = new Intent(this, StandAloneActivity.class);
        intent.putExtra("name", txt_YourName.getText().toString());
        intent.putExtra("head", headNum);
        intent.putExtra("canChow", canChow);
        startActivity(intent);
    }

    public void HeadPreOnClick(View v) {
        if (headNum > 1) {
            headNum--;
        } else {
            headNum = Constant.MAX_HEAD_COUNT;
        }
        setHead();
    }

    public void HeadNextOnClick(View v) {
        if (headNum < Constant.MAX_HEAD_COUNT) {
            headNum++;
        } else {
            headNum = 1;
        }
        setHead();
    }

    public void onCustomizeThemeClicked(View view) {
        //TODO 吃、碰、杠、吃胡、抢杠、自摸、杠上开花、八花算胡、补花成和算杠开
        //TODO 东南西北算花、中发白算花、东南西北刻子算花、中发白刻子算花、中发白刻子算倍率
        //TODO 按花起胡、按番起胡
        //TODO 可胡牌型选择
        if (theme_option.getVisibility() == View.VISIBLE) {
            theme_option.setVisibility(View.INVISIBLE);
        } else {
            theme_option.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        int id = buttonView.getId();
        switch (id) {
            case R.id.chk_chow:
                if (isChecked) {
                    canChow = true;
                } else {
                    canChow = false;
                }
                break;
        }
    }
}
