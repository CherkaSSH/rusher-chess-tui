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
    }

    /**
     * Values
     */
    private String pendingOutgoingInviteOpponent = null;
    private String pendingIncomingInviteFrom = null;
    @Getter
    private State state;

    @Override
    public ModuleCommand createCommand(){
        return new ModuleCommand(this){

            @CommandExecutor
            @CommandExecutor.Argument({"opponent"})
            public String createGame(String opponent){
                if (mc.player == null || isOffline()) return "Not connected to a server.";
                if (!onlinePlayers().contains(opponent)) return "Player not found";
                pendingOutgoingInviteOpponent = opponent;
                mc.getConnection().sendCommand("w " + opponent + " [CHESS] INVITE " + mc.player.getName().getString());
                return "Chess invitation sent to " + opponent;
            }

            @CommandExecutor
            @CommandExecutor.Argument({"inviterName"})
            public String acceptGame(String inviterName){
                if (mc.player == null || isOffline()) return "Not connected to a server.";
                if (pendingIncomingInviteFrom == null) return "No pending chess invitations.";
                if (!pendingIncomingInviteFrom.equalsIgnoreCase(inviterName)) {
                    return "No pending invitation from " + inviterName + " or invitation expired.";
                }
                state = new State(inviterName, Side.BLACK);
                // White (inviter) moves first, this is set in State constructor and when inviter processes accept.
                // state.board.setSideToMove(Side.WHITE); 
                mc.getConnection().sendCommand("w " + inviterName + " [CHESS] ACCEPT " + mc.player.getName().getString());
                pendingIncomingInviteFrom = null; // Clear the pending invite
                return "Game accepted with " + inviterName + ". You are Black. White to move.";
            }

            @CommandExecutor
            @CommandExecutor.Argument({"from", "to"})
            public String makeMove(String from, String to) {
                if (isOffline()) return "You are not connected to a server.";
                if (state == null || state.board == null) return "No game in progress.";
                if (state.board.getSideToMove() != state.myside) return "Not your turn to move.";

                String fromSq = from.toUpperCase();
                String toSq = to.toUpperCase();
                Square squareFrom;
                Square squareTo;

                try {
                    squareFrom = Square.fromValue(fromSq);
                    squareTo = Square.fromValue(toSq);
                } catch (IllegalArgumentException e) {
                    return "Invalid square format. Use format like 'e2' 'e4'.";
                }

                Move move = new Move(squareFrom, squareTo);

                if (!state.board.isMoveLegal(move, true)) {
                    return "Illegal move.";
                }

                state.board.doMove(move);
                String message = "[CHESS] MOVE " + fromSq + toSq;
                mc.getConnection().sendCommand("w " + state.getOpponent() + " " + message);

                return "Move " + fromSq + toSq + " made. Sent to " + state.getOpponent() + ".";
            }

            @CommandExecutor
            public String resignGame(){ // Renamed from leave
                if (isOffline()) return "You are not connected to a server.";
                if (state == null || state.board == null) return "No game in progress to resign from.";
                
                String opponentName = state.getOpponent(); // Get opponent name for feedback before nullifying state
                
                mc.getConnection().sendCommand("w " + opponentName + " [CHESS] RESIGN");
                
                state = null;
                pendingIncomingInviteFrom = null;
                pendingOutgoingInviteOpponent = null;
                
                return "You resigned the game against " + opponentName + ".";
            }

        };
    }

    @Subscribe
    public void onChat(EventAddChat event){
        // if (cAccept==0) state = null; // cAccept removed
        String msg=event.getChatComponent().getString();

        // Expected format: <sender> whispers: [CHESS] <COMMAND> <args>
        String prefix = " whispers: [CHESS] ";
        int prefixIndex = msg.indexOf(prefix);

        if (prefixIndex == -1) return; // Not a chess message or wrong format

        String sender = msg.substring(0, prefixIndex);
        // remove <> from sender, if present
        if (sender.startsWith("<") && sender.endsWith(">")) {
            sender = sender.substring(1, sender.length() - 1);
        }
        
        if (mc.player != null && sender.equalsIgnoreCase(mc.player.getName().getString())) {
            return; // Ignore messages from self
        }

        String commandAndArgs = msg.substring(prefixIndex + prefix.length());
        String[] parts = commandAndArgs.split(" ", 2); // Split into command and the rest (args)
        String command = parts[0].toUpperCase();
        String args = parts.length > 1 ? parts[1] : "";

        if (command.equals("INVITE")) {
            // INVITE <inviterName is the sender>
            pendingIncomingInviteFrom = sender;
            this.getLogger().info("Chess invitation received from " + sender + ". Use '.chess accept " + sender + "' to accept.");
            // ChatUtils.print() // TODO: Replace getLogger with ChatUtils if available and appropriate
            return;
        } else if (command.equals("ACCEPT")) {
            // ACCEPT <accepterName is the sender>
            if (pendingOutgoingInviteOpponent != null && pendingOutgoingInviteOpponent.equalsIgnoreCase(sender)) {
                this.getLogger().info(sender + " accepted your chess invitation. You are White. White to move.");
                state = new State(sender, Side.WHITE);
                // state.board.setSideToMove(Side.WHITE); // Set in State constructor
                pendingOutgoingInviteOpponent = null; // Clear pending invite
            } else {
                this.getLogger().info(sender + " sent an ACCEPT, but you didn't have a pending invite to them or it expired.");
            }
            return;
        }

        // For other commands, ensure there's an active game and the sender is the opponent
        if (state == null || state.getOpponent() == null || !sender.equalsIgnoreCase(state.getOpponent())) {
            return;
        }

        switch (command) {
            case "MOVE":
                if (args.length() != 4) {
                    this.getLogger().info("Invalid MOVE format from " + sender + ": " + args);
                    return;
                }
                String fromStr = args.substring(0, 2).toUpperCase();
                String toStr = args.substring(2, 4).toUpperCase();
                Square squareFrom;
                Square squareTo;
                try {
                    squareFrom = Square.fromValue(fromStr);
                    squareTo = Square.fromValue(toStr);
                } catch (IllegalArgumentException e) {
                    this.getLogger().info("Invalid square in MOVE from " + sender + ": " + args);
                    return;
                }
                Move move = new Move(squareFrom, squareTo);

                if (state.board.getSideToMove() == state.myside) {
                    this.getLogger().info("Move received from " + sender + " but it's not their turn.");
                    return;
                }

                if (state.board.isMoveLegal(move, true)) {
                    state.board.doMove(move);
                    this.getLogger().info("Move " + args.toUpperCase() + " from " + sender + " applied. It's now your turn.");
                    // Optional: ChatUtils.print("Opponent played " + args.toUpperCase() + ". Your turn.");
                } else {
                    this.getLogger().info("Illegal move " + args.toUpperCase() + " received from " + sender + ".");
                }
                break;
            case "RESIGN": // Renamed from STOP
                this.getLogger().info(state.getOpponent() + " resigned the game.");
                // Optional: ChatUtils.print("Your opponent resigned. Game over.");
                state = null;
                pendingIncomingInviteFrom = null;
                pendingOutgoingInviteOpponent = null;
                break;
            default:
                this.getLogger().info("Unknown chess command from " + sender + ": " + command + " " + args);
        }
    }

    private List<String> onlinePlayers(){
        List<String> ls = new java.util.ArrayList<>();
        if(isOffline()) return ls;
        for (PlayerInfo pi: Objects.requireNonNull(mc.getConnection()).getOnlinePlayers()){
            ls.add(pi.getProfile().getName());
        }
        return ls;
    }

    boolean isOffline(){ // Renamed from online
        return mc.getConnection() == null;
    }
}
