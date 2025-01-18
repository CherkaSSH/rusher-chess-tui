package org.churka;

import org.rusherhack.client.api.feature.hud.TextHudElement;

public class GameHudElement extends TextHudElement {
    public GameHudElement() {
        super("ChessRepresentation");
    }

    @Override
    public String getText() {
        return Main.getState().getBoard();
    }
}
