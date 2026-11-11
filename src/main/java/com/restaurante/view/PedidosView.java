
package com.restaurante.view;

import com.restaurante.model.Pedido;
import com.restaurante.model.Usuario;
import com.restaurante.model.DetallePedido;
import com.restaurante.service.PedidoService;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
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

@Route("pedidos")
@PageTitle("Gestión de pedidos | Sabor & Fuego")
public class PedidosView extends VerticalLayout {

    private final PedidoService pedidoService;

    private final Grid<Pedido> tabla = new Grid<>(Pedido.class, false);

    private final ComboBox<String> filtroEstado =
            new ComboBox<>("Filtrar por estado");

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final NumberFormat formatoMoneda =
            NumberFormat.getCurrencyInstance(new Locale("es", "CO"));

    private Div tarjetaTotal;
    private Div tarjetaPendientes;
    private Div tarjetaPreparacion;
    private Div tarjetaCamino;
    private Div tarjetaEntregados;

    public PedidosView(PedidoService pedidoService) {

        this.pedidoService = pedidoService;

        Usuario usuario = (Usuario) VaadinSession.getCurrent()
                .getAttribute("usuarioActual");

        if (usuario == null ||
                !"ADMIN".equalsIgnoreCase(usuario.getRol())) {

            Notification.show("No tienes permisos para acceder.");
            getUI().ifPresent(ui -> ui.navigate("login"));
            return;
        }

        configurarPagina();

        // Encabezado
        H2 titulo = new H2("Gestión de pedidos");
        titulo.getStyle()
                .set("color", "#D6AD55")
                .set("font-size", "30px")
                .set("margin", "0");

        Paragraph subtitulo = new Paragraph(
                "Consulta y administra los pedidos de tus clientes."
        );

        subtitulo.getStyle()
                .set("color", "#AAAAAA")
                .set("margin", "5px 0 15px 0");

        Button volver = new Button("Volver al panel", event ->
                getUI().ifPresent(ui -> ui.navigate("admin"))
        );

        estilizarBotonSecundario(volver);

        HorizontalLayout encabezado =
                new HorizontalLayout(titulo, volver);

        encabezado.setWidthFull();
        encabezado.setAlignItems(Alignment.CENTER);
        encabezado.setJustifyContentMode(
                JustifyContentMode.BETWEEN
        );
        getStyle().set("flex-wrap", "wrap");

        // Estadísticas
        H3 tituloResumen = new H3("Resumen de pedidos");
        tituloResumen.getStyle()
                .set("color", "#D6AD55")
                .set("margin", "10px 0");

        tarjetaTotal = crearTarjeta("Total de pedidos", "0");
        tarjetaPendientes = crearTarjeta("Pendientes", "0");
        tarjetaPreparacion = crearTarjeta("En preparación", "0");
        tarjetaCamino = crearTarjeta("En camino", "0");
        tarjetaEntregados = crearTarjeta("Entregados", "0");

        HorizontalLayout estadisticas = new HorizontalLayout(
                tarjetaTotal,
                tarjetaPendientes,
                tarjetaPreparacion,
                tarjetaCamino,
                tarjetaEntregados
        );

        estadisticas.setWidthFull();
        estadisticas.setSpacing(true);
        getStyle().set("flex-wrap", "wrap");
        // Filtros
        filtroEstado.setLabel("Filtrar por estado");
        filtroEstado.setItems(
                "TODOS",
                "PENDIENTE",
                "EN_PREPARACION",
                "EN_CAMINO",
                "ENTREGADO"
        );

        filtroEstado.setItemLabelGenerator(estado -> {
            if (estado == null) return "";
            return nombreEstado(estado);
        });

        filtroEstado.setValue("TODOS");
        filtroEstado.setWidth("240px");

        estilizarCampo(filtroEstado);

        filtroEstado.addValueChangeListener(event -> cargarPedidos());

        Button actualizar = new Button("Actualizar pedidos", event -> {
            cargarPedidos();
            actualizarEstadisticas();
        });

        estilizarBotonPrincipal(actualizar);

        HorizontalLayout herramientas =
                new HorizontalLayout(filtroEstado, actualizar);

        herramientas.setAlignItems(Alignment.END);
        getStyle().set("flex-wrap", "wrap");
        herramientas.setWidthFull();

        // Tabla
        configurarTabla();

        Div contenedorTabla = new Div(tabla);
        contenedorTabla.setWidthFull();

        contenedorTabla.getStyle()
                .set("background", "#1B1B1B")
                .set("border", "1px solid #383838")
                .set("border-radius", "12px")
                .set("padding", "10px")
                .set("box-sizing", "border-box")
                .set("overflow-x", "auto");

        add(
                encabezado,
                subtitulo,
                tituloResumen,
                estadisticas,
                herramientas,
                contenedorTabla
        );

        cargarPedidos();
        actualizarEstadisticas();
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

    private Div crearTarjeta(String titulo, String valor) {

        Div tarjeta = new Div();

        H3 nombre = new H3(titulo);
        nombre.getStyle()
                .set("color", "#BBBBBB")
                .set("font-size", "13px")
                .set("font-weight", "500")
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
                .set("padding", "18px")
                .set("box-sizing", "border-box")
                .set("flex", "1 1 160px")
                .set("min-width", "150px");

        return tarjeta;
    }

    private void configurarTabla() {

        tabla.setWidthFull();
        tabla.setHeight("520px");

        tabla.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

        tabla.getElement().getThemeList().add("dark");

        tabla.getStyle()
                .set("background", "#1B1B1B")
                .set("color", "white")
                .set("--lumo-base-color", "#1B1B1B")
                .set("--lumo-body-text-color", "#EEEEEE")
                .set("--lumo-header-text-color", "#D6AD55")
                .set("--lumo-primary-color", "#D6AD55");

        tabla.addColumn(Pedido::getId)
                .setHeader("N.º pedido")
                .setAutoWidth(true)
                .setFlexGrow(0);

        tabla.addColumn(p ->
                p.getUsuario() != null
                        ? p.getUsuario().getNombre()
                        : "Sin cliente"
        ).setHeader("Cliente").setAutoWidth(true);

        tabla.addColumn(p ->
                p.getFechaPedido() != null
                        ? p.getFechaPedido().format(FORMATO_FECHA)
                        : "-"
        ).setHeader("Fecha").setAutoWidth(true);

        tabla.addColumn(p ->
                p.getDireccionEntrega() != null
                        ? p.getDireccionEntrega()
                        : "-"
        ).setHeader("Dirección").setAutoWidth(true);

        tabla.addColumn(p ->
                p.getTelefonoContacto() != null
                        ? p.getTelefonoContacto()
                        : "-"
        ).setHeader("Teléfono").setAutoWidth(true);

        tabla.addColumn(p ->
                formatoMoneda.format(
                        p.getTotal() != null
                                ? p.getTotal()
                                : BigDecimal.ZERO
                )
        ).setHeader("Total").setAutoWidth(true);

        tabla.addComponentColumn(pedido -> {

            Span estado = new Span(nombreEstado(pedido.getEstado()));

            estado.getStyle()
                    .set("display", "inline-block")
                    .set("padding", "6px 10px")
                    .set("border-radius", "20px")
                    .set("font-size", "12px")
                    .set("font-weight", "bold")
                    .set("background", colorEstado(pedido.getEstado()))
                    .set("color", "#FFFFFF");

            return estado;

        }).setHeader("Estado").setAutoWidth(true);

        tabla.addComponentColumn(pedido -> {

            Button detalle = new Button("Ver detalle",
                    event -> mostrarDetalle(pedido));

            estilizarBotonPrincipal(detalle);

            return detalle;

        }).setHeader("Detalles").setAutoWidth(true);

        tabla.addComponentColumn(pedido -> {

            ComboBox<String> estados = new ComboBox<>();

            estados.setItems(
                    "PENDIENTE",
                    "EN_PREPARACION",
                    "EN_CAMINO",
                    "ENTREGADO"
            );

            estados.setItemLabelGenerator(this::nombreEstado);
            estados.setValue(pedido.getEstado());
            estados.setWidth("180px");

            estilizarCampo(estados);

            Button guardar = new Button("Guardar", event -> {

                try {

                    pedidoService.actualizarEstado(
                            pedido.getId(),
                            estados.getValue()
                    );

                    Notification.show(
                            "Estado del pedido actualizado."
                    );

                    cargarPedidos();
                    actualizarEstadisticas();

                } catch (Exception e) {

                    Notification.show(
                            "Error: " + e.getMessage()
                    );
                }
            });

            estilizarBotonPrincipal(guardar);

            HorizontalLayout acciones =
                    new HorizontalLayout(estados, guardar);

            acciones.setAlignItems(Alignment.CENTER);

            return acciones;

        }).setHeader("Actualizar estado").setAutoWidth(true);
    }

    private void cargarPedidos() {

        try {

            String estado = filtroEstado.getValue();

            tabla.setItems(
                    pedidoService.listarPedidosPorEstado(estado)
            );

        } catch (Exception e) {

            Notification.show(
                    "No se pudieron cargar los pedidos: " + e.getMessage()
            );
        }
    }

    private void actualizarEstadisticas() {

        try {

            List<Pedido> pedidos = pedidoService.listarPedidos();

            long total = pedidos.size();

            long pendientes = contarEstado(pedidos, "PENDIENTE");
            long preparacion = contarEstado(pedidos, "EN_PREPARACION");
            long camino = contarEstado(pedidos, "EN_CAMINO");
            long entregados = contarEstado(pedidos, "ENTREGADO");

            actualizarValor(tarjetaTotal, total);
            actualizarValor(tarjetaPendientes, pendientes);
            actualizarValor(tarjetaPreparacion, preparacion);
            actualizarValor(tarjetaCamino, camino);
            actualizarValor(tarjetaEntregados, entregados);

        } catch (Exception e) {

            Notification.show(
                    "No se pudieron actualizar las estadísticas."
            );
        }
    }

    private long contarEstado(List<Pedido> pedidos, String estado) {

        return pedidos.stream()
                .filter(p -> estado.equalsIgnoreCase(p.getEstado()))
                .count();
    }

    private void actualizarValor(Div tarjeta, long valor) {

        if (tarjeta.getComponentCount() > 1) {

            H2 numero = (H2) tarjeta.getComponentAt(1);
            numero.setText(String.valueOf(valor));
        }
    }

    private void mostrarDetalle(Pedido pedido) {

        Dialog dialogo = new Dialog();

        dialogo.setWidth("750px");
        dialogo.setMaxWidth("95vw");

        dialogo.getElement().getThemeList().add("dark");

        dialogo.getElement().getStyle()
                .set("--lumo-base-color", "#1B1B1B")
                .set("--lumo-body-text-color", "#EEEEEE")
                .set("--lumo-primary-color", "#D6AD55");

        VerticalLayout contenido = new VerticalLayout();
        contenido.setWidthFull();
        contenido.setPadding(true);
        contenido.setSpacing(true);

        H2 titulo = new H2("Pedido #" + pedido.getId());
        titulo.getStyle().set("color", "#D6AD55");

        Usuario cliente = pedido.getUsuario();

        contenido.add(
                titulo,
                crearTexto("Cliente",
                        cliente != null ? cliente.getNombre() : "Sin cliente"),
                crearTexto("Correo",
                        cliente != null ? cliente.getCorreo() : "No disponible"),
                crearTexto("Dirección",
                        pedido.getDireccionEntrega()),
                crearTexto("Teléfono",
                        pedido.getTelefonoContacto()),
                crearTexto("Fecha",
                        pedido.getFechaPedido() != null
                                ? pedido.getFechaPedido().format(FORMATO_FECHA)
                                : "-"),
                crearTexto("Estado del pedido",
                        nombreEstado(pedido.getEstado())),
                crearTexto("Estado del pago",
                        pedido.getEstadoPago())
        );

        H3 tituloProductos = new H3("Productos del pedido");
        tituloProductos.getStyle().set("color", "#D6AD55");

        Grid<DetallePedido> detalles = new Grid<>();

        detalles.addColumn(d ->
                d.getProducto() != null
                        ? d.getProducto().getNombre()
                        : "Producto no disponible"
        ).setHeader("Producto");

        detalles.addColumn(DetallePedido::getCantidad)
                .setHeader("Cantidad");

        detalles.addColumn(d ->
                formatoMoneda.format(
                        d.getPrecioUnitario() != null
                                ? d.getPrecioUnitario()
                                : BigDecimal.ZERO
                )
        ).setHeader("Precio unitario");

        detalles.addColumn(d ->
                formatoMoneda.format(
                        d.getSubtotal() != null
                                ? d.getSubtotal()
                                : BigDecimal.ZERO
                )
        ).setHeader("Subtotal");

        detalles.setWidthFull();
        detalles.setHeight("230px");
        detalles.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        detalles.getElement().getThemeList().add("dark");

        detalles.getStyle()
                .set("background", "#1B1B1B")
                .set("color", "white");

        detalles.setItems(pedido.getDetalles());

        H2 total = new H2(
                "Total: " + formatoMoneda.format(
                        pedido.getTotal() != null
                                ? pedido.getTotal()
                                : BigDecimal.ZERO
                )
        );

        total.getStyle().set("color", "#D6AD55");

        Button cerrar = new Button("Cerrar", event -> dialogo.close());
        estilizarBotonPrincipal(cerrar);

        contenido.add(
                tituloProductos,
                detalles,
                total,
                cerrar
        );

        contenido.getStyle()
                .set("background", "#1B1B1B")
                .set("color", "white")
                .set("border-radius", "12px");

        dialogo.add(contenido);
        dialogo.open();
    }

    private Paragraph crearTexto(String titulo, String valor) {

        Paragraph texto = new Paragraph(
                titulo + ": " + (valor != null ? valor : "-")
        );

        texto.getStyle()
                .set("color", "#DDDDDD")
                .set("margin", "3px 0");

        return texto;
    }

    private String nombreEstado(String estado) {

        if (estado == null) return "Sin estado";

        switch (estado) {
            case "PENDIENTE":
                return "Pendiente";
            case "EN_PREPARACION":
                return "En preparación";
            case "EN_CAMINO":
                return "En camino";
            case "ENTREGADO":
                return "Entregado";
            default:
                return estado;
        }
    }

    private String colorEstado(String estado) {

        if (estado == null) return "#555555";

        switch (estado) {
            case "PENDIENTE":
                return "#98732E";
            case "EN_PREPARACION":
                return "#7956A5";
            case "EN_CAMINO":
                return "#326D9B";
            case "ENTREGADO":
                return "#397A50";
            default:
                return "#555555";
        }
    }

    private void estilizarBotonPrincipal(Button boton) {

        boton.getStyle()
                .set("background", "#D6AD55")
                .set("color", "#111111")
                .set("font-weight", "bold")
                .set("border-radius", "8px")
                .set("padding", "8px 14px");
    }

    private void estilizarBotonSecundario(Button boton) {

        boton.getStyle()
                .set("background", "#292929")
                .set("color", "#D6AD55")
                .set("border", "1px solid #D6AD55")
                .set("border-radius", "8px")
                .set("padding", "8px 14px");
    }

    private void estilizarCampo(ComboBox<String> campo) {

        campo.getStyle()
                .set("color", "white")
                .set("--lumo-base-color", "#252525")
                .set("--lumo-body-text-color", "#FFFFFF")
                .set("--lumo-primary-color", "#D6AD55")
                .set("--lumo-primary-text-color", "#D6AD55");
    }
}
