package com.thiru.investment_tracker.core.service;
//
//import static com.thiru.investment_tracker.config.RabbitMQConfig.QUEUE_NAME;
//import static com.thiru.investment_tracker.config.RabbitMQConfig.ROUTING_KEY;
//import static com.thiru.investment_tracker.config.RabbitMQConfig.TOPIC_EXCHANGE_NAME;
//
//import org.springframework.amqp.rabbit.annotation.RabbitListener;
//import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Slf4j
public class QueueService {

//	private final RabbitTemplate rabbitTemplate;
//
//	public void seed(Object message) {
//		rabbitTemplate.convertAndSend(TOPIC_EXCHANGE_NAME, ROUTING_KEY, message);
//		log.info("Sent message: {}", message);
//	}
//
//	@RabbitListener(queues = QUEUE_NAME)
//	public void receiveMessage(String message) {
//		// Handle the received message here
//		log.info("Received message: {}", message);
//	}

}
