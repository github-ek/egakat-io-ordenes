package com.egakat.io.ordenes.tasks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.egakat.io.ordenes.service.api.IngredionRemesasPushService;

@Component
public class Task {

	@Autowired
	private IngredionRemesasPushService service;
	
	@Scheduled(cron = "${cron-remesas}")
	public void run() {
		service.push();
	}
}
