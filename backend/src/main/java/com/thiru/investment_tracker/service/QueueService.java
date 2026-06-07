package com.thiru.investment_tracker.service;
//
//import static com.thiru.investment_tracker.config.RabbitMQConfig.QUEUE_NAME;
//import static com.thiru.investment_tracker.config.RabbitMQConfig.ROUTING_KEY;
//import static com.thiru.investment_tracker.config.RabbitMQConfig.TOPIC_EXCHANGE_NAME;
//
//import org.springframework.amqp.rabbit.annotation.RabbitListener;
//import org.springframework.amqp.rabbit.core.RabbitTemplate;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Log4j2
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
