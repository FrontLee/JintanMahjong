package com.vinson.mahjong.base;

public class Tile implements Cloneable{
    private int ID;
    public boolean Exist;

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public Tile() {
        ID = 0;
        Exist = true;
    }

    public void setID(int id) {
        ID = id;
    }

    public int getID() {
        return ID;
    }
}
