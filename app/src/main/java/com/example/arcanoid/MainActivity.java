package com.example.arcanoid;

import androidx.appcompat.app.AppCompatActivity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity implements JGameLib.GameEvent {
    JGameLib gameLib = null;
    static int rows = 34, cols = 22;
    JGameLib.Card cardBall, cardRacket;
    JGameLib.Card cardEdgeL, cardEdgeR, cardEdgeT, cardEdgeB;
    int remain = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        gameLib = findViewById(R.id.gameLib);
        initGame();
    }

    @Override
    protected void onDestroy() {
        if(gameLib != null)
            gameLib.clearMemory();
        super.onDestroy();
    }

    private void initGame() {
        gameLib.setScreenGrid(cols, rows);
        gameLib.listener(this);
        newGame();
    }

    void newGame() {
        gameLib.addCardColor(Color.rgb(10, 10, 100));
        int x = gameLib.random(1, cols-2);
        cardBall = gameLib.addCardColor(Color.rgb(255, 255, 255), x, rows/2+1, 1, 1);
        cardBall.checkCollision();
        cardEdgeL = gameLib.addCardColor(Color.rgb(255,255,255), 0,0,1,rows);
        cardEdgeL.checkCollision();
        cardEdgeR = gameLib.addCardColor(Color.rgb(255,255,255), cols-1,0,1,rows);
        cardEdgeR.checkCollision();
        cardEdgeT = gameLib.addCardColor(Color.rgb(255,255,255), 1,0,cols-2,1);
        cardEdgeT.checkCollision();
        cardEdgeB = gameLib.addCardColor(Color.rgb(255,255,140), 1,rows-1,cols-2,1);
        cardEdgeB.checkCollision();
        cardRacket = gameLib.addCardColor(Color.rgb(255,255,255), 8,rows-3,4,1);
        cardRacket.checkCollision();

        remain = 0;
        for(int y=2; y < rows/2; y+=2) {
            for(x=3; x < cols-4; x+=4) {
                if(gameLib.random(2) == 0) continue;
                JGameLib.Card cardBlock = gameLib.addCardColor(Color.rgb(192,192,255), x,y,4,2);
                cardBlock.edgeThick(0.2);
                cardBlock.checkCollision();
                remain ++;
            }
        }
    }

    void stopGame() {
        cardBall.stopMoving();
        gameLib.clearMemory();
        gameLib.popupDialog(null, "Game is finished!", "Close");
    }

    // User Event start ====================================

    public void onStart(View v) {
        int x = gameLib.random(1, cols-2);
        cardBall.move(x, rows/2+1);
        double speed = 0.4;
        if(gameLib.random(0,1) == 0)
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
    public void onGameWorkEnded(JGameLib.Card card, JGameLib.WorkType workType) {}

    @Override
    public void onGameTouchEvent(JGameLib.Card card, int action, float blockX, float blockY) {}

    @Override
    public void onGameSensor(int sensorType, float x, float y, float z) {}

    @Override
    public void onGameCollision(JGameLib.Card card1, JGameLib.Card card2) {
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
            JGameLib.DirType dir = card1.collisionDir(card2);
            gameLib.removeCard(card2);
            remain --;
            if(remain > 0) {
                if(dir == JGameLib.DirType.LEFT || dir == JGameLib.DirType.RIGHT) {
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
    public void onGameTimer(int what) {}

    // Game Event end ====================================

}