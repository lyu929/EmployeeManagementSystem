package io.github.lyu929.ems.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Money arithmetic: BigDecimal with two decimals and banker's rounding (never double). */
public final class Money {

    public static final int SCALE = 2;
    public static final RoundingMode ROUNDING = RoundingMode.HALF_EVEN;
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private Money() {}

    public static BigDecimal of(String amount) {
        return new BigDecimal(amount).setScale(SCALE, ROUNDING);
    }

    public static BigDecimal round(BigDecimal amount) {
        return amount.setScale(SCALE, ROUNDING);
    }

    /** {@code amount * (1 + percent / 100)}, e.g. a 3.5 % raise. */
    public static BigDecimal applyPercent(BigDecimal amount, BigDecimal percent) {
        return amount.multiply(HUNDRED.add(percent)).divide(HUNDRED, SCALE, ROUNDING);
    }

    public static BigDecimal times(BigDecimal amount, BigDecimal rate) {
        return amount.multiply(rate).setScale(SCALE, ROUNDING);
    }
}
