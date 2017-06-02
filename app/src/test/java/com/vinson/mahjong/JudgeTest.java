package com.vinson.mahjong;

import com.vinson.mahjong.base.ChowType;
import com.vinson.mahjong.base.PlayerType;
import com.vinson.mahjong.mahjong.Judge;
import com.vinson.mahjong.mahjong.Player;

import junit.framework.TestCase;

import java.util.List;

public class JudgeTest extends TestCase {
    public void setUp() throws Exception {
        super.setUp();

    }

    public void tearDown() throws Exception {

    }

    public void testGetWinSides() throws Exception {

    }

    public void testHasSelfDrawn() throws Exception {

    }

    public void testHasKongOrPong() throws Exception {

    }

    public void testHasSelfKong() throws Exception {

    }

    public void testHasChow() throws Exception {
        Player self = new Player("test", "for test", false, PlayerType.HUMAN);
        self.clearHands();
        self.addHands(1);
        self.addHands(3);
        self.addHands(6);
        self.addHands(7);
        self.addHands(7);
        self.addHands(14);
        self.addHands(14);
        self.addHands(14);
        self.addHands(17);
        self.addHands(19);
        self.addHands(27);
        self.addHands(27);
        self.addHands(29);
        List<ChowType> chowTypes = Judge.hasChow(18, self);
        System.out.println("可吃类型" + chowTypes.size());
    }

}