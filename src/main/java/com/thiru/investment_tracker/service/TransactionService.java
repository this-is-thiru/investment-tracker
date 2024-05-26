package com.thiru.investment_tracker.service;

import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.thiru.investment_tracker.common.CommonUtil;
import com.thiru.investment_tracker.dto.AssetRequest;
import com.thiru.investment_tracker.entity.Transaction;
import com.thiru.investment_tracker.repository.TransactionRepository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
@Service
@Transactional
public class TransactionService {

	private final TransactionRepository transactionRepository;

	public void addTransaction(AssetRequest assetRequest) {

		if (assetRequest.getTransactionDate() == null) {
			assetRequest.setTransactionDate(new Date());
		}
		Transaction transaction = CommonUtil.copy(assetRequest, Transaction.class);
		transaction.setTotalValue(transaction.getPrice() * transaction.getQuantity());

		log.info("Transaction: {}, added successfully", transaction.getTransactionType());
		transactionRepository.save(transaction);
	}

	public List<Transaction> allTransactions() {
		return transactionRepository.findAll();
	}

}
