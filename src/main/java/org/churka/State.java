package org.churka;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.Side;
import lombok.Getter;

public class State {
    public State(String opponentName, Side side) {
        this.opponent = opponentName;
        this.myside = side;
        this.board = new Board();
        // White always makes the first move in a new game.
        this.board.setSideToMove(Side.WHITE);
    }

    @Getter
    private final String opponent;
    public Board board;
    public Side myside;

    public String getBoard() {
        StringBuilder temp=new StringBuilder();
        Piece[] pieces = board.boardToArray();
        for (int rows = 0; rows <= 7; rows++) {
            temp.append("|").append(rows).append("|");
            for (int column = 0; column <= 7; column++) {
                temp.append(pieces[rows * 8 + column].getFanSymbol()).append("|");
            }
            temp.append("\n");
        }
        temp.append("|a|b|c|d|e|f|g|h|");
        return temp.toString();
    }
}
