package com.egakat.io.ordenes.service.impl.alistamiento;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Service;

import com.egakat.econnect.maestros.client.service.api.lookup.LookUpService;
import com.egakat.integration.dto.ActualizacionDto;
import com.egakat.integration.dto.ErrorIntegracionDto;
import com.egakat.integration.enums.EstadoIntegracionType;
import com.egakat.integration.service.impl.MapServiceImpl;
import com.egakat.io.ordenes.dto.OrdenAlistamientoDto;
import com.egakat.io.ordenes.dto.OrdenAlistamientoLineaDto;
import com.egakat.io.ordenes.service.api.alistamiento.OrdenesAlistamientoMapService;
import com.egakat.io.ordenes.service.api.crud.OrdenesAlistamientoCrudService;
import com.egakat.wms.ordenes.constants.IntegracionesConstants;

import lombok.val;

@Service
public class OrdenesAlistamientoMapServiceImpl extends MapServiceImpl<OrdenAlistamientoDto>
		implements OrdenesAlistamientoMapService {

	// @formatter:off
	private static final String TRANSLATE_ORDEN_ID_QUERY = "" 
	+ "SELECT "
	+ "    a.id_orden_alistamiento "
	+ "FROM pConnect.dbo.ordenes_alistamiento a "
	+ "WHERE "
	+ "	   a.id_cliente = :id_cliente "
	+ "AND a.numero_orden = :numero_orden "; 
	// @formatter:on

	@Autowired
	private OrdenesAlistamientoCrudService crudService;

	@Autowired
	private LookUpService lookUpLocalService;

	@Autowired
	protected NamedParameterJdbcTemplate jdbcTemplate;

	@Override
	protected OrdenesAlistamientoCrudService getCrudService() {
		return crudService;
	}

	@Override
	protected String getIntegracion() {
		return IntegracionesConstants.ORDENES_DE_ALISTAMIENTO;
	}

	protected LookUpService getLookUpService() {
		return lookUpLocalService;
	}

	@Override
	protected List<ActualizacionDto> getPendientes() {
		val estado = EstadoIntegracionType.ESTRUCTURA_VALIDA;
		val subestado = "";

		val result = getActualizacionesService()
				.findAllByIntegracionAndEstadoIntegracionAndSubEstadoIntegracionIn(getIntegracion(), estado, subestado);
		return result;
	}

	@Override
	protected void map(OrdenAlistamientoDto input, ActualizacionDto actualizacion, List<ErrorIntegracionDto> errores) {
		translateCliente(input);
		translateBodega(input);
		translateOrdenAlistamiento(input);

		input.getLineas().parallelStream().forEach(linea -> {
			translateNumeroLinea(input, linea);
			translateProducto(input, linea);
			translateEstadoInventario(input, linea);
		});
	}

	private void translateCliente(OrdenAlistamientoDto model) {
		String key = defaultKey(model.getClientId());

		model.setIdCliente(null);
		val id = getLookUpService().findClienteIdByCodigoWms(key);
		model.setIdCliente(id);
	}

	private void translateBodega(OrdenAlistamientoDto model) {
		String key = defaultKey(model.getWhId());

		model.setIdBodega(null);
		val id = getLookUpService().findBodegaIdByCodigo(key);
		model.setIdBodega(id);
	}

	private void translateOrdenAlistamiento(OrdenAlistamientoDto model) {
		model.setIdOrden(null);
		val cliente = model.getIdCliente();
		if (cliente != null) {
			String numero_orden = defaultKey(model.getOrdnum());

			// @formatter:off
			SqlParameterSource namedParameters = new MapSqlParameterSource()
					.addValue("id_cliente", cliente)
					.addValue("numero_orden", numero_orden);
			// @formatter:on

			try {
				val id = jdbcTemplate.queryForObject(TRANSLATE_ORDEN_ID_QUERY, namedParameters, Long.class);
				model.setIdOrden(id);
			} catch (EmptyResultDataAccessException e) {
				;
			}
		}
	}

	private void translateNumeroLinea(OrdenAlistamientoDto model, OrdenAlistamientoLineaDto linea) {
		linea.setNumeroLinea(null);
		try {
			val numeroLinea = Integer.parseInt(linea.getOrdlin());
			linea.setNumeroLinea(numeroLinea);
		} catch (NumberFormatException e) {

		}
	}

	private void translateProducto(OrdenAlistamientoDto model, OrdenAlistamientoLineaDto linea) {
		linea.setIdProducto(null);
		val cliente = model.getIdCliente();
		if (cliente != null) {
			String key = defaultKey(linea.getPrtnum());

			val id = getLookUpService().findProductoIdByClienteIdAndCodigo(cliente.longValue(), key);
			linea.setIdProducto(id);
		}
	}

	private void translateEstadoInventario(OrdenAlistamientoDto model, OrdenAlistamientoLineaDto linea) {
		String key = defaultKey(linea.getInvsts());

		linea.setIdEstadoInventario(null);
		val id = getLookUpService().findEstadoInventarioIdByCodigo(key);
		linea.setIdEstadoInventario(id);
	}

	@Override
	protected void validate(OrdenAlistamientoDto input, ActualizacionDto actualizacion,
			List<ErrorIntegracionDto> errores) {
		errores.clear();

		List<OrdenAlistamientoLineaDto> lineas = input.getLineas();
		if (input.getIdCliente() == null) {
			errores.add(errorAtributoNoHomologado(input, "CLIENT_ID", input.getClientId()));
		}

		if (input.getIdBodega() == null) {
			errores.add(errorAtributoNoHomologado(input, "WH_ID", input.getWhId()));
		}

		if (input.getIdOrden() == null) {
			errores.add(errorAtributoNoHomologado(input, "ORDNUM", input.getWhId()));
		}

		lineas.parallelStream().forEach(linea -> {
			// TODO VALIDAR LINEAS Y SUBLINEAS EXTERNAS UNICAS
			if (linea.getIdProducto() == null) {
				errores.add(errorAtributoNoHomologado(input, "PRTNUM", linea.getPrtnum(), asArg(linea)));
			}
			if (linea.getIdEstadoInventario() == null) {
				errores.add(errorAtributoNoHomologado(input, "INVSTS", linea.getInvsts(), asArg(linea)));
			}
		});

	}

	private String[] asArg(OrdenAlistamientoLineaDto linea, String... args) {
		val result = new String[6 + args.length];
		int i = 0;
		result[i++] = linea.getOrdlin();
		result[i++] = linea.getPrtnum();
		result[i++] = linea.getInvsts();
		result[i++] = String.valueOf(linea.getStgqty());

		for (val a : args) {
			result[i++] = a;
		}

		return result;
	}

	@Override
	protected void onSuccess(OrdenAlistamientoDto input, ActualizacionDto actualizacion) {
		EstadoIntegracionType estado = EstadoIntegracionType.VALIDADO;
		String subestado = "";

		actualizacion.setEstadoIntegracion(estado);
		actualizacion.setSubEstadoIntegracion(subestado);
		actualizacion.setReintentos(0);
	}

	@Override
	protected void updateOnSuccess(OrdenAlistamientoDto input, ActualizacionDto actualizacion) {
		getCrudService().update(input, actualizacion, actualizacion.getEstadoIntegracion());
	}

	@Override
	protected void onError(ActualizacionDto actualizacion, List<ErrorIntegracionDto> errores) {
		val estado = EstadoIntegracionType.ERROR_VALIDACION;

		actualizacion.setEstadoIntegracion(estado);
	}
}