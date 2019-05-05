package com.egakat.io.ordenes.service.impl.alistamiento;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.egakat.core.web.client.components.RestClient;
import com.egakat.core.web.client.properties.RestProperties;
import com.egakat.integration.dto.ActualizacionDto;
import com.egakat.integration.dto.ErrorIntegracionDto;
import com.egakat.integration.enums.EstadoIntegracionType;
import com.egakat.integration.service.impl.rest.RestIntegracionEntityDownloadServiceImpl;
import com.egakat.io.ordenes.dto.OrdenAlistamientoCancelacionDto;
import com.egakat.io.ordenes.dto.OrdenAlistamientoDto;
import com.egakat.io.ordenes.dto.OrdenAlistamientoLineaDto;
import com.egakat.io.ordenes.dto.OrdenAlistamientoLoteDto;
import com.egakat.io.ordenes.service.api.alistamiento.OrdenesAlistamientoDownloadService;
import com.egakat.io.ordenes.service.api.crud.OrdenesAlistamientoCrudService;
import com.egakat.wms.ordenes.client.components.WmsOrdenesRestClient;
import com.egakat.wms.ordenes.client.properties.WmsOrdenesRestProperties;
import com.egakat.wms.ordenes.constants.IntegracionesConstants;
import com.egakat.wms.ordenes.constants.OrdenesAlistamientoEstadoConstants;
import com.egakat.wms.ordenes.constants.RestConstants;
import com.egakat.wms.ordenes.dto.alistamientos.OrdShipmentDto;
import com.egakat.wms.ordenes.dto.alistamientos.OrdShipmentLineCancelacionDto;
import com.egakat.wms.ordenes.dto.alistamientos.OrdShipmentLineDto;
import com.egakat.wms.ordenes.dto.alistamientos.OrdShipmentLineLoteDto;

import lombok.val;

@Service
public class OrdenesAlistamientoDownloadServiceImpl
		extends RestIntegracionEntityDownloadServiceImpl<OrdShipmentDto, OrdenAlistamientoDto, Object>
		implements OrdenesAlistamientoDownloadService {

	@Autowired
	private OrdenesAlistamientoCrudService crudService;

	@Autowired
	private WmsOrdenesRestProperties properties;

	@Autowired
	private WmsOrdenesRestClient restClient;

	@Override
	protected OrdenesAlistamientoCrudService getCrudService() {
		return crudService;
	}

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
		return RestConstants.SUSCRIPCIONES_ORDENES_ALISTAMIENTO_BY_PK;
	}

	@Override
	protected List<ActualizacionDto> getPendientes() {
		val estado = EstadoIntegracionType.NO_PROCESADO;
		val subestado = OrdenesAlistamientoEstadoConstants.DESGARGAR_LINEAS_ALISTADAS;

		val result = getActualizacionesService()
				.findAllByIntegracionAndEstadoIntegracionAndSubEstadoIntegracionIn(getIntegracion(), estado, subestado);
		return result;
	}

	@Override
	protected OrdShipmentDto getInput(ActualizacionDto actualizacion, List<ErrorIntegracionDto> errores) {
		val url = getUrl();
		val query = getQuery();
		val client_id = actualizacion.getArg1();
		val wh_id = actualizacion.getArg0();
		val ordnum = actualizacion.getArg2();

		val response = getRestClient().getOneQuery(url, query, OrdShipmentDto.class, client_id, wh_id, ordnum);
		val result = response.getBody();
		return result;
	}

	@Override
	protected OrdenAlistamientoDto asOutput(OrdShipmentDto input, ActualizacionDto actualizacion,
			List<ErrorIntegracionDto> errores) {
		val model = new OrdenAlistamientoDto();

		model.setIntegracion(actualizacion.getIntegracion());
		model.setIdExterno(actualizacion.getIdExterno());
		model.setCorrelacion(actualizacion.getCorrelacion());

		model.setClientId(input.getClientId());
		model.setWhId(input.getWhId());
		model.setOrdnum(input.getOrdnum());
		model.setOrdtyp(input.getOrdtyp());

		model.setLineas(asLineas(input));
		return model;

	}

	private List<OrdenAlistamientoLineaDto> asLineas(OrdShipmentDto input) {
		val lineas = new ArrayList<OrdenAlistamientoLineaDto>();
		int i = 0;
		for (val e : input.getLineas()) {
			val model = asLinea(i++, e);
			lineas.add(model);
		}
		return lineas;
	}

	private OrdenAlistamientoLineaDto asLinea(int numeroLinea, OrdShipmentLineDto input) {
		val model = new OrdenAlistamientoLineaDto();

		model.setOrdlin(input.getOrdlin());
		model.setPrtnum(input.getPrtnum());
		model.setInvsts(input.getInvsts());
		model.setOrdqty(input.getOrdqty());
		model.setStgqty(input.getStgqty());
		model.setShpqty(input.getShpqty());

		model.setCancelaciones(asCancelaciones(input));

		model.setLotes(asLotes(input));

		return model;
	}

	private List<OrdenAlistamientoCancelacionDto> asCancelaciones(OrdShipmentLineDto input) {
		val result = new ArrayList<OrdenAlistamientoCancelacionDto>();
		int i = 0;
		for (val e : input.getCancelaciones()) {
			val model = asCancelacion(i++, e);
			result.add(model);
		}
		return result;
	}

	private OrdenAlistamientoCancelacionDto asCancelacion(int i, OrdShipmentLineCancelacionDto input) {
		val model = new OrdenAlistamientoCancelacionDto();

		model.setPrtnum(input.getPrtnum());
		model.setCancod(input.getCancod());
		model.setLngdsc(input.getLngdsc());
		model.setRemqty(input.getRemqty());
		model.setCanUsrId(input.getCanUsrId());
		model.setCanDte(input.getCanDte());

		return model;
	}

	private List<OrdenAlistamientoLoteDto> asLotes(OrdShipmentLineDto input) {
		val result = new ArrayList<OrdenAlistamientoLoteDto>();
		int i = 0;
		for (val e : input.getLotes()) {
			val model = asLote(i++, e);
			result.add(model);
		}
		return result;
	}

	private OrdenAlistamientoLoteDto asLote(int i, OrdShipmentLineLoteDto input) {
		val model = new OrdenAlistamientoLoteDto();

		model.setPrtnum(input.getPrtnum());
		model.setInvsts(input.getInvsts());
		model.setUntqty(input.getUntqty());
		model.setLotnum(input.getLotnum());
		model.setOrgcod(input.getOrgcod());
		model.setExpireDte(input.getExpireDte());

		return model;
	}

	@Override
	protected Object push(OrdenAlistamientoDto output, OrdShipmentDto input, ActualizacionDto actualizacion,
			List<ErrorIntegracionDto> errores) {
		return null;
	}

	@Override
	protected void onSuccess(Object result, OrdenAlistamientoDto output, OrdShipmentDto input,
			ActualizacionDto actualizacion) {
		val estado = EstadoIntegracionType.ESTRUCTURA_VALIDA;
		val subestado = "";

		actualizacion.setEstadoIntegracion(estado);
		actualizacion.setSubEstadoIntegracion(subestado);
		actualizacion.setReintentos(0);
	}

	@Override
	protected void updateOnSuccess(Object result, OrdenAlistamientoDto output, OrdShipmentDto input,
			ActualizacionDto actualizacion) {
		getCrudService().create(output, actualizacion, actualizacion.getEstadoIntegracion());
	}

	@Override
	protected void onError(ActualizacionDto actualizacion, List<ErrorIntegracionDto> errores) {
		val estado = EstadoIntegracionType.ERROR_ESTRUCTURA;

		actualizacion.setEstadoIntegracion(estado);
	}
}
