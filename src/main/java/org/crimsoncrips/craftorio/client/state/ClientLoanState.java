package org.crimsoncrips.craftorio.client.state;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.math.BigInteger;

@OnlyIn(Dist.CLIENT)
public class ClientLoanState {

    private static boolean available = false;
    private static BigInteger owed = BigInteger.ZERO;
    private static BigInteger borrowed = BigInteger.ZERO;
    private static boolean sacrificed = false;
    private static double interestPercent = 20.0;

    private ClientLoanState() {}

    public static void update(BigInteger owed, BigInteger borrowed, boolean sacrificed, double interestPercent) {
        available = true;
        ClientLoanState.interestPercent = interestPercent;
        ClientLoanState.owed = owed;
        ClientLoanState.borrowed = borrowed;
        ClientLoanState.sacrificed = sacrificed;
    }

    public static void clear() {
        available = false;
    }

    public static boolean isAvailable() {
        return available;
    }

    public static BigInteger getOwed() {
        return owed;
    }

    public static BigInteger getBorrowed() {
        return borrowed;
    }

    public static boolean isSacrificed() {
        return sacrificed;
    }

    public static double getInterestPercent() {
        return interestPercent;
    }
}
