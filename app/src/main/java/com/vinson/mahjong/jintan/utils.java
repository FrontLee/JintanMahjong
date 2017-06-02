package com.vinson.mahjong.jintan;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.util.Log;

import com.vinson.mahjong.base.Constant;
import com.vinson.mahjong.base.DoubleLink;
import com.vinson.mahjong.base.Node;
import com.vinson.mahjong.base.Tile;
import com.vinson.mahjong.base.Win;
import com.vinson.mahjong.base.WinType;
import com.vinson.mahjong.mahjong.Player;

public class utils {
    public static void sleep(int mills) {
        try {
            Thread.sleep(mills);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static int[][] getArrayFromDoubleLink(DoubleLink<Tile> link) {
        int hand[][] = { { 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0 } };
        Node<Tile> tile = link.first;

        while (tile != null) {
            int card = tile.data.getID();
            if (card < 10) {
                hand[0][card - 1]++;
            } else if (card < 20) {
                hand[1][card - 11]++;
            } else if (card < 30) {
                hand[2][card - 21]++;
            } else if (card < 40) {
                hand[3][card - 31]++;
            }
            tile = tile.next;
        }
        return hand;
    }

    public static String getBearing(int bearing) {
        String result = "";
        switch (bearing) {
            case 0:
                result = "东";
                break;
            case 1:
                result = "南";
                break;
            case 2:
                result = "西";
                break;
            case 3:
                result = "北";
                break;
            default:
                result = "Wrong Bearing:" + bearing;
                break;

        }
        return result;
    }

    public static String randomName(int length) {
        String str = null;
        int highPos, lowPos;
        Random random = new Random();
        String captcha = "";
        
        for (int i = 0; i < length; i++) {
            highPos = (176 + Math.abs(random.nextInt(39)));
            lowPos = 161 + Math.abs(random.nextInt(93));

            byte[] b = new byte[2];
            b[0] = (new Integer(highPos)).byteValue();
            b[1] = (new Integer(lowPos)).byteValue();

            try {
                str = new String(b, "GB2312");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            captcha += str;
        }
        return captcha;
    }

    public static int randomHead(){
        Random random = new Random();
        int num = random.nextInt(Constant.MAX_HEAD_COUNT) + 1;

        return num;
    }
}
