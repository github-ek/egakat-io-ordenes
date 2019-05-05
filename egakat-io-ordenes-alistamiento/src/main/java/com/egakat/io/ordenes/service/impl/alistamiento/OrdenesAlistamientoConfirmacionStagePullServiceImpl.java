package com.egakat.io.ordenes.service.impl.alistamiento;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.egakat.core.web.client.components.RestClient;
import com.egakat.core.web.client.properties.RestProperties;
import com.egakat.integration.dto.ActualizacionDto;
import com.egakat.integration.service.impl.rest.RestPullServiceImpl;
import com.egakat.io.ordenes.service.api.alistamiento.OrdenesAlistamientoConfirmacionStagePullService;
import com.egakat.wms.ordenes.client.components.WmsOrdenesRestClient;
import com.egakat.wms.ordenes.client.properties.WmsOrdenesRestProperties;
import com.egakat.wms.ordenes.constants.IntegracionesConstants;
import com.egakat.wms.ordenes.constants.OrdenesAlistamientoEstadoConstants;
import com.egakat.wms.ordenes.constants.RestConstants;

import lombok.val;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class OrdenesAlistamientoConfirmacionStagePullServiceImpl extends RestPullServiceImpl<String>
		implements OrdenesAlistamientoConfirmacionStagePullService {

	@Autowired
	private WmsOrdenesRestProperties properties;

	@Autowired
	private WmsOrdenesRestClient restClient;

	@Override
	protected RestProperties getProperties() {
		return properties;
	}

	@Override
	protected RestClient getRestClient() {
		return restClient;
	}

	@Override
	protected String getIntegracion() {
		return IntegracionesConstants.ORDENES_DE_ALISTAMIENTO;
	}

	@Override
	protected String getApiEndPoint() {
		return RestConstants.SUSCRIPCIONES_ORDENES_ALISTAMIENTO;
	}

	@Override
	protected String getQuery() {
		return RestConstants.SUSCRIPCIONES_ORDENES_ALISTAMIENTO_EN_STAGE;
	}

	@Override
	public void pull() {
		val operacion = getOperacion();
		val correlacion = defaultCorrelacion();
		val url = getUrl();
		val query = getQuery();
		int total = 0;
		String format = "integracion={}, operación= {} ,url= {}{}";

		log.debug(format, getIntegracion(), operacion, url, query);
		try {
			val response = getRestClient().getAllQuery(url, query, String[].class);
			val inputs = Arrays.asList(response.getBody());

			enqueue(correlacion, inputs);
		} catch (RuntimeException e) {
			boolean ignorar = isRetryableException(e);
			getErroresService().create(getIntegracion(), correlacion, "", ignorar, e);
			log.error("Exception:", e);
		}

		format = "integracion={}, operación= {}: Finalización de la consulta de ordenes de alistamieto creadas en WMS, total={}, url={}{}";
		log.debug(format, getIntegracion(), operacion, total, url, query);
	}

	@Override
	protected ActualizacionDto asModel(String correlacion, String input) {
		ActualizacionDto result = null;
		val optional = getActualizacionesService().findByIntegracionAndIdExterno(getIntegracion(), input);

		if (optional.isPresent()) {
			result = optional.get();
			switch (result.getSubEstadoIntegracion()) {
			case OrdenesAlistamientoEstadoConstants.CONFIRMAR_STAGE:
				break;
			default:
				result = null;
				break;
			}
		}

		return result;
	}

	@Override
	protected boolean shouldBeDiscarded(String input, ActualizacionDto actualizacion) {
		if (actualizacion == null) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	protected void onSuccess(String input, ActualizacionDto actualizacion) {
		actualizacion.setSubEstadoIntegracion(OrdenesAlistamientoEstadoConstants.STAGE_CONFIRMADO);
		actualizacion.setReintentos(0);
	}

	@Override
	protected void updateOnSuccess(String input, ActualizacionDto actualizacion) {
		getActualizacionesService().update(actualizacion);
	}
}
