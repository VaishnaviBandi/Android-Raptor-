package com.example.raptor;

import android.graphics.Bitmap;
import android.graphics.Canvas;

import java.util.Calendar;

public class TBorder extends GameObject {
    private Bitmap image;
    public TBorder(Bitmap res, int x, int y, int h){
        Height = h;
        Width = 20;

        this.x = x;
        this.y = y;

        dx = GamePanel.Movespeed;
        image = Bitmap.createBitmap(res, 0, 0, Width, Height);
    }

    public void update(){
        x += dx;
    }

    public void draw(Canvas canvas){
        try{
            canvas.drawBitmap(image, x, y, null);
        }
        catch (Exception e){

        }
    }
}
