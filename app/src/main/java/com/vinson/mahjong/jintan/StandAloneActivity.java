package com.vinson.mahjong.jintan;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.vinson.mahjong.AI.AI;
import com.vinson.mahjong.base.AvailableOperations;
import com.vinson.mahjong.base.ChowType;
import com.vinson.mahjong.base.Constant;
import com.vinson.mahjong.base.KongOperation;
import com.vinson.mahjong.base.KongType;
import com.vinson.mahjong.base.Operation;
import com.vinson.mahjong.base.PerformKongPongResult;
import com.vinson.mahjong.base.PlayerType;
import com.vinson.mahjong.base.PongOperation;
import com.vinson.mahjong.base.UserOperation;
import com.vinson.mahjong.base.Win;
import com.vinson.mahjong.base.WinType;
import com.vinson.mahjong.base.log;
import com.vinson.mahjong.mahjong.Gambling;
import com.vinson.mahjong.mahjong.Judge;
import com.vinson.mahjong.mahjong.Player;
import com.vinson.mahjong.mahjong.Wall;
import com.vinson.mahjong.view.GameView;

import java.util.ArrayList;
import java.util.List;

public class StandAloneActivity extends Activity implements GameView.OnOperationClickedListener {
    private String name = "";
    private int headNum = 1;
    private Gambling gambling = new Gambling();
    private Player players[] = new Player[4];
    private GameView gameView;
    private boolean playing;
    private long exitTime = 0;
    private int discardTile;
    private Wall wall = new Wall();
    private Operation selectedOperation;
    private boolean isNextGamblingClicked = false;
    private long time = 0;
    private boolean canChow = true;
    private PlayTask task;
    private boolean isExit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);// 隐藏标题
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);// 设置全屏
        setContentView(R.layout.activity_game);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        name = bundle.getString("name");
        headNum = bundle.getInt("head");
        canChow = bundle.getBoolean("canChow");

        players[0] = new Player(name, "", false, PlayerType.HUMAN);
        players[0].HeadNum = headNum;
        players[1] = new Player(utils.randomName(2), "", true, PlayerType.AI);
        players[1].HeadNum = (headNum + 1) % Constant.MAX_HEAD_COUNT;
        players[2] = new Player(utils.randomName(2), "", true, PlayerType.AI);
        players[2].HeadNum = (headNum + 2) % Constant.MAX_HEAD_COUNT;
        players[3] = new Player(utils.randomName(2), "", true, PlayerType.AI);
        players[3].HeadNum = (headNum + 3) % Constant.MAX_HEAD_COUNT;

        gameView = (GameView) findViewById(R.id.game_view);
        gameView.setOnOperationClickedListener(this);
        gameView.setGambling(gambling);
        gambling.setPlayers(players);
        gambling.setWall(wall);

        isExit = false;
        task = new PlayTask();
        task.start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == 40)
            isNextGamblingClicked = true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_DOWN) {

            if ((System.currentTimeMillis() - exitTime) > 2000) {
                Toast.makeText(getApplicationContext(), "再按一次退出程序",
                        Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                finish();
            }
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    // 点击菜单按钮响应的事件
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 12, 0, "托管");
        menu.add(0, 0, 0, "明牌模式");
        menu.add(0, 13, 0,"学习");
        menu.add(0, 1, 1, "规则");
        menu.add(0, 2, 2, "清除积分");
        menu.add(0, 4, 3, "测试明杠");
        menu.add(0, 5, 3, "测试暗杠");
        menu.add(0, 6, 3, "测试抢杠");
        menu.add(0, 7, 3, "测试多暗杠");
        menu.add(0, 8, 3, "测试胡杠碰");
        menu.add(0, 9, 3, "测试多抢杠");
        menu.add(0, 10, 3, "测试花胡");
        menu.add(0, 11, 3, "测试4杠单吊");
        return true;
    }

    @Override
    // 菜单被选中时触发的事件
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 0) {//明牌模式
            if (gambling.isTestMode)
                gambling.isTestMode = false;
            else
                gambling.isTestMode = true;
        } else if (item.getItemId() == 13) {//学习模式
            if (gambling.isLearnMode) {
                gambling.isLearnMode = false;
            } else {
                gambling.isLearnMode = true;
            }
        } else if (item.getItemId() == 1) {//规则
            Intent intent = new Intent(StandAloneActivity.this, ThemeActivity.class);
            startActivity(intent);
        } else if (item.getItemId() == 2) {//清除积分
            gambling.getSelfPlayer().Score = 0;
            saveScores(0);
        } else if (item.getItemId() == 12) {// 托管：AI操作
            if (gambling.isHosted) {
                gambling.getSelfPlayer().AI = false;
                gambling.isHosted = false;
            } else {
                gambling.getSelfPlayer().AI = true;
                gambling.isHosted = true;
            }
        } else if (item.getItemId() == 4) {// 明杠
            Player player = gambling.getSelfPlayer();
            player.ClearData();
            player.addOpenHands(1);
            player.addOpenHands(1);
            player.addOpenHands(1);
            player.addHands(2);
            player.addHands(3);
            player.addHands(4);
            player.addHands(5);
            player.addHands(6);
            player.addHands(7);
            player.addHands(8);
            player.addHands(9);
            player.addHands(9);
            player.addHands(9);
            player.addHands(12);

        } else if (item.getItemId() == 5) {// 暗杠
            Player player = gambling.getSelfPlayer();
            player.ClearData();
            player.addHands(1);
            player.addHands(1);
            player.addHands(1);
            player.addHands(2);
            player.addHands(3);
            player.addHands(4);
            player.addHands(5);
            player.addHands(6);
            player.addHands(7);
            player.addHands(8);
            player.addHands(9);
            player.addHands(9);
            player.addHands(9);
            player.addHands(12);

        } else if (item.getItemId() == 6) {// 抢杠
            Player player = gambling.getSelfPlayer();
            player.ClearData();
            player.addOpenHands(1);
            player.addOpenHands(1);
            player.addOpenHands(1);
            player.addHands(2);
            player.addHands(3);
            player.addHands(4);
            player.addHands(5);
            player.addHands(6);
            player.addHands(7);
            player.addHands(9);
            player.addHands(9);
            player.addHands(9);
            player.addHands(9);
            player.addHands(1);

            player = gambling.getRightPlayer();
            player.ClearData();
            player.addHands(2);
            player.addHands(3);
            player.addHands(4);
            player.addHands(4);
            player.addHands(4);
            player.addHands(5);
            player.addHands(5);
            player.addHands(7);
            player.addHands(7);
            player.addHands(7);
            player.addHands(8);
            player.addHands(8);
            player.addHands(8);
            player.addFlower(41);
            player.addFlowerCount(1);

        } else if (item.getItemId() == 7) {// 多暗杠
            Player player = gambling.getSelfPlayer();
            player.ClearData();
            player.addHands(1);
            player.addHands(1);
            player.addHands(1);
            player.addHands(1);
            player.addHands(2);
            player.addHands(2);
            player.addHands(2);
            player.addHands(2);
            player.addHands(3);
            player.addHands(3);
            player.addHands(3);
            player.addHands(3);
            player.addHands(4);
            player.addHands(8);

        } else if (item.getItemId() == 8) {// 胡碰杠
            Player player = gambling.getSelfPlayer();
            player.ClearData();
            player.addHands(1);
            player.addHands(1);
            player.addHands(1);
            player.addHands(2);
            player.addHands(3);
            player.addHands(4);
            player.addHands(5);
            player.addHands(6);
            player.addHands(7);
            player.addHands(8);
            player.addHands(9);
            player.addHands(9);
            player.addHands(9);
            player.addHands(12);

            player = gambling.getRightPlayer();
            player.ClearData();
            player.addHands(12);
            player.addHands(13);
            player.addHands(14);
            player.addHands(14);
            player.addHands(14);
            player.addHands(15);
            player.addHands(15);
            player.addHands(17);
            player.addHands(17);
            player.addHands(17);
            player.addHands(18);
            player.addHands(18);
            player.addHands(18);

        } else if (item.getItemId() == 9) {// 多明抢
            Player player = gambling.getSelfPlayer();
            player.ClearData();
            player.addHands(2);
            player.addHands(3);
            player.addHands(11);
            player.addHands(11);
            player.addHands(11);
            player.addHands(12);
            player.addHands(12);
            player.addHands(12);
            player.addHands(13);
            player.addHands(13);
            player.addHands(13);
            player.addHands(31);
            player.addHands(31);
            player.addHands(7);

            player = gambling.getRightPlayer();
            player.ClearData();
            player.addOpenHands(1);
            player.addOpenHands(1);
            player.addOpenHands(1);
            player.addHands(22);
            player.addHands(22);
            player.addHands(22);
            player.addHands(23);
            player.addHands(23);
            player.addHands(23);
            player.addHands(25);
            player.addHands(26);
            player.addHands(27);
            player.addHands(34);

            player = gambling.getLeftPlayer();
            player.ClearData();
            player.addHands(2);
            player.addHands(3);
            player.addHands(14);
            player.addHands(14);
            player.addHands(14);
            player.addHands(15);
            player.addHands(15);
            player.addHands(15);
            player.addHands(16);
            player.addHands(16);
            player.addHands(16);
            player.addHands(32);
            player.addHands(32);

            player = gambling.getTopPlayer();
            player.ClearData();
            player.addHands(2);
            player.addHands(3);
            player.addHands(17);
            player.addHands(17);
            player.addHands(17);
            player.addHands(18);
            player.addHands(18);
            player.addHands(18);
            player.addHands(19);
            player.addHands(19);
            player.addHands(19);
            player.addHands(33);
            player.addHands(33);

        } else if (item.getItemId() == 10) {// 花胡
            Player player = gambling.getSelfPlayer();
            player.ClearData();
            player.addHands(2);
            player.addHands(3);
            player.addHands(11);
            player.addHands(11);
            player.addHands(11);
            player.addHands(12);
            player.addHands(12);
            player.addHands(12);
            player.addHands(13);
            player.addHands(13);
            player.addHands(13);
            player.addHands(31);
            player.addHands(31);
            player.addHands(48);
            player.addFlower(41);
            player.addFlower(42);
            player.addFlower(43);
            player.addFlower(44);
            player.addFlower(45);
            player.addFlower(46);
            player.addFlower(47);
        } else if (item.getItemId() == 11) {// 4杠单吊
            Player player = gambling.getSelfPlayer();
            player.ClearData();
            player.addBlackHands(1);
            player.addBlackHands(1);
            player.addBlackHands(1);
            player.addBlackHands(1);
            player.addBlackHands(2);
            player.addBlackHands(2);
            player.addBlackHands(2);
            player.addBlackHands(2);
            player.addBlackHands(3);
            player.addBlackHands(3);
            player.addBlackHands(3);
            player.addBlackHands(3);
            player.addBlackHands(4);
            player.addBlackHands(4);
            player.addBlackHands(4);
            player.addBlackHands(4);
            player.addHands(5);
        }
        return true;
    }

    public void menuOnClick(View v) {
        openOptionsMenu();
    }

    @Override
    public void onOperationClick(Operation operation) {
        switch(operation) {
            case OPERATION_WIN:
                selectedOperation = Operation.OPERATION_WIN;
                break;
            case OPERATION_PONG:
                selectedOperation = Operation.OPERATION_PONG;
                break;
            case OPERATION_KONG:
                selectedOperation = Operation.OPERATION_KONG;
                break;
            case OPERATION_DISCARD:
                int tile = gambling.getSelectedTile();
                if (isDiscardAvailable(tile)) {
                    discardTile = tile;
                    selectedOperation = Operation.OPERATION_DISCARD;
                }
                break;
            case OPERATION_PASS:
            case OPERATION_DRAW:
                selectedOperation = Operation.OPERATION_PASS;
                break;
            case OPERATION_CHOW:
                selectedOperation = Operation.OPERATION_CHOW;
                break;
        }
    }

    @Override
    public void onDestroy() {
        isExit = true;
        super.onDestroy();
    }

    private boolean isDiscardAvailable(int tile) {
        if ((tile > 0 && tile < 10)
                || (tile > 10 && tile < 20)
                || (tile > 20 && tile < 30)
                || (tile > 30 && tile < 38))
            return true;
        else
            return false;
    }

    private int readScores() {
        SharedPreferences preferences = getSharedPreferences("userInfo",
                Activity.MODE_PRIVATE);
        return preferences.getInt("Player", 0);
    }

    private void saveScores(int score) {
        SharedPreferences sharedPreferences = getSharedPreferences("userInfo",
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("Player", score);
        editor.commit();
    }

    public int waitForInput() {
        discardTile = -2;
        selectedOperation = null;
        int timeCount = 0;
        while (selectedOperation == null) {
            utils.sleep(300);
            timeCount++;
        }
        return timeCount;
    }

    class PlayTask extends Thread {
        @Override
        public void run() {
            while (!isExit) {
                initialize();
                while (playing) {
                    time = System.currentTimeMillis();
                    //补花,无牌则流局
                    if (!gambling.exchangeFlower()) break;
                    //花胡则结束
                    if (gambling.isFlowerWin()) break;
                    //获得可用操作，自摸、杠、舍
                    AvailableOperations availableOperation = gambling.getSelfAvailableOperation();
                    //获取操作
                    UserOperation selfOperation = getSelfOperation(availableOperation);
                    // 执行操作
                    if (selfOperation.operation == Operation.OPERATION_WIN) {// 自摸
                        gambling.selfDraw();
                        break;
                    } else if (selfOperation.operation == Operation.OPERATION_KONG) {// 杠, 如为明杠可以被抢
                        KongOperation kongOperation = selfOperation.kongOperation;
                        gambling.selfKong(kongOperation);
                        List<Integer> robKongSides = getRobKong(kongOperation);
                        if (robKongSides.size() > 0) {//有抢杠
                            if (whetherRobKong(robKongSides, kongOperation.tile)) {//抢杠
                                gambling.robKong(kongOperation.tile);
                                break;
                            }
                        }
                        if (!gambling.countervail()) break;//补牌，如无牌则流局
                        continue;// 补花->判杠自摸
                    }
                    //重置杠开和碰牌标志
                    gambling.gangAndWin = false;
                    gambling.isJustPong = false;
                    //出牌，由于不自摸和杠必为出牌，且为减少缩进，所以未进行操作判断
                    gambling.discard(selfOperation.tile, time);
                    //TODO save the gambling info for restoring
                    int discardTile = selfOperation.tile;
                    // 获取其他3家可用操作
                    List<AvailableOperations> availableOperations = getOtherAvailableOperations(discardTile, canChow);
                    // 获取其他3家操作
                    List<UserOperation> otherOperations = getOtherOperations(availableOperations, discardTile);
                    //执行操作
                    //优先处理胡牌
                    boolean isWin = gambling.performWin(otherOperations, discardTile);
                    if (isWin) {
                        gambling.makeDiscardTransparent(discardTile);
                        break;
                    }
                    //无胡牌，则处理其他操作
                    PerformKongPongResult result = gambling.performKongPong(otherOperations, discardTile);
                    if (result.hasPongOrGong) {
                        if (result.isWallOut) break;
                        continue;//补花
                    }
                    boolean chowResult = gambling.performChow(otherOperations, discardTile);
                    if (chowResult) continue;//补花
                    // 摸牌
                    if (!gambling.draw()) break;// 流局
                }// 一次舍牌结束
                gambling.getSelfPlayer().selectedPos = -1;
                utils.sleep(500);
                gambling.isEnded = true;
                saveScores(gambling.getSelfPlayer().Score);
                showStatics();
                waitForUserToStartNewGambling();
                gambling.clearPlayers();
                gambling.clearOperations();
                gambling.alreadyHu.clear();
                gotoNextDealer();
                playing = false;
            }
        }
    }

    private void gotoNextDealer() {
        if (gambling.dealer == 3) {
            gambling.dealer = 0;
            if (gambling.circle == 3) {
                gambling.circle = 0;
            } else {
                gambling.circle++;
            }
        } else {
            gambling.dealer++;
        }
    }

    private List<UserOperation> getOtherOperations(List<AvailableOperations> availableOperations, int discardTile) {
        List<UserOperation> userOperations = new ArrayList<UserOperation>();
        for (AvailableOperations operations: availableOperations) {
            UserOperation userOperation;
            int side = operations.side;
            if (gambling.isPlayerAI(side)) {
                userOperation = AI.chooseOperationForDiscard(operations, discardTile, gambling.getAIBasis(side));
            } else {
                gambling.tileDisplayInCenter = discardTile;
                gambling.displayOperations(operations);
                userOperation = getUserOperationForDiscard(operations, discardTile);
            }
            userOperations.add(userOperation);
        }

        return userOperations;
    }

    private UserOperation getUserOperationForDiscard(AvailableOperations operations, int discardTile) {
        UserOperation userOperation = new UserOperation();
        userOperation.side = operations.side;
        gambling.getSelfPlayer().selectedPos = -1;
        waitForInput();
        gambling.clearOperations();
        userOperation.operation = selectedOperation;
        if (selectedOperation == Operation.OPERATION_CHOW) {
            userOperation.chowType = getChowType(operations, discardTile);
        }
        return userOperation;
    }

    private ChowType getChowType(AvailableOperations operations, int discardTile) {
        ChowType chowType = null;
        if (operations.chowTypes.size() > 1) {//处理存在多个吃情况的选择
            selectedOperation = null;
            gambling.operations.add(Operation.OPERATION_CHOW);
            gambling.displayOperations = true;
            gambling.getSelfPlayer().selectedPos = -1;
            gambling.chowSelectMode = true;
            for (int i = 0; selectedOperation != Operation.OPERATION_CHOW; i = (i + 1) % operations.chowTypes.size()) {
                chowType = operations.chowTypes.get(i);
                if (chowType == ChowType.CHOW_LEFT) {
                    gambling.chowPos1 = gambling.getSelfPlayer().getTilePosFromRight(discardTile + 1);
                    gambling.chowPos2 = gambling.getSelfPlayer().getTilePosFromLeft(discardTile + 2);
                } else if (chowType == ChowType.CHOW_CENTER) {
                    gambling.chowPos1 = gambling.getSelfPlayer().getTilePosFromRight(discardTile - 1);
                    gambling.chowPos2 = gambling.getSelfPlayer().getTilePosFromLeft(discardTile + 1);
                } else {
                    gambling.chowPos1 = gambling.getSelfPlayer().getTilePosFromRight(discardTile - 2);
                    gambling.chowPos2 = gambling.getSelfPlayer().getTilePosFromLeft(discardTile - 1);
                }
                log.i("chowPos1:" + gambling.chowPos1 + " chowPos2:" + gambling.chowPos2);
                utils.sleep(1000);
            }
            gambling.chowSelectMode = false;
            gambling.clearOperations();
        } else {
            chowType = operations.chowTypes.get(0);
        }
        return chowType;
    }

    private List<AvailableOperations> getOtherAvailableOperations(int discardTile, boolean canChow) {
        List<AvailableOperations> availableOperations = new ArrayList<AvailableOperations>();
        List<ChowType> hasChow = new ArrayList<ChowType>();
        boolean isRobGong = false;
        List<Integer> winSides = Judge.getWinSides(gambling, discardTile, isRobGong);
        PongOperation hasTriple = Judge.hasKongOrPong(gambling.currentSide, discardTile, gambling);
        if (canChow)
            hasChow = Judge.hasChow(discardTile, gambling.getNextPlayer());
        int chowSide = (gambling.currentSide + 1) % 4;
        for (int side = chowSide; side != gambling.currentSide; side = (side + 1) % 4) {
            AvailableOperations operation = new AvailableOperations();
            operation.side = side;
            //添加和牌操作
            if (winSides.contains(side)) {
                operation.operations.add(Operation.OPERATION_WIN);
            }
            //添加杠碰操作
            if (side == hasTriple.side) {
                if (hasTriple.operation == Operation.OPERATION_KONG) {
                    operation.operations.add(Operation.OPERATION_KONG);
                    operation.operations.add(Operation.OPERATION_PONG);
                } else {
                    operation.operations.add(Operation.OPERATION_PONG);
                }
            }
            //添加吃操作
            if (hasChow.size() > 0 && side == chowSide) {
                operation.operations.add(Operation.OPERATION_CHOW);
                operation.chowTypes = hasChow;
            }
            //如果此位置的可用操作不为空，则添加至可用操作列表
            if (operation.operations.size() > 0) {
                operation.operations.add(Operation.OPERATION_PASS);
                availableOperations.add(operation);
            }
        }

        return availableOperations;
    }

    private UserOperation getSelfOperation(AvailableOperations availableOperation) {
        UserOperation userOperation;
        if (gambling.isCurrentPlayerAI()) {
            userOperation = gambling.getAIOperationForDraw(availableOperation);
        } else {
            gambling.displayOperations(availableOperation);
            userOperation = getUserOperationForDraw(availableOperation);
        }
        return userOperation;
    }

    private void initialize() {
        gambling.isEnded = false;
        gambling.currentSide = gambling.dealer;
        playing = true;
        //TODO restore gambling info
        gambling.clearPlayers();
        if (gambling.circle == 0 && gambling.dealer == 0) {
            wall.rollBearing(2, gambling.getPlayers());
            gambling.calSelfSide();
            gambling.getSelfPlayer().Score = readScores();
        }
        gambling.circle = 0;
        wall.shuffle();
        gambling.frontPosition = wall.roll(gambling.dealer);
        gambling.rearPosition = gambling.frontPosition - 1;
        wall.deal(gambling.getPlayers(), gambling.frontPosition, gambling.dealer);
        gambling.frontPosition = (gambling.frontPosition + 53) % 144;
        gambling.sortAllPlayersHand(gambling.getPlayers());
    }

    private void waitForUserToStartNewGambling() {
        isNextGamblingClicked = false;
        while (!isNextGamblingClicked) {
            utils.sleep(300);
        }
    }

    public List<Integer> getRobKong(KongOperation kongOperation) {// 杠 // false 有抢杠或流局
        List<Integer> winSides;
        if (kongOperation.type == KongType.EXPOSED_KONG) {// 明杠可以被抢杠
            boolean isRobKong = true;
            winSides = Judge.getWinSides(gambling, kongOperation.tile, isRobKong);
            return winSides;
        }
        return new ArrayList<Integer>();
    }

    private boolean whetherRobKong(List<Integer> robKongSides, int tile) {
        boolean isWin = false;
        for (int i = 0; i < robKongSides.size(); i ++) {
            if (gambling.isPlayerAI(robKongSides.get(i))) {
                if (AI.whetherWin()) {
                    gambling.chowWin(robKongSides.get(i), tile, WinType.ROB_KONG);
                    isWin = true;
                }
            } else {
                gambling.displayRobKongOperation();
                waitForInput();
                gambling.clearOperations();
                if (selectedOperation == Operation.OPERATION_WIN) {
                    gambling.chowWin(robKongSides.get(i), tile, WinType.ROB_KONG);
                    isWin = true;
                }
            }
        }
        return isWin;
    }
    private UserOperation getUserOperationForDraw(AvailableOperations operations) {
        UserOperation userOperation = new UserOperation();
        userOperation.side = gambling.currentSide;
        gambling.getSelfPlayer().selectedPos = gambling.getSelfPlayer().getHandSize();
        waitForInput();
        gambling.clearOperations();
        userOperation.operation = selectedOperation;
        if (userOperation.operation == Operation.OPERATION_KONG) {//用户输入杠
            userOperation.kongOperation = getKongOperation(operations.kongOperations);
        } else if (userOperation.operation == Operation.OPERATION_DISCARD) {
            userOperation.tile = discardTile;
        }
        return userOperation;
    }

    private KongOperation getKongOperation(List<KongOperation> operations) {
        KongOperation userOperation = new KongOperation();
        if (operations.size() > 1) {//处理存在多个杠情况的选择
            selectedOperation = null;
            gambling.operations.add(Operation.OPERATION_KONG);
            gambling.displayOperations = true;
            for (int i = 0; selectedOperation != Operation.OPERATION_KONG; i = (i + 1) % operations.size()) {
                userOperation = operations.get(i);
                gambling.getSelfPlayer().selectedPos = -1;
                gambling.kongSelectMode = true;
                gambling.currentKongTile = userOperation.tile;
                utils.sleep(1000);
            }
            gambling.kongSelectMode = false;
            gambling.clearOperations();
        } else {
            userOperation = operations.get(0);
        }
        return userOperation;
    }

    private void showStatics() {
        Intent intent = new Intent(StandAloneActivity.this, StatisticsActivity.class);
        intent.putExtra("size", gambling.alreadyHu.size());
        String wScores;
        for (int i = 0; i < gambling.alreadyHu.size(); i++) {
            Win win = gambling.alreadyHu.get(i);
            wScores = Judge.getScores(gambling.getPlayers(), win);
            intent.putExtra("bearing" + i, gambling.getPlayers()[win.winSide].HeadNum
                            + "-" + gambling.getPlayers()[win.winSide].Name);
            intent.putExtra("account" + i, wScores);
        }
        intent.putExtra("ai", gambling.isHosted);
        startActivityForResult(intent, 1);
    }
}
