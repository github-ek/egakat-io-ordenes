package com.egakat.io.ordenes.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RemesaItemDto {
	@JsonIgnore
	private Long id;
	@JsonIgnore
	private int linea;
	@JsonIgnore
	private int sublinea;

	@JsonProperty("id_producto_econnect")
	private String idProductoEconnect;
	@JsonProperty("codigo_producto")
	private String codigoProducto;
	@JsonProperty("producto_nombre")
	private String productoNombre;
	
	@JsonProperty("producto_codigoministerio")
	private String productoCodigoMinisterio;
	@JsonProperty("naturaleza_carga")
	private String naturalezaCarga;
	@JsonProperty("tipo_producto")
	private String tipoProducto;
	
	@JsonProperty("codigo_empaque")
	private String codigoEmpaque;
	@JsonProperty("peso")
	private String peso;
	@JsonProperty("peso_bruto")
	private String pesoBruto;
	@JsonProperty("cantidad")
	private String cantidad;
	@JsonProperty("volumen")
	private String volumen;
	
	@JsonProperty("valor_declarado")
	private String valorDeclarado;
	@JsonProperty("descripcion_detalle_remesa")
	private String descripcionDetalleRemesa;
	@JsonProperty("predistribucion")
	private String predistribucion;
	
	@JsonProperty("factor_conversion")
	private String factorConversion;
	@JsonProperty("cantidad_recibida")
	private String cantidadRecibida;
	
	@JsonProperty("lote")
	private String lote;
	@JsonProperty("estado_inventario_nombre")
	private String estadoInventarioNombre;
	@JsonProperty("fecha_vencimiento")
	private String fechaVencimiento;
}