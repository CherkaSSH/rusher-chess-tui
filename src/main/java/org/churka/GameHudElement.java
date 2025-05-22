package org.churka;

import org.rusherhack.client.api.feature.hud.TextHudElement;

public class GameHudElement extends TextHudElement {
    public GameHudElement() {
        super("ChessRepresentation");
    }

    @Override
    public String getText() {
        State gameState = Main.getState();
        if (gameState == null) {
            return "No active chess game.";
        }
        return gameState.getBoard();
    }
}
