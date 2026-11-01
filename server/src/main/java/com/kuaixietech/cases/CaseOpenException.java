package com.kuaixietech.cases;
/** Must roll back the enclosing financial transaction, not return a partial success. */
public class CaseOpenException extends IllegalStateException {public CaseOpenException(){super("DISPUTE_OPEN");}}
