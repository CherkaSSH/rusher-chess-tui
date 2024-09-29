package org.churka;

import org.rusherhack.client.api.feature.module.Module;
import org.rusherhack.client.api.feature.module.ModuleCategory;

public class chesstui extends Module {
    public chesstui() {
        super("Chess", ModuleCategory.CHAT);
    }
    public void print(String mes){
        this.logger.info(mes);
    }

}
