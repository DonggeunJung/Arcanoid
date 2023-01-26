package com.example.arcanoid;

import androidx.appcompat.app.AppCompatActivity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity implements Mosaic.GameEvent {
    Mosaic mosaic = null;
    static int rows = 34, cols = 22;
    Mosaic.Card cardBall, cardRacket;
    Mosaic.Card cardEdgeL, cardEdgeR, cardEdgeT, cardEdgeB;
    int remain = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mosaic = findViewById(R.id.mosaic);
        initGame();
    }

    @Override
    protected void onDestroy() {
        if(mosaic != null)
            mosaic.clearMemory();
        super.onDestroy();
    }

    private void initGame() {
        mosaic.setScreenGrid(cols, rows);
        mosaic.listener(this);
        newGame();
    }

    void newGame() {
        mosaic.addCardColor(Color.rgb(10, 10, 100));
        int x = mosaic.random(1, cols-2);
        cardBall = mosaic.addCardColor(Color.rgb(255, 255, 255), x, rows/2+1, 1, 1);
        cardBall.checkCollision();
        cardEdgeL = mosaic.addCardColor(Color.rgb(255,255,255), 0,0,1,rows);
        cardEdgeL.checkCollision();
        cardEdgeR = mosaic.addCardColor(Color.rgb(255,255,255), cols-1,0,1,rows);
        cardEdgeR.checkCollision();
        cardEdgeT = mosaic.addCardColor(Color.rgb(255,255,255), 1,0,cols-2,1);
        cardEdgeT.checkCollision();
        cardEdgeB = mosaic.addCardColor(Color.rgb(255,255,140), 1,rows-1,cols-2,1);
        cardEdgeB.checkCollision();
        cardRacket = mosaic.addCardColor(Color.rgb(255,255,255), 8,rows-3,4,1);
        cardRacket.checkCollision();

        remain = 0;
        for(int y=2; y < rows/2; y+=2) {
            for(x=3; x < cols-4; x+=4) {
                if(mosaic.random(2) == 0) continue;
                Mosaic.Card cardBlock = mosaic.addCardColor(Color.rgb(192,192,255), x,y,4,2);
                cardBlock.edgeThick(0.2);
                cardBlock.checkCollision();
                remain ++;
            }
        }
    }

    void stopGame() {
        cardBall.stopMoving();
        mosaic.clearMemory();
        mosaic.popupDialog(null, "Game is finished!", "Close");
    }

    // User Event start ====================================

    public void onStart(View v) {
        int x = mosaic.random(1, cols-2);
        cardBall.move(x, rows/2+1);
        double speed = 0.4;
        if(mosaic.random(0,1) == 0)
            cardBall.movingDir(speed, speed);
        else
            cardBall.movingDir(-speed, speed);
    }

    public void onBtnArrow(View v) {
        double gapHrz = 2;
        switch (v.getId()) {
            case R.id.btnLeft: {
                if(cardRacket.screenRect().left < 1+gapHrz)
                    cardRacket.move(1, rows-3);
                else
                    cardRacket.moveGap(-gapHrz, 0);
                break;
            }
            default: {
                if(cardRacket.screenRect().right >= cols-1)
                    cardRacket.move(cols-cardRacket.screenRect().width()-1, rows-3);
                else
                    cardRacket.moveGap(gapHrz, 0);
            }
        }
    }

    // User Event end ====================================

    // Game Event start ====================================

    @Override
    public void onGameWorkEnded(Mosaic.Card card, Mosaic.WorkType workType) {}

    @Override
    public void onGameTouchEvent(Mosaic.Card card, int action, float x, float y) {}

    @Override
    public void onGameSensor(int sensorType, float x, float y, float z) {}

    @Override
    public void onGameCollision(Mosaic.Card card1, Mosaic.Card card2) {
        if (cardEdgeL.equals(card2) || cardEdgeR.equals(card2)) {
            if(cardEdgeL.equals(card2))
                cardBall.move(1, cardBall.screenRect().top);
            else
                cardBall.move(cols-2, cardBall.screenRect().top);
            cardBall.movingDir(-cardBall.unitHrz, cardBall.unitVtc);
        } else if (cardEdgeT.equals(card2) || cardRacket.equals(card2)) {
            cardBall.movingDir(cardBall.unitHrz, -cardBall.unitVtc);
        } else if(cardEdgeB.equals(card2)) {
            stopGame();
            newGame();
        } else {
            Mosaic.DirType dir = card1.collisionDir(card2);
            mosaic.removeCard(card2);
            remain --;
            if(remain > 0) {
                if(dir == Mosaic.DirType.LEFT || dir == Mosaic.DirType.RIGHT) {
                    cardBall.movingDir(-cardBall.unitHrz, cardBall.unitVtc);
                } else {
                    cardBall.movingDir(cardBall.unitHrz, -cardBall.unitVtc);
                }
            } else {
                stopGame();
                newGame();
            }
        }
    }

    @Override
    public void onGameTimer() {}

    // Game Event end ====================================

}