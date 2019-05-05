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
import com.egakat.io.ordenes.service.api.alistamiento.OrdenesAlistamientoConfirmacionStageAckPushService;
import com.egakat.wms.ordenes.client.components.WmsOrdenesRestClient;
import com.egakat.wms.ordenes.client.properties.WmsOrdenesRestProperties;
import com.egakat.wms.ordenes.constants.IntegracionesConstants;
import com.egakat.wms.ordenes.constants.OrdenesAlistamientoEstadoConstants;
import com.egakat.wms.ordenes.constants.RestConstants;

import lombok.val;

@Service
public class OrdenesAlistamientoConfirmacionStageAckPushServiceImpl extends RestPushServiceImpl<Object, Object, Object>
		implements OrdenesAlistamientoConfirmacionStageAckPushService {

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
		val result = String.format("PUSH ACK STAGE SUSCRIPCION %s", getIntegracion());
		return result;
	}

	@Override
	protected List<ActualizacionDto> getPendientes() {
		val estado = EstadoIntegracionType.NO_PROCESADO;
		val subestado = OrdenesAlistamientoEstadoConstants.STAGE_CONFIRMADO;

		val result = getActualizacionesService()
				.findAllByIntegracionAndEstadoIntegracionAndSubEstadoIntegracionIn(getIntegracion(), estado, subestado);
		return result;
	}

	@Override
	protected ActualizacionDto getInput(ActualizacionDto actualizacion, List<ErrorIntegracionDto> errores) {
		return actualizacion;
	}

	@Override
	protected Object asOutput(Object input, ActualizacionDto actualizacion, List<ErrorIntegracionDto> errores) {
		return "";
	}

	@Override
	protected Object push(Object output, Object input, ActualizacionDto actualizacion,
			List<ErrorIntegracionDto> errores) {
		val url = getUrl();
		val query = RestConstants.SUSCRIPCIONES_ORDENES_ALISTAMIENTO_EN_STAGE_ACK;
		val id = actualizacion.getIdExterno();

		getRestClient().put(url + query, output, Object.class, id);
		return "";
	}

	@Override
	protected void onSuccess(Object result, Object output, Object input, ActualizacionDto actualizacion) {
		val subestado = OrdenesAlistamientoEstadoConstants.DESGARGAR_LINEAS_ALISTADAS;

		actualizacion.setSubEstadoIntegracion(subestado);
		actualizacion.setReintentos(0);
	}

	@Override
	protected void updateOnSuccess(Object result, Object output, Object input, ActualizacionDto actualizacion) {
		getActualizacionesService().update(actualizacion);
	}

	@Override
	protected void onError(ActualizacionDto actualizacion, List<ErrorIntegracionDto> errores) {
		val subestado = OrdenesAlistamientoEstadoConstants.ERROR_NOTIFICANDO_ACK_STAGE;

		actualizacion.setSubEstadoIntegracion(subestado);
		actualizacion.setReintentos(0);
	}
}