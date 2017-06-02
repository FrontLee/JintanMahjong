package com.vinson.mahjong.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.vinson.mahjong.base.Constant;
import com.vinson.mahjong.base.DoubleLink;
import com.vinson.mahjong.mahjong.Gambling;
import com.vinson.mahjong.base.Node;
import com.vinson.mahjong.base.Operation;
import com.vinson.mahjong.mahjong.Player;
import com.vinson.mahjong.base.Tile;
import com.vinson.mahjong.mahjong.GameImage;
import com.vinson.mahjong.jintan.utils;

import java.util.List;

public class GameView extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder holder;
    private RenderThread renderThread;
    private boolean isDraw = false;// 控制绘制的开关
    private Gambling gambling;
    private Context context;
    private int viewWidth, viewHeight;
    private int selfCardWidth, selfCardHeight, otherWidth, otherHeight, gap, sideImageSize, operationSize;
    private Paint transparentPaint, textPaint, textBackgroudPaint;
    private int leftDiscardLines, leftDiscardCountInLine, topDiscardLines, topDiscardCountInLine;
    private int opAreaStartX, opAreaStartY, opAreaEndX, opAreaEndY;
    private int cardAreaStartX, cardAreaStartY, cardAreaEndX, cardAreaEndY;

    private OnOperationClickedListener onOperationClickedListener;
    private Operation operation;
    private Operation clickedOperation;

    public GameView(Context context) {
        super(context);
        initView(context);
    }

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public GameView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView(context);
    }

    private void initView(Context context) {
        this.context = context;
        holder = this.getHolder();
        holder.addCallback(this);

        setZOrderOnTop(true);
        holder.setFormat(PixelFormat.TRANSLUCENT);

        transparentPaint = new Paint();
        transparentPaint.setStyle(Paint.Style.STROKE);
        transparentPaint.setAlpha(95);

        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);

        textBackgroudPaint = new Paint();
        textBackgroudPaint.setColor(Color.RED);
    }

    public void setGambling(Gambling gambling) {
        this.gambling = gambling;
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        viewWidth = getMeasuredWidth();
        viewHeight = getMeasuredHeight();
        selfCardWidth = viewWidth / 20;
        selfCardHeight = selfCardWidth * 3 / 2;
        otherWidth = selfCardWidth / 2;
        otherHeight = selfCardHeight / 2;
        sideImageSize = selfCardWidth * 5 / 2;
        gap = otherWidth / 2;
        operationSize = (viewHeight - otherHeight * 3 - selfCardHeight - gap * 4 - sideImageSize) / 2 - otherHeight - gap;

        int max_count = Constant.MAX_DISCARD_COUNT;
        int space = viewHeight - otherHeight * 3 - selfCardHeight - gap * 4;
        leftDiscardCountInLine = space / otherWidth;
        leftDiscardLines = max_count / leftDiscardCountInLine;
        if (max_count % leftDiscardCountInLine > 0)
            leftDiscardLines++;

        space = viewWidth - otherHeight * 6 - gap * 6 - leftDiscardLines * otherHeight * 2;
        topDiscardCountInLine = space / otherWidth;
        topDiscardLines = max_count / topDiscardCountInLine;
        if (max_count % topDiscardCountInLine > 0) {
            topDiscardLines++;
        }

        opAreaStartY = viewHeight - otherHeight - selfCardHeight - gap * 2 - operationSize;
        opAreaEndY = opAreaStartY + operationSize;
        opAreaEndX = viewWidth - otherHeight * (3 + leftDiscardLines) - gap * 3 - operationSize;
        cardAreaStartY = opAreaEndY;
        cardAreaEndY = opAreaEndY + gap + selfCardHeight;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        isDraw = true;
        new RenderThread().start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        isDraw = false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        int y = (int) event.getY();
        int x = (int) event.getX();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                operation = getClickedOperation(x, y);
                clickedOperation = operation;
                if (y >= cardAreaStartY) {
                    performSelection(x, y);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (y >= cardAreaStartY) {
                    performSelection(x, y);
                }
                break;
            case MotionEvent.ACTION_UP:
                clickedOperation = null;
                Operation upPosition = getClickedOperation(x, y);
                if (upPosition == operation && upPosition != null) {
                    performOperation(operation);
                }
                break;
        }

        return true;
    }

    private Operation getClickedOperation(int x, int y) {
        if (!gambling.displayOperations) return null;
        if (y >= opAreaStartY && y <= opAreaEndY && x >= opAreaStartX + operationSize && x <= opAreaEndX + operationSize) {
            int opCount = gambling.operations.size();
            for (int i = 0; i < opCount; i++) {
                int endPos = opAreaEndX + operationSize - i * operationSize - i * gap;
                int startPos = endPos - operationSize;
                if (x >= startPos && x <= endPos) {
                    return gambling.operations.get(i);
                }
            }
        }
        return null;
    }

    private void performOperation(Operation operation) {
        onOperationClickedListener.onOperationClick(operation);
    }

    private void performSelection(int x, int y) {
        if (gambling.kongSelectMode || gambling.chowSelectMode) return;
        int spacing = x - cardAreaStartX;
        int pos = spacing / selfCardWidth;
        if (spacing % selfCardWidth > 0) {
            pos++;
        }
        Player player = gambling.getSelfPlayer();
        if (gambling.currentSide == gambling.getSelfSide()) {
            int lastTileX = cardAreaStartX + selfCardWidth * (player.getHandSize() - 1) + gap;
            if (x >= lastTileX && x <= lastTileX + selfCardWidth) {
                pos = player.getHandSize();
            }
        }
        player.selectedPos = pos;
    }

    public void setOnOperationClickedListener(OnOperationClickedListener listener) {
        onOperationClickedListener = listener;
    }

    public interface OnOperationClickedListener {
        void onOperationClick(Operation operation);
    }

    private class RenderThread extends Thread {
        @Override
        public void run() {
            while (isDraw) {
                drawUI();
            }
            super.run();
        }
    }

    public void drawUI() {
        Canvas canvas = holder.lockCanvas();
        long time = System.currentTimeMillis();
        try {
            drawCanvas(canvas);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            holder.unlockCanvasAndPost(canvas);
            time = System.currentTimeMillis() - time;
            //Log.e("majiang", "draw time:" + time);
            SystemClock.sleep(30);
        }
    }

    private void drawCanvas(Canvas canvas) {
        if (gambling == null || canvas == null) return;
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        drawPlayerInfo(canvas);
        drawLeftPlayerInfo(canvas);
        drawTopPlayerInfo(canvas);
        drawRightPlayerInfo(canvas);
        drawSelf(canvas);
        drawLeft(canvas);
        drawRight(canvas);
        drawTop(canvas);
        drawGamblingInfo(canvas);
        drawLeftDiscard(canvas);
        drawRightDiscard(canvas);
        drawSelfDiscard(canvas);
        drawTopDiscard(canvas);
        drawOperations(canvas);
    }

    private void drawPlayerInfo(Canvas canvas) {
        int x = 0;
        int y = viewHeight - selfCardHeight - otherHeight - gap;
        Rect posInImage = GameImage.getHeadImagePos(gambling.getSelfPlayer().HeadNum);
        Rect posInCanvas = new Rect(x, y, x + selfCardHeight, y + selfCardHeight);
        canvas.drawBitmap(GameImage.headImage, posInImage, posInCanvas, null);

        int x1 = x + selfCardHeight;
        int textSize = selfCardHeight / 3;
        textPaint.setTextSize(textSize);
        int y1 = y + selfCardHeight / 2 - textSize * 1 / 5;
        canvas.drawText(gambling.getSelfPlayer().Name, x1, y1, textPaint);
        y1 += textSize;
        canvas.drawText(gambling.getSelfPlayer().Score + "", x1, y1, textPaint);

        int x2 = x + selfCardHeight - textSize;
        int y2 = y;
        if (gambling.isHosted) {
            canvas.drawRect(x2, y2, x2 + textSize, y2 + textSize, textBackgroudPaint);
            canvas.drawText("托", x2, y2 + textSize * 4 / 5, textPaint);
            y2 += textSize;
        }
        if (gambling.isLearnMode) {
            canvas.drawRect(x2, y2, x2 + textSize, y2 + textSize, textBackgroudPaint);
            canvas.drawText("学", x2, y2 + textSize * 4 / 5, textPaint);
        }

    }

    private void drawLeftPlayerInfo(Canvas canvas) {
        int x = 0;
        int y = gap;
        Rect posInImage = GameImage.getHeadImagePos(gambling.getLeftPlayer().HeadNum);
        Rect posInCanvas = new Rect(x, y, x + selfCardHeight, y + selfCardHeight);
        canvas.drawBitmap(GameImage.headImage, posInImage, posInCanvas, null);

        int textSize = selfCardHeight / 3;
        textPaint.setTextSize(textSize);
        y += selfCardHeight + textSize * 4 / 5;
        canvas.drawText(gambling.getLeftPlayer().Name, x, y, textPaint);
        y += textSize;
        canvas.drawText(gambling.getLeftPlayer().Score + "", x, y, textPaint);
    }

    private void drawTopPlayerInfo(Canvas canvas) {
        int x = otherHeight * (3 + leftDiscardLines) + gap * 3;
        int y = gap;
        Rect posInImage = GameImage.getHeadImagePos(gambling.getTopPlayer().HeadNum);
        Rect posInCanvas = new Rect(x, y, x + selfCardHeight, y + selfCardHeight);
        canvas.drawBitmap(GameImage.headImage, posInImage, posInCanvas, null);

        x += selfCardHeight;
        int textSize = selfCardHeight / 3;
        textPaint.setTextSize(textSize);
        y += selfCardHeight / 2 - textSize * 1 / 4;
        canvas.drawText(gambling.getTopPlayer().Name, x, y, textPaint);
        y += textSize;
        canvas.drawText(gambling.getTopPlayer().Score + "", x, y, textPaint);
    }

    private void drawRightPlayerInfo(Canvas canvas) {
        int x = viewWidth - selfCardHeight;
        int y = gap;
        Rect posInImage = GameImage.getHeadImagePos(gambling.getRightPlayer().HeadNum);
        Rect posInCanvas = new Rect(x, y, x + selfCardHeight, y + selfCardHeight);
        canvas.drawBitmap(GameImage.headImage, posInImage, posInCanvas, null);

        int textSize = selfCardHeight / 3;
        textPaint.setTextSize(textSize);
        y += selfCardHeight + textSize * 4 / 5;
        canvas.drawText(gambling.getRightPlayer().Name, x, y, textPaint);
        y += textSize;
        canvas.drawText(gambling.getRightPlayer().Score + "", x, y, textPaint);
    }

    private void drawSelf(Canvas canvas) {
        Player selfPlayer = gambling.getSelfPlayer();
        if (selfPlayer == null) return;
        drawSelfFlowerOpenBlack(selfPlayer, canvas);
        drawSelfHand(selfPlayer, canvas);
    }

    private void drawLeft(Canvas canvas) {
        Player player = gambling.getLeftPlayer();
        if (player == null) return;
        drawLeftFlowerOpenBlack(player, canvas);
        drawLeftHand(player, canvas);
    }

    private void drawRight(Canvas canvas) {
        Player player = gambling.getRightPlayer();
        if (player == null) return;
        drawRightFlowerOpenBlack(player, canvas);
        drawRightHand(player, canvas);
    }

    private void drawTop(Canvas canvas) {
        Player player = gambling.getTopPlayer();
        if (player == null) return;
        drawTopFlowerOpenBlack(player, canvas);
        drawTopHand(player, canvas);
    }

    private void drawGamblingInfo(Canvas canvas) {
        Rect posInImage;
        if (gambling.currentSide == gambling.getLeftSide()) {
            posInImage = GameImage.getLeftSideImagePos();
        } else if (gambling.currentSide == gambling.getRightSide()) {
            posInImage = GameImage.getRightSideImagePos();
        } else if (gambling.currentSide == gambling.getSelfSide()) {
            posInImage = GameImage.getSelfSideImagePos();
        } else if (gambling.currentSide == gambling.getTopSide()) {
            posInImage = GameImage.getTopSideImagePos();
        } else {
            posInImage = GameImage.getOtherSideImagePos();
        }
        int x = viewWidth / 2 - sideImageSize / 2;
        int y = viewHeight / 2 - sideImageSize / 2 - (selfCardHeight - otherHeight) / 2;
        Rect posInCanvas = new Rect(x, y, x + sideImageSize, y + sideImageSize);
        canvas.drawBitmap(GameImage.sideImage, posInImage, posInCanvas, null);

        String roundInfo = utils.getBearing(gambling.circle) + (gambling.dealer + 1) + "局";
        x = otherHeight * (3 + leftDiscardLines) + gap * 3;
        y = viewHeight / 2;
        int textSize = (viewWidth / 2 - sideImageSize / 2 - x) / roundInfo.length();
        textPaint.setTextSize(textSize);
        canvas.drawText(roundInfo, x, y, textPaint);

        String remanidingInfo = "余" + gambling.getRemainder() + "张";
        x = viewWidth / 2 + sideImageSize / 2 + gap;
        y = viewHeight / 2;
        textSize = (viewWidth - x - otherHeight * (3 + leftDiscardLines) - gap * 3) / remanidingInfo.length();
        textPaint.setTextSize(textSize);
        canvas.drawText(remanidingInfo, x, y, textPaint);
    }

    private void drawLeftDiscard(Canvas canvas) {
        Player player = gambling.getLeftPlayer();
        Node<Tile> discard = player.Discard.first;
        int countInLine = leftDiscardCountInLine;
        int lineCount = leftDiscardLines;
        int startY = otherHeight * 2 + gap * 2;
        //因size % countInLine == 0的补偿关系需多加一个 otherHeight
        int x = otherHeight * 3 + gap * 2 + lineCount * otherHeight;
        int y = startY;
        int size = 0;
        while (discard != null) {
            if (size % countInLine == 0) {
                x -= otherHeight;
                y = startY;
            }
            Rect rectInImage = GameImage.getLeftOpenImagePos(discard.data.getID());
            Rect rectInCanvas = new Rect(x, y, x + otherHeight, y + otherWidth);
            Paint paint = null;
            if (!discard.data.Exist) paint = transparentPaint;
            canvas.drawBitmap(GameImage.mahjongImage, rectInImage, rectInCanvas, paint);
            y += otherWidth;
            size++;
            discard = discard.next;
        }
    }

    private void drawRightDiscard(Canvas canvas) {
        Player player = gambling.getRightPlayer();
        Node<Tile> discard = player.Discard.first;
        int countInLine = leftDiscardCountInLine;
        int lineCount = leftDiscardLines;
        int startY = viewHeight - selfCardHeight - otherHeight - gap * 2 - otherWidth;
        //因size % countInLine == 0的补偿关系需多减一个 otherHeight
        int x = viewWidth - otherHeight * 4 - gap * 2 - lineCount * otherHeight;
        int y = startY;
        int size = 0;
        while (discard != null) {
            if (size % countInLine == 0) {
                x += otherHeight;
                y = startY;
            }
            Rect rectInImage = GameImage.getRightOpenImagePos(discard.data.getID());
            Rect rectInCanvas = new Rect(x, y, x + otherHeight, y + otherWidth);
            Paint paint = null;
            if (!discard.data.Exist) paint = transparentPaint;
            canvas.drawBitmap(GameImage.mahjongImage, rectInImage, rectInCanvas, paint);
            y -= otherWidth;
            size++;
            discard = discard.next;
        }
    }

    private void drawSelfDiscard(Canvas canvas) {
        Player player = gambling.getSelfPlayer();
        Node<Tile> discard = player.Discard.first;
        int countInLine = topDiscardCountInLine;
        int startX = otherHeight * 3 + gap * 3 + leftDiscardLines * otherHeight;
        int x = startX;
        int y = viewHeight / 2 - (selfCardHeight - otherHeight) / 2 + sideImageSize / 2 - otherHeight;
        int size = 0;
        while (discard != null) {
            if (size % countInLine == 0) {
                x = startX;
                y += otherHeight;
            }
            Rect rectInImage = GameImage.getSelfOpenImagePos(discard.data.getID());
            Rect rectInCanvas = new Rect(x, y, x + otherWidth, y + otherHeight);
            Paint paint = null;
            if (!discard.data.Exist) paint = transparentPaint;
            canvas.drawBitmap(GameImage.mahjongImage, rectInImage, rectInCanvas, paint);
            x += otherWidth;
            size++;
            discard = discard.next;
        }
    }

    private void drawTopDiscard(Canvas canvas) {
        Player player = gambling.getTopPlayer();
        Node<Tile> discard = player.Discard.first;
        int countInLine = topDiscardCountInLine;
        int lineCount = topDiscardLines;
        int startX = viewWidth - otherHeight * 3 - gap * 3 - leftDiscardLines * otherHeight - otherWidth;
        int x = startX;
        int y = otherHeight * 2 + gap * 2 + lineCount * otherHeight;
        int size = 0;
        while (discard != null) {
            if (size % countInLine == 0) {
                x = startX;
                y -= otherHeight;
            }
            Rect rectInImage = GameImage.getTopOpenImagePos(discard.data.getID());
            Rect rectInCanvas = new Rect(x, y, x + otherWidth, y + otherHeight);
            Paint paint = null;
            if (!discard.data.Exist) paint = transparentPaint;
            canvas.drawBitmap(GameImage.mahjongImage, rectInImage, rectInCanvas, paint);
            x -= otherWidth;
            size++;
            discard = discard.next;
        }
    }

    private void drawOperations(Canvas canvas) {
        if (!gambling.displayOperations) return;
        List<Operation> operations = gambling.operations;
        if (operations == null || operations.isEmpty()) return;
        int x = opAreaEndX;
        int y = opAreaStartY;
        boolean displayTileInCenter = false;
        for (int i = 0; i < operations.size(); i++) {
            Rect posInImage = null;
            switch (operations.get(i)) {
                case OPERATION_WIN:
                    displayTileInCenter = true;
                    if (clickedOperation == Operation.OPERATION_WIN)
                        posInImage = GameImage.getWinPressedImagePos();
                    else
                        posInImage = GameImage.getWinImagePos();
                    break;
                case OPERATION_PONG:
                    displayTileInCenter = true;
                    if (clickedOperation == Operation.OPERATION_PONG)
                        posInImage = GameImage.getPongPressedImagePos();
                    else
                        posInImage = GameImage.getPongImagePos();
                    break;
                case OPERATION_PASS:
                    displayTileInCenter = true;
                    if (clickedOperation == Operation.OPERATION_PASS)
                        posInImage = GameImage.getPassPressedImagePos();
                    else
                        posInImage = GameImage.getPassImagePos();
                    break;
                case OPERATION_KONG:
                    displayTileInCenter = true;
                    if (clickedOperation == Operation.OPERATION_KONG)
                        posInImage = GameImage.getKongPressedImagePos();
                    else
                        posInImage = GameImage.getKongImagePos();
                    break;
                case OPERATION_DISCARD:
                    if (clickedOperation == Operation.OPERATION_DISCARD)
                        posInImage = GameImage.getDiscardPressedImagePos();
                    else
                        posInImage = GameImage.getDiscardImagePos();
                    break;
                case OPERATION_CHOW:
                    if (clickedOperation == Operation.OPERATION_CHOW)
                        posInImage = GameImage.getChowPressedImagePos();
                    else
                        posInImage = GameImage.getChowImagePos();
                    break;
            }
            if (posInImage != null) {
                Rect posInCanvas = new Rect(x, y, x + operationSize, y + operationSize);
                canvas.drawBitmap(GameImage.operationImage, posInImage, posInCanvas, null);
                x -= operationSize + gap;
            }
        }
        opAreaStartX = x;
        if (displayTileInCenter && gambling.tileDisplayInCenter > 0) {
            Rect posInImage = GameImage.getSelfHandImagePos(gambling.tileDisplayInCenter);
            x = viewWidth / 2 - selfCardWidth * 3 / 4;
            y = viewHeight / 2 - selfCardHeight * 3 / 4;
            Rect posInCanvas = new Rect(x, y, x + selfCardWidth * 3 / 2, y + selfCardHeight * 3 / 2);
            canvas.drawBitmap(GameImage.mahjongImage, posInImage, posInCanvas, null);
        }
    }

    private void drawSelfFlowerOpenBlack(Player player, Canvas canvas) {
        Node<Tile> flower = player.getFlower().first;
        Node<Tile> inOpen = player.OpenHands.first;
        Node<Tile> inBlack = player.getBlackHands().first;

        int x = otherHeight * 3 + gap * 2;
        int y = viewHeight - otherHeight;
        int size = 0;
        while (flower != null) {
            Rect rectInImage = GameImage.getSelfOpenImagePos(flower.data.getID());
            Rect rectInCanvas = new Rect(x, y, x + otherWidth, y + otherHeight);
            canvas.drawBitmap(GameImage.mahjongImage, rectInImage, rectInCanvas, null);
            x += otherWidth;
            size++;
            flower = flower.next;
        }

        if (size > 0) {
            x += gap;
        }

        size = 0;
        while (inOpen != null) {
            Rect rectInImage = GameImage.getSelfOpenImagePos(inOpen.data.getID());
            Rect rectInCanvas = new Rect(x, y, x + otherWidth, y + otherHeight);
            Paint paint = null;
            if (!inOpen.data.Exist) paint = transparentPaint;
            canvas.drawBitmap(GameImage.mahjongImage, rectInImage, rectInCanvas, paint);
            x += otherWidth;
            size++;
            inOpen = inOpen.next;
        }

        if (size > 0) {
            x += gap;
        }

        while (inBlack != null) {
            Rect rectInImage = GameImage.getSelfBlackImagePos(inBlack.data.getID());
            Rect rectInCanvas = new Rect(x, y, x + otherWidth, y + otherHeight);
            canvas.drawBitmap(GameImage.mahjongImage, rectInImage, rectInCanvas, null);
            x += otherWidth;
            inBlack = inBlack.next;
        }
    }

    private void drawSelfHand(Player player, Canvas canvas) {
        DoubleLink<Tile> handLink = player.getHands();
        Node<Tile> inHand = handLink.first;
        int tileCount = player.getHandSize();
        int x = (viewWidth - selfCardWidth * tileCount + gap) / 2;
        int y = viewHeight - selfCardHeight - otherHeight - gap;
        cardAreaStartX = x;
        int currentPos = 0;
        int size = player.getHandSizeFromExposed() + handLink.size;
        while (inHand != null) {
            currentPos++;
            Rect rectInImage = GameImage.getSelfHandImagePos(inHand.data.getID());
            Rect rectInCanvas;
            if (currentPos == player.selectedPos
                    || (gambling.kongSelectMode && inHand.data.getID() == gambling.currentKongTile)
                    || (gambling.chowSelectMode && (currentPos == gambling.chowPos1 || currentPos == gambling.chowPos2)))
                rectInCanvas = new Rect(x, y - gap, x + selfCardWidth, y - gap + selfCardHeight);
            else
                rectInCanvas = new Rect(x, y, x + selfCardWidth, y + selfCardHeight);
            canvas.drawBitmap(GameImage.mahjongImage, rectInImage, rectInCanvas, null);

            if (size > 13 && currentPos == tileCount - 1) {
                x += gap;
            }
            x += selfCardWidth;
            inHand = inHand.next;
        }
        cardAreaEndX = x;
    }

    private void drawLeftFlowerOpenBlack(Player player, Canvas canvas) {
        Node<Tile> flower = player.getFlower().first;
        Node<Tile> inOpen = player.OpenHands.first;
        Node<Tile> inBlack = player.getBlackHands().first;

        int x = -otherHeight;
        int startY = viewHeight - otherHeight - selfCardHeight - gap * 2 - otherWidth * 13;
        int y = startY;
        int size = 0;
        int count = 0;
        while (flower != null) {
            if (count % 12 == 0) {
                x += otherHeight;
                y = startY;
            }
            Rect rectInImage = GameImage.getLeftOpenImagePos(flower.data.getID());
            Rect rectInCanvas = new Rect(x, y, x + otherHeight, y + otherWidth);
            canvas.drawBitmap(GameImage.mahjongImage, rectInImage, rectInCanvas, null);
            y += otherWidth;
            size++;
            count++;
            flower = flower.next;
        }

        if (size > 0) {
            y += gap;
        }

        size = 0;
        while (inOpen != null) {
            if (count % 12 == 0) {
                x += otherHeight;
                y = startY;
            }
            Rect rectInImage = GameImage.getLeftOpenImagePos(inOpen.data.getID());
            Rect rectInCanvas = new Rect(x, y, x + otherHeight, y + otherWidth);
            Paint paint = null;
            if (!inOpen.data.Exist) paint = transparentPaint;
            canvas.drawBitmap(GameImage.mahjongImage, rectInImage, rectInCanvas, paint);
            y += otherWidth;
            size++;
            count++;
            inOpen = inOpen.next;
        }

        if (size > 0) {
            y += gap;
        }

        while (inBlack != null) {
            if (count % 12 == 0) {
                x += otherHeight;
                y = startY;
            }
            Rect rectInImage;
            Rect rectInCanvas = new Rect(x, y, x + otherHeight, y + otherWidth);
            if (gambling.isEnded || gambling.isTestMode) {
                rectInImage = GameImage.getLeftOpenImagePos(inBlack.data.getID());
                canvas.drawBitmap(GameImage.mahjongImage, rectInImage, rectInCanvas, transparentPaint);
            } else {
                rectInImage = GameImage.getLeftBlackImagePos();
                canvas.drawBitmap(GameImage.mahjongImage, rectInImage, rectInCanvas, null);
            }

            y += otherWidth;
            count++;
            inBlack = inBlack.next;
        }
    }

    private void drawLeftHand(Player player, Canvas canvas) {
        DoubleLink<Tile> link = player.getHands();
        Node<Tile> inHand = link.first;
        int x = otherHeight * 2 + gap;
        int height = viewHeight - selfCardHeight - gap * 2 - otherHeight;
        int y = (height - otherWidth * player.getHandSize()) / 2;
        while (inHand != null) {
            Rect rectInImage;
            Rect rectInCanvas = new Rect(x, y, x + otherHeight, y + otherWidth);
            if (gambling.isEnded || gambling.isTestMode)
                rectInImage = GameImage.getLeftOpenImagePos(inHand.data.getID());
            else
                rectInImage = GameImage.getLeftHandImagePos();
            canvas.drawBitmap(GameImage.mahjongImage, rectInImage, rectInCanvas, null);
            y += otherWidth;
            inHand = inHand.next;
        }
    }

    private void drawRightFlowerOpenBlack(Player player, Canvas canvas) {
        Node<Tile> flower = player.getFlower().first;
        Node<Tile> inOpen = player.OpenHands.first;
        Node<Tile> inBlack = player.getBlackHands().first;

        int x = viewWidth;
        int startY = viewHeight - otherHeight - gap * 2 - selfCardHeight - otherWidth;
        int y = startY;
        int size = 0;
        int count = 0;
        while (flower != null) {
            if (count % 12 == 0) {
                x -= otherHeight;
                y = startY;
            }
            Rect rectInImage = GameImage.getRightOpenImagePos(flower.data.getID());
            Rect rectInCanvas = new Rect(x, y, x + otherHeight, y + otherWidth);
            canvas.drawBitmap(GameImage.mahjongImage, rectInImage, rectInCanvas, null);
            y -= otherWidth;
            size++;
            count++;
            flower = flower.next;
        }

        if (size > 0) {
            y -= gap;
        }

        size = 0;
        while (inOpen != null) {
            if (count % 12 == 0) {
                x -= otherHeight;
                y = startY;
            }
            Rect rectInImage = GameImage.getRightOpenImagePos(inOpen.data.getID());
            Rect rectInCanvas = new Rect(x, y, x + otherHeight, y + otherWidth);
            Paint paint = null;
            if (!inOpen.data.Exist) paint = transparentPaint;
            canvas.drawBitmap(GameImage.mahjongImage, rectInImage, rectInCanvas, paint);
            y -= otherWidth;
            size++;
            count++;
            inOpen = inOpen.next;
        }

        if (size > 0) {
            y -= gap;
        }

        while (inBlack != null) {
            if (count % 12 == 0) {
                x -= otherHeight;
                y = startY;
            }
            Rect rectInImage;
            Rect rectInCanvas = new Rect(x, y, x + otherHeight, y + otherWidth);
            if (gambling.isEnded || gambling.isTestMode) {
                rectInImage = GameImage.getRightOpenImagePos(inBlack.data.getID());
                canvas.drawBitmap(GameImage.mahjongImage, rectInImage, rectInCanvas, transparentPaint);
            } else {
                rectInImage = GameImage.getRightBlackImagePos();
                canvas.drawBitmap(GameImage.mahjongImage, rectInImage, rectInCanvas, null);
            }
            y -= otherWidth;
            count++;
            inBlack = inBlack.next;
        }
    }

    private void drawRightHand(Player player, Canvas canvas) {
        DoubleLink<Tile> link = player.getHands();
        Node<Tile> inHand = link.first;
        int x = viewWidth - otherHeight * 3 - gap;
        int height = viewHeight - selfCardHeight - gap * 2 - otherHeight;
        int y = height - (height - otherWidth * player.getHandSize()) / 2;
        while (inHand != null) {
            Rect rectInImage;
            Rect rectInCanvas = new Rect(x, y, x + otherHeight, y + otherWidth);
            if (gambling.isEnded || gambling.isTestMode)
                rectInImage = GameImage.getRightOpenImagePos(inHand.data.getID());
            else
                rectInImage = GameImage.getRightHandImagePos();
            canvas.drawBitmap(GameImage.mahjongImage, rectInImage, rectInCanvas, null);
            y -= otherWidth;
            inHand = inHand.next;
        }
    }

    private void drawTopFlowerOpenBlack(Player player, Canvas canvas) {
        Node<Tile> flower = player.getFlower().first;
        Node<Tile> inOpen = player.OpenHands.first;
        Node<Tile> inBlack = player.getBlackHands().first;

        int x = viewWidth - otherHeight * 3 - gap - otherWidth;
        int y = 0;
        int size = 0;
        while (flower != null) {
            Rect rectInImage = GameImage.getTopOpenImagePos(flower.data.getID());
            Rect rectInCanvas = new Rect(x, y, x + otherWidth, y + otherHeight);
            canvas.drawBitmap(GameImage.mahjongImage, rectInImage, rectInCanvas, null);
            x -= otherWidth;
            size++;
            flower = flower.next;
        }

        if (size > 0) {
            x -= gap;
        }

        size = 0;
        while (inOpen != null) {
            Rect rectInImage = GameImage.getTopOpenImagePos(inOpen.data.getID());
            Rect rectInCanvas = new Rect(x, y, x + otherWidth, y + otherHeight);
            Paint paint = null;
            if (!inOpen.data.Exist) paint = transparentPaint;
            canvas.drawBitmap(GameImage.mahjongImage, rectInImage, rectInCanvas, paint);
            x -= otherWidth;
            size++;
            inOpen = inOpen.next;
        }

        if (size > 0) {
            x -= gap;
        }

        while (inBlack != null) {
            Rect rectInImage;
            Rect rectInCanvas = new Rect(x, y, x + otherWidth, y + otherHeight);
            if (gambling.isEnded || gambling.isTestMode) {
                rectInImage = GameImage.getTopOpenImagePos(inBlack.data.getID());
                canvas.drawBitmap(GameImage.mahjongImage, rectInImage, rectInCanvas, transparentPaint);
            } else {
                rectInImage = GameImage.getTopBlackImagePos();
                canvas.drawBitmap(GameImage.mahjongImage, rectInImage, rectInCanvas, null);
            }
            x -= otherWidth;
            inBlack = inBlack.next;
        }
    }

    private void drawTopHand(Player player, Canvas canvas) {
        DoubleLink<Tile> link = player.getHands();
        Node<Tile> inHand = link.first;
        int width = viewWidth - otherWidth * 4 - gap * 4;
        int x = viewWidth - otherWidth * 2 - gap * 2 - (width - otherWidth * player.getHandSize()) / 2;
        int y = otherHeight + gap;
        while (inHand != null) {
            Rect rectInImage;
            Rect rectInCanvas = new Rect(x, y, x + otherWidth, y + otherHeight);
            if (gambling.isEnded || gambling.isTestMode)
                rectInImage = GameImage.getTopOpenImagePos(inHand.data.getID());
            else
                rectInImage = GameImage.getTopHandImagePos();
            canvas.drawBitmap(GameImage.mahjongImage, rectInImage, rectInCanvas, null);
            x -= otherWidth;
            inHand = inHand.next;
        }
    }
}
