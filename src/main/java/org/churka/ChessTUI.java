package org.churka;

import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.move.Move;
import lombok.Getter;
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
    @Getter
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
            @CommandExecutor.Argument({"from","to","piece"})
            public String makeMove(String from, String to, String piece){
                if(online()) return "Get on the server";
                if(state.board.getSideToMove()!=state.myside) return "Not your time to move";
                if(state.board.isMoveLegal(new Move(Square.fromValue(from), Square.fromValue(to),Piece.fromValue(piece)),true)) return "Illegal move";
                mc.getConnection().sendCommand("w "+state.getOpponent()+" CHESS_MOVE "+from+" "+to+" "+piece);
                return "Made move";
            }

            @CommandExecutor
            public String leave(){
                if(online()|state==null) return "Not online";
                //:troll:
                mc.getConnection().sendCommand("w "+state.getOpponent()+" IMGAY");
                mc.getConnection().sendChat("IM GAY");
                state.board=null;
                return "Coward";
            }

        };
    }

    @Subscribe
    public void onChat(EventAddChat event){
        if (cAccept==0) state = null;
        String msg=event.getChatComponent().getString();
        if (msg.contains("CHESS_REQUEST") & msg.contains("From")){
            oppmaybe = msg.split(" ")[1];
        } else if (msg.contains("CHESS_MOVE") & msg.contains(state.getOpponent()) & msg.split(" ").length==6) {
            getLogger().info("New move from"+state.getOpponent());
            String[] temp = msg.split(" ");
            state.board.doMove(new Move(Square.fromValue(temp[3]),
                    Square.fromValue(temp[4]),
                    Piece.fromValue(temp[5])),
                    true);
        } else if (msg.contains("IM GAY") & msg.contains(mc.player.getName().getString())) {
            event.setCancelled(true);
        } else if (msg.contains("IM GAY") & msg.contains(state.getOpponent())){
            this.getLogger().info(state.getOpponent()+" left the game");
        }
    }

    private List<String> onlinePlayers(){
        List<String> ls = new java.util.ArrayList<>();
        if(online()) return ls;
        for (PlayerInfo pi: Objects.requireNonNull(mc.getConnection()).getOnlinePlayers()){
            ls.add(pi.getProfile().getName());
        }
        return ls;
    }

    boolean online(){
        return mc.getConnection() == null;
    }
}
