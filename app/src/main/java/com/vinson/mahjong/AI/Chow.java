package com.vinson.mahjong.AI;

import com.vinson.mahjong.base.AIBasis;
import com.vinson.mahjong.base.ChowOperation;
import com.vinson.mahjong.base.ChowType;
import com.vinson.mahjong.base.Demand;
import com.vinson.mahjong.base.LowestDemand;
import com.vinson.mahjong.base.Operation;
import com.vinson.mahjong.base.Probability;
import com.vinson.mahjong.jintan.utils;

import java.util.List;

public class Chow {
    public static ChowOperation getDemandAfterChow(List<ChowType> chowTypes, int discardTile, AIBasis aiBasis) {
        ChowOperation result = new ChowOperation();
        Demand afterDemand = new Demand();
        afterDemand.waiting = 9;
        int hand[][] = utils.getArrayFromDoubleLink(aiBasis.self.getHands());
        // 吃前
        Demand preDemand = AI.getDemand(hand, aiBasis);
        int suit = discardTile / 10;
        int index = discardTile % 10 - 1;
        for (int i = 0; i < chowTypes.size(); i++) {
            ChowType chowType = chowTypes.get(i);
            if (chowType == ChowType.CHOW_LEFT) {
                hand[suit][index + 1]--;
                hand[suit][index + 2]--;
            } else if (chowType == ChowType.CHOW_CENTER) {
                hand[suit][index - 1]--;
                hand[suit][index + 1]--;
            } else {
                hand[suit][index - 2]--;
                hand[suit][index - 1]--;
            }
            LowestDemand demand = AI.getLowestDemandAfterDiscard(hand, aiBasis);
            Probability probability = demand.probability;
            if (afterDemand.waiting > demand.waiting ||
                    (afterDemand.waiting == demand.waiting && probability.isGreaterThan(afterDemand.probability))) {
                afterDemand.waiting = demand.waiting;
                afterDemand.probability = probability;
                result.chowType = chowType;
            }
            if (chowType == ChowType.CHOW_LEFT) {
                hand[suit][index + 1]++;
                hand[suit][index + 2]++;
            } else if (chowType == ChowType.CHOW_CENTER) {
                hand[suit][index - 1]++;
                hand[suit][index + 1]++;
            } else {
                hand[suit][index - 2]++;
                hand[suit][index - 1]++;
            }
        }
        if (afterDemand.waiting < preDemand.waiting ||
                (afterDemand.waiting == preDemand.waiting && afterDemand.probability.isGreaterThan(preDemand.probability))) {
            result.demand = afterDemand;
            result.operation = Operation.OPERATION_CHOW;
        } else {
            result.demand = preDemand;
            result.chowType = null;
            result.operation = Operation.OPERATION_PASS;
        }
        return result;
    }
}
