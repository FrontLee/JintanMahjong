package com.vinson.mahjong.AI;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import com.vinson.mahjong.base.AIBasis;
import com.vinson.mahjong.base.AvailableOperations;
import com.vinson.mahjong.base.ChowOperation;
import com.vinson.mahjong.base.Demand;
import com.vinson.mahjong.base.DoubleLink;
import com.vinson.mahjong.base.KongOperation;
import com.vinson.mahjong.base.LowestDemand;
import com.vinson.mahjong.base.Node;
import com.vinson.mahjong.base.Operation;
import com.vinson.mahjong.base.OperationWithDemand;
import com.vinson.mahjong.base.Probability;
import com.vinson.mahjong.base.Tile;
import com.vinson.mahjong.base.UserOperation;
import com.vinson.mahjong.base.Waiting;
import com.vinson.mahjong.base.log;
import com.vinson.mahjong.jintan.utils;

//TODO 1类牌的余数为0时，上和数应无效
public class AI {

    public static LowestDemand getLowestDemandAfterDiscard(int[][] hand, AIBasis aiBasis) {
        int tile;
        LowestDemand lowestDemand = new LowestDemand();
        lowestDemand.probability = new Probability();
        //计算最低上和数，及对应的牌
        List<Waiting> tileWaiting = getLowestWaitingForDiscard(hand, aiBasis);
        log.i("lowest waiting" + tileWaiting.get(0).waiting);
        lowestDemand.waiting = tileWaiting.get(0).waiting;
        //通过计算概率，确定出最终要打的牌
        for (int i = 0; i < tileWaiting.size(); i++) {
            tile = tileWaiting.get(i).tile;
            log.i("[出牌后最小需求]打出最小上和牌：" + tile);
            int suit = tile / 10;
            int tileIndex = tile % 10 - 1;
            hand[suit][tileIndex]--;
            Probability probability = getProbability(hand, aiBasis, lowestDemand.waiting);
            hand[suit][tileIndex]++;
            log.i("[出牌后最小需求]概率为：" + probability.highPriorityTileCount +
                    "-" + probability.middlePriorityTileCount);
            if (probability.isGreaterThan(lowestDemand.probability)) {
                lowestDemand.probability = probability;
                lowestDemand.tile = tile;
                log.i("[出牌后最小需求]确定新最小需求：" + tile + "->" +
                        probability.highPriorityTileCount +
                        "-" + probability.middlePriorityTileCount);
            }
        }
        return lowestDemand;

    }

    public static List<Waiting> getLowestWaitingForDiscard(int[][] hand, AIBasis aiBasis) {
        List<Waiting> tileWaiting = new ArrayList<Waiting>();
        int tile;//计算最小上和数、及各牌的上和数
        int lowestWaiting = 9;
        for (int suit = 0; suit < 4; suit++) {// 每种牌都删一张试试
            for (int tileIndex = 0; tileIndex < hand[suit].length; tileIndex++) {
                if (hand[suit][tileIndex] < 1) continue;
                tile = suit * 10 + tileIndex + 1;
                hand[suit][tileIndex]--;
                // 舍牌后上和数
                int waiting = getWaiting(hand);
                tileWaiting.add(new Waiting(tile, waiting));
                if (waiting < lowestWaiting)
                    lowestWaiting = waiting;
                hand[suit][tileIndex]++;
            }
        }
        log.i("最小需求为：" + lowestWaiting);
        //删除上和数非最小的牌
        Iterator<Waiting> iterator = tileWaiting.iterator();
        while(iterator.hasNext()){
            Waiting waiting = iterator.next();
            if(waiting.waiting > lowestWaiting){
                iterator.remove();
            } else {
                log.i("打出：" + waiting.tile);
            }
        }

        return tileWaiting;
    }

    //获取上和数，及概率
    public static Demand getDemand(int[][] hand, AIBasis aiBasis) {
        Demand demand = new Demand();
        // 获得需求
        demand.waiting = getWaiting(hand);
        demand.probability = getProbability(hand, aiBasis, demand.waiting);
        return demand;
    }

    public static boolean hasPair(int[][] hand) {
        boolean result = false;
        for (int suit = 0; suit < hand.length; suit++) {
            for (int tileIndex = 0; tileIndex < hand[suit].length; tileIndex++) {
                if (hand[suit][tileIndex] == 2) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    //获取上和数
    public static int getWaiting(int[][] hand) {
        int lowestWaiting;
        int waiting;
        lowestWaiting = getWaitingFor13Orphans(hand);
        waiting = getWaitingFor7Pairs(hand);
        if (lowestWaiting > waiting) lowestWaiting = waiting;
        waiting = getWaitingForNormal(hand, true);
        if (lowestWaiting > waiting) lowestWaiting = waiting;
        return lowestWaiting;
    }

    private static int getWaitingFor13Orphans(int[][] hand) {
        int orphans = 0;
        int hasPair = 0;
        for (int suit = 0; suit < 3; suit++) {
            if (hand[suit][0] > 0) orphans++;
            if (hand[suit][0] > 1) hasPair = 1;
            if (hand[suit][8] > 0) orphans++;
            if (hand[suit][8] > 1) hasPair = 1;
        }
        for (int tile = 0; tile < hand[3].length; tile++) {
            if(hand[3][tile] > 0) orphans++;
            if(hand[3][tile] > 1) hasPair = 1;
        }
        if (14 - orphans - hasPair == 0)
            return -1;
        else
            return 14 - orphans - hasPair;
    }

    private static int getWaitingFor7Pairs(int[][] hand) {
        int pairs = 0;
        for (int suit = 0; suit < 4; suit++) {
            for (int tileIndex = 0; tileIndex < hand[suit].length; tileIndex++) {
                if (hand[suit][tileIndex] >= 2) {
                    pairs++;
                }
                if (hand[suit][tileIndex] == 4) {
                    pairs++;
                }
            }
        }
        if (7 - pairs == 0)
            return -1;
        else
            return 7 - pairs;
    }

    //获取上和数 非十三幺、七对
    public static int getWaitingForNormal(int[][] hand, boolean considerPair) {
        int waiting;
        int count = 0;
        //获取
        List<int[]> triple = new ArrayList<int[]>();
        List<int[]> sequence = new ArrayList<int[]>();
        List<int[]> pair = new ArrayList<int[]>();
        for (int suit = 0; suit < 4; suit++) {
            for (int tileIndex = 0; tileIndex < hand[suit].length; tileIndex++) {
                if (hand[suit][tileIndex] > 1) {
                    pair.add(new int[] { suit, tileIndex });// 获得将牌
                }
                if (hand[suit][tileIndex] > 2) {
                    triple.add(new int[] { suit, tileIndex });// 获得刻子
                }
                if (suit < 3) {// 字牌无顺子
                    if (hand[suit][tileIndex] > 0) {
                        count++;
                        if (tileIndex == hand[suit].length - 1) {
                            if (count > 2) {
                                // 获得顺子 使用范围 int[1]=< x <int[1] + int[2]
                                sequence.add(new int[] { suit, tileIndex - count + 1, count });
                            }
                            count = 0;
                        }
                    } else if (hand[suit][tileIndex] == 0) {
                        if (count > 2) {
                            // 获得顺子 使用范围 int[1]=< x <int[1] + int[2]
                            sequence.add(new int[] { suit, tileIndex - count, count });
                        }
                        count = 0;
                    }
                }
            }
        }
        if (triple.size() == 0 && sequence.size() == 0//剥离到无顺无刻，
                && (!considerPair || (considerPair && pair.size() == 0))) {//且不考虑对子或考虑对子但无对子，即剩余不成顺子的单张和对子
            return getWaitingForScattered(hand, considerPair);
        } else {//有刻、或有顺、或考虑对子且有对子，则尝试剥离
            waiting = 9;
            for (int i = 0; i < triple.size(); i++) {// 有刻字则获得最小需求
                int a[] = triple.get(i);
                // 去掉刻子
                hand[a[0]][a[1]] -= 3;
                // 获得需求
                int demandCount1 = getWaitingForNormal(hand, considerPair);
                if (waiting > demandCount1) { // 需求少
                    waiting = demandCount1;
                }
                // 恢复
                hand[a[0]][a[1]] += 3;
                if (waiting == -1) return waiting;
            }

            for (int suit = 0; suit < sequence.size(); suit++) {// 还有顺子则获取最小需求
                int a[] = sequence.get(suit);
                for (int tileIndex = a[1]; tileIndex < a[1] + a[2] - 2; tileIndex++) {//顺子的可能情况，比如a[2](即连牌的个数) = 4，则存在2种情况
                    // 去掉顺子
                    hand[a[0]][tileIndex]--;
                    hand[a[0]][tileIndex + 1]--;
                    hand[a[0]][tileIndex + 2]--;
                    // 获得需求
                    int demandCount2 = getWaitingForNormal(hand, considerPair);
                    if (waiting > demandCount2) { // 需求少
                        waiting = demandCount2;
                    }
                    // 恢复顺子
                    hand[a[0]][tileIndex]++;
                    hand[a[0]][tileIndex + 1]++;
                    hand[a[0]][tileIndex + 2]++;
                    if (waiting == -1) return waiting;
                }
            }

            if (considerPair) {
                for (int i = 0; i < pair.size(); i++) {// 还有对子则获取最小需求
                    int a[] = pair.get(i);
                    // 去掉对子
                    hand[a[0]][a[1]] -= 2;
                    // 获得需求
                    int demandCount3 = getWaitingForNormal(hand, false);
                    if (waiting > demandCount3) { // 需求少
                        waiting = demandCount3;
                    }
                    // 恢复对子
                    hand[a[0]][a[1]] += 2;
                    if (waiting == -1) return waiting;
                }
            }

            return waiting;
        }
    }
    //获取无刻无顺牌的上和数
    private static int getWaitingForScattered(int[][] hand, boolean considerPair) {
        int demandCount;
        int count;
        int lugCount = 0;
        demandCount = 9;
        count = 0;
        for (int suit = 0; suit < 4; suit++) {
            for (int tileIndex = 0; tileIndex < hand[suit].length; tileIndex++) {
                if (hand[suit][tileIndex] > 0) {
                    count += hand[suit][tileIndex];// 共有多少牌
                    if (hand[suit][tileIndex] == 2) {// 先算对子, 防止 3344型牌
                        lugCount++;
                    } else if (suit < 3) {// 字牌无搭子
                        if (tileIndex + 1 < hand[suit].length && hand[suit][tileIndex + 1] > 0) {//靠搭
                            lugCount++;
                            count += hand[suit][tileIndex + 1];
                            tileIndex++;
                        } else if (tileIndex + 2 < hand[suit].length
                                && hand[suit][tileIndex + 2] > 0) {//嵌搭
                            lugCount++;
                            count += hand[suit][tileIndex + 1];
                            count += hand[suit][tileIndex + 2];
                            tileIndex += 2;
                        }
                    }
                }
            }
        }
        if (count == 0) {//可胡牌
            return -1;
        }

        if ((count + 2) % 3 == 0) {//余1张4张7张
            if (lugCount > (count + 2) / 3)
                lugCount = (count + 2) / 3;
            demandCount = (count + 2) * 2 / 3;
            if (considerPair & hasPair(hand)){
                demandCount--;
            }
        } else if ((count + 1) % 3 == 0) {//余2张5张8张
            if (lugCount > (count + 1) / 3)
                lugCount = (count + 1) / 3;
            demandCount = (count + 1) * 2 / 3;
            if (considerPair & hasPair(hand))// 考虑对子
                demandCount--;
        } else if (count % 3 == 0) {//余3张6张9张 非常规情况,可能出现在碰牌时手中有花 或胡牌，
            if (lugCount > count / 3)
                lugCount = (count) / 3;
            demandCount = count * 2 / 3;
        }
        demandCount -= lugCount;
        if (count == 1) demandCount--;
        return demandCount;
    }

    public static Probability getProbability(int[][] hand, AIBasis aiBasis, int originWaiting) {
        if (originWaiting == -1) {
            return new Probability(0, 0, 0);
        }
        Probability probability = new Probability();
        List<Integer> related = getRelatedTiles(hand);
        List<Integer> secondaryCandidates = new ArrayList<Integer>();
        //计算1类
        for (int i = 0; i < related.size(); i++) {
            int tile = related.get(i);
            int suit = tile / 10;
            int tileIndex = tile % 10 - 1;
            hand[suit][tileIndex] += 1;
            int waiting = getWaiting(hand);
            log.i("[概率]牌：" + tile + " -> 上和数：" + waiting);
            if (waiting < originWaiting) {
                int remainCount = getRemaining(aiBasis, tile);
                probability.highPriorityTileCount += remainCount;
                log.i("[概率]1类牌确定" + tile);
            } else if (waiting == originWaiting){
                secondaryCandidates.add(tile);
                log.i("[概率]2类候选牌确定：" + tile);
            }
            hand[suit][tileIndex] -= 1;
        }
        log.i("[概率]1类牌余量：" + probability.highPriorityTileCount + "\n");
        //计算2类
        for (int i = 0; i < secondaryCandidates.size(); i++) {
            int tile = secondaryCandidates.get(i);
            int suit = tile / 10;
            int tileIndex = tile % 10 - 1;
            hand[suit][tileIndex] += 1;
            log.i("[概率]当前2类候选牌：" + tile);
            //获取打出后上和数最少的牌
            List<Waiting> waiting = getLowestWaitingForDiscard(hand, aiBasis);
            w:for (int j = 0; j < waiting.size(); j++) {
                int discard = waiting.get(j).tile;
                int discardSuit = discard / 10;
                int discardIndex = discard % 10 - 1;
                //打出牌张
                hand[discardSuit][discardIndex]--;
                //计算1类数量
                int primary = getPrimaryCount(hand, aiBasis, originWaiting);
                log.i("[概率]打出：" + discard + "后 -> 1类牌余量：" + primary);
                //恢复牌张
                hand[discardSuit][discardIndex]++;
                //如1类数量增多，则为2类牌
                if (primary > probability.highPriorityTileCount) {
                    int remainCount = getRemaining(aiBasis, tile);
                    probability.middlePriorityTileCount += remainCount;
                    log.i("[概率]确定2类牌：" + tile);
                    break w;
                }
            }
            hand[suit][tileIndex] -= 1;
        }

        return probability;
    }

    //计算1类数量
    private static int getPrimaryCount(int[][] hand, AIBasis aiBasis, int originWaiting) {
        int primary = 0;
        List<Integer> related = getRelatedTiles(hand);
        for (int j = 0; j < related.size(); j++) {
            int tile = related.get(j);
            int suit = tile / 10;
            int tileIndex = tile % 10 - 1;
            hand[suit][tileIndex] += 1;
            int waiting = getWaiting(hand);
            if (waiting < originWaiting) {
                int remainCount = getRemaining(aiBasis, tile);
                primary += remainCount;
            }
            hand[suit][tileIndex] -= 1;
        }
        return primary;
    }

    private static List<Integer> getRelatedTiles(int[][] hand) {
        List<Integer> need = new ArrayList<Integer>();
        for (int suit = 0; suit < 4; suit++) {
            for (int tileIndex = 0; tileIndex < hand[suit].length; tileIndex++) {
                if (suit == 3) {// 字牌只判将
                    if (hand[suit][tileIndex] == 2) {
                        need.add(suit * 10 + tileIndex + 1);// 将，则为相关牌
                    }
                } else if (hand[suit][tileIndex] > 0) {// 数牌判将和搭子
                    if (hand[suit][tileIndex] > 0) {//每张牌都为相关牌
                        need.add(suit * 10 + tileIndex + 1);
                    }
                    if (tileIndex + 1 < hand[suit].length && hand[suit][tileIndex + 1] > 0) {//顺子
                        if (tileIndex > 0) //例，非1万
                            need.add(suit * 10 + tileIndex);// 左边为相关牌
                        if (tileIndex < hand[suit].length - 2)//例，非8、9万
                            need.add(suit * 10 + tileIndex + 3); // 右边第2张为相关牌
                    } else if (tileIndex + 2 < hand[suit].length && hand[suit][tileIndex + 2] > 0) {// 嵌搭
                        need.add(suit * 10 + tileIndex + 2);// 加嵌张
                        if (tileIndex > 0)//例，非1万
                            need.add(suit * 10 + tileIndex);// 左边为相关牌
                        if (tileIndex < hand[suit].length - 3)//例，非8、9万
                            need.add(suit * 10 + tileIndex + 4); // 右边第3张为相关牌
                    } else {//非顺子、或塔子，则左右两边为相关牌
                        if (tileIndex > 0) //例，非1万
                            need.add(suit * 10 + tileIndex);// 左边为相关牌
                        if (tileIndex < hand[suit].length - 1)//例，非9万
                            need.add(suit * 10 + tileIndex + 2); // 右边为相关牌
                    }
                }
            }
        }

        need = removeDuplicate(need);
        return need;
    }

    private static int getDiscard(AIBasis aiBasis) {
        int hand[][] = utils.getArrayFromDoubleLink(aiBasis.self.getHands());
        LowestDemand demand = AI.getLowestDemandAfterDiscard(hand, aiBasis);
        return demand.tile;
    }

    private static int getRemaining(AIBasis aiBasis, int tile) {
        int count = 0;
        count += aiBasis.self.searchHands(tile);
        count += aiBasis.self.searchDiscard(tile);
        count += searchTile(aiBasis.leftDiscard, tile);
        count += searchTile(aiBasis.leftOpen, tile);
        count += searchTile(aiBasis.rightDiscard, tile);
        count += searchTile(aiBasis.rightOpen, tile);
        count += searchTile(aiBasis.topDiscard, tile);
        count += searchTile(aiBasis.topOpen, tile);
        count = 4 - count;

        return count;
    }
    //搜索链表中指定牌的数量
    private static int searchTile(DoubleLink<Tile> link, int tile) {
        int result = 0;
        Node<Tile> node = link.first;
        while(node != null) {
            if (node.data.getID() == tile && node.data.Exist) {
                result++;
            }
            node = node.next;
        }
        return result;
    }

    private static List<Integer> removeDuplicate(List<Integer> need) {
        HashSet<Integer> h = new HashSet<Integer>(need);
        need.clear();
        need.addAll(h);
        return need;
    }

    public static UserOperation chooseOperationForDraw(AvailableOperations availableOperations, AIBasis aiBasis) {
        UserOperation result = new UserOperation();
        result.operation = Operation.OPERATION_DISCARD;
        List<Operation> operations = availableOperations.operations;
        if (operations.contains(Operation.OPERATION_WIN)) {// AI自摸优先
            result.operation = Operation.OPERATION_WIN;
        } else if (operations.contains(Operation.OPERATION_KONG)) {// 有杠
            List<KongOperation> kongOperations = availableOperations.kongOperations;
            for (int i = 0; i < kongOperations.size(); i++) {
                boolean selfQuadruplet = Quadruplet.isNeedToSelfKong(kongOperations.get(i), aiBasis);
                if (selfQuadruplet) {
                    result.operation = Operation.OPERATION_KONG;
                    result.kongOperation = kongOperations.get(i);
                    break;
                }
            }
            if (result.operation == Operation.OPERATION_DISCARD) {
                result.tile = AI.getDiscard(aiBasis);
            }
        } else {// 舍牌
            result.tile = AI.getDiscard(aiBasis);
        }

        return result;
    }

    public static boolean whetherWin() {
        return true;
    }

    public static UserOperation chooseOperationForDiscard(AvailableOperations availableOperations, int discardTile, AIBasis aiBasis) {
        UserOperation result = new UserOperation();
        result.side = availableOperations.side;
        result.operation = Operation.OPERATION_PASS;
        List<Operation> operations = availableOperations.operations;
        if (operations.contains(Operation.OPERATION_WIN)) {// AI胡牌优先
            result.operation = Operation.OPERATION_WIN;
            return result;
        }

        OperationWithDemand pongOperation = null;
        if (operations.contains(Operation.OPERATION_KONG)) {// 有杠，判杠碰
            pongOperation = Quadruplet.chooseOperationForKong(discardTile, aiBasis);
        } else if (operations.contains(Operation.OPERATION_PONG)) {// 有碰无杠,判碰
            pongOperation = Triple.chooseOperationForPong(discardTile, aiBasis);
        }

        ChowOperation chowOperation = null;
        if (operations.contains(Operation.OPERATION_CHOW)) {
            chowOperation = Chow.getDemandAfterChow(availableOperations.chowTypes, discardTile, aiBasis);
        }

        if (pongOperation != null && chowOperation != null) {
            int pongWaiting = pongOperation.demand.waiting;
            int chowWaiting = chowOperation.demand.waiting;
            Probability pongProbability = pongOperation.demand.probability;
            Probability chowProbability = chowOperation.demand.probability;
            if (pongWaiting > chowWaiting ||
                    (pongWaiting == chowWaiting && pongProbability.isLessThan(chowProbability))) {
                result.operation = chowOperation.operation;
                result.chowType = chowOperation.chowType;
            } else {
                result.operation = pongOperation.operation;
            }
        } else if (pongOperation != null && chowOperation == null) {
            result.operation = pongOperation.operation;
        } else if (pongOperation == null && chowOperation != null) {
            result.operation = chowOperation.operation;
            result.chowType = chowOperation.chowType;
        }

        return result;
    }
}
