package com.mavora.shared.domain;

import java.util.Currency;
import java.util.Objects;

/**
 * Monetary amount stored as integer cents. Never use floating point for money.
 */
public record Money(long amountCents, Currency currency) {

    public Money {
        Objects.requireNonNull(currency, "currency is required");
        if (amountCents < 0) {
            throw new IllegalArgumentException("amount cannot be negative");
        }
    }

    public static Money ofCents(long amountCents, String currencyCode) {
        return new Money(amountCents, Currency.getInstance(currencyCode));
    }

    public static Money euros(long amountCents) {
        return ofCents(amountCents, "EUR");
    }

    public Money plus(Money other) {
        requireSameCurrency(other);
        return new Money(Math.addExact(amountCents, other.amountCents), currency);
    }

    public boolean isZero() {
        return amountCents == 0;
    }

    private void requireSameCurrency(Money other) {
        if (!currency.equals(other.currency)) {
            throw new IllegalArgumentException("currency mismatch: " + currency + " vs " + other.currency);
        }
    }
}
