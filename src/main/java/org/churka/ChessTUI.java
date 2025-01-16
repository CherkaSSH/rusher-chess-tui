package org.churka;

import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.move.Move;
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
    private State state;

    @Override
    public ModuleCommand createCommand(){
        return new ModuleCommand(this){

            @CommandExecutor
            @CommandExecutor.Argument({"opponent"})
            public String createGame(String opponent){
                if (!onlinePlayers().contains(opponent)) return "Player not found";
                mc.getConnection().sendCommand("w " + opponent + " CHESSREQUEST");
                cAccept=chat.getValue();
                return "Sent request";
            }

            @CommandExecutor
            public String acceptGame(){
                if (oppmaybe==null) return "No game requests";
                state=new State(oppmaybe,Side.BLACK);
                state.board.setSideToMove(Side.BLACK);
                return "Game accepted";
            }

            @CommandExecutor
            @CommandExecutor.Argument({"from,to"})
            public String makeMove(Square from, Square to){
                if(!online()) return "Get on the server";
                if(state.board.getSideToMove()!=state.myside) return "Not your time to move";
                if(state.board.isMoveLegal(new Move(from,to),true)) return "Illigal move";
                mc.getConnection().sendCommand("w "+state.getOpponent()+" MOVE "+from+" "+to);
                return "Made move";
            }

        };
    }
    @Subscribe
    public void onChat(EventAddChat event){
        if (cAccept==0) state=null;
        String msg=event.getChatComponent().getString();
        if (msg.contains("CHESSREQUEST")){
            oppmaybe=msg.split(" ")[1];
        }
    }

    private List<String> onlinePlayers(){
        List<String> ls = List.of();
        if(!online()) return ls;
        for (PlayerInfo pi: Objects.requireNonNull(mc.getConnection()).getOnlinePlayers()){
            ls.add(pi.getProfile().getName());
        }
        return ls;
    }

    boolean online(){
        return mc.getConnection()!=null;
    }
}
