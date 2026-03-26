package com.gpp.bankmanagement.repository;

import com.gpp.bankmanagement.entity.AccountSummaryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountSummaryRepository extends JpaRepository<AccountSummaryEntity, String> {
}