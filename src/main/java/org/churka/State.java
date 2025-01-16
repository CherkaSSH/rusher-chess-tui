package org.churka;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Side;

public class State {
    public State(String opponent,Side side){
        this.opponent=opponent;
        this.myside=side;
        this.board=new Board();
    }

    private final String opponent;
    public Board board;
    public Side myside;

    String getOpponent(){
        return opponent;
    }
}
