package org.churka;

import org.rusherhack.client.api.RusherHackAPI;
import org.rusherhack.client.api.plugin.Plugin;

public class main extends Plugin {
    @Override
    public void onLoad() {
        RusherHackAPI.getModuleManager().registerFeature(new chesstui());
        this.logger.info("loaded chess tui");
    }

    @Override
    public void onUnload() {

    }
}
