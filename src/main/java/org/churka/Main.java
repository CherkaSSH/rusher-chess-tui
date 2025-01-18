package org.churka;

import org.rusherhack.client.api.RusherHackAPI;
import org.rusherhack.client.api.plugin.Plugin;

public class Main extends Plugin {
    static ChessTUI tui = new ChessTUI();
    @Override
    public void onLoad() {
        RusherHackAPI.getModuleManager().registerFeature(tui);
        RusherHackAPI.getHudManager().registerFeature(new GameHudElement());
        this.logger.info("loaded chess tui");
    }

    @Override
    public void onUnload() {

    }

    public static State getState(){
        return tui.getState();
    }
}
