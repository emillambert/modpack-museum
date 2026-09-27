package net.modpackmuseum.core.emc;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmcLedgerTest {
    @Test
    void neverAllowsNegativeBalance() {
        EmcLedger ledger = new EmcLedger();
        ledger.credit(64);
        assertFalse(ledger.tryDebit(65));
        assertEquals(64, ledger.balance());
        assertTrue(ledger.tryDebit(64));
        assertEquals(0, ledger.balance());
    }

    @Test
    void rejectsNegativeAndOverflowingCredits() {
        EmcLedger ledger = new EmcLedger(Long.MAX_VALUE);
        assertThrows(IllegalArgumentException.class, () -> ledger.credit(-1));
        assertThrows(ArithmeticException.class, () -> ledger.credit(1));
    }
}
