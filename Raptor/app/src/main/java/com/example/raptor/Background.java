package com.example.raptor;

import android.graphics.Bitmap;
import android.graphics.Canvas;

public class Background {
    private Bitmap image;
    private int x,y,dx;

    public Background(Bitmap res){

        image = res;
        dx =  GamePanel.Movespeed;
    }

    public void update(){
        x += dx;
        if(x < -GamePanel.width){
            x=0;
        }
    }

    public void draw(Canvas canvas){
        canvas.drawBitmap(image, x, y,null);
        if(x<0){
            canvas.drawBitmap(image, x+GamePanel.width,y,null);
        }
    }
}
