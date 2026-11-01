package com.kuaixietech.insurance;
/** Stops the enclosing trade transaction before any historical premium is collected or released. */
public class InsurancePremiumReconciliationRequired extends IllegalStateException {
 public InsurancePremiumReconciliationRequired(){super("INSURANCE_PREMIUM_RECONCILIATION_REQUIRED");}
}
