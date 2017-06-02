package com.vinson.mahjong.AI;

import com.vinson.mahjong.base.AIBasis;
import com.vinson.mahjong.base.Demand;
import com.vinson.mahjong.base.LowestDemand;
import com.vinson.mahjong.base.Operation;
import com.vinson.mahjong.base.OperationWithDemand;
import com.vinson.mahjong.jintan.utils;

public class Triple {

    // 优先级:杠>过
    public static LowestDemand getDemandAfterPong(int tile, int[][] hand, AIBasis aiBasis) {
        int suit = tile / 10;
        int tileIndex = tile % 10 - 1;
        // 删2张要碰的牌
        hand[suit][tileIndex] -= 2;
        // 求打出一张牌后的最小需求
        LowestDemand demand = AI.getLowestDemandAfterDiscard(hand, aiBasis);
        // 重新添加2张删掉的牌
        hand[suit][tileIndex] += 2;

        return demand;
    }

    // 优先级:过>碰
    public static OperationWithDemand chooseOperationForPong(int tile, AIBasis aiBasis) {
        OperationWithDemand result = new OperationWithDemand();
        int hand[][] = utils.getArrayFromDoubleLink(aiBasis.self.getHands());
        // 碰前
        Demand prePriority = AI.getDemand(hand, aiBasis);
        // 碰后
        LowestDemand afterDemand = getDemandAfterPong(tile, hand, aiBasis);
        //字牌优先级:碰>继续,其他：继续 > 碰
        if (isDemandGreatThan(prePriority, afterDemand, tile)) {
            result.operation = Operation.OPERATION_PONG;
            Demand demand = new Demand();
            demand.waiting = afterDemand.waiting;
            demand.probability = afterDemand.probability;
            result.demand = demand;
        } else {
            result.operation = Operation.OPERATION_PASS;
            result.demand = prePriority;
        }
        return result;
    }

    public static boolean isDemandGreatThan(Demand demand1, LowestDemand demand2, int tile) {
        if (tile > 30) {//东南西北中发白，只判断1类牌数,且优先保留
            if (demand1.waiting > demand2.waiting //demand1上听数大，则需求大
                    || (demand1.waiting == demand2.waiting // 或上听数相同，但demand1 1类牌数少，则需求大
                    && demand1.probability.highPriorityTileCount <= demand2.probability.highPriorityTileCount)) {
                return true;
            }
        } else if (demand1.waiting > demand2.waiting //demand1上听数大，则需求大
                || (demand1.waiting == demand2.waiting // 或上听数相同，但demand1概率小，则需求大
                && demand1.probability.isLessThan(demand2.probability))) {
            return true;
        }
        return false;
    }
}
