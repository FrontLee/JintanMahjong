package com.vinson.mahjong.base;

public class Probability {
    public int highPriorityTileCount;
    public int middlePriorityTileCount;
    public int lowPriorityTileCount;

    public Probability(int highPriorityTileCount, int middlePriorityTileCount, int lowPriorityTileCount) {
        this.highPriorityTileCount = highPriorityTileCount;
        this.middlePriorityTileCount = middlePriorityTileCount;
        this.lowPriorityTileCount = lowPriorityTileCount;
    }

    public Probability() {

    }

    public boolean isGreaterThan(Probability probability) {
        if (highPriorityTileCount > probability.highPriorityTileCount)
            return true;
        else if (highPriorityTileCount == probability.highPriorityTileCount) {
            if (middlePriorityTileCount > probability.middlePriorityTileCount)
                return true;
        }
        return false;
    }

    public boolean isLessThan(Probability probability) {
        if (highPriorityTileCount < probability.highPriorityTileCount)
            return true;
        else if (highPriorityTileCount == probability.highPriorityTileCount) {
            if (middlePriorityTileCount < probability.middlePriorityTileCount)
                return true;
        }
        return false;
    }
}
