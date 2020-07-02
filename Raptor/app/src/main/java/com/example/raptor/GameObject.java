package com.example.raptor;

import android.graphics.Rect;

public abstract class GameObject {

    protected int x;
    protected int y;
    protected int dx;
    protected int dy;
    protected int Width;
    protected int Height;

    public void setX(int x){
        this.x = x;
    }
    public void setY(int y){
        this.y = y;
    }
    public int getX(){
        return x;
    }
    public int getY(){
        return y;
    }
    public int getWidth(){
        return Width;
    }
    public int getHeight(){
        return Height;
    }
    public Rect getRectangle(){
        return new Rect(x, y, x+Width, y+Height);
    }
}
