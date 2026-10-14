package com.tcs.account.exception;

/** F3: se lanza cuando un retiro supera el saldo disponible. */
public class SaldoNoDisponibleException extends RuntimeException {
    public SaldoNoDisponibleException() {
        super("Saldo no disponible");
    }
}
