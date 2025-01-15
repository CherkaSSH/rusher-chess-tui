package org.churka;

import net.minecraft.client.multiplayer.PlayerInfo;
import org.rusherhack.client.api.events.client.chat.EventAddChat;
import org.rusherhack.client.api.feature.command.ModuleCommand;
import org.rusherhack.client.api.feature.module.ModuleCategory;
import org.rusherhack.client.api.feature.module.ToggleableModule;
import org.rusherhack.core.command.annotations.CommandExecutor;
import org.rusherhack.core.event.subscribe.Subscribe;
import org.rusherhack.core.setting.NumberSetting;

import java.util.List;
import java.util.Objects;

public class ChessTUI extends ToggleableModule {
    public ChessTUI() {
        super("Chess", ModuleCategory.CHAT);
        this.registerSettings(chat);
    }

    /**
     * Settings
     */
    public final NumberSetting<Integer> chat = new NumberSetting<>("Count","# of texts to await the request",15,1,100);

    /**
     * Values
     */
    int cAccept;
    private String oppmaybe;
    @Override
    public ModuleCommand createCommand(){
        return new ModuleCommand(this){

            @CommandExecutor
            @CommandExecutor.Argument({"opponent"})
            public String createGame(String opponent){
                if (!online().contains(opponent)) return "Player not found";
                mc.getConnection().sendCommand("w " + opponent + " CHESSREQUEST");
                cAccept=chat.getValue();
                return "Sent request";
            }


        };
    }
    @Subscribe
    public void onChat(EventAddChat event){
        cAccept--;
        String msg=event.getChatComponent().getString();
        if (msg.contains("CHESSREQUEST")){
            oppmaybe=msg.split("")[1];
        }
    }

    List<String> online(){
        List<String> ls = List.of();
        for (PlayerInfo pi: Objects.requireNonNull(mc.getConnection()).getOnlinePlayers()){
            ls.add(pi.getProfile().getName());
        }
        return ls;
    }
}
