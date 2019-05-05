package com.egakat.io.ordenes.service.impl.alistamiento;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.egakat.io.ordenes.service.api.alistamiento.OrdenesAlistamientoBdiIntegrationService;
import com.egakat.io.ordenes.service.api.alistamiento.OrdenesAlistamientoConfirmacionCreacionAckPushService;
import com.egakat.io.ordenes.service.api.alistamiento.OrdenesAlistamientoConfirmacionCreacionPullService;
import com.egakat.io.ordenes.service.api.alistamiento.OrdenesAlistamientoConfirmacionStageAckPushService;
import com.egakat.io.ordenes.service.api.alistamiento.OrdenesAlistamientoConfirmacionStagePullService;
import com.egakat.io.ordenes.service.api.alistamiento.OrdenesAlistamientoCreacionSuscripcionesPullService;
import com.egakat.io.ordenes.service.api.alistamiento.OrdenesAlistamientoCreacionSuscripcionesPushService;
import com.egakat.io.ordenes.service.api.alistamiento.OrdenesAlistamientoDownloadService;
import com.egakat.io.ordenes.service.api.alistamiento.OrdenesAlistamientoMapService;
import com.egakat.wms.ordenes.constants.IntegracionesConstants;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class OrdenesAlistamientoBdiIntegrationServiceImpl implements OrdenesAlistamientoBdiIntegrationService {

	@Autowired
	private OrdenesAlistamientoCreacionSuscripcionesPullService creacionSuscripcionesPullService;

	@Autowired
	private OrdenesAlistamientoCreacionSuscripcionesPushService creacionSuscripcionesPushService;

	@Autowired
	private OrdenesAlistamientoConfirmacionCreacionPullService confirmacionCreacionPullService;

	@Autowired
	private OrdenesAlistamientoConfirmacionCreacionAckPushService confirmacionCreacionAckPushService;

	@Autowired
	private OrdenesAlistamientoConfirmacionStagePullService confirmacionStagePullService;

	@Autowired
	private OrdenesAlistamientoConfirmacionStageAckPushService confirmacionStageAckPushService;

	@Autowired
	private OrdenesAlistamientoDownloadService downloadService;

	@Autowired
	private OrdenesAlistamientoMapService mapService;

	@Value("${cron-retries}")
	private Integer retries;

	@Value("${cron-delay-between-retries}")
	private Long delayBetweenRetries;

	@Override
	public String getIntegracion() {
		return IntegracionesConstants.ORDENES_DE_ALISTAMIENTO;
	}

	public void run() {
		crearSuscripcion();
		confirmacionCreacionOrden();
		confirmacionOrdenAlistamientoEnStage();
	}

	private void crearSuscripcion() {
		creacionSuscripcionesPullService.pull();

		for (int i = 0; i < retries; i++) {
			log.debug("INTEGRACION {}: intento {} de {}", getIntegracion(), i + 1, retries);
			boolean success = true;

			success &= creacionSuscripcionesPushService.push();

			if (success) {
				break;
			} else {
				sleep();
			}
		}
	}

	private void confirmacionCreacionOrden() {
		confirmacionCreacionPullService.pull();

		for (int i = 0; i < retries; i++) {
			log.debug("INTEGRACION {}: intento {} de {}", getIntegracion(), i + 1, retries);
			boolean success = true;

			success &= confirmacionCreacionAckPushService.push();

			if (success) {
				break;
			} else {
				sleep();
			}
		}
	}

	private void confirmacionOrdenAlistamientoEnStage() {
		confirmacionStagePullService.pull();

		for (int i = 0; i < retries; i++) {
			log.debug("INTEGRACION {}: intento {} de {}", getIntegracion(), i + 1, retries);
			boolean success = true;

			success &= confirmacionStageAckPushService.push();
			success &= downloadService.push();
			success &= mapService.map();

			if (success) {
				break;
			} else {
				sleep();
			}
		}
	}

	private void sleep() {
		try {
			Thread.sleep(delayBetweenRetries * 1000);
		} catch (InterruptedException e) {
			;
		}
	}
}
