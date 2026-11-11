
package com.restaurante.view;

import com.restaurante.model.Pedido;
import com.restaurante.model.Usuario;
import com.restaurante.service.PedidoService;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Route("reportes")
@PageTitle("Reportes de ventas | Sabor & Fuego")
public class ReportesView extends VerticalLayout {

    private final PedidoService pedidoService;

    private final NumberFormat formatoMoneda =
            NumberFormat.getCurrencyInstance(new Locale("es", "CO"));

    private final DateTimeFormatter formatoFecha =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public ReportesView(PedidoService pedidoService) {

        this.pedidoService = pedidoService;

        Usuario usuario = (Usuario) VaadinSession.getCurrent()
                .getAttribute("usuarioActual");

        // Comprobar permisos
        if (usuario == null ||
                !"ADMIN".equalsIgnoreCase(usuario.getRol())) {

            Notification.show("No tienes permisos para acceder.");
            getUI().ifPresent(ui -> ui.navigate("login"));
            return;
        }

        configurarPagina();
        construirContenido();
    }

    private void construirContenido() {

        // Encabezado
        H1 titulo = new H1("Reportes de ventas");

        titulo.getStyle()
                .set("color", "#D6AD55")
                .set("margin", "0");

        Paragraph descripcion = new Paragraph(
                "Consulta el rendimiento y las ventas de Sabor & Fuego."
        );

        descripcion.getStyle()
                .set("color", "#CCCCCC");

        // Obtener pedidos
        List<Pedido> todosLosPedidos = pedidoService.listarPedidos();

        // Solo contar pedidos pagados
        List<Pedido> pedidosPagados = todosLosPedidos.stream()
                .filter(this::estaPagado)
                .collect(Collectors.toList());

        LocalDate hoy = LocalDate.now();

        LocalDateTime inicioHoy = hoy.atStartOfDay();
        LocalDateTime inicioSemana = hoy.minusDays(6).atStartOfDay();
        LocalDateTime inicioMes = hoy.withDayOfMonth(1).atStartOfDay();

        LocalDateTime ahora = LocalDateTime.now();

        // Filtrar ventas por periodo
        List<Pedido> ventasHoy = filtrarPorFecha(
                pedidosPagados, inicioHoy, ahora
        );

        List<Pedido> ventasSemana = filtrarPorFecha(
                pedidosPagados, inicioSemana, ahora
        );

        List<Pedido> ventasMes = filtrarPorFecha(
                pedidosPagados, inicioMes, ahora
        );

        // Calcular cantidades
        BigDecimal totalHoy = calcularVentas(ventasHoy);
        BigDecimal totalSemana = calcularVentas(ventasSemana);
        BigDecimal totalMes = calcularVentas(ventasMes);
        BigDecimal totalGeneral = calcularVentas(pedidosPagados);

        // Título de estadísticas
        H2 resumen = new H2("Resumen de ventas");

        resumen.getStyle()
                .set("color", "#D6AD55");

        // Tarjetas
        HorizontalLayout tarjetas = new HorizontalLayout(
                crearTarjeta(
                        "Ventas de hoy",
                        formatoMoneda.format(totalHoy),
                        ventasHoy.size() + " pedidos"
                ),
                crearTarjeta(
                        "Últimos 7 días",
                        formatoMoneda.format(totalSemana),
                        ventasSemana.size() + " pedidos"
                ),
                crearTarjeta(
                        "Este mes",
                        formatoMoneda.format(totalMes),
                        ventasMes.size() + " pedidos"
                ),
                crearTarjeta(
                        "Ventas acumuladas",
                        formatoMoneda.format(totalGeneral),
                        pedidosPagados.size() + " pedidos pagados"
                )
        );

        tarjetas.setWidthFull();
        tarjetas.setWrap(true);
        tarjetas.setSpacing(true);

        // Tabla de historial de ventas
        H2 tituloTabla = new H2("Historial de ventas");

        tituloTabla.getStyle()
                .set("color", "#D6AD55");

        Grid<Pedido> tabla = new Grid<>(Pedido.class, false);

        tabla.addClassName("tabla-reportes");

        tabla.addColumn(Pedido::getId)
                .setHeader("N.º pedido")
                .setAutoWidth(true);

        tabla.addColumn(pedido -> {
                    Usuario cliente = pedido.getUsuario();

                    return cliente != null
                            ? cliente.getNombre()
                            : "Cliente no disponible";
                })
                .setHeader("Cliente")
                .setAutoWidth(true);

        tabla.addColumn(pedido -> {
                    Usuario cliente = pedido.getUsuario();

                    return cliente != null
                            ? cliente.getCorreo()
                            : "Sin correo";
                })
                .setHeader("Correo")
                .setAutoWidth(true);

        tabla.addColumn(pedido ->
                        pedido.getFechaPedido() != null
                                ? pedido.getFechaPedido().format(formatoFecha)
                                : "Sin fecha"
                )
                .setHeader("Fecha")
                .setAutoWidth(true);

        tabla.addColumn(pedido ->
                        pedido.getTotal() != null
                                ? formatoMoneda.format(pedido.getTotal())
                                : formatoMoneda.format(BigDecimal.ZERO)
                )
                .setHeader("Total")
                .setAutoWidth(true);

        tabla.addColumn(Pedido::getEstado)
                .setHeader("Estado del pedido")
                .setAutoWidth(true);

        tabla.setItems(pedidosPagados);
        tabla.setWidthFull();
        tabla.setAllRowsVisible(true);

        // Estilos generales de la tabla
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
                .set("--lumo-contrast-60pct", "#BBBBBB")
                .set("--lumo-primary-color", "#D6AD55")
                .set("--lumo-primary-text-color", "#D6AD55");

        // Botones
        Button actualizar = new Button(
                "Actualizar reportes",
                event -> getUI().ifPresent(ui -> ui.getPage().reload())
        );

        Button volver = new Button(
                "Volver al panel",
                event -> getUI().ifPresent(ui -> ui.navigate("admin"))
        );

        estilizarBoton(actualizar);
        estilizarBoton(volver);

        HorizontalLayout botones = new HorizontalLayout(
                actualizar,
                volver
        );

        botones.setWidthFull();
        botones.setWrap(true);

        add(
                titulo,
                descripcion,
                resumen,
                tarjetas,
                tituloTabla,
                tabla,
                botones
        );
    }

    private boolean estaPagado(Pedido pedido) {

        return pedido.getEstadoPago() != null
                && "PAGADO".equalsIgnoreCase(pedido.getEstadoPago());
    }

    private List<Pedido> filtrarPorFecha(
            List<Pedido> pedidos,
            LocalDateTime inicio,
            LocalDateTime fin) {

        return pedidos.stream()
                .filter(pedido -> pedido.getFechaPedido() != null)
                .filter(pedido ->
                        !pedido.getFechaPedido().isBefore(inicio)
                                && !pedido.getFechaPedido().isAfter(fin)
                )
                .collect(Collectors.toList());
    }

    private BigDecimal calcularVentas(List<Pedido> pedidos) {

        return pedidos.stream()
                .map(Pedido::getTotal)
                .filter(total -> total != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Div crearTarjeta(
            String titulo,
            String valor,
            String descripcion) {

        Div tarjeta = new Div();

        H3 nombre = new H3(titulo);

        nombre.getStyle()
                .set("color", "#BBBBBB")
                .set("font-size", "14px")
                .set("margin", "0 0 12px 0");

        H2 numero = new H2(valor);

        numero.getStyle()
                .set("color", "#D6AD55")
                .set("font-size", "23px")
                .set("margin", "0 0 10px 0")
                .set("overflow-wrap", "anywhere");

        Paragraph detalle = new Paragraph(descripcion);

        detalle.getStyle()
                .set("color", "#999999")
                .set("font-size", "12px")
                .set("margin", "0");

        tarjeta.add(nombre, numero, detalle);

        tarjeta.getStyle()
                .set("background", "#1B1B1B")
                .set("border", "1px solid #383838")
                .set("border-radius", "12px")
                .set("padding", "20px")
                .set("box-sizing", "border-box")
                .set("flex", "1 1 220px")
                .set("min-width", "220px");

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

    private void estilizarBoton(Button boton) {

        boton.getStyle()
                .set("background", "#D6AD55")
                .set("color", "#111111")
                .set("font-weight", "bold")
                .set("border-radius", "8px")
                .set("padding", "10px 18px");
    }
}
