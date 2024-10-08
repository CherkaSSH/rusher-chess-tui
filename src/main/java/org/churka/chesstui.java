package org.churka;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.move.Move;
import org.rusherhack.client.api.events.client.chat.EventChatMessage;
import org.rusherhack.client.api.feature.command.ModuleCommand;
import org.rusherhack.client.api.feature.module.Module;
import org.rusherhack.client.api.feature.module.ModuleCategory;
import org.rusherhack.core.command.annotations.CommandExecutor;
import org.rusherhack.core.event.subscribe.Subscribe;
import org.rusherhack.core.setting.NumberSetting;

import java.util.Timer;
import java.util.TimerTask;

public class chesstui extends Module {
    public chesstui() {
        super("Chess", ModuleCategory.CHAT);
    }
    Board board=null;
    String opponent;
    STATUS status=STATUS.AWAITINGGAME;
    Timer timer = new Timer();
    public final NumberSetting<Long> timerSetting = new NumberSetting("TimeOut","Time to await for the other person to reply",15,1,100);
    @Override
    public ModuleCommand createCommand(){
        return new ModuleCommand(this){
            @CommandExecutor
            public String getBoard(){
                if(board!=null) return board.toString(); else return "No game yet";
            }
            @CommandExecutor(subCommand = "move")
            @CommandExecutor.Argument({"from","to"})
            public String move(String from,String to){
                if(board!=null){
                    board.doMove(new Move(Square.fromValue(from),Square.fromValue(to)),true);
                    return "Moved "+board.getPiece(Square.fromValue(from))+" to "+to;

                }else{
                    return "No board dude";
                }
            }
            @CommandExecutor(subCommand = "start")
            public void newGame(String player){
                if (board==null){
                    board = new Board();
                    opponent=player;
                    status=STATUS.AWAITINGGAME;
                    timer.schedule(new TimerTask(){
                        @Override
                        public void run() {
                            status=STATUS.DENIED;
                        }
                    },timerSetting.getValue()*1000);
                    mc.getConnection().sendCommand("msg "+player+" START GAME");
                }else{logger.info("dude you have a game already");}
            };
            @CommandExecutor(subCommand = "deny")
            public void denyGame(){
                mc.getConnection().sendCommand("r DENY");
            }
        };
    }
    @Subscribe
    public void onChat(EventChatMessage event){
        String contents = event.getMessage();
        if (status == STATUS.AWAITINGGAME & contents.contains(opponent)){
            if (contents.contains("ACCEPT")){
                status=STATUS.INGAME;
                this.logger.info(opponent+" accepted your request");
                timer.cancel();
            }else if (contents.contains("DENY")){
                status=STATUS.DENIED;
                opponent=null;
                this.logger.info(opponent+" denied your request");
            };
        }
    }

    enum STATUS{
        INGAME,AWAITINGGAME,DENIED,WON,LOST,FORFEIT,AWATINGMOVE
    }
    enum TURN{
        me,otherguy;
    }
}
