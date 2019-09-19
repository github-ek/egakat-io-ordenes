package com.egakat.io.ordenes.service.impl;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;

import com.egakat.core.web.client.components.RestClient;
import com.egakat.core.web.client.properties.RestProperties;
import com.egakat.integration.dto.ActualizacionDto;
import com.egakat.integration.dto.ErrorIntegracionDto;
import com.egakat.integration.enums.EstadoIntegracionType;
import com.egakat.integration.enums.EstadoNotificacionType;
import com.egakat.integration.service.impl.rest.RestPushServiceImpl;
import com.egakat.io.ordenes.components.SilogtranRestClient;
import com.egakat.io.ordenes.components.SilogtranRestProperties;
import com.egakat.io.ordenes.components.SilogtranTokenGenerator;
import com.egakat.io.ordenes.constants.IntegracionesConstants;
import com.egakat.io.ordenes.constants.SilogtranRestConstants;
import com.egakat.io.ordenes.dto.RemesaDto;
import com.egakat.io.ordenes.dto.RemesaItemDto;
import com.egakat.io.ordenes.service.api.IngredionRemesasPushService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.val;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class IngredionRemesasPushServiceImpl extends RestPushServiceImpl<RemesaDto, RemesaDto, String>
		implements IngredionRemesasPushService {

	// @formatter:off
	private static final String SQL_MENSAJE = "" 
			+ "SELECT \r\n" 
			+ "	   a.* \r\n"
			+ "FROM dbo.mensajes_remesas_silogtran_ingredion a \r\n"
			+ "WHERE\r\n"
			+ "	   a.integracion = :integracion\r\n" 
			+ "AND a.correlacion = :correlacion\r\n"
			+ "AND a.id_externo = :id_externo\r\n";
	// @formatter:on

	// @formatter:off
	private static final String SQL_MENSAJE_LINEAS = "" 
			+ "SELECT \r\n" 
			+ "		a.* \r\n"
			+ "FROM dbo.mensajes_remesas_silogtran_ingredion_lineas a\r\n" 
			+ "WHERE\r\n" 
			+ "		a.id_mensaje = :id\r\n";
	// @formatter:on

	private static final String EXPIRED_TOKEN = "expired token";

	private static final String FIELD_MSG = "msg";

	@Autowired
	private SilogtranRestProperties properties;

	@Autowired
	private SilogtranRestClient restClient;

	@Autowired
	private NamedParameterJdbcTemplate jdbcTemplate;

	private ObjectMapper mapper;

	@Value("${com.silogtran.rest.api-secret-value}")
	private String apiSecretValue;

	@Autowired
	private SilogtranTokenGenerator tokenGenerator;

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
		return SilogtranRestConstants.postRemesas;
	}

	@Override
	protected String getIntegracion() {
		return IntegracionesConstants.ORDENES_TRANSPORTE_INGREDION;
	}

	@Override
	protected void init(List<ActualizacionDto> actualizaciones) {
		super.init(actualizaciones);
		this.mapper = new ObjectMapper();
	}

	@Override
	protected List<ActualizacionDto> getPendientes() {
		val estado = EstadoIntegracionType.VALIDADO;
		val subestado = "MENSAJE_CREADO";

		val result = getActualizacionesService()
				.findAllByIntegracionAndEstadoIntegracionAndSubEstadoIntegracionIn(getIntegracion(), estado, subestado);
		return result;
	}

	@Override
	protected RemesaDto getInput(ActualizacionDto actualizacion, List<ErrorIntegracionDto> errores) {
		MapSqlParameterSource parameters = new MapSqlParameterSource();
		parameters.addValue("integracion", actualizacion.getIntegracion());
		parameters.addValue("correlacion", actualizacion.getCorrelacion());
		parameters.addValue("id_externo", actualizacion.getIdExterno());

		val result = jdbcTemplate.queryForObject(SQL_MENSAJE, parameters, getRowMapper());
		return result;
	}

	private RowMapper<RemesaDto> getRowMapper() {
		return new RowMapper<RemesaDto>() {
			@Override
			public RemesaDto mapRow(ResultSet rs, int i) throws SQLException {
				val result = new RemesaDto();

				result.setId(rs.getLong("id_mensaje"));
				result.setIdOrden(rs.getLong("id_orden_transporte"));

				result.setCentroCosto(rs.getString("centro_costo"));
				result.setTipoRemesa(rs.getString("tipo_remesa"));
				result.setCodigoCliente(rs.getString("codigo_cliente"));
				result.setDivisionCliente(rs.getString("division_cliente"));

				result.setNombreRemitente(rs.getString("nombre_remitente"));
				result.setTipoDocumentoRemitente(rs.getString("tipo_documento_remitente"));
				result.setDocumentoRemitente(rs.getString("documento_remitente"));
				result.setDireccionRemitente(rs.getString("direccion_remitente"));
				result.setTelefonoRemitente(rs.getString("telefono_remitente"));
				result.setContactoRemitente(rs.getString("contacto_remitente"));
				result.setCiudadRemitente(rs.getString("ciudad_remitente"));
				result.setDepartamentoRemitente(rs.getString("departamento_remitente"));

				result.setNombreDestinatario(rs.getString("nombre_destinatario"));
				result.setTipoDocumentoDestinatario(rs.getString("tipo_documento_destinatario"));
				result.setDocumentoDestinatario("899999239");
				result.setDireccionDestinatario(rs.getString("direccion_destinatario"));
				result.setTelefonoDestinatario(rs.getString("telefono_destinatario"));
				result.setContactoDestinatario1(rs.getString("contacto_destinatario1"));
				result.setContactoDestinatario2(rs.getString("contacto_destinatario2"));
				result.setCiudadDestinatario(rs.getString("ciudad_destinatario"));
				result.setDepartamentoDestinatario(rs.getString("departamento_destinatario"));

				result.setZonaCiudadDestinatario(rs.getString("zona_ciudad_destinatario"));
				result.setCoordenadaXLongitud(rs.getString("coordenada_x_longitud"));
				result.setCoordenadaYLatitud(rs.getString("coordenada_y_latitud"));

				result.setObservacionRemesa(rs.getString("observacion_remesa"));
				result.setFechaCompromisoMinima(rs.getString("fecha_compromiso_minima"));
				result.setFechaCompromisoMaxima(rs.getString("fecha_compromiso_maxima"));
				result.setHoraCompromisoMinima(rs.getString("hora_compromiso_minima"));
				result.setHoraCompromisoMaxima(rs.getString("hora_compromiso_maxima"));

				result.setPlacaVehiculo(rs.getString("placa_vehiculo"));
				result.setSecuenciaEntrega(rs.getInt("secuencia_entrega"));

				result.setTipoRemision(rs.getString("tipo_remision"));
				result.setRemision(rs.getString("remision"));
				result.setDocumentoWms(rs.getString("documento_wms"));
				result.setDocumentoNumeroSolicitud(rs.getString("documento_numero_solicitud"));
				result.setIdOrdentransporte(rs.getString("id_ordentransporte"));

				result.setPuntoCodigoAlterno(rs.getString("punto_codigo_alterno"));
				result.setRegional(rs.getString("regional"));
				result.setCiudadNombreAlterno(rs.getString("ciudad_nombre_alterno"));
				result.setBodegaCodigoAlterno(rs.getString("bodega_codigo_alterno"));
				result.setPrograma(rs.getString("programa"));
				result.setPlanta(rs.getString("planta"));
				result.setPeriodoActa(rs.getString("periodo_acta"));

				result.setItems(getLineasByIdMensaje(result.getId()));

				result.getItems().forEach(a -> {
					a.setPredistribucion(result.getPeriodoActa());
				});

				return result;
			}

			private List<RemesaItemDto> getLineasByIdMensaje(Long id) {
				MapSqlParameterSource parameters = new MapSqlParameterSource();
				parameters.addValue("id", id);

				val result = jdbcTemplate.query(SQL_MENSAJE_LINEAS, parameters, getLineasRowMapper());
				return result;
			}

			private RowMapper<RemesaItemDto> getLineasRowMapper() {
				return new RowMapper<RemesaItemDto>() {
					@Override
					public RemesaItemDto mapRow(ResultSet rs, int i) throws SQLException {
						val result = new RemesaItemDto();

						result.setId(rs.getLong("id"));
						result.setLinea(rs.getInt("numero_linea"));
						result.setSublinea(rs.getInt("numero_sublinea"));

						result.setIdProductoEconnect(rs.getString("id_producto_econnect"));
						result.setCodigoProducto(rs.getString("codigo_producto"));
						result.setProductoNombre(rs.getString("producto_nombre"));

						result.setProductoCodigoMinisterio(rs.getString("producto_codigoministerio"));
						result.setNaturalezaCarga(rs.getString("naturaleza_carga"));
						result.setTipoProducto(rs.getString("tipo_producto"));

						result.setCodigoEmpaque(rs.getString("codigo_empaque"));
						result.setPeso(rs.getString("peso"));
						result.setPesoBruto(rs.getString("peso_bruto"));
						result.setCantidad(rs.getString("cantidad"));
						result.setVolumen(rs.getString("volumen"));

						result.setValorDeclarado(rs.getString("valor_declarado"));
						result.setDescripcionDetalleRemesa(rs.getString("descripcion_detalle_remesa"));
						result.setPredistribucion(rs.getString("predistribucion"));

						result.setFactorConversion(rs.getString("factor_conversion"));
						result.setCantidadRecibida(rs.getString("cantidad_recibida"));

						result.setLote(rs.getString("lote"));
						result.setEstadoInventarioNombre(rs.getString("estado_inventario_nombre"));
						result.setFechaVencimiento(rs.getString("fecha_vencimiento"));

						return result;
					}
				};
			}
		};
	}

	@Override
	protected RemesaDto asOutput(RemesaDto input, ActualizacionDto actualizacion, List<ErrorIntegracionDto> errores) {
		return input;
	}

	@Override
	protected String push(RemesaDto output, RemesaDto input, ActualizacionDto actualizacion,
			List<ErrorIntegracionDto> errores) {
		try {
			String result = null;
			val url = getUrl();

			ObjectNode body = asBodyRequest(output);

			log.debug("body={}", body);
			val response = getRestClient().post(url, body, String.class);

			result = readResponse(actualizacion, response.getBody(), errores);

			return result;
		} catch (HttpStatusCodeException e) {
			if (cacheEvict(e)) {
				tokenGenerator.cacheEvict();
			}
			throw e;
		}
	}

	private ObjectNode asBodyRequest(RemesaDto output) {
		ObjectNode body;
		body = this.mapper.createObjectNode();
		JsonNode remesa = this.mapper.convertValue(output, JsonNode.class);

		body.put("secret", apiSecretValue);
		body.putArray("remesas").add(remesa);
		return body;
	}

	private String readResponse(ActualizacionDto actualizacion, String response, List<ErrorIntegracionDto> errores) {
		String result = "";

		try {
			JsonNode node = this.mapper.readTree(response);

			val remesas = node.get("remesas");

			if (remesas != null) {
				result = node.get("numero_confirmacion").asText();

				readMessages(actualizacion, remesas, errores);
			} else {
				errorRespuestaNoEsperada(actualizacion, node, response, errores);
			}
		} catch (IOException e) {
			val error = getErroresService().error(actualizacion, "", e);
			errores.add(error);
		}

		return result;
	}

	private void readMessages(ActualizacionDto actualizacion, JsonNode remesas, List<ErrorIntegracionDto> errores) {
		val remesa = remesas.get(0);
		val success = remesa.get("success").asBoolean();
		if (!success) {
			remesa.get("msg").forEach(errorMapper(actualizacion, errores));
		}
	}

	private void errorRespuestaNoEsperada(ActualizacionDto actualizacion, JsonNode node, String response,
			List<ErrorIntegracionDto> errores) {
		val msg = node.get("msg");
		String codigo;
		String mensaje;

		if (msg != null) {
			codigo = "";
			mensaje = msg.asText();
		} else {
			codigo = "Respuesta no valida";
			mensaje = response;
		}

		val error = getErroresService().error(actualizacion, codigo, mensaje);
		errores.add(error);
	}

	private Consumer<JsonNode> errorMapper(ActualizacionDto actualizacion, List<ErrorIntegracionDto> errores) {
		return a -> {
			val codigo = StringUtils.defaultString(a.get("codigo").asText());
			val mensaje = StringUtils.defaultString(a.get("mensaje").asText());

			val error = getErroresService().error(actualizacion, codigo, mensaje);
			errores.add(error);
		};
	}

	private boolean cacheEvict(HttpStatusCodeException e) {
		boolean result = false;

		if (e.getStatusCode().equals(HttpStatus.UNAUTHORIZED)) {
			result = true;
		} else {
			if (e.getStatusCode().equals(HttpStatus.INTERNAL_SERVER_ERROR)) {
				try {
					val body = e.getResponseBodyAsString();
					val node = this.mapper.readTree(body).findValue(FIELD_MSG);

					if (node != null) {
						val msg = node.asText();
						if (EXPIRED_TOKEN.equalsIgnoreCase(msg)) {
							result = true;
						}
					}
				} catch (IOException e1) {
					;
				}
			}
		}

		return result;
	}

	@Override
	protected void onSuccess(String result, RemesaDto output, RemesaDto input, ActualizacionDto actualizacion) {
		val estado = EstadoIntegracionType.CARGADO;
		val subestado = "MENSAJE_ENVIADO";

		actualizacion.setEstadoIntegracion(estado);
		actualizacion.setSubEstadoIntegracion(subestado);
		actualizacion.setReintentos(0);
		actualizacion.setDatos(result);
	}

	@Override
	protected void updateOnSuccess(String result, RemesaDto output, RemesaDto input, ActualizacionDto actualizacion) {
		getActualizacionesService().update(actualizacion);
	}

	@Override
	protected void onError(ActualizacionDto actualizacion, List<ErrorIntegracionDto> errores) {
		val subestado = "ERROR_ENVIO";
		val estadoNotificacion = EstadoNotificacionType.ERROR;
		
		actualizacion.setSubEstadoIntegracion(subestado);
		actualizacion.setEstadoNotificacion(estadoNotificacion);
		actualizacion.setReintentos(0);
	}
}
