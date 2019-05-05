package com.egakat.io.ordenes.service.impl.alistamiento;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import com.egakat.integration.dto.ActualizacionDto;
import com.egakat.integration.enums.EstadoIntegracionType;
import com.egakat.integration.enums.EstadoNotificacionType;
import com.egakat.integration.service.impl.jdbc.JdbcPullServiceImpl;
import com.egakat.io.ordenes.service.api.alistamiento.OrdenesAlistamientoCreacionSuscripcionesPullService;
import com.egakat.wms.ordenes.constants.IntegracionesConstants;

import lombok.val;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class OrdenesAlistamientoCreacionSuscripcionesPullServiceImpl extends JdbcPullServiceImpl<Map<String, Object>>
		implements OrdenesAlistamientoCreacionSuscripcionesPullService {

	@Autowired
	private NamedParameterJdbcTemplate jdbcTemplate;

	@Override
	protected NamedParameterJdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	@Override
	protected String getIntegracion() {
		return IntegracionesConstants.ORDENES_DE_ALISTAMIENTO;
	}

	@Override
	protected String getSql() {
		val result = "SELECT * FROM dbo.OrdenesAlistamientoMensajesNuevos()";
		return result;
	}

	@Override
	public void pull() {
		val operacion = getOperacion();
		val correlacion = defaultCorrelacion();
		val sql = getSql();
		int total = 0;
		String format = "integracion={}, operación={}, sql={}";

		log.debug(format, getIntegracion(), operacion, sql);
		try {
			val paramMap = new HashMap<String, Object>();
			val inputs = getJdbcTemplate().queryForList(sql, paramMap);
			total = inputs.size();

			enqueue(correlacion, inputs);
		} catch (RuntimeException e) {
			boolean ignorar = isRetryableException(e);
			getErroresService().create(getIntegracion(), correlacion, "", ignorar, e);
			log.error("Exception:", e);
		}

		format = "integracion={}, operación={}, sql={}, total={}";
		log.debug(format, getIntegracion(), operacion, sql, total);
	}

	@Override
	protected ActualizacionDto asModel(String correlacion, Map<String, Object> input) {
		val arg0 = (String) input.get("wh_id");
		val arg1 = (String) input.get("client_id");
		val arg2 = (String) input.get("ordnum");
		val arg3 = String.valueOf(input.get("id_mensaje"));
		val arg4 = String.valueOf(input.get("id_orden_alistamiento"));

		val result = new ActualizacionDto();

		result.setIntegracion(getIntegracion());
		result.setCorrelacion(correlacion);
		result.setIdExterno((String) input.get("id_externo"));
		result.setEstadoIntegracion(EstadoIntegracionType.NO_PROCESADO);
		result.setSubEstadoIntegracion("");
		result.setEstadoNotificacion(EstadoNotificacionType.SIN_NOVEDAD);

		result.setArg0(arg0);
		result.setArg1(arg1);
		result.setArg2(arg2);
		result.setArg3(arg3);
		result.setArg4(arg4);

		return result;
	}
}
