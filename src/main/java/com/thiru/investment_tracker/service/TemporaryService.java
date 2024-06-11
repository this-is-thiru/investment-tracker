package com.thiru.investment_tracker.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class TemporaryService {

    private final QueueService queueService;

	@Async
	public void producerMethod(String message) {
		// Producer logic
		Object data = message + " ";
		queueService.seed(data);
	}
}
