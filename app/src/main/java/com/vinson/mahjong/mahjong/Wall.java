package com.vinson.mahjong.mahjong;

import java.util.Random;

import android.util.Log;

import com.vinson.mahjong.jintan.utils;

public class Wall {

    /*
     * 144张牌: 1~9万 Character 11~19筒 Dot 21~29索 Bamboo 31~37 字牌 Wind+Dragon
     * 41~48花Flower
     */

    public Wall() {

    }

    private int all[] = { 1, 1, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3, 4, 4, 4, 4,
            5, 5, 5, 5, 6, 6, 6, 6, 7, 7, 7, 7, 8, 8, 8, 8, 9, 9, 9, 9, 11, 11,
            11, 11, 12, 12, 12, 12, 13, 13, 13, 13, 14, 14, 14, 14, 15, 15, 15,
            15, 16, 16, 16, 16, 17, 17, 17, 17, 18, 18, 18, 18, 19, 19, 19, 19,
            21, 21, 21, 21, 22, 22, 22, 22, 23, 23, 23, 23, 24, 24, 24, 24, 25,
            25, 25, 25, 26, 26, 26, 26, 27, 27, 27, 27, 28, 28, 28, 28, 29, 29,
            29, 29, 31, 31, 31, 31, 32, 32, 32, 32, 33, 33, 33, 33, 34, 34, 34,
            34, 35, 35, 35, 35, 36, 36, 36, 36, 37, 37, 37, 37, 41, 42, 43, 44,
            45, 46, 47, 48 };

    private int[] wall;

    public void shuffle() {
        wall = new int[144];
        for (int locatedCount = 0; locatedCount < 144; locatedCount++) {
            Random random = new Random();
            //从原数组中随机取一个位置
            int pos = random.nextInt(144 - locatedCount);// 0 ~ (144-locatedCount - 1)
            //将随机位置上的数填入新数组中
            wall[locatedCount] = all[pos];
            //将原数组中，最后一位（144 - locatedCount - 1）上的数与随机位置上的数，进行交换
            all[pos] = all[144 - locatedCount - 1];
            all[144 - locatedCount - 1] = wall[locatedCount];
        }
    }

    public int roll(int bearing) {
        Random r = new Random();
        int n1 = Math.abs(r.nextInt()) % 6 + 1;
        int n2 = Math.abs(r.nextInt()) % 6 + 1;
        int n3 = -1;
        int a = (n1 + n2) % 4;
        int b = -1;
        switch (a) {
            case 0:
                b = 1;
                break;
            case 1:
                b = 0;
                break;
            case 2:
                b = 3;
                break;
            case 3:
                b = 2;
                break;
            default:
                b = -1;
                break;
        }

        if (n1 > n2) {
            n3 = ((b + bearing) % 4) * 36 + n2 * 2;
        } else {
            n3 = ((b + bearing) % 4) * 36 + n1 * 2;
        }
        return n3;
    }

    public void rollBearing(int n, Player[] players) {
        int length = players.length;
        int m[] = new int[length];
        int bearing[] = new int[length];

        for (int i = 0; i < length; i++) {
            m[i] = randomRoll(n);
            players[i].Bearing = 0;
            bearing[i] = 0;
        }

        for (int i = 0; i < length; i++) {
            for (int j = i + 1; j < length; j++) {
                if (m[i] >= m[j]) {
                    players[i].Bearing++;
                    bearing[i]++;
                } else {
                    players[j].Bearing++;
                    bearing[j]++;
                }
            }
        }

        Player temp = null;
        for (int i = 0; i < length; i++) {
            for (int j = i + 1; j < length; j++) {
                if (players[i].Bearing > players[j].Bearing) {
                    temp = players[i];
                    players[i] = players[j];
                    players[j] = temp;
                }
            }

        }
    }

    private int randomRoll(int n) {
        Random r = new Random();
        int m = 0;
        for (int i = 0; i < n; i++) {
            m = m + Math.abs(r.nextInt()) % 6 + 1;
        }
        return m;
    }

    // 发牌
    public void deal(Player[] players, int position,
                            int whoFirst) {
        int length = players.length;
        int pos = position;

        for (int k = 0; k < 3; k++) {
            for (int i = 0; i < length; i++) {
                for (int j = 0; j < 4; j++) {
                    players[(i + whoFirst) % length].addHands(wall[pos]);
                    wall[pos] = 0;
                    pos = (pos + 1) % 144;
                }
            }
        }
        players[whoFirst].addHands(wall[pos]);
        wall[pos] = 0;
        pos = (pos + 1) % 144;
        for (int i = 0; i < length; i++) {
            players[(i + whoFirst) % length].addHands(wall[pos]);
            wall[pos] = 0;
            pos = (pos + 1) % 144;

        }
    }

    public int getTile(int position) {
        int tile = wall[position];
        wall[position] = 0;
        return tile;
    }

    public int getRemainderCount() {
        int count = 0;
        for (int i = 0; i < 144; i++) {
            if (wall[i] > 0) {
                count++;
            }
        }
        return count;
    }
}
