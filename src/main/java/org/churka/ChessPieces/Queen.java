package org.churka.ChessPieces;

import org.churka.IChessPiece;
import org.churka.InvalidMove;
import org.churka.Util;
import org.churka.chesstui;

public class Queen implements IChessPiece {
    public Queen(CPColor type){this.color=type;}

    private Util.Coord pos;
    private CPColor color;
    private chesstui ct;

    @Override
    public boolean isValidMove(int x, int y, IChessPiece[] board) {
        boolean diag = x-pos.getX()==y-pos.getY();
        boolean str = pos.getX()==x ^ pos.getY()==y;
        return diag^str;
    }

    @Override
    public void setPos(int x, int y, IChessPiece[] board) {
        if (isValidPos(x,y)&(isValidMove(x,y,board))) pos.setCoords(new int[]{x,y}); else ct.print(new InvalidMove(this).toString());
    }

    @Override
    public CPColor getType() {
        return type;
    }
}
