
package com.egakat.io.ordenes.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RemesaDto  {
	@JsonIgnore
	private Long id;
	@JsonIgnore
	private Long idOrden;
	
	@JsonProperty("centro_costo")
	private String centroCosto;
	@JsonProperty("tipo_remesa")
	private String tipoRemesa;
	@JsonProperty("codigo_cliente")
	private String codigoCliente;
	@JsonProperty("division_cliente")
	private String divisionCliente;
	
	@JsonProperty("nombre_remitente")
	private String nombreRemitente;
	@JsonProperty("tipo_documento_remitente")
	private String tipoDocumentoRemitente;
	@JsonProperty("documento_remitente")
	private String documentoRemitente;
	@JsonProperty("direccion_remitente")
	private String direccionRemitente;
	@JsonProperty("telefono_remitente")
	private String telefonoRemitente;
	@JsonProperty("contacto_remitente")
	private String contactoRemitente;
	@JsonProperty("ciudad_remitente")
	private String ciudadRemitente;
	@JsonProperty("departamento_remitente")
	private String departamentoRemitente;
	
	@JsonProperty("nombre_destinatario")
	private String nombreDestinatario;
	@JsonProperty("tipo_documento_destinatario")
	private String tipoDocumentoDestinatario;
	@JsonProperty("documento_destinatario")
	private String documentoDestinatario;
	@JsonProperty("direccion_destinatario")
	private String direccionDestinatario;
	@JsonProperty("telefono_destinatario")
	private String telefonoDestinatario;
	@JsonProperty("contacto_destinatario1")
	private String contactoDestinatario1;
	@JsonProperty("contacto_destinatario2")
	private String contactoDestinatario2;
	@JsonProperty("ciudad_destinatario")
	private String ciudadDestinatario;
	@JsonProperty("departamento_destinatario")
	private String departamentoDestinatario;
	
	@JsonProperty("zona_ciudad_destinatario")
	private String zonaCiudadDestinatario;
	@JsonProperty("coordenada_x_longitud")
	private String coordenadaXLongitud;
	@JsonProperty("coordenada_y_latitud")
	private String coordenadaYLatitud;
	
	@JsonProperty("observacion_remesa")
	private String observacionRemesa;
	@JsonProperty("fecha_compromiso_minima")
	private String fechaCompromisoMinima;
	@JsonProperty("fecha_compromiso_maxima")
	private String fechaCompromisoMaxima;
	@JsonProperty("hora_compromiso_minima")
	private String horaCompromisoMinima;
	@JsonProperty("hora_compromiso_maxima")
	private String horaCompromisoMaxima;
	
	@JsonProperty("placa_vehiculo")
	private String placaVehiculo;
	@JsonProperty("secuencia_entrega")
	public Integer secuenciaEntrega;
	
	@JsonProperty("tipo_remision")
	private String tipoRemision;
	@JsonProperty("remision")
	private String remision;
	@JsonProperty("documento_wms")
	private String documentoWms;
	@JsonProperty("documento_numero_solicitud")
	private String documentoNumeroSolicitud;
	@JsonProperty("id_ordentransporte")
	private String idOrdentransporte;
	
	@JsonProperty("punto_codigo_alterno")
	private String puntoCodigoAlterno;
	@JsonProperty("regional")
	private String regional;
	@JsonProperty("ciudad_nombre_alterno")
	private String ciudadNombreAlterno;
	@JsonProperty("bodega_codigo_alterno")
	private String bodegaCodigoAlterno;
	@JsonProperty("programa")
	private String programa;
	@JsonProperty("planta")
	private String planta;
	@JsonProperty("periodo_acta")
	private String periodoActa;
	
	@JsonProperty("items")
	public List<RemesaItemDto> items = null;
}