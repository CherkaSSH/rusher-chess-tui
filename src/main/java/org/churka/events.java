package org.churka;

import com.github.bhlangonijr.chesslib.*;
import org.churka.chesstui;

public class events implements BoardEventListener {
    events(Board b){
        this.board=b;
    }
    Board board;
    public Piece[] parr=new Piece[8];
    @Override
    public void onEvent(BoardEvent event) {
        if (event.getType() == BoardEventType.ON_MOVE){
            if(board.getSideToMove()==Side.WHITE){
                for(int i=0;i<parr.length;i++){
                    parr[i]=board.boardToArray()[i];
                }
            };
            if(board.getSideToMove()==Side.BLACK){
                for(int i=0;i<parr.length;i++){
                    parr[i]=board.boardToArray()[i+7*8-1];
                }
            };
        }
    }
}
