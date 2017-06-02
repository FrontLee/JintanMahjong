package com.vinson.mahjong.AI;

import com.vinson.mahjong.base.AIBasis;
import com.vinson.mahjong.base.Demand;
import com.vinson.mahjong.base.KongOperation;
import com.vinson.mahjong.base.KongType;
import com.vinson.mahjong.base.LowestDemand;
import com.vinson.mahjong.base.Operation;
import com.vinson.mahjong.base.OperationWithDemand;
import com.vinson.mahjong.jintan.utils;

public class Quadruplet {

    public static OperationWithDemand chooseOperationForKong(int tile, AIBasis aiBasis) {
        OperationWithDemand result = new OperationWithDemand();
        if (tile > 30) {
            result.operation = Operation.OPERATION_KONG;
            return result;//风张直接杠
        }
        int hand[][] = utils.getArrayFromDoubleLink(aiBasis.self.getHands());
        // 碰/杠前
        Demand prePriority = AI.getDemand(hand, aiBasis);
        // 碰后
        LowestDemand afterTriple = Triple.getDemandAfterPong(tile, hand, aiBasis);
        // 杠后
        Demand afterQuadruplet = getDemandAfterKong(tile, hand, aiBasis);
        // 比较 优先级:杠>继续>碰
        Demand nextPriority;
        // 先比 碰前?碰后
        if (Triple.isDemandGreatThan(prePriority, afterTriple, tile)) {//需求变小则碰
            result.operation = Operation.OPERATION_PONG;
            nextPriority = new Demand();
            nextPriority.waiting = afterTriple.waiting;
            nextPriority.probability = afterTriple.probability;
            result.demand = nextPriority;
        } else {
            result.operation = Operation.OPERATION_PASS;
            nextPriority = prePriority;
            result.demand = prePriority;
        }
        // 再比 杠后?前面最优
        if (isDemandGreatThan(nextPriority, afterQuadruplet)) {//需求变小则杠
            result.operation = Operation.OPERATION_KONG;
            result.demand = afterQuadruplet;
        }

        return result;
    }

    public static boolean isNeedToSelfKong(KongOperation operation, AIBasis aiBasis) {
        if (operation.tile > 30) return true;//风张直接杠
        int hand[][] = utils.getArrayFromDoubleLink(aiBasis.self.getHands());
        // 碰/杠前
        Demand prePriority = AI.getDemand(hand, aiBasis);
        // 杠后
        Demand nextQuadruplet = getDemandAfterSelfKong(hand, aiBasis, operation);
        // 比较 优先级:杠>继续
        if (isDemandGreatThan(prePriority, nextQuadruplet)) {//需求变小则杠
            return true;
        }

        return false;
    }

    public static Demand getDemandAfterSelfKong(int[][] hand, AIBasis aiBasis, KongOperation operation) {
        int suit = operation.tile / 10;
        int tileIndex = operation.tile % 10 - 1;
        // 删除要杠的牌
        if (operation.type == KongType.EXPOSED_KONG) {
            hand[suit][tileIndex]--;
        } else {
            hand[suit][tileIndex] -= 4;
        }
        // 求最小需求,不用打一张
        Demand nextPriority = AI.getDemand(hand, aiBasis);
        // 添加已删除的牌
        if (operation.type == KongType.EXPOSED_KONG) {
            hand[suit][tileIndex]++;
        } else {
            // 添加已删除的4张牌
            hand[suit][tileIndex] += 4;
        }

        return nextPriority;
    }

    public static Demand getDemandAfterKong(int tile, int[][] hand, AIBasis aiBasis) {
        int suit = tile / 10;
        int tileIndex = tile % 10 - 1;
        // 删除3张要杠的牌
        hand[suit][tileIndex] -= 3;
        // 求最小需求,不用打一张
        Demand nextPriority = AI.getDemand(hand, aiBasis);
        // 添加已删除的3张牌
        hand[suit][tileIndex] += 3;

        return nextPriority;
    }

    public static boolean isDemandGreatThan(Demand demand1, Demand demand2) {
        if (demand1.waiting > demand2.waiting //demand1上听数大，则需求大
                || (demand1.waiting == demand2.waiting // 或上听数相同，但demand1 1类牌数少，则需求大
                && demand1.probability.highPriorityTileCount <= demand2.probability.highPriorityTileCount)) {//杠优先
            return true;
        }
        return false;
    }
}
