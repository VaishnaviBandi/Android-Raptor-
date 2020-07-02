package com.example.raptor;

import android.graphics.Bitmap;
import android.graphics.Canvas;

public class BBorder extends GameObject {

    private Bitmap image;

    public BBorder(Bitmap res, int x, int y){
        Height = 200;
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
        canvas.drawBitmap(image, x, y, null);
    }
}
