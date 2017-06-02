package com.vinson.mahjong.mahjong;

import android.util.Log;

import com.vinson.mahjong.AI.AI;
import com.vinson.mahjong.base.AIBasis;
import com.vinson.mahjong.base.ChowType;
import com.vinson.mahjong.base.Constant;
import com.vinson.mahjong.base.Demand;
import com.vinson.mahjong.base.DoubleLink;
import com.vinson.mahjong.base.KongOperation;
import com.vinson.mahjong.base.KongType;
import com.vinson.mahjong.base.Node;
import com.vinson.mahjong.base.Operation;
import com.vinson.mahjong.base.PongOperation;
import com.vinson.mahjong.base.Tile;
import com.vinson.mahjong.base.Win;
import com.vinson.mahjong.base.WinType;
import com.vinson.mahjong.jintan.utils;

import java.util.ArrayList;
import java.util.List;

public class Judge {
    public static List<Integer> getWinSides(Gambling gambling, int tile, boolean isRobKong) {
        int currentSide = gambling.currentSide;
        Player[] players = gambling.getPlayers();
        List<Integer> result = new ArrayList<Integer>();
        for (int i = (currentSide + 1) % 4; i != currentSide; i = (i + 1) % 4) {
            if (players[i].hasFlowerInHand()) continue;// 手牌有花不能胡
            int hand[][] = utils.getArrayFromDoubleLink(players[i].getHands());
            int suit = tile / 10;
            int tileIndex = tile % 10 - 1;
            hand[suit][tileIndex] += 1;

            if (!isRobKong) {
                if (players[i].getFlowerCount() < 1) {// 无明花
                    if (!hasMultipleOrFlowerInHand(hand)) {// 无暗花也无番
                        hand[suit][tileIndex] -= 1;
                        continue;
                    }
                }
            }

            Demand demand = new Demand();
            demand.waiting = AI.getWaiting(hand);
            demand.probability = AI.getProbability(hand, gambling.getAIBasis(i), demand.waiting);
            if (demand.waiting == -1) {
                result.add(i);
            }
            hand[suit][tileIndex] -= 1;
        }
        return result;
    }

    public static boolean hasSelfDrawn(AIBasis aiBasis) {
        boolean result = false;
        if (aiBasis.self.hasFlowerInHand()) return false;// 手牌有花不能胡哦
        int hand[][] = utils.getArrayFromDoubleLink(aiBasis.self.getHands());
        int demand = AI.getWaiting(hand);
        if (demand == -1) {
            result = true;
        }
        return result;
    }

    public static PongOperation hasKongOrPong(int whoDiscard, int tileID, Gambling gambling) {
        PongOperation operation = new PongOperation();
        operation.operation = null;
        operation.side = -1;
        Player[] players = gambling.getPlayers();
        for (int i = 0; i < players.length; i++) {
            if (i == whoDiscard) continue;// 不判断自己
            int count = 0;
            DoubleLink<Tile> hands = players[i].getHands();
            Node<Tile> tile = hands.last;

            while (tile != null) {
                if (tile.data.getID() == tileID) {
                    count++;
                }
                tile = tile.previous;
            }
            if (count == 2) {
                operation.operation = Operation.OPERATION_PONG;
                operation.side = i;
                return operation;
            } else if (count == 3) {
                operation.operation = Operation.OPERATION_KONG;
                operation.side = i;
                return operation;
            }
        }

        return operation;
    }

    public static List<KongOperation> hasSelfKong(Player player) {
        List<KongOperation> kongOperations = new ArrayList<KongOperation>();
        int hand[][] = utils.getArrayFromDoubleLink(player.getHands());
        //暗杠
        for (int suit = 0; suit < hand.length; suit++) {
            for (int tileIndex = 0; tileIndex < hand[suit].length; tileIndex++) {
                if (hand[suit][tileIndex] == 4) {
                    KongOperation operation = new KongOperation();
                    operation.tile = suit * 10 + tileIndex + 1;
                    operation.type = KongType.CONCEALED_KONG;
                    kongOperations.add(operation);
                }
            }
        }
        hand = utils.getArrayFromDoubleLink(player.getOpenHands());
        //明杠
        List<Integer> openTiles = new ArrayList<Integer>();
        for (int suit = 0; suit < hand.length; suit++) {
            for (int tileIndex = 0; tileIndex < hand[suit].length; tileIndex++) {
                if (hand[suit][tileIndex] == 3) {
                    openTiles.add(suit * 10 + tileIndex + 1);
                }
            }
        }

        for (int i = 0; i < openTiles.size(); i++) {
            int openTile = openTiles.get(i);
            if (player.searchHands(openTile) > 0) {
                KongOperation operation = new KongOperation();
                operation.tile = openTile;
                operation.type = KongType.EXPOSED_KONG;
                kongOperations.add(operation);
            }
        }

        return kongOperations;
    }

    private static boolean hasMultipleOrFlowerInHand(int[][] hand) {
        int totalCount = 0;//总牌数
        int pairCount = 0;//对子数量
        int tripleCount = 0;//刻字数量
        int flowerCount = 0;//花的数量
        int suitCounts[] = {0, 0, 0, 0};//万-筒-索-风张数量
        for(int suit = 0; suit < hand.length; suit++) {
            for (int tileIndex = 0; tileIndex < hand[suit].length; tileIndex++) {
                totalCount += hand[suit][tileIndex];
                if(hand[suit][tileIndex] == 2) pairCount++;//对子
                else if (hand[suit][tileIndex] == 3) {//刻子
                    tripleCount++;
                    if (suit == 3) flowerCount++;//字牌刻子，flowerCount仅做判花用非花数
                }else if (hand[suit][tileIndex] == 4) flowerCount++;//暗杠
                suitCounts[suit] += hand[suit][tileIndex];
            }
        }
        int multiple = 0;//倍数
        if (totalCount == 1) {// 独钓
            multiple += 2;
        } else if (pairCount == 1 && tripleCount * 3 + 2 == totalCount) {// 对对胡
            multiple += 2;
        }
        int suitType = 0;
        for (int i = 0; i < 3; i++) {//万筒索
            if (suitCounts[i] > 0) suitType++;
        }
        if (suitType == 1 && suitCounts[3] > 0) {//万筒索只有一种，且有字牌
            multiple += 2;
        } else if (suitType == 1 && suitCounts[3] == 0){//万筒索只有一种，且无字牌
            multiple += 4;
        }

        return (multiple > 0 || flowerCount > 0);
    }

    public static String getScores(Player[] players, Win win) {
        StringBuilder sb = new StringBuilder();
        int count = 1;
        int flower = 0;
        int multiple = 1;
        int total = 0;

        if (players[win.winSide].Flower.size == 8) {
            sb.append("花胡  封顶\n");
            total = (10 + 8 * 5) * 4;
            for (int i = 0; i < players.length; i++) {
                if (i == win.winSide) {
                    players[win.winSide].Score += total * 3;
                } else {
                    players[i].Score -= total;
                }
            }
            sb.append("总计:" + total + " X 3");
            return sb.toString();
        }


        flower = players[win.winSide].getFlowerCount()
                + players[win.winSide].getYeFlowerInHand();
        if (win.tile > 30 && win.tile < 40) {
            flower--;
        }

        if (flower == 0) {
            sb.append("无花果\n");
            flower = 1;
        } else {
            sb.append("花 X " + flower + "\n");
        }

        if (players[win.winSide].getHands().size == 2) {// 独钓
            sb.append("独吊 1番\n");
            count *= 2;
        } else if (isPairHu(players[win.winSide])) {// 对对胡
            sb.append("对对胡 1番\n");
            count *= 2;
        }
        int uniform = isUniform(players[win.winSide]);
        if (uniform == 1) {
            sb.append("混一色 1番\n");
            count *= 2;
        } else if (uniform == 2) {
            sb.append("清一色 2番\n");
            count *= 4;
        }

        //TODO 三元、四喜、字一色、九莲宝灯、十三幺、七对、平胡、其他
        if (win.type == WinType.KONG_WIN) {
            sb.append("杠开  1番\n");
            count *= 2;
            multiple = 3;
        } else if (win.type == WinType.ROB_KONG) {
            sb.append("抢杠\n");
            multiple = 3;
        } else if (win.type == WinType.SELF_DRAWN) {
            sb.append("自摸\n");
            multiple = 3;
        } else if (count == 1) {
            sb.append("鸡胡\n");
            multiple = 1;
        }

        total = (10 + flower * 5) * count;

        if (win.type == WinType.KONG_WIN || win.type == WinType.SELF_DRAWN) {// 杠开 自摸
            for (int i = 0; i < players.length; i++) {
                if (i == win.winSide) {
                    players[win.winSide].Score += total * 3;
                } else {
                    players[i].Score -= total;
                }
            }

        } else if (win.type == WinType.ROB_KONG) {// 抢杠
            players[win.winSide].Score += total * 3;
            players[win.gunnerSide].Score -= total * 3;
        } else {// 鸡胡
            players[win.winSide].Score += total;
            players[win.gunnerSide].Score -= total;
        }

        sb.append("总计:" + total + " X " + multiple);
        Log.d(Constant.TAG, sb.toString());

        return sb.toString();
    }

    public static boolean isPairHu(Player player) {
        Node<Tile> hand;
        List<Integer> kinds = new ArrayList<Integer>();
        int tile;

        hand = player.OpenHands.first;
        while (hand != null) {
            tile = hand.data.getID();
            if (!kinds.contains(tile)) {
                kinds.add(tile);
            }
            hand = hand.next;
        }

        hand = player.getBlackHands().first;
        while (hand != null) {
            tile = hand.data.getID();
            if (!kinds.contains(tile)) {
                kinds.add(tile);
            }
            hand = hand.next;
        }

        hand = player.getHands().first;
        while (hand != null) {
            tile = hand.data.getID();
            if (!kinds.contains(tile)) {
                kinds.add(tile);
            }
            hand = hand.next;
        }

        if (kinds.size() == 5) {
            return true;
        } else {
            return false;
        }
    }

    public static int isUniform(Player player) {
        Node<Tile> hand;
        List<Integer> kinds = new ArrayList<Integer>();
        int tile = 0;
        boolean hasC = false;

        hand = player.OpenHands.first;
        while (hand != null) {
            tile = hand.data.getID();
            if (tile > 0 && tile < 10) {
                if (!kinds.contains(1)) {
                    kinds.add(1);
                }
            } else if (tile > 10 && tile < 20) {
                if (!kinds.contains(2)) {
                    kinds.add(2);
                }
            } else if (tile > 20 && tile < 30) {
                if (!kinds.contains(3)) {
                    kinds.add(3);
                }
            } else if (tile > 30 && tile < 40) {
                hasC = true;
            }

            hand = hand.next;
        }

        hand = player.getBlackHands().first;
        while (hand != null) {
            tile = hand.data.getID();
            if (tile > 0 && tile < 10) {
                if (!kinds.contains(1)) {
                    kinds.add(1);
                }
            } else if (tile > 10 && tile < 20) {
                if (!kinds.contains(2)) {
                    kinds.add(2);
                }
            } else if (tile > 20 && tile < 30) {
                if (!kinds.contains(3)) {
                    kinds.add(3);
                }
            } else if (tile > 30 && tile < 40) {
                hasC = true;
            }

            hand = hand.next;
        }

        hand = player.getHands().first;
        while (hand != null) {
            tile = hand.data.getID();
            if (tile > 0 && tile < 10) {
                if (!kinds.contains(1)) {
                    kinds.add(1);
                }
            } else if (tile > 10 && tile < 20) {
                if (!kinds.contains(2)) {
                    kinds.add(2);
                }
            } else if (tile > 20 && tile < 30) {
                if (!kinds.contains(3)) {
                    kinds.add(3);
                }
            } else if (tile > 30 && tile < 40) {
                hasC = true;
            }

            hand = hand.next;
        }

        if (kinds.size() == 1) {
            if (hasC) {
                return 1;
            } else {
                return 2;
            }
        } else {
            return 0;
        }
    }

    public static List<ChowType> hasChow(int discardTile, Player player) {
        List<ChowType> chowTypes = new ArrayList<ChowType>();
        int hand[][] = utils.getArrayFromDoubleLink(player.getHands());
        int tileSuit = discardTile / 10;
        int tileIndex = discardTile % 10 - 1;
        if (tileSuit > 2) return chowTypes;//字牌无吃
        int suit[] = hand[tileSuit];
        if (tileIndex < suit.length - 2 &&  suit[tileIndex + 1] * suit[tileIndex + 2] > 0) {
            chowTypes.add(ChowType.CHOW_LEFT);
        }
        if (tileIndex > 0 && tileIndex < suit.length - 1 && suit[tileIndex + 1] * suit[tileIndex - 1] > 0) {
            chowTypes.add(ChowType.CHOW_CENTER);
        }
        if (tileIndex > 1 && suit[tileIndex - 2] * suit[tileIndex - 1] > 0) {
            chowTypes.add(ChowType.CHOW_RIGHT);
        }
        return chowTypes;
    }
}
