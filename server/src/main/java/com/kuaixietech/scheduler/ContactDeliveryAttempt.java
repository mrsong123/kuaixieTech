package com.kuaixietech.scheduler;

/** Signals a persisted failed/unconfirmed claim without placing its contents in the exception message. */
public final class ContactDeliveryAttempt extends IllegalStateException {
    public ContactDeliveryAttempt(String code, Throwable cause) { super(code,cause); }
}
