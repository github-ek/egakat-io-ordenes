package com.egakat.io.ordenes.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.egakat.integration.dto.IntegracionEntityDto;

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
public class ActaDto extends IntegracionEntityDto {

	public Long getId() {
		return this.idSolicitudActa;
	}

	private String registro;

	private Long rowNumber;
	private Long idSolicitudActa;
	private String estadoSolicitud;
	private Long idBodega;
	private Long idBodegaVirtual;
	private LocalDate fechaMinimaSolicitada;
	private LocalDate fechaMaximaSolicitada;
	private LocalDateTime stgdte;

	
	private String centroCosto;
	private String tipoRemesa;
	private String clienteCodigoAlternoTms;
	private String clienteDivision;
	private String solicitudTipoDocumento;
	private String solicitudNumeroDocumento;
	private String ordenAlistamientoNumeroDocumento;
	private String remisionTipoRemision;
	private String remisionNumeroDocumento;

	private String remitenteTipoIdentificacion;
	private String remitenteIdentificacion;
	private String remitenteNombre;
	private String remitenteDepartamento;
	private String remitenteCiudad;
	private String remitenteDireccion;
	private String remitenteTelefono;
	private String remitenteContacto;

	private String destinatarioTipoIdentificacion;
	private String destinatarioIdentificacion;
	private String destinatarioNombre;
	private String destinatarioDepartamento;
	private String destinatarioCiudad;
	private String destinatarioDireccion;
	private String destinatarioTelefono;
	private String destinatarioContacto;
	private String destinatarioCiudadZona;
	private BigDecimal destinatarioCoordenadaX;
	private BigDecimal destinatarioCoordenadaY;

	private String fechaCompromisoInicial;
	private String fechaCompromisoFinal;
	private String horaCompromisoInicial;
	private String horaCompromisoFinal;

	private String placaVehiculo;
	private Integer secuenciaEntrega;
	private Integer seguro;
	private Integer tarifa;
	private String bodegaCodigoAlterno;
	private String programa;

	private String ciudadCodigoAlterno;
	private String ciudadNombreAlterno;

	private String puntoCodigoAlterno;
	private String puntoNombreAlterno;
	private String regional;
	private String planta;
	private String periodo;
	
	private String responsablePrincipal;
	private String responsableSuplente;
	private String remesaObservacion;

	private Long idProducto;
	private String productoCodigo;
	private String productoNombre;
	private String IdEstadoInventario;
	private String estadoInventarioNombre;
	private String lote;
	private String fechaVencimiento;

	private BigDecimal cantidad;
	private String unidadMedidaCodigoAlternoTms;
	private Integer factorConversion;
	private BigDecimal cantidadEmpaques;
	private String unidadEmpaqueCodigoAlternoTms;
	private BigDecimal pesoEmpaques;
	private BigDecimal pesoBrutoEmpaques;
	private BigDecimal volumenEmpaques;
	private Integer valorDeclarado;
	private String predistribucion;
}
