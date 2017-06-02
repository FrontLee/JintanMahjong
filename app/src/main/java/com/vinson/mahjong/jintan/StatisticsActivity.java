package com.vinson.mahjong.jintan;

import java.util.Timer;
import java.util.TimerTask;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.vinson.mahjong.mahjong.GameImage;

public class StatisticsActivity extends Activity {
    private TextView bearing1, account1, bearing2, account2, bearing3,
            account3;
    private ImageView head1, head2, head3;
    private TextView bearing[], account[];
    private ImageView head[];
    private Context context;
    private Activity a;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);// 隐藏标题
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);// 设置全屏
        setContentView(R.layout.activity_statistic);

        context = this;
        a = this;

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        head1 = (ImageView) findViewById(R.id.head1);
        bearing1 = (TextView) findViewById(R.id.hubearing1);
        account1 = (TextView) findViewById(R.id.huaccount1);
        head2 = (ImageView) findViewById(R.id.head2);
        bearing2 = (TextView) findViewById(R.id.hubearing2);
        account2 = (TextView) findViewById(R.id.huaccount2);
        head3 = (ImageView) findViewById(R.id.head3);
        bearing3 = (TextView) findViewById(R.id.hubearing3);
        account3 = (TextView) findViewById(R.id.huaccount3);
        bearing = new TextView[] { bearing1, bearing2, bearing3 };
        account = new TextView[] { account1, account2, account3 };
        head = new ImageView[] { head1, head2, head3 };
        int size = bundle.getInt("size");
        boolean AIPlay = bundle.getBoolean("ai");
        if (size == 0) {
            account1.setText("流局");
        } else {
            for (int i = 0; i < size; i++) {
                String strBearing[] = bundle.getString("bearing" + i).split("-");
                String strAccount = bundle.getString("account" + i);
                Bitmap headImage = GameImage.getHeadImage(Integer.parseInt(strBearing[0]));
                head[i].setImageBitmap(headImage);
                bearing[i].setText(strBearing[1]);
                account[i].setText(strAccount);
            }
        }
        if (AIPlay) {
            Timer timer = new Timer();
            timer.schedule(task, 5000);
        }

    }

    TimerTask task = new TimerTask() {
        public void run() {
            Intent aintent = new Intent(context, StandAloneActivity.class);
            setResult(40, aintent);
            a.finish();
        }
    };

    public void ControlOnClick(View v) {
        Intent aintent = new Intent(this, StandAloneActivity.class);
        setResult(40, aintent);
        finish();
    }

    @Override
    public void onBackPressed() {

    }

}
