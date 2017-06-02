package com.vinson.mahjong;

import com.vinson.mahjong.AI.AI;
import com.vinson.mahjong.AI.Chow;
import com.vinson.mahjong.base.AIBasis;
import com.vinson.mahjong.base.ChowOperation;
import com.vinson.mahjong.base.ChowType;
import com.vinson.mahjong.base.Demand;
import com.vinson.mahjong.base.DoubleLink;
import com.vinson.mahjong.base.LowestDemand;
import com.vinson.mahjong.base.PlayerType;
import com.vinson.mahjong.base.Tile;
import com.vinson.mahjong.base.log;
import com.vinson.mahjong.mahjong.Player;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

public class AITest extends TestCase {
    private Player self;
    private int hand[][];
    private AIBasis aiBasis;
    public void setUp() throws Exception {
        super.setUp();
        //TODO 获取出哪张牌后需求最小测试用例
        //TODO 获取最小需求测试用例。上和数和1类牌和伪2类牌的数量。
        //TODO 获取上和数（上多少张成和）测试用例
        //TODO 获取1类牌和伪2类牌的数量测试用例
        //TODO 选择出牌测试用例。根据需求判定。
        //TODO 抓牌后【自摸、杠、出牌】操作的选择测试用例。自摸最优，其他根据需求判定。在上听数不变时，杠优先。
        //TODO 其他家出牌后【吃和、杠、碰、过】操作的选择测试用例。胡最优，其他根据需求判定。在上听数不变时，杠优先。东南西北中发白，碰优先
        self = new Player("test", "for test", false, PlayerType.HUMAN);
        DoubleLink<Tile> empty = new DoubleLink<Tile>();
        aiBasis = new AIBasis();
        aiBasis.self = self;
        aiBasis.leftDiscard = empty;
        aiBasis.leftOpen = empty;
        aiBasis.rightDiscard = empty;
        aiBasis.rightOpen = empty;
        aiBasis.topDiscard = empty;
        aiBasis.topOpen = empty;
        log.JUNIT_TEST = true;
    }

    public void tearDown() throws Exception {
        log.JUNIT_TEST = false;
    }

    public void testGetLowestDemandAfterDiscard() throws Exception {
        hand = new int[][]{ { 0, 0, 1, 1, 1, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 1, 1, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0 } };
        self.clearHands();
        self.addHands(3);
        self.addHands(4);
        self.addHands(5);
        self.addHands(17);
        self.addHands(18);
        int waiting = AI.getWaitingForNormal(hand, true);
        System.out.print("normal waiting:" + waiting);
        LowestDemand demand = AI.getLowestDemandAfterDiscard(hand, aiBasis);
        System.out.print("打：" + demand.tile + " -> " + demand.waiting + "->" + demand.probability.highPriorityTileCount
                + "-" + demand.probability.middlePriorityTileCount );
    }

    public void testGetDemand() throws Exception {
        hand = new int[][]{ { 2, 1, 1, 1, 0, 0, 1, 0, 1 },
                { 0, 1, 1, 1, 0, 0, 0, 0, 0 }, { 0, 0, 1, 0, 0, 0, 0, 1, 1 },
                { 0, 0, 0, 0, 0, 0, 0 } };
        self.clearHands();
        self.addHands(1);
        self.addHands(1);
        self.addHands(2);
        self.addHands(3);
        self.addHands(4);
        self.addHands(7);
        self.addHands(9);
        self.addHands(12);
        self.addHands(13);
        self.addHands(14);
        self.addHands(23);
        self.addHands(28);
        self.addHands(29);
        Demand demand = AI.getDemand(hand, aiBasis);
        System.out.print(demand.waiting + "->" + demand.probability.highPriorityTileCount
                + "-" + demand.probability.middlePriorityTileCount );
    }

    public void testGetWaiting() throws Exception {
        hand = new int[][]{ { 0, 0, 1, 1, 1, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 1, 1, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0 } };
        int demand = AI.getWaiting(hand);
        System.out.println("最终上和数：" + demand);
//        hand = new int[][]{ { 1, 0, 0, 0, 0, 0, 0, 0, 1 },
//                { 1, 0, 0, 0, 0, 0, 0, 0, 1 }, { 1, 0, 0, 0, 0, 0, 0, 0, 2 },
//                { 1, 1, 1, 1, 1, 1, 1 } };
//        demand = AI.getWaiting(hand);
//        System.out.println("最终上和数：" + demand);
//        hand = new int[][]{ { 2, 2, 0, 0, 2, 0, 0, 0, 4 },
//                { 0, 0, 0, 0, 4, 0, 0, 0, 1 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0 },
//                { 0, 0, 0, 0, 0, 0, 0 } };
//        demand = AI.getWaiting(hand);
//        System.out.println("最终上和数：" + demand);
    }

    public void testGetDiscard() throws Exception {

    }

    public void testChooseOperationForDraw() throws Exception {

    }

    public void testChooseOperationForDiscard() throws Exception {

    }

    public void testGetDemandAfterChow() throws Exception {
        self.clearHands();
        self.addHands(2);
        self.addHands(3);
        self.addHands(7);
        self.addHands(12);
        self.addHands(13);
        self.addHands(14);
        self.addHands(16);
        self.addHands(17);
        self.addHands(18);
        self.addHands(22);
        self.addHands(24);
        self.addHands(26);
        self.addHands(28);
        List<ChowType> chowTypes = new ArrayList<ChowType>();
        chowTypes.add(ChowType.CHOW_CENTER);
        ChowOperation chowOperation = Chow.getDemandAfterChow(chowTypes, 13, aiBasis);
        System.out.print(chowOperation.operation + " - " + chowOperation.demand.waiting + " - " + chowOperation.demand.probability.highPriorityTileCount);
    }
}