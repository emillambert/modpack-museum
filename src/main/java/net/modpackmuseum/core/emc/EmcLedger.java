package net.modpackmuseum.core.emc;

/**
 * Minimal, transactional-style EMC balance primitive.
 *
 * <p>This is not yet connected to players. It establishes the no-negative-balance and no-overflow
 * rules before the Transmutation Table exists.</p>
 */
public final class EmcLedger {
    private long balance;

    public EmcLedger() {
        this(0);
    }

    public EmcLedger(long initialBalance) {
        if (initialBalance < 0) {
            throw new IllegalArgumentException("initialBalance must be >= 0");
        }
        this.balance = initialBalance;
    }

    public long balance() {
        return balance;
    }

    public void credit(long amount) {
        requirePositiveOrZero(amount);
        balance = Math.addExact(balance, amount);
    }

    /** Deducts atomically if sufficient funds exist. */
    public boolean tryDebit(long amount) {
        requirePositiveOrZero(amount);
        if (amount > balance) {
            return false;
        }
        balance -= amount;
        return true;
    }

    private static void requirePositiveOrZero(long amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("amount must be >= 0");
        }
    }
}
