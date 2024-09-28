package org.churka;


public class ChessPiece {
    private int[] pos = {0,0};
    boolean isValidMove(int x,int y){
        return false;
    };
    public int[] getPos(){return pos;};
    public void setPos(int x,int y){pos[0]=x;pos[1]=y;}
}
