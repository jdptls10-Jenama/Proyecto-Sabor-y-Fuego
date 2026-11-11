
package com.restaurante.view;

import com.restaurante.model.DetallePedido;
import com.restaurante.model.Pedido;
import com.restaurante.model.Usuario;
import com.restaurante.service.PedidoService;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Route("mis-pedidos")
@PageTitle("Mis pedidos | Sabor & Fuego")
public class MisPedidosView extends VerticalLayout {

    private final PedidoService pedidoService;

    private final NumberFormat formatoMoneda =
            NumberFormat.getCurrencyInstance(new Locale("es", "CO"));

    private final DateTimeFormatter formatoFecha =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final Usuario usuarioActual;

    public MisPedidosView(PedidoService pedidoService) {

        this.pedidoService = pedidoService;

        usuarioActual = (Usuario) VaadinSession.getCurrent()
                .getAttribute("usuarioActual");

        // Comprobar sesión y permisos
        if (usuarioActual == null ||
                !"CLIENTE".equalsIgnoreCase(usuarioActual.getRol())) {

            Notification.show("Debes iniciar sesión como cliente.");
            getUI().ifPresent(ui -> ui.navigate("login"));
            return;
        }

        configurarPagina();
        construirContenido();
    }

    private void construirContenido() {

        H1 titulo = new H1("Mis pedidos");

        titulo.getStyle()
                .set("color", "#D6AD55")
                .set("margin", "0");

        Paragraph descripcion = new Paragraph(
                "Consulta tus compras y revisa el estado de cada pedido."
        );

        descripcion.getStyle()
                .set("color", "#CCCCCC");

        // Consultar únicamente los pedidos del cliente actual
        List<Pedido> pedidos = pedidoService.listarPedidosPorUsuario(
                usuarioActual.getId()
        );

        // Estadísticas
        long totalPedidos = pedidos.size();

        long pendientes = pedidos.stream()
                .filter(p -> "PENDIENTE".equalsIgnoreCase(p.getEstado()))
                .count();

        long entregados = pedidos.stream()
                .filter(p -> "ENTREGADO".equalsIgnoreCase(p.getEstado()))
                .count();

        H2 resumen = new H2("Resumen de compras");

        resumen.getStyle()
                .set("color", "#D6AD55");

        HorizontalLayout tarjetas = new HorizontalLayout(
                crearTarjeta("Total de pedidos", String.valueOf(totalPedidos)),
                crearTarjeta("Pendientes", String.valueOf(pendientes)),
                crearTarjeta("Entregados", String.valueOf(entregados))
        );

        tarjetas.setWidthFull();
        tarjetas.setWrap(true);
        tarjetas.setSpacing(true);

        // Historial
        H2 historialTitulo = new H2("Historial de compras");

        historialTitulo.getStyle()
                .set("color", "#D6AD55");

        Grid<Pedido> tabla = new Grid<>(Pedido.class, false);

        tabla.addColumn(Pedido::getId)
                .setHeader("N.º pedido")
                .setAutoWidth(true);

        tabla.addColumn(p ->
                        p.getFechaPedido() != null
                                ? p.getFechaPedido().format(formatoFecha)
                                : "Sin fecha"
                )
                .setHeader("Fecha")
                .setAutoWidth(true);

        tabla.addColumn(Pedido::getDireccionEntrega)
                .setHeader("Dirección de entrega")
                .setAutoWidth(true);

        tabla.addColumn(p ->
                        p.getTotal() != null
                                ? formatoMoneda.format(p.getTotal())
                                : formatoMoneda.format(BigDecimal.ZERO)
                )
                .setHeader("Total")
                .setAutoWidth(true);

        tabla.addComponentColumn(p -> crearEstado(p.getEstado()))
                .setHeader("Estado")
                .setAutoWidth(true);

        tabla.addComponentColumn(p -> {

            Button detalle = new Button(
                    "Ver detalle",
                    event -> mostrarDetalle(p)
            );

            detalle.getStyle()
                    .set("background", "#D6AD55")
                    .set("color", "#111111")
                    .set("font-weight", "bold")
                    .set("border-radius", "7px");

            return detalle;

        }).setHeader("Acciones").setAutoWidth(true);

        tabla.setItems(pedidos);
        tabla.setWidthFull();
        tabla.setAllRowsVisible(true);

        // Diseño de la tabla
        tabla.getStyle()
                .set("background-color", "#1B1B1B")
                .set("color", "#EEEEEE")
                .set("border", "1px solid #383838")
                .set("border-radius", "12px")
                .set("overflow", "hidden")
                .set("--lumo-base-color", "#1B1B1B")
                .set("--lumo-body-text-color", "#EEEEEE")
                .set("--lumo-header-text-color", "#D6AD55")
                .set("--lumo-contrast", "#EEEEEE")
                .set("--lumo-contrast-90pct", "#EEEEEE")
                .set("--lumo-contrast-60pct", "#BBBBBB");

        // Mensaje cuando no existen compras
        if (pedidos.isEmpty()) {

            Paragraph sinPedidos = new Paragraph(
                    "Todavía no tienes pedidos registrados."
            );

            sinPedidos.getStyle()
                    .set("color", "#AAAAAA")
                    .set("background", "#1B1B1B")
                    .set("padding", "20px")
                    .set("border-radius", "10px");

            add(
                    titulo,
                    descripcion,
                    resumen,
                    tarjetas,
                    historialTitulo,
                    sinPedidos
            );

        } else {

            add(
                    titulo,
                    descripcion,
                    resumen,
                    tarjetas,
                    historialTitulo,
                    tabla
            );
        }

        Button volver = new Button(
                "Volver al inicio",
                event -> getUI().ifPresent(ui -> ui.navigate(""))
        );

        volver.getStyle()
                .set("background", "#D6AD55")
                .set("color", "#111111")
                .set("font-weight", "bold")
                .set("border-radius", "8px")
                .set("padding", "10px 18px");

        add(volver);
    }

    // Mostrar el estado con colores
    private Span crearEstado(String estado) {

        String texto = estado == null ? "SIN ESTADO" : estado;

        String color;

        switch (texto.toUpperCase(Locale.ROOT)) {

            case "PENDIENTE":
                color = "#D6AD55";
                break;

            case "EN_PREPARACION":
                color = "#E69545";
                break;

            case "EN_CAMINO":
                color = "#5B9BD5";
                break;

            case "ENTREGADO":
                color = "#65B879";
                break;

            default:
                color = "#AAAAAA";
                break;
        }

        Span etiqueta = new Span(texto.replace("_", " "));

        etiqueta.getStyle()
                .set("background", color)
                .set("color", "#111111")
                .set("font-weight", "bold")
                .set("padding", "6px 10px")
                .set("border-radius", "20px")
                .set("font-size", "12px");

        return etiqueta;
    }

    // Mostrar los productos de un pedido
    private void mostrarDetalle(Pedido pedido) {

        Dialog dialogo = new Dialog();

        dialogo.setWidth("750px");
        dialogo.setMaxWidth("95vw");

        VerticalLayout contenido = new VerticalLayout();

        contenido.setPadding(true);
        contenido.setSpacing(true);

        contenido.getStyle()
                .set("background", "#101010")
                .set("color", "white");

        H2 titulo = new H2("Detalle del pedido #" + pedido.getId());

        titulo.getStyle()
                .set("color", "#D6AD55")
                .set("margin", "0");

        Paragraph fecha = new Paragraph(
                "Fecha: " + (pedido.getFechaPedido() != null
                        ? pedido.getFechaPedido().format(formatoFecha)
                        : "Sin fecha")
        );

        Paragraph direccion = new Paragraph(
                "Dirección: " + pedido.getDireccionEntrega()
        );

        Paragraph estado = new Paragraph(
                "Estado: " + pedido.getEstado()
        );

        Paragraph pago = new Paragraph(
                "Estado del pago: " + pedido.getEstadoPago()
        );

        for (Paragraph texto : List.of(fecha, direccion, estado, pago)) {
            texto.getStyle().set("color", "#CCCCCC");
        }

        H3 productosTitulo = new H3("Productos comprados");

        productosTitulo.getStyle()
                .set("color", "#D6AD55");

        Grid<DetallePedido> tablaProductos =
                new Grid<>(DetallePedido.class, false);

        tablaProductos.addColumn(d ->
                d.getProducto() != null
                        ? d.getProducto().getNombre()
                        : "Producto no disponible"
        ).setHeader("Producto").setAutoWidth(true);

        tablaProductos.addColumn(DetallePedido::getCantidad)
                .setHeader("Cantidad")
                .setAutoWidth(true);

        tablaProductos.addColumn(d ->
                formatoMoneda.format(d.getPrecioUnitario())
        ).setHeader("Precio unitario").setAutoWidth(true);

        tablaProductos.addColumn(d ->
                formatoMoneda.format(d.getSubtotal())
        ).setHeader("Subtotal").setAutoWidth(true);

        tablaProductos.setItems(pedido.getDetalles());
        tablaProductos.setWidthFull();
        tablaProductos.setAllRowsVisible(true);

        tablaProductos.getStyle()
                .set("background-color", "#1B1B1B")
                .set("color", "#EEEEEE")
                .set("border", "1px solid #383838")
                .set("border-radius", "10px")
                .set("--lumo-base-color", "#1B1B1B")
                .set("--lumo-body-text-color", "#EEEEEE")
                .set("--lumo-header-text-color", "#D6AD55")
                .set("--lumo-contrast", "#EEEEEE");

        H2 total = new H2(
                "Total: " + formatoMoneda.format(pedido.getTotal())
        );

        total.getStyle()
                .set("color", "#D6AD55");

        Button cerrar = new Button("Cerrar", event -> dialogo.close());

        cerrar.getStyle()
                .set("background", "#D6AD55")
                .set("color", "#111111")
                .set("font-weight", "bold")
                .set("border-radius", "8px");

        contenido.add(
                titulo,
                fecha,
                direccion,
                estado,
                pago,
                productosTitulo,
                tablaProductos,
                total,
                cerrar
        );

        dialogo.add(contenido);
        dialogo.open();
    }

    // Crear tarjetas estadísticas
    private Div crearTarjeta(String titulo, String valor) {

        Div tarjeta = new Div();

        H3 nombre = new H3(titulo);

        nombre.getStyle()
                .set("color", "#BBBBBB")
                .set("font-size", "14px")
                .set("margin", "0 0 12px 0");

        H2 numero = new H2(valor);

        numero.getStyle()
                .set("color", "#D6AD55")
                .set("font-size", "27px")
                .set("margin", "0");

        tarjeta.add(nombre, numero);

        tarjeta.getStyle()
                .set("background", "#1B1B1B")
                .set("border", "1px solid #383838")
                .set("border-radius", "12px")
                .set("padding", "20px")
                .set("box-sizing", "border-box")
                .set("flex", "1 1 180px")
                .set("min-width", "180px");

        return tarjeta;
    }

    private void configurarPagina() {

        setWidthFull();
        setPadding(true);
        setSpacing(true);

        getStyle()
                .set("background-color", "#101010")
                .set("color", "white")
                .set("min-height", "100vh")
                .set("height", "auto")
                .set("box-sizing", "border-box")
                .set("margin", "0");
    }
}
