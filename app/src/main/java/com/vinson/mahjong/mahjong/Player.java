package com.vinson.mahjong.mahjong;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

import com.vinson.mahjong.base.Constant;
import com.vinson.mahjong.base.DoubleLink;
import com.vinson.mahjong.base.ExchangeFlowerResult;
import com.vinson.mahjong.base.Node;
import com.vinson.mahjong.base.PlayerType;
import com.vinson.mahjong.base.Tile;
import com.vinson.mahjong.jintan.utils;

public class Player {
    public String Name;
    public int HeadNum;
    public String Description;
    public boolean AI;
    public PlayerType Type;
    public int Bearing;
    public int Score;
    private DoubleLink<Tile> Hands;
    public DoubleLink<Tile> Discard;
    public DoubleLink<Tile> OpenHands;
    private DoubleLink<Tile> BlackHands;
    public DoubleLink<Tile> Flower;
    private int FlowerCount;
    public String Operation;
    public int selectedPos = -1;

    public Player(String name, String description, boolean AI, PlayerType type) {
        Name = name;
        Description = description;
        this.AI = AI;
        Bearing = 0;
        Score = 0;
        Type = type;
        HeadNum = 1;

        Hands = new DoubleLink<Tile>();
        Discard = new DoubleLink<Tile>();
        OpenHands = new DoubleLink<Tile>();
        BlackHands = new DoubleLink<Tile>();
        Flower = new DoubleLink<Tile>();
        FlowerCount = 0;
    }

    public void addHands(int tile) {
        Tile hands = new Tile();
        hands.setID(tile);
        hands.Exist = true;
        Hands.insertLast(hands);
    }

    public DoubleLink<Tile> getHands() {
        return Hands;
    }
    public DoubleLink<Tile> getOpenHands() {
        return OpenHands;
    }

    public int searchHands(int tile) {
        int result = 0;
        Node<Tile> f = Hands.first;
        while (f != null) {
            if (f.data.getID() == tile) {
                result++;
            }
            f = f.next;
        }
        return result;
    }

    public int searchOpenHand(int tile) {
        int result = 0;
        Node<Tile> f = OpenHands.first;
        while (f != null) {
            if (f.data.getID() == tile) {
                result++;
            }
            f = f.next;
        }
        return result;
    }

    public int deleteHands(int tile) {
        int result = -1;
        Node<Tile> f = Hands.first;
        while (f != null) {
            if (f.data.getID() == tile) {
                result = Hands.delete(f);
                break;
            }
            f = f.next;
        }
        return result;
    }

    public void addDiscard(int tile) {
        Tile discard = new Tile();
        discard.setID(tile);
        discard.Exist = true;
        Discard.insertLast(discard);
    }

    public int getTilePosFromLeft(int tile) {
        int result = 0;
        Node<Tile> hand = Hands.first;
        while(hand != null) {
            result ++;
            if (hand.data.getID() == tile) {
                break;
            }
            hand = hand.next;
        }
        return result;
    }

    public int getTilePosFromRight(int tile) {
        int result = 0;
        Node<Tile> hand = Hands.last;
        while(hand != null) {
            if (hand.data.getID() == tile) {
                break;
            }
            result ++;
            hand = hand.previous;
        }
        return Hands.size - result;
    }

    public int searchDiscard(int tile) {
        int result = 0;
        Node<Tile> f = Discard.first;
        while (f != null) {
            if (f.data.getID() == tile && f.data.Exist == true) {
                result++;
            }
            f = f.next;
        }
        return result;
    }

    public void removeDiscard(int tile) {
        Node<Tile> f = Discard.last;
        while (f != null) {
            if (f.data.getID() == tile && f.data.Exist) {
                f.data.Exist = false;
                break;
            }
            f = f.previous;
        }
    }

    public void removeOpenHand(int tile) {
        Node<Tile> f = OpenHands.last;
        while (f != null) {
            if (f.data.getID() == tile && f.data.Exist) {
                f.data.Exist = false;
                break;
            }
            f = f.previous;
        }
    }

    public void addOpenHands(int tile) {
        Tile openhand = new Tile();
        openhand.setID(tile);
        openhand.Exist = true;
        OpenHands.insertLast(openhand);
    }

    public void addBlackHands(int tile) {
        Tile blackHands = new Tile();
        blackHands.setID(tile);
        blackHands.Exist = true;
        BlackHands.insertLast(blackHands);
    }

    public DoubleLink<Tile> getBlackHands() {
        return BlackHands;
    }

    public void addFlower(int tile) {
        Tile hands = new Tile();
        hands.setID(tile);
        hands.Exist = true;
        Flower.insertLast(hands);
        FlowerCount++;
    }

    public DoubleLink<Tile> getFlower() {
        return Flower;
    }
    
    public void addFlowerCount(int count) {
        FlowerCount = FlowerCount + count;
    }

    //TODO 计算明面上的花数
    public int getFlowerCount() {
        return FlowerCount;
    }

    public int getYeFlowerInHand() {
        int count = 0;
        int flowers = 0;
        Node<Tile> hand = Hands.first;
        List<Integer> yeFlower = new ArrayList<Integer>();
        List<Integer> yeFlower2 = new ArrayList<Integer>();
        while (hand != null) {
            int tile = hand.data.getID();
            if (tile > 30 && tile < 40) {
                yeFlower.add(tile);
                if (!yeFlower2.contains(tile))
                    yeFlower2.add(tile);
            }
            hand = hand.next;
        }
        for (int i = 0; i < yeFlower2.size(); i++) {
            int tile = yeFlower2.get(i);
            count = 0;
            for (int j = 0; j < yeFlower.size(); j++) {
                if (tile == yeFlower.get(j)) {
                    count++;
                }
            }
            if (count == 3) {
                flowers += 2;
            } else if (count == 4) {
                flowers += 4;
            }
        }
        return flowers;
    }

    public void ClearData() {
        Hands = new DoubleLink<Tile>();
        Discard = new DoubleLink<Tile>();
        OpenHands = new DoubleLink<Tile>();
        BlackHands = new DoubleLink<Tile>();
        Flower = new DoubleLink<Tile>();
        FlowerCount = 0;
    }
    
    public void clearHands() {
        Hands = new DoubleLink<Tile>();
    }

    public boolean hasFlowerInHand() {
        boolean result = false;
        Node<Tile> f = Hands.last;
        while (f != null) {
            if (f.data.getID() > 40) {
                result = true;
                break;
            }
            f = f.previous;
        }
        return result;
    }

    public void sortTilesInHand() {
        sort(Hands);
    }
    
    public int getHandSizeFromExposed() {
        int openHand[][] = utils.getArrayFromDoubleLink(OpenHands);
        int kongCount = BlackHands.size / 4;
        for (int suit = 0; suit < openHand.length; suit++) {
            for (int index = 0; index < openHand[suit].length; index++) {
                if (openHand[suit][index] == 4) {
                    kongCount++;
                }
            }
        }
        return OpenHands.size + BlackHands.size - kongCount;
    }

    //因大部分时间是有序的所以使用插入排序
    public void sort(DoubleLink<Tile> link) {
        Node<Tile> tile;
        Node<Tile> previous;
        if (link.size == 0) return;
        tile = link.first.next;
        Tile tmp;
        while (tile != null) {
            tmp = tile.data;
            previous = tile.previous;
            while (previous != null && tmp.getID() < previous.data.getID()) {
                previous.next.data = previous.data;
                previous = previous.previous;
            }
            if (previous == null) {
                link.first.data = tmp;
            } else {
                previous.next.data = tmp;
            }
            tile = tile.next;
        }
    }

    public int getHandSize() {
        return Hands.size;
    }

    public int getHandByIndex(int index) {
        int count = 0;
        Node<Tile> f = Hands.first;
        while (f != null) {
            count++;
            if (count == index) {
                return f.data.getID();
            }
            f = f.next;
        }
        return -2;
    }

    public int getSelectedTile() {
        return getHandByIndex(selectedPos);
    }

    // 补花
    public ExchangeFlowerResult exchangeFlower(Wall wall, int rearPosition ) {
        int changedCard;
        boolean hasFlower = true;
        Node<Tile> node;
        int flowerCount = 0;
        while (hasFlower) {
            hasFlower = false;
            node = Hands.last;
            while (node != null) {
                if (node.data.getID() <= 40) {
                    node = node.previous;
                    continue;
                }
                addFlower(node.data.getID());
                changedCard = wall.getTile(rearPosition);
                node.data.setID(changedCard);
                if (changedCard > 40)
                    hasFlower = true;
                else
                    flowerCount++;
                if (rearPosition == 0)
                    rearPosition = 143;
                else
                    rearPosition--;
            }
        }
        return new ExchangeFlowerResult(rearPosition, flowerCount);
    }

    // 舍牌
    public int discard(int tile) {
        int result = deleteHands(tile);
        addDiscard(tile);
        sortTilesInHand();
        return result;
    }

    // 摸牌
    public int draw(Wall wall, int tilePosition) {
        int tile = wall.getTile(tilePosition);
        sortTilesInHand();
        addHands(tile);
        return tile;
    }

    // 补牌
    public int drawRear(Wall wall, int rearPosition) {
        int tile = wall.getTile(rearPosition);
        sortTilesInHand();
        addHands(tile);
        if (rearPosition == 0) {
            rearPosition = 143;
        } else {
            rearPosition--;
        }
        return rearPosition;
    }
}
