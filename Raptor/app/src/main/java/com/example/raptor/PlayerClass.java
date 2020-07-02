package com.example.raptor;

import android.graphics.Bitmap;
import android.graphics.Canvas;


public class PlayerClass extends GameObject{

    private Bitmap spritesheet;
    private int score;
    private boolean up;
    private boolean playing;
    private Animation animation = new Animation();
    private long startTime;

    public PlayerClass(Bitmap res, int w, int h, int numFrames){
        x = 100;
        y = GamePanel.height/2;
        dy = 0;
        score = 0;
        Height = h;
        Width = w;

        Bitmap[] image = new Bitmap[numFrames];
        spritesheet = res;

        for(int i= 0; i < image.length; i++){
            image [i] = Bitmap.createBitmap(spritesheet, i*Width, 0, Width, Height);
        }
        animation.setFrames(image);
        animation.setDelay(10);
        startTime = System.nanoTime();
    }
    public void setUp(boolean b){
        up = b;
    }
    public void update() {
        long elapsed = (System.nanoTime() / 1000000);
        if (elapsed > 100) {
            score++;
            startTime = System.nanoTime();
        }
        animation.update();

        if (up) {
            dy -= 1;
        } else {
            dy += 1;
        }
        if (dy > 14) dy = 14;
        if (dy < -14) dy = -14;

        y += dy * 2;
    }

        public void draw(Canvas canvas)
        {
            canvas.drawBitmap (animation.getImage(), x, y, null) ;
        }
    public int getScore ( )
    {
    return score;
    }
    public boolean getplaying() {return playing;}
    public void setplaying (boolean b) {playing = b;}
    public void resetDYACC () {dy = 0;}
    public void resetScore () {score = 0;}
}
