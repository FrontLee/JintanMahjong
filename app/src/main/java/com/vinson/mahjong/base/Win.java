package com.vinson.mahjong.base;

public class Win {
    public int winSide;
    public int gunnerSide;
    public WinType type;
    public int tile;

    public Win(int winSide, int gunnerSide, WinType type, int tile) {
        this.winSide = winSide;
        this.gunnerSide = gunnerSide;
        this.type = type;
        this.tile = tile;
    }
}
