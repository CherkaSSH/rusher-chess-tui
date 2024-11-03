package org.churka;

import com.github.bhlangonijr.chesslib.*;
import com.github.bhlangonijr.chesslib.move.Move;
import org.rusherhack.client.api.events.client.chat.EventAddChat;
import org.rusherhack.client.api.feature.command.ModuleCommand;
import org.rusherhack.client.api.feature.module.Module;
import org.rusherhack.client.api.feature.module.ModuleCategory;
import org.rusherhack.core.command.annotations.CommandExecutor;
import org.rusherhack.core.event.subscribe.Subscribe;
import org.rusherhack.core.setting.NumberSetting;

import java.util.*;

public class chesstui extends Module {
    public chesstui() {
        super("Chess", ModuleCategory.CHAT);
        board.addEventListener(BoardEventType.ON_MOVE,new events(board));
    }

    public final NumberSetting<Long> timerSetting = new NumberSetting("TimeOut","Time to await for the other person to reply",15,1,100);

    Board board=null;
    String opponent;
    CLIENT_STATUS clientstatus= CLIENT_STATUS.AWAITING_GAME;
    STATUS gamestatus=STATUS.NO_GAME;
    Timer timer = new Timer();

    @Override
    public ModuleCommand createCommand(){
        return new ModuleCommand(this){
            @CommandExecutor
            public String getBoard(){
                if(board!=null&mc.getConnection()!=null) return betterToString(); else return "No game yet";
            }

            @CommandExecutor(subCommand = "move")
            @CommandExecutor.Argument({"from","to"})
            public String move(String from,String to){
                if(board!=null){
                    if (board.isMoveLegal(new Move(Square.fromValue(from),Square.fromValue(to)),true))
                    {
                        board.doMove(new Move(Square.fromValue(from), Square.fromValue(to)), true);
                        gamestatus = STATUS.AWAITING_MOVE;
                        Objects.requireNonNull(mc.getConnection()).sendCommand("msg " + opponent + " MOVE " + from + " " + to);
                        board.doMove(new Move(Square.fromValue(from), Square.fromValue(to)), true);
                        return "Moved " + board.getPiece(Square.fromValue(from)) + " to " + to + ".\n Waiting for the next move..";
                    }
                    else return "illegal move, try another move";
                }
                return "No board dude";
            }
            @CommandExecutor(subCommand = "start")
            public void newgame(String player){
                if (board==null&mc.player!=null)
                {
                    board = new Board();
                    opponent=player;
                    clientstatus=CLIENT_STATUS.AWAITING_GAME;
                    timer.schedule(new TimerTask(){
                        @Override
                        public void run() {
                            clientstatus=CLIENT_STATUS.DENIED;
                        }
                    },timerSetting.getValue()*1000);
                    mc.getConnection().sendCommand("msg "+player+" START "+mc.player.getName().getString());
                }
                else logger.info("dude you have a game already");
            };
            @CommandExecutor(subCommand = "deny")
            public void denyGame(){
                Objects.requireNonNull(mc.getConnection()).sendCommand("r DENY");
            }
        };
    }

    @Subscribe
    public void onChat(EventAddChat event){
        String contents = event.getChatComponent().getString();
        if (clientstatus == CLIENT_STATUS.AWAITING_GAME & contents.contains(opponent)){
            if (contents.contains("ACCEPT")&contents.contains(opponent)){
                clientstatus = CLIENT_STATUS.IN_GAME;
                this.logger.info(opponent+" accepted your request)");
                board=new Board();
                timer.cancel();
            }else if (contents.contains("DENY")&contents.contains(opponent)){
                clientstatus = CLIENT_STATUS.DENIED;
                opponent=null;
                this.logger.info(opponent+" denied your request(");
            };
        }
        if (clientstatus == CLIENT_STATUS.IN_GAME & contents.contains(opponent)){
            String[] mesarr=contents.split(" ");
            try{
                Square from=Square.fromValue(mesarr[mesarr.length-1].toUpperCase());
                Square to=Square.fromValue(mesarr[mesarr.length-2].toUpperCase());
                board.doMove(new Move(from,to),true);
                board.setSideToMove(Side.WHITE);
            }catch (Exception e){
                getLogger().info("ERROR: unsupported move");
                System.out.println(e);
            }
        }
        if (contents.contains("START")){
            logger.info("New game. Do you accept?");
        }
    }

    String betterToString(){
        StringBuilder builder = new StringBuilder();
        String[][] twoDPieces = new String[8][8];
        Piece[] pieces=board.boardToArray();
        for (int i = 0; i < 64; i++) {
            if (pieces[i]!=Piece.NONE) {
                twoDPieces[(i-i%8)/8][i%8]=pieces[i].getFanSymbol();
            } else twoDPieces[(i-i%8)/8][i%8]="‾";
        }
        builder.append("--------------------\n");
        for (int row = 0; row < 8; row++) {
            builder.append("|");
            for (int column = 0; column < 8; column++) {
                builder.append(twoDPieces[row][column]);
            }
            builder.append("|\n");
        }
        builder.append("--------------------\n");
        return builder.toString();
    }

    enum STATUS{
        WON,LOST,FORFEIT, AWAITING_MOVE,DRAW,NO_GAME
    }
    enum CLIENT_STATUS {
        IN_GAME, AWAITING_GAME,DENIED
    }
    enum TURN{
        me,otherguy;
    }
}
