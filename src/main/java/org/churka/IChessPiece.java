package org.churka;

import org.churka.ChessPieces.CPColor;

public interface IChessPiece {
    Util.Coord pos = null;
    CPColor type = null;

    default boolean isValidPos(int x, int y){
        return x<7 & y<7;
    }

    default Util.Coord getPos(){return pos;};

    boolean isValidMove(int x, int y, IChessPiece[] board);

    void setPos(int x, int y, IChessPiece[] board);

    CPColor getType();
}
