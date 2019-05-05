package com.egakat.io.ordenes.tasks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.egakat.io.ordenes.service.api.alistamiento.OrdenesAlistamientoBdiIntegrationService;

@Component
public class Task {

	@Autowired
	private OrdenesAlistamientoBdiIntegrationService ordenesAlistamientoService;
	
	@Scheduled(cron = "${cron.ordenes}")
	public void run() {
		ordenesAlistamientoService.run();
	}
}
