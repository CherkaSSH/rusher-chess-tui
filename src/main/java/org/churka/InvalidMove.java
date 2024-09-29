package org.churka;

public class InvalidMove extends RuntimeException {
    public InvalidMove(IChessPiece iChessPiece) {
        super(iChessPiece.getType().toString()+" Moved unexpectedly");
    }
}
