package org.crimsoncrips.craftorio.server.loan;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.events.ServerEvents;
import org.crimsoncrips.craftorio.registries.effect.CraftorioEffects;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.List;

public final class CraftorioLoanShark {

    public static final int ACTION_TAKE = 0;
    public static final int ACTION_PAY_ALL = 1;
    public static final int ACTION_PAY_PARTIAL = 2;
    public static final int ACTION_ACKNOWLEDGE_REMARK = 3;

    private CraftorioLoanShark() {}

    public static boolean hasLoan(Player player) {
        return CraftorioMisc.getLoanOwed(player).signum() > 0;
    }

    public static BigInteger withInterest(BigInteger given) {
        return withInterest(given, Craftorio.SERVER_CONFIG.LOAN_INTEREST_PERCENT.get());
    }

    public static BigInteger withInterest(BigInteger given, double interestPercent) {
        BigDecimal factor = BigDecimal.ONE.add(BigDecimal.valueOf(interestPercent).divide(BigDecimal.valueOf(100)));
        BigInteger owed = new BigDecimal(given).multiply(factor).setScale(0, RoundingMode.CEILING).toBigInteger();
        return owed.min(CraftorioMisc.pointThreshold());
    }

    public static void handle(ServerPlayer player, int action) {
        switch (action) {
            case ACTION_TAKE -> takeLoan(player);
            case ACTION_PAY_ALL -> repay(player, true);
            case ACTION_PAY_PARTIAL -> repay(player, false);
            case ACTION_ACKNOWLEDGE_REMARK -> {
                if (CraftorioMisc.isLoanSacrificed(player)) {
                    CraftorioMisc.setLoanSacrificed(false, player);
                    sync(player);
                }
            }
            default -> {
            }
        }
    }

    private static void takeLoan(ServerPlayer player) {
        if (hasLoan(player)) {
            say(player, "misc.craftorio.loan_shark_already_indebted", CraftorioMisc.bigIntFormat(CraftorioMisc.getLoanOwed(player)));
            return;
        }

        BigInteger points = CraftorioMisc.getPoints(player);
        if (points.signum() >= 0) return;

        BigInteger given = points.negate();
        CraftorioMisc.setPoints(BigInteger.ZERO, player);
        CraftorioMisc.setLoanBorrowed(given, player);
        CraftorioMisc.setLoanOwed(withInterest(given), player);
        CraftorioMisc.setLoanSacrificed(false, player);

        sync(player);
        strike(player);
    }

    private static void repay(ServerPlayer player, boolean full) {
        BigInteger owed = CraftorioMisc.getLoanOwed(player);
        BigInteger points = CraftorioMisc.getPoints(player);
        if (owed.signum() <= 0 || points.signum() <= 0) return;
        if (full && points.compareTo(owed) < 0) return;

        BigInteger payment = points.min(owed);
        CraftorioMisc.setPoints(points.subtract(payment), player);

        BigInteger remaining = owed.subtract(payment);
        if (remaining.signum() <= 0) {
            clearLoan(player);
        } else {
            CraftorioMisc.setLoanOwed(remaining, player);
        }
        sync(player);
    }

    private static void clearLoan(Player player) {
        CraftorioMisc.setLoanOwed(BigInteger.ZERO, player);
        CraftorioMisc.setLoanBorrowed(BigInteger.ZERO, player);
        CraftorioMisc.setLoanSacrificed(false, player);
        CraftorioMisc.removeEffectsIf(player, CraftorioEffects::isLoanMarked);
    }

    public static void sync(ServerPlayer player) {
        ServerEvents.syncUniversalState(player);
        if (CraftorioMisc.universalBased(CraftorioMisc.universalLevel(player)) && player.getServer() != null) {
            for (ServerPlayer online : player.getServer().getPlayerList().getPlayers()) {
                ServerEvents.syncUniversalState(online);
            }
        }
    }

    private static void strike(ServerPlayer player) {
        List<CraftorioEffects> effects = CraftorioLoanEffects.pick(player.level().registryAccess(), player.getRandom(), 1);
        if (effects.isEmpty()) return;

        CraftorioMisc.grantEffect(player, effects.get(0), false, true);
    }

    public static void onSacrifice(ServerPlayer player) {
        boolean loan = hasLoan(player);
        if (loan != CraftorioMisc.isLoanSacrificed(player)) {
            CraftorioMisc.setLoanSacrificed(loan, player);
            sync(player);
        }
    }

    private static void say(ServerPlayer player, String key, Object... args) {
        player.sendSystemMessage(Component.translatable("misc.craftorio.loan_shark_name").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD)
                .append(Component.literal(": ").withStyle(ChatFormatting.DARK_RED))
                .append(Component.translatable(key, args).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)));
    }
}
