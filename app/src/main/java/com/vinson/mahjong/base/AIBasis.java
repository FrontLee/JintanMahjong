package com.vinson.mahjong.base;

import com.vinson.mahjong.mahjong.Player;

public class AIBasis {
    public DoubleLink<Tile> leftOpen;
    public DoubleLink<Tile> rightOpen;
    public DoubleLink<Tile> topOpen;
    public DoubleLink<Tile> leftDiscard;
    public DoubleLink<Tile> rightDiscard;
    public DoubleLink<Tile> topDiscard;
    public Player self;
}
