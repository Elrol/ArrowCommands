package dev.elrol.arrow.commands.libs;

import dev.elrol.arrow.data.ExactLocation;
import dev.elrol.arrow.libs.ModTranslations;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;

import java.util.*;

public class CommandUtils {

    /*
     * UUID : Target UUID
     */
    private static final Map<UUID,TpRequest> tpRequests = new HashMap<>();


    public static boolean isOp(ServerCommandSource source) {
        ServerPlayerEntity player = source.getPlayer();
        if(player == null) {
            return false;
        }
        return source.hasPermissionLevel(4);
    }

    public static void requestTeleport(ServerPlayerEntity sender, ServerPlayerEntity target, boolean tpToSender) {
        if(tpRequests.containsKey(target.getUuid()) && tpRequests.get(target.getUuid()).isSender(sender)) {
            sender.sendMessage(ModTranslations.err("existing_request"));
            return;
        }
        tpRequests.put(target.getUuid(), new TpRequest(tpToSender, sender));

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if(tpRequests.containsKey(target.getUuid())) {
                    tpRequests.remove(target.getUuid());
                    sender.sendMessage(ModTranslations.err("tpa_timed_out"));
                    target.sendMessage(sender.getStyledDisplayName().copy().append(ModTranslations.err("other_tpa_timed_out")));
                }
            }
        }, 30000);

        sender.sendMessage(ModTranslations.msg(tpToSender ? "sent_tpa_here_1" : "sent_tpa_1")
                .append(Objects.requireNonNull(target.getDisplayName()).copy().formatted(Formatting.WHITE))
                .append(ModTranslations.msg(tpToSender ? "sent_tpa_here_2" : "sent_tpa_2"))
        );

        target.sendMessage(sender.getStyledDisplayName().copy()
                .append(ModTranslations.msg(tpToSender ? "received_tpa_here" : "received_tpa"))
                .setStyle(Style.EMPTY.withItalic(false).withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/tpaccept")))
        );
    }

    public static void acceptTeleport(ServerPlayerEntity target) {
        if(tpRequests.containsKey(target.getUuid())) {
            TpRequest request = tpRequests.get(target.getUuid());
            ServerPlayerEntity sender = Objects.requireNonNull(target.getServer()).getPlayerManager().getPlayer(request.getRequester());

            target.sendMessage(ModTranslations.msg(request.isRequestingTpToSender() ? "accepted_tpa_to" : "accepted_tpa_from").append(sender.getDisplayName()));
            sender.sendMessage(target.getStyledDisplayName().copy().append(ModTranslations.msg("accepted_tpa")));

            (request.isRequestingTpToSender() ? target : sender).sendMessage(ModTranslations.warn("teleporting").formatted(Formatting.YELLOW));

            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    if(request.isRequestingTpToSender()) ExactLocation.from(sender).teleport(target);
                    else ExactLocation.from(target).teleport(sender);
                }
            }, 1000);

            tpRequests.remove(target.getUuid());
        }
    }

    private static class TpRequest {
        UUID requester;
        boolean tpToSender;

        public TpRequest(boolean tpToSender, ServerPlayerEntity requester) {
            this.requester = requester.getUuid();
            this.tpToSender = tpToSender;
        }

        public boolean isRequestingTpToSender() {
            return tpToSender;
        }

        public boolean isSender(ServerPlayerEntity player) {
            return requester.equals(player.getUuid());
        }

        public UUID getRequester() { return requester; }
    }
}
