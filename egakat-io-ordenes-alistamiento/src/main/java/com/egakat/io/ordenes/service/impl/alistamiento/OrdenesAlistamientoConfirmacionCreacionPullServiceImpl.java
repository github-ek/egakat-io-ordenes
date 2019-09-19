package com.egakat.io.ordenes.service.impl.alistamiento;

import java.time.LocalDateTime;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.egakat.core.web.client.components.RestClient;
import com.egakat.core.web.client.properties.RestProperties;
import com.egakat.integration.dto.ActualizacionDto;
import com.egakat.integration.service.impl.rest.RestPullServiceImpl;
import com.egakat.io.ordenes.service.api.alistamiento.OrdenesAlistamientoConfirmacionCreacionPullService;
import com.egakat.wms.ordenes.client.components.WmsOrdenesRestClient;
import com.egakat.wms.ordenes.client.properties.WmsOrdenesRestProperties;
import com.egakat.wms.ordenes.constants.IntegracionesConstants;
import com.egakat.wms.ordenes.constants.OrdenesAlistamientoEstadoConstants;
import com.egakat.wms.ordenes.constants.RestConstants;

import lombok.val;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class OrdenesAlistamientoConfirmacionCreacionPullServiceImpl extends RestPullServiceImpl<String>
		implements OrdenesAlistamientoConfirmacionCreacionPullService {

	// @formatter:off
	private static final String UPDATE_QUERY = "" 
	+ "UPDATE a " 
	+ "SET "
	+ " 	a.estado_orden = 'CREACION_CONFIRMADA', "
	+ "		a.fecha_confirmacion_creacion = :fecha "
	+ "FROM pConnect.dbo.ordenes_alistamiento a " 
	+ "WHERE " 
	+ "	   a.id_orden_alistamiento = :id "
	+ "AND a.estado_orden = 'MENSAJE_ENVIADO' ";
	// @formatter:on

	@Autowired
	private WmsOrdenesRestProperties properties;

	@Autowired
	private WmsOrdenesRestClient restClient;

	@Autowired
	protected NamedParameterJdbcTemplate jdbcTemplate;

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
		return RestConstants.SUSCRIPCIONES_ORDENES_ALISTAMIENTO_CREADAS;
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
			case OrdenesAlistamientoEstadoConstants.CONFIRMAR_CREACION:
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
		actualizacion.setSubEstadoIntegracion(OrdenesAlistamientoEstadoConstants.CREACION_CONFIRMADA);
		actualizacion.setReintentos(0);
	}

	@Transactional(readOnly = false)
	@Override
	protected void updateOnSuccess(String input, ActualizacionDto actualizacion) {
		getActualizacionesService().update(actualizacion);

		Long id = Long.parseLong(actualizacion.getArg4());
		ordenAlistamientoConfirmada(id);
	}

	private void ordenAlistamientoConfirmada(Long id) {
		SqlParameterSource namedParameters = new MapSqlParameterSource().addValue("id", id).addValue("fecha",
				LocalDateTime.now());
		jdbcTemplate.update(UPDATE_QUERY, namedParameters);
	}
}
