package com.gpp.bankmanagement.repository;

import com.gpp.bankmanagement.entity.TransactionHistoryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionHistoryRepository extends JpaRepository<TransactionHistoryEntity, String> {

    Page<TransactionHistoryEntity> findByAccountIdOrderByTimestampAsc(String accountId, Pageable pageable);

    long countByAccountId(String accountId);
}