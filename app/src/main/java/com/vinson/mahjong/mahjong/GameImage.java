package com.vinson.mahjong.mahjong;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;

import com.vinson.mahjong.jintan.R;
import com.vinson.mahjong.system.App;

import java.io.InputStream;

public class GameImage {
    public static Bitmap mahjongImage = loadRawImage(R.raw.majiang);
    public static Bitmap operationImage = loadRawImage(R.raw.operations);
    public static Bitmap headImage = loadRawImage(R.raw.head);
    public static Bitmap sideImage = loadRawImage(R.raw.side);
    private static int tileHeight = mahjongImage.getWidth() / 9;
    private static int tileWidth = (mahjongImage.getHeight() - 19 * tileHeight) / 10;
    private static int operationSize = operationImage.getWidth() / 6;
    private static int headSize = headImage.getWidth() / 5;
    private static int sideSize = sideImage.getWidth() / 5;

    public static Rect getSelfHandImagePos(int tile) {
        Point point = getTileImagePosition(tile, tileWidth, tileHeight, 0);
        return new Rect(point.x, point.y, point.x + tileWidth, point.y + tileHeight);
    }

    public static Rect getSelfOpenImagePos(int tile) {
        Point point = getTileImagePosition(tile, tileWidth, tileHeight, tileHeight * 5);
        return new Rect(point.x, point.y, point.x + tileWidth, point.y + tileHeight);
    }

    public static Rect getSelfBlackImagePos(int tile) {
        Point point = getTileImagePosition(tile, tileWidth, tileHeight, tileHeight * 10);
        return new Rect(point.x, point.y, point.x + tileWidth, point.y + tileHeight);
    }

    public static Rect getTopOpenImagePos(int tile) {
        Point point = getTileImagePosition(tile, tileWidth, tileHeight, tileHeight * 14);
        return new Rect(point.x, point.y, point.x + tileWidth, point.y + tileHeight);
    }

    public static Rect getTopBlackImagePos() {
        Point point = getTileImagePosition(38, tileWidth, tileHeight, tileHeight * 14);
        return new Rect(point.x, point.y, point.x + tileWidth, point.y + tileHeight);
    }

    public static Rect getTopHandImagePos() {
        Point point = getTileImagePosition(39, tileWidth, tileHeight, tileHeight * 14);
        return new Rect(point.x, point.y, point.x + tileWidth, point.y + tileHeight);
    }

    public static Rect getLeftOpenImagePos(int tile) {
        Point point = getTileImagePosition(tile, tileHeight, tileWidth, tileHeight * 19);
        return new Rect(point.x, point.y, point.x + tileHeight, point.y + tileWidth);
    }

    public static Rect getLeftBlackImagePos() {
        Point point = getTileImagePosition(38, tileHeight, tileWidth, tileHeight * 19);
        return new Rect(point.x, point.y, point.x + tileHeight, point.y + tileWidth);
    }

    public static Rect getLeftHandImagePos() {//other
        Point point = getTileImagePosition(39, tileHeight, tileWidth, tileHeight * 19);
        return new Rect(point.x, point.y, point.x + tileHeight, point.y + tileWidth);
    }

    public static Rect getRightOpenImagePos(int tile) {
        Point point = getTileImagePosition(tile, tileHeight, tileWidth, tileHeight * 19 + tileWidth * 5);
        return new Rect(point.x, point.y, point.x + tileHeight, point.y + tileWidth);
    }

    public static Rect getRightBlackImagePos() {
        Point point = getTileImagePosition(38, tileHeight, tileWidth, tileHeight * 19 + tileWidth * 5);
        return new Rect(point.x, point.y, point.x + tileHeight, point.y + tileWidth);
    }

    public static Rect getRightHandImagePos() {
        Point point = getTileImagePosition(39, tileHeight, tileWidth, tileHeight * 19 + tileWidth * 5);
        return new Rect(point.x, point.y, point.x + tileHeight, point.y + tileWidth);
    }

    public static Rect getWinImagePos() {
        return new Rect(0, 0, operationSize, operationSize);
    }

    public static Rect getWinPressedImagePos() {
        return new Rect(0, operationSize, operationSize, operationSize * 2);
    }

    public static Rect getKongImagePos() {
        return new Rect(operationSize, 0, operationSize * 2, operationSize);
    }

    public static Rect getKongPressedImagePos() {
        return new Rect(operationSize, operationSize, operationSize * 2, operationSize * 2);
    }

    public static Rect getPongImagePos() {
        return new Rect(operationSize * 2, 0, operationSize * 3, operationSize);
    }

    public static Rect getPongPressedImagePos() {
        return new Rect(operationSize * 2, operationSize, operationSize * 3, operationSize * 2);
    }

    public static Rect getPassImagePos() {
        return new Rect(operationSize * 3, 0, operationSize * 4, operationSize);
    }

    public static Rect getPassPressedImagePos() {
        return new Rect(operationSize * 3, operationSize, operationSize * 4, operationSize * 2);
    }

    public static Rect getDiscardImagePos() {
        return new Rect(operationSize * 4, 0, operationSize * 5, operationSize);
    }

    public static Rect getDiscardPressedImagePos() {
        return new Rect(operationSize * 4, operationSize, operationSize * 5, operationSize * 2);
    }

    public static Rect getChowImagePos() {
        return new Rect(operationSize * 5, 0, operationSize * 6, operationSize);
    }

    public static Rect getChowPressedImagePos() {
        return new Rect(operationSize * 5, operationSize, operationSize * 6, operationSize * 2);
    }

    public static Rect getHeadImagePos(int index) {
        int row = index / 5;
        int column = index % 5 - 1;
        if (index % 5 == 0) {
            row--;
            column = 4;
        }
        int startX = column * headSize;
        int startY = row * headSize;
        return new Rect(startX, startY, startX + headSize, startY + headSize);
    }

    public static Bitmap getHeadImage(int index) {
        Rect headInImage = getHeadImagePos(index);
        Bitmap head = Bitmap.createBitmap(GameImage.headImage, headInImage.left,
                headInImage.top, headInImage.width(), headInImage.height());
        return head;
    }

    public static Rect getSelfSideImagePos() {
        return new Rect(sideSize, 0, sideSize * 2, sideSize);
    }

    public static Rect getLeftSideImagePos() {
        return new Rect(sideSize * 2, 0, sideSize * 3, sideSize);
    }

    public static Rect getRightSideImagePos() {
        return new Rect(sideSize * 3, 0, sideSize * 4, sideSize);
    }

    public static Rect getTopSideImagePos() {
        return new Rect(sideSize * 4, 0, sideSize * 5, sideSize);
    }

    public static Rect getOtherSideImagePos() {
        return new Rect(0, 0, sideSize, sideSize);
    }

    private static Point getTileImagePosition(int tile, int tileWidth, int tileHeight, int initY) {
        Point point = new Point();
        int row = tile / 10;
        int column = tile % 10 - 1;
        point.x = tileWidth * column;
        point.y = initY + tileHeight * row;
        return point;
    }

    public static Bitmap loadRawImage(int resId) {
        Resources res = App.getAppContext().getResources();
        InputStream inStream = res.openRawResource(resId);
        return BitmapFactory.decodeStream(inStream);
    }
}
