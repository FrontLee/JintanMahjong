package com.vinson.mahjong.mahjong;

import com.vinson.mahjong.AI.AI;
import com.vinson.mahjong.base.AIBasis;
import com.vinson.mahjong.base.AvailableOperations;
import com.vinson.mahjong.base.ChowType;
import com.vinson.mahjong.base.ExchangeFlowerResult;
import com.vinson.mahjong.base.KongOperation;
import com.vinson.mahjong.base.KongType;
import com.vinson.mahjong.base.Operation;
import com.vinson.mahjong.base.PerformKongPongResult;
import com.vinson.mahjong.base.PlayerType;
import com.vinson.mahjong.base.UserOperation;
import com.vinson.mahjong.base.Win;
import com.vinson.mahjong.base.WinType;
import com.vinson.mahjong.base.log;
import com.vinson.mahjong.jintan.utils;

import java.util.ArrayList;
import java.util.List;

public class Gambling {
    private Player[] players;
    public int dealer;//当前局庄家
    private int remainder;//余牌
    private int selfSide;
    private int rightSide;
    private int topSide;
    private int leftSide;
    public int circle;//风圈
    public int currentSide;//当前出牌方
    private Wall wall;//牌墙
    public int frontPosition;//牌头的位置
    public int rearPosition;//牌尾的位置
    public List<Operation> operations = new ArrayList<Operation>();//可显示的操作
    public boolean gangAndWin = false;//杠开标志
    public boolean isJustPong = false;//刚碰过的标志，刚碰过不能胡牌
    public List<Win> alreadyHu = new ArrayList<Win>();//已胡牌的信息
    public boolean isHosted = false;//是否托管
    public boolean displayOperations = false;//是否显示操作
    public boolean isEnded = false;//是否一盘结束，结束需显示所有方位的手牌
    public boolean isTestMode = false;//测试模式，即明牌模式
    public boolean kongSelectMode = false;//选杠模式，多杠时适用
    public int currentKongTile;//多杠时显示哪只杠
    public boolean chowSelectMode = false;
    public int chowPos1;
    public int chowPos2;
    public int tileDisplayInCenter = -2;//在屏幕中央显示的牌，用于杠碰胡
    private AIBasis aiBasis = new AIBasis();
    public boolean isLearnMode = false;

    public void sortAllPlayersHand(Player players[]) {
        for (int i = 0; i < players.length; i++) {
            players[i].sortTilesInHand();
        }
    }

    public Player getSelfPlayer() {
        return players[selfSide];
    }

    public Player getLeftPlayer() {
        return players[leftSide];
    }

    public Player getTopPlayer() {
        return players[topSide];
    }

    public Player getRightPlayer() {
        return players[rightSide];
    }

    public int getRemainder() {
        return remainder;
    }

    public void setPlayers(Player[] players) {
        this.players = players;
    }

    public void setSelfSide(int selfSide) {
        this.selfSide = selfSide;
        this.rightSide = (selfSide + 1) % 4;
        this.topSide = (selfSide + 2) % 4;
        this.leftSide = (selfSide + 3) % 4;
    }

    public int getLeftSide() {
        return leftSide;
    }

    public int getSelfSide() {
        return selfSide;
    }

    public int getRightSide() {
        return rightSide;
    }

    public int getTopSide() {
        return topSide;
    }

    public void clearPlayers() {
        for (int i = 0; i < players.length; i++) {
            players[i].ClearData();
        }
    }

    public void setWall(Wall wall) {
        this.wall = wall;
    }

    public Player[] getPlayers() {
        return players;
    }

    public void calSelfSide() {
        for (int i = 0; i < players.length; i++) {
            if (players[i].Type == PlayerType.HUMAN) {
                setSelfSide(i);
                break;
            }
        }
    }

    public boolean isCurrentPlayerAI() {
        return players[currentSide].AI;
    }

    public boolean isPlayerAI(int side) {
        return players[side].AI;
    }

    public void clearOperations() {
        tileDisplayInCenter = -2;
        displayOperations = false;
        operations.clear();
    }

    public int getSelectedTile() {
        return players[selfSide].getSelectedTile();
    }

    public AIBasis getAIBasis(int side) {
        Player self = players[side];
        Player right = players[(side + 1) % 4];
        Player top = players[(side + 2) % 4];
        Player left = players[(side + 3) % 4];

        aiBasis.self = self;
        aiBasis.rightDiscard = right.Discard;
        aiBasis.rightOpen = right.OpenHands;
        aiBasis.topDiscard = top.Discard;
        aiBasis.topOpen = top.OpenHands;
        aiBasis.leftDiscard = left.Discard;
        aiBasis.leftOpen = left.OpenHands;

        return aiBasis;
    }

    public boolean exchangeFlower() {// false 流局
        // 补花
        ExchangeFlowerResult exFlower;
        Player player = players[currentSide];
        if (player.hasFlowerInHand()) {
            remainder = wall.getRemainderCount();
            if (remainder > 0) {// 补花
                exFlower = player.exchangeFlower(wall, rearPosition);
                if (exFlower.exchangedCount > 1) player.sortTilesInHand();
                rearPosition = exFlower.rearPosition;
                gangAndWin = true;
            } else { // 流局
                return false;
            }
        }
        return true;
    }

    public boolean isFlowerWin() {
        Player player = players[currentSide];
        if (player.getFlower().size == 8) { // 花胡
            Win win = new Win(currentSide, currentSide, WinType.OTHER, 0);
            alreadyHu.add(win);
            return true;
        }
        return false;
    }

    public AvailableOperations getSelfAvailableOperation() {
        AvailableOperations result = new AvailableOperations();
        result.side = currentSide;
        AIBasis aiBasis = getAIBasis(currentSide);
        result.kongOperations = Judge.hasSelfKong(players[currentSide]);
        boolean hasSelfWin = Judge.hasSelfDrawn(aiBasis);
        if (hasSelfWin && !isJustPong)
            result.operations.add(Operation.OPERATION_WIN);
        if (result.kongOperations.size() > 0)
            result.operations.add(Operation.OPERATION_KONG);
        result.operations.add(Operation.OPERATION_DISCARD);
        return result;
    }

    public UserOperation getAIOperationForDraw(AvailableOperations availableOperations) {
        AIBasis aiBasis = getAIBasis(currentSide);
        UserOperation userOperation = AI.chooseOperationForDraw(availableOperations, aiBasis);
        return userOperation;
    }

    public void displayOperations(AvailableOperations availableOperations) {
        displayOperations = false;
        operations.clear();
        for (Operation operation:availableOperations.operations) {
            operations.add(operation);
        }
        displayOperations = true ;
    }

    public void makeDiscardTransparent(int tile) {
        players[currentSide].removeDiscard(tile);
    }

    public void displayRobKongOperation() {
        displayOperations = false;
        operations.clear();
        operations.add(Operation.OPERATION_WIN);
        operations.add(Operation.OPERATION_PASS);
        displayOperations = true;
    }

    public boolean performWin(List<UserOperation> operations, int discardTile) {
        boolean isWin = false;
        List<Integer> winSides = new ArrayList<Integer>();
        for (int i = 0; i < operations.size(); i++) {
            UserOperation operation = operations.get(i);
            if (operation.operation == Operation.OPERATION_WIN) {
                chowWin(operation.side, discardTile, WinType.OTHER);
                isWin = true;
                winSides.add(i);
            }
        }
        return isWin;
    }

    public PerformKongPongResult performKongPong(List<UserOperation> operations, int discardTile) {
        PerformKongPongResult result = new PerformKongPongResult();
        result.isWallOut = false;
        result.hasPongOrGong = false;
        for (UserOperation operation: operations) {
            if (operation.operation == Operation.OPERATION_KONG) {
                result.hasPongOrGong = true;
                if (!kong(operation.side, discardTile)) result.isWallOut = true;// 流局
            } else if (operation.operation == Operation.OPERATION_PONG) {
                result.hasPongOrGong = true;
                pong(operation.side, discardTile);
            }
        }
        return result;
    }

    private void chow(int side, ChowType chowType, int discardTile) {
        int tile1, tile2;
        if (chowType == ChowType.CHOW_LEFT) {
            tile1 = discardTile + 1;
            tile2 = discardTile + 2;
        } else if (chowType == ChowType.CHOW_CENTER) {
            tile1 = discardTile - 1;
            tile2 = discardTile + 1;
        } else {
            tile1 = discardTile - 2;
            tile2 = discardTile - 1;
        }
        players[side].deleteHands(tile1);
        players[side].deleteHands(tile2);
        if (chowType == ChowType.CHOW_LEFT) {
            players[side].addOpenHands(discardTile);
            players[side].addOpenHands(tile1);
            players[side].addOpenHands(tile2);
        }else if (chowType == ChowType.CHOW_CENTER) {
            players[side].addOpenHands(tile1);
            players[side].addOpenHands(discardTile);
            players[side].addOpenHands(tile2);
        } else {
            players[side].addOpenHands(tile1);
            players[side].addOpenHands(tile2);
            players[side].addOpenHands(discardTile);
        }

        players[currentSide].removeDiscard(discardTile);
        currentSide = (currentSide + 1) % 4;
    }


    public void selfDraw() {
        if (gangAndWin) {// 杠开
            Win win = new Win(currentSide, currentSide, WinType.KONG_WIN, 0);
            alreadyHu.add(win);
        } else {// 自摸
            Win win = new Win(currentSide, currentSide, WinType.SELF_DRAWN, 0);
            alreadyHu.add(win);
        }
    }
    public void selfKong(KongOperation kongOperation) {
        quadruplet(currentSide, currentSide, kongOperation);
        players[currentSide].sortTilesInHand();
        isJustPong = false;
    }

    public boolean countervail() {
        if (wall.getRemainderCount() > 0) {// 补牌
            rearPosition = players[currentSide].drawRear(wall, rearPosition);
            gangAndWin = true;
            return true;
        } else {// 流局
            return false;
        }
    }

    public void chowWin(int side, int tile, WinType winType) {
        players[side].addHands(tile);
        Win win = new Win(side, currentSide, winType, tile);
        alreadyHu.add(win);
    }

    public int discard(int tile, long drawTime) {// 舍牌
        if (players[currentSide].AI) {
            long time = System.currentTimeMillis() - drawTime;
            if (time < 1000) {
                int sleep = 1000 - (int)time;
                utils.sleep(sleep);
                log.i("sleep:" + sleep);
            }
        }
        int result = players[currentSide].discard(tile);
        getSelfPlayer().selectedPos = -1;
        return result;
    }

    public boolean kong(int side, int discardTile) {// 杠, 如为 false 则流局
        KongOperation operation = new KongOperation();
        operation.tile = discardTile;
        operation.type = KongType.EXPOSED_KONG;
        quadruplet(currentSide, side, operation);
        currentSide = side;
        remainder = wall.getRemainderCount();
        if (remainder > 0) {// 补牌
            int endDraw = players[currentSide].drawRear(wall, rearPosition);
            rearPosition = endDraw;
            gangAndWin = true;
        } else {// 流局
            return false;
        }
        return true;
    }

    public void pong(int side, int discardTile) {// 碰
        // 删2张碰掉的牌
        players[side].deleteHands(discardTile);
        players[side].deleteHands(discardTile);
        // 加3张到明牌中
        players[side].addOpenHands(discardTile);
        players[side].addOpenHands(discardTile);
        players[side].addOpenHands(discardTile);
        // 删除舍牌
        players[currentSide].removeDiscard(discardTile);
        //
        if (discardTile > 30) {
            players[side].addFlowerCount(1);
        }
        currentSide = side;
        isJustPong = true;
    }

    public boolean draw() {// 摸牌 false 流局
        remainder = wall.getRemainderCount();
        if (remainder > 0) {
            currentSide = (currentSide + 1) % 4;
            players[currentSide].draw(wall, frontPosition);
            frontPosition = (frontPosition + 1) % 144;
        } else { // 流局
            return false;
        }// 摸牌结束
        return true;
    }

    public void quadruplet(int discardSide, int kongSide, KongOperation kongOperation) {
        int tile = kongOperation.tile;
        // 删除discard
        players[discardSide].removeDiscard(tile);
        if (discardSide != kongSide) {
            // 删除3张杠掉的牌
            players[kongSide].deleteHands(tile);
            players[kongSide].deleteHands(tile);
            players[kongSide].deleteHands(tile);
            // 添加4张到明牌
            players[kongSide].addOpenHands(tile);
            players[kongSide].addOpenHands(tile);
            players[kongSide].addOpenHands(tile);
            players[kongSide].addOpenHands(tile);
            players[kongSide].addFlowerCount(1);
            if (tile > 30) {
                players[kongSide].addFlowerCount(2);
            }
        } else {
            if (kongOperation.type == KongType.EXPOSED_KONG) {// 明杠
                players[kongSide].deleteHands(tile);
                players[kongSide].addOpenHands(tile);
                players[kongSide].addFlowerCount(1);
                if (tile > 30) {
                    players[kongSide].addFlowerCount(2);
                }
            } else if (kongOperation.type == KongType.CONCEALED_KONG) {
                // 删除4张暗杠掉的牌
                players[kongSide].deleteHands(tile);
                players[kongSide].deleteHands(tile);
                players[kongSide].deleteHands(tile);
                players[kongSide].deleteHands(tile);

                // 添加4张到暗牌
                players[kongSide].addBlackHands(tile);
                players[kongSide].addBlackHands(tile);
                players[kongSide].addBlackHands(tile);
                players[kongSide].addBlackHands(tile);

                players[kongSide].addFlowerCount(2);
                if (tile > 30) {
                    players[kongSide].addFlowerCount(2);
                }
            }
        }
    }

    public void robKong(int tile) {
        //删除当前方明牌
        players[currentSide].removeOpenHand(tile);
    }

    public Player getNextPlayer() {
        int side = (currentSide +1) % 4;
        return players[side];
    }

    public boolean performChow(List<UserOperation> otherOperations, int discardTile) {
        boolean result = false;
        for (UserOperation operation: otherOperations) {
            if (operation.operation == Operation.OPERATION_CHOW) {
                result = true;
                chow(operation.side, operation.chowType, discardTile);
            }
        }
        return result;
    }
}
