package org.churka;

public class State {
    public State(String opponent){
        this.opponent=opponent;
    }

    private final String opponent;

    String getOpponent(){
        return opponent;
    }
}
