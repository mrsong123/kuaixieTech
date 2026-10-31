package com.kuaixietech.service.finance;
import java.math.BigDecimal;
public interface WithdrawMigrationService {
 String requestWithdrawal(String authenticatedUserId, BigDecimal amount);
 String reviewWithdrawal(String authenticatedAdminId, String withdrawalId, boolean approve, String note);
 String markWithdrawalPaid(String authenticatedAdminId, String withdrawalId, String note);
}
