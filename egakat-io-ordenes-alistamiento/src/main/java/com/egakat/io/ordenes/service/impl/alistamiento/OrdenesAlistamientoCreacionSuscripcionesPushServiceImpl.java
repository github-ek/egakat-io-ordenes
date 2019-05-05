package com.egakat.io.ordenes.service.impl.alistamiento;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.egakat.core.web.client.components.RestClient;
import com.egakat.core.web.client.properties.RestProperties;
import com.egakat.integration.dto.ActualizacionDto;
import com.egakat.integration.dto.ErrorIntegracionDto;
import com.egakat.integration.enums.EstadoIntegracionType;
import com.egakat.integration.service.impl.rest.RestPushServiceImpl;
import com.egakat.integration.suscripciones.dto.SuscripcionDto;
import com.egakat.io.ordenes.service.api.alistamiento.OrdenesAlistamientoCreacionSuscripcionesPushService;
import com.egakat.wms.ordenes.client.components.WmsOrdenesRestClient;
import com.egakat.wms.ordenes.client.properties.WmsOrdenesRestProperties;
import com.egakat.wms.ordenes.constants.IntegracionesConstants;
import com.egakat.wms.ordenes.constants.OrdenesAlistamientoEstadoConstants;
import com.egakat.wms.ordenes.constants.RestConstants;

import lombok.val;

@Service
public class OrdenesAlistamientoCreacionSuscripcionesPushServiceImpl
		extends RestPushServiceImpl<Object, SuscripcionDto, Object>
		implements OrdenesAlistamientoCreacionSuscripcionesPushService {

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
	protected String getApiEndPoint() {
		return RestConstants.SUSCRIPCIONES_ORDENES_ALISTAMIENTO;
	}

	@Override
	protected String getIntegracion() {
		return IntegracionesConstants.ORDENES_DE_ALISTAMIENTO;
	}

	@Override
	protected String getOperacion() {
		val result = String.format("PUSH CREACION SUSCRIPCION %s", getIntegracion());
		return result;
	}

	@Override
	protected List<ActualizacionDto> getPendientes() {
		val estado = EstadoIntegracionType.NO_PROCESADO;
		val subestado = "";

		val result = getActualizacionesService()
				.findAllByIntegracionAndEstadoIntegracionAndSubEstadoIntegracionIn(getIntegracion(), estado, subestado);
		return result;
	}

	@Override
	protected Object getInput(ActualizacionDto actualizacion, List<ErrorIntegracionDto> errores) {
		return "";
	}

	@Override
	protected SuscripcionDto asOutput(Object input, ActualizacionDto actualizacion, List<ErrorIntegracionDto> errores) {
		val result = new SuscripcionDto();

		result.setSuscripcion(getIntegracion());
		result.setIdExterno(actualizacion.getIdExterno());
		result.setEstadoSuscripcion(OrdenesAlistamientoEstadoConstants.CONFIRMAR_CREACION);
		result.setArg0(actualizacion.getArg0());
		result.setArg1(actualizacion.getArg1());
		result.setArg2(actualizacion.getArg2());
		result.setArg3(actualizacion.getArg3());
		result.setArg4(actualizacion.getArg4());

		return result;
	}

	@Override
	protected Object push(SuscripcionDto output, Object input, ActualizacionDto actualizacion,
			List<ErrorIntegracionDto> errores) {
		val url = getUrl();

		getRestClient().post(url, output, Object.class);
		return "";
	}

	@Override
	protected void onSuccess(Object result, SuscripcionDto output, Object input, ActualizacionDto actualizacion) {
		actualizacion.setSubEstadoIntegracion(OrdenesAlistamientoEstadoConstants.CONFIRMAR_CREACION);
		actualizacion.setReintentos(0);
	}

	@Override
	protected void updateOnSuccess(Object result, SuscripcionDto output, Object input, ActualizacionDto actualizacion) {
		getActualizacionesService().update(actualizacion);
	}

	@Override
	protected void onError(ActualizacionDto actualizacion, List<ErrorIntegracionDto> errores) {
		actualizacion.setSubEstadoIntegracion(OrdenesAlistamientoEstadoConstants.ERROR_CREANDO_SUSCRIPCION);
	}
}