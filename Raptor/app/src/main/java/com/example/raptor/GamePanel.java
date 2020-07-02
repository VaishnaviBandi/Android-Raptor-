package com.example.raptor;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.Random;

public class GamePanel extends SurfaceView implements SurfaceHolder.Callback
{
    public static final int width = 856;
    public static final int height = 480;
    public static final int Movespeed = -5;
    private MainThread thread;
    private Background bg;

    private PlayerClass playerClass;

    private ArrayList<Smoke> smokepuff;
    private long smokeStartTime;

    private ArrayList<Missile> missiles;
    private long missileStartTime;
//    private long missilePassed;
    private Random rand = new Random();
    private ArrayList<TBorder> topborder;
    private ArrayList<BBorder> botborder;
    private int minborderH;
    private int maxborderH;
    private boolean topdown = true;
    private boolean botdown = true;
    private int difficulty = 20; //proportional.
    private boolean newGameCreated;
    private Explosion explosion;
    private long startReset;
    private boolean reset;
    private boolean disappear;
    private boolean started = false;
    private int best;

    public GamePanel(Context context) {
        super(context);

        getHolder().addCallback(this);

        setFocusable(true);
    }
    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        boolean retry = true;
        int counter = 0;
        while(retry && counter<1000){
            try{
                counter++;
                thread.setRunning(false);
                thread.join();
                retry = false;
                SharedPreferences prefs = this.getContext().getSharedPreferences("BEST", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt("key", best);
                editor.commit();
                thread = null;
            }
            catch(InterruptedException e){e.printStackTrace();}
        }
    }


    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        bg = new Background(BitmapFactory.decodeResource(getResources(),R.drawable.grassbg1));
        playerClass = new PlayerClass(BitmapFactory.decodeResource(getResources(),R.drawable.helicopter), 65,25,3);
        SharedPreferences prefs = this.getContext().getSharedPreferences("BEST", Context.MODE_PRIVATE);
        best = prefs.getInt("key", 0);
        smokepuff = new ArrayList<Smoke>();
        missiles = new ArrayList<Missile>();
        topborder = new ArrayList<TBorder>();
        botborder = new ArrayList<BBorder>();

        smokeStartTime = System.nanoTime();
        missileStartTime = System.nanoTime();

        thread = new MainThread(getHolder(), this);
        thread.setRunning(true); //game loop
        thread.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        if(event.getAction() == MotionEvent.ACTION_DOWN){
            if(!playerClass.getplaying() &&newGameCreated && reset)
            {
                playerClass.setplaying(true);
                playerClass.setUp(true);
            }
            if(playerClass.getplaying())
            {
                reset = false;
                playerClass.setUp(true);
            }
            return true;
        }
        if(event.getAction()==MotionEvent.ACTION_UP){
//            if(!started)started =true;

            playerClass.setUp(false);
            return true;
        }
        return super.onTouchEvent(event);
    }


    public void update()
    {
        if(playerClass.getplaying())
        {
            if(botborder.isEmpty())
            {
                playerClass.setplaying(false);
                return;
            }
            if(topborder.isEmpty())
            {
                playerClass.setplaying((false));
                return;
            }
            bg.update();
            playerClass.update();

            maxborderH = 30+playerClass.getScore()/difficulty;
            if(maxborderH >height/4) maxborderH = height/4;
            minborderH = 5+playerClass.getScore()/difficulty;

            for( int i = 0; i< botborder.size(); i++)
            {
                if(collision(botborder.get(i), playerClass)){
                    playerClass.setplaying(false);
                }
            }
            for( int i = 0; i < topborder.size(); i++)
            {
                if(collision(topborder.get(i), playerClass)){
                    playerClass.setplaying(false);
                }
            }
            this.updatetopborder();
            this.updatebotborder();

            long missilePassed = (System.nanoTime()-missileStartTime)/1000000;
            if(missilePassed > (2000 - playerClass.getScore()/4))
            {
                if(missiles.size() == 0)
                {
                    missiles.add(new Missile(BitmapFactory.decodeResource(getResources(),R.drawable.missile),
                            width +10, height/2, 45, 15, playerClass.getScore(), 13));
                }

                else {
                        missiles.add(new Missile(BitmapFactory.decodeResource(getResources(), R.drawable.missile),
                                width+10, (int)(rand.nextDouble()*(height - (maxborderH*2))+maxborderH), 45, 15, playerClass.getScore(), 13));
                }
                missileStartTime = System.nanoTime();
            }
            // checking collision and removing
            for(int i = 0; i < missiles.size(); i++){
                missiles.get(i).update();
                if(collision(missiles.get(i),playerClass))
                {
                    missiles.remove(i);
                    playerClass.setplaying(false);
                    break;
                }
                if(missiles.get(i).getX()<-100){
                    missiles.remove(i);
                    break;
                }
            }

            long elapsed = (System.nanoTime() - smokeStartTime)/1000000;
            if(elapsed > 120){
                smokepuff.add(new Smoke(playerClass.getX(),playerClass.getY()+10));
                smokeStartTime = System.nanoTime();
            }
            for(int i = 0; i<smokepuff.size(); i++)
            {
                smokepuff.get(i).update();
                if(smokepuff.get(i).getX()<-10)
                {
                    smokepuff.remove(i);
                }
            }
        }
        else {
            playerClass.resetDYACC();

            if(!reset)
            {
                newGameCreated = false;
                startReset = System.nanoTime();
                reset = true;
                disappear = true;
                explosion = new Explosion(BitmapFactory.decodeResource(getResources(), R.drawable.explosion),
                        playerClass.getX(), playerClass.getY()-30, 100, 100, 25);
//                explosion.update();
                if (playerClass.getScore() > best) best = playerClass.getScore();
            }

            explosion.update();

            long resetElapsed = (System.nanoTime()-startReset)/1000000;
            if(resetElapsed >2500 && !newGameCreated){
//                explosion.update();
                newGame();
            }
        }
    }

    public boolean collision(GameObject a, GameObject b)
    {
        if(Rect.intersects(a.getRectangle(), b.getRectangle()))
        {
            if(!started)started =true;
            return true;
        }
        return false;
    }

    @Override
    public void draw(Canvas canvas){

        final float scaleFactorX = getWidth()/(width*1.f);
        final float scaleFactorY = getHeight()/(height*1.f);

        if (canvas != null) {
            final int savedState = canvas.save();
            canvas.scale(scaleFactorX, scaleFactorY);
            bg.draw(canvas);
            if(!disappear){
                playerClass.draw(canvas);
            }
            for(Smoke sp:smokepuff){
                sp.draw(canvas);
            }
            for(Missile ms: missiles){
                ms.draw(canvas);
            }
            for (TBorder tb : topborder){
                tb.draw(canvas);
            }
            for(BBorder bb: botborder){
                bb.draw(canvas);
            }
            if(started){
                explosion.draw(canvas);
            }
            drawText(canvas);
            canvas.restoreToCount(savedState);
        }
    }
    public void updatetopborder()
    {
        if(playerClass.getScore()%50 ==0){
            topborder.add(new TBorder(BitmapFactory.decodeResource(getResources(),R.drawable.brick),
                    topborder.get(topborder.size()-1).getX()+20, 0, (int) ((rand.nextDouble()*maxborderH))+1));
        }

        for(int i = 0; i<topborder.size();i++){
            topborder.get(i).update();
            if(topborder.get(i).getX()<-20) {
                topborder.remove(i);

                if(topborder.get(topborder.size()-1).getHeight()>=maxborderH){
                    topdown = false;
                }
                if(topborder.get(topborder.size()-1).getHeight() <=minborderH) {
                    topdown = true;
                }
                if(topdown){
                    topborder.add(new TBorder(BitmapFactory.decodeResource(getResources(),
                            R.drawable.brick), topborder.get(topborder.size()-1).getX()+20, 0,
                            topborder.get(topborder.size()-1).getHeight()+1));
                }
                else{
                    topborder.add(new TBorder(BitmapFactory.decodeResource(getResources(),
                            R.drawable.brick), topborder.get(topborder.size()-1).getX()+20, 0,
                            topborder.get(topborder.size()-1).getHeight()-1));
                }
            }

        }
    }

    public void updatebotborder()
    {
        if(playerClass.getScore()%40 ==0){
            botborder.add(new BBorder(BitmapFactory.decodeResource(getResources(),R.drawable.brick),
                    botborder.get(botborder.size()-1).getX()+20,(int)((rand.nextDouble()*maxborderH)+(height - maxborderH))));
        }
        for(int i = 0; i<botborder.size();i++) {
            botborder.get(i).update();
            if (botborder.get(i).getX() < -20) {
                botborder.remove(i);

                if (botborder.get(botborder.size() - 1).getY() <= height - maxborderH) {
                    botdown = true;
                }
                if (botborder.get(botborder.size() - 1).getY() >= height - minborderH) {
                    botdown = false;
                }
                if (botdown) {
                    botborder.add(new BBorder(BitmapFactory.decodeResource(getResources(),
                            R.drawable.brick), botborder.get(botborder.size() - 1).getX() + 20,
                            botborder.get(botborder.size() - 1).getY() + 1));
                } else {
                    botborder.add(new BBorder(BitmapFactory.decodeResource(getResources(),
                            R.drawable.brick), botborder.get(botborder.size() - 1).getX() + 20,
                            botborder.get(botborder.size() - 1).getY() - 1));
                }
            }
        }
        }
    public void newGame()
    {
        disappear = false;
        topborder.clear();
        botborder.clear();
        smokepuff.clear();
        missiles.clear();
        started = false;


        minborderH = 5;
        maxborderH = 30;

        playerClass.resetDYACC();

        playerClass.setY(height/2);

        if(playerClass.getScore()>best){
            best  = playerClass.getScore();
        }
         playerClass.resetScore();


        //for starting top border
        for(int  i = 0; i*20 < width+40; i++)
        {
            if(i == 0){
                topborder.add(new TBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick), i*20, 0 ,10));
            }
            else{
                topborder.add(new TBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick),
                        i*20, 0 ,topborder.get(i-1).getHeight()+1));
            }
        }
        for (int i = 0; i*20<width+40; i++){
            if( i == 0){
                botborder.add(new BBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick),
                        i*20, height- minborderH));
            }
            else{
                botborder.add(new BBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick), i*20,
                        botborder.get(i-1).getY()-1));
            }
        }
        newGameCreated = true;
    }
    public void drawText(Canvas canvas)
    {
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(30);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("DISTANCE: "+ (playerClass.getScore()*3), 10, height-10, paint);
        canvas.drawText("Best: " + best, width-215, height-10,paint);

        if(!playerClass.getplaying()&& newGameCreated && reset)
        {
            Paint paint1 =new Paint();

            paint1.setTextSize((60));
            paint1.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            canvas.drawText("RAPTOR", width/2-50, height/3, paint1);
            paint1.setTextSize((30));

            canvas.drawText("TAP TO START", width/2-50, height/2, paint1);

            paint1.setTextSize(20);
            canvas.drawText("# Tap and Hold to Go Up", width/2-50, height/2+20, paint1);
            canvas.drawText("# Release to Go Down", width/2-50, height/2+40, paint1);
        }
    }
}
