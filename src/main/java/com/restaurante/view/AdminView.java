
package com.restaurante.view;

import com.restaurante.model.Pedido;
import com.restaurante.model.Usuario;
import com.restaurante.service.PedidoService;

import com.vaadin.flow.component.button.Button;
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
import java.util.List;
import java.util.Locale;

@Route("admin")
@PageTitle("Panel administrativo | Sabor & Fuego")
public class AdminView extends VerticalLayout {

    private final PedidoService pedidoService;

    public AdminView(PedidoService pedidoService) {

        this.pedidoService = pedidoService;

        Usuario usuario = (Usuario) VaadinSession.getCurrent()
                .getAttribute("usuarioActual");

        // Comprobar sesión de administrador
        if (usuario == null ||
                !"ADMIN".equalsIgnoreCase(usuario.getRol())) {

            Notification.show("No tienes permisos para acceder.");
            getUI().ifPresent(ui -> ui.navigate("login"));
            return;
        }

        configurarPagina();

        // Encabezado
        H1 titulo = new H1("Panel administrativo");
        titulo.getStyle()
                .set("color", "#D6AD55")
                .set("margin", "0");

        Paragraph bienvenida = new Paragraph(
                "Bienvenido, " + usuario.getNombre()
        );

        bienvenida.getStyle()
                .set("color", "#CCCCCC")
                .set("font-size", "16px");

        // Obtener pedidos de PostgreSQL
        List<Pedido> pedidos = pedidoService.listarPedidos();

        long totalPedidos = pedidos.size();

        long pendientes = pedidos.stream()
                .filter(p -> "PENDIENTE".equalsIgnoreCase(p.getEstado()))
                .count();

        long enPreparacion = pedidos.stream()
                .filter(p -> "EN_PREPARACION".equalsIgnoreCase(p.getEstado()))
                .count();

        long entregados = pedidos.stream()
                .filter(p -> "ENTREGADO".equalsIgnoreCase(p.getEstado()))
                .count();

        BigDecimal ventas = pedidos.stream()
                .filter(p -> "PAGADO".equalsIgnoreCase(p.getEstadoPago()))
                .map(Pedido::getTotal)
                .filter(total -> total != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        NumberFormat formatoMoneda = NumberFormat.getCurrencyInstance(
                new Locale("es", "CO")
        );

        // Tarjetas de estadísticas
        H2 resumenTitulo = new H2("Resumen del restaurante");
        resumenTitulo.getStyle().set("color", "#D6AD55");

        HorizontalLayout filaEstadisticas = new HorizontalLayout(
                crearTarjeta("Pedidos totales", String.valueOf(totalPedidos)),
                crearTarjeta("Pendientes", String.valueOf(pendientes)),
                crearTarjeta("En preparación", String.valueOf(enPreparacion)),
                crearTarjeta("Entregados", String.valueOf(entregados))
        );

        filaEstadisticas.setWidthFull();
        filaEstadisticas.setWrap(true);
        filaEstadisticas.setSpacing(true);

        Div tarjetaVentas = crearTarjeta(
                "Ventas pagadas",
                formatoMoneda.format(ventas)
        );

        tarjetaVentas.setWidthFull();

        // Botones de navegación
        Button pedidosButton = new Button(
                "Gestionar pedidos",
                event -> getUI().ifPresent(ui -> ui.navigate("pedidos"))
        );

        Button productosButton = new Button(
                "Administrar productos",
                event -> getUI().ifPresent(ui -> ui.navigate("productos"))
        );

        Button reportesButton = new Button(
                "Reportes de ventas",
                event -> getUI().ifPresent(ui -> ui.navigate("reportes"))
        );

        Button cerrarSesion = new Button("Cerrar sesión", event -> {

            VaadinSession.getCurrent().setAttribute("usuarioActual", null);

            getUI().ifPresent(ui -> ui.navigate(""));

        });

        estilizarBoton(pedidosButton);
        estilizarBoton(productosButton);
        estilizarBoton(reportesButton);

        cerrarSesion.getStyle()
                .set("background", "#333333")
                .set("color", "white")
                .set("border-radius", "8px");

        HorizontalLayout botones = new HorizontalLayout(
                pedidosButton,
                productosButton,
                reportesButton,
                cerrarSesion
        );

        botones.setWidthFull();
        botones.setWrap(true);

        add(
                titulo,
                bienvenida,
                resumenTitulo,
                filaEstadisticas,
                tarjetaVentas,
                botones
        );
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
                .set("font-size", "14px")
                .set("margin", "0 0 12px 0");

        H2 numero = new H2(valor);
        numero.getStyle()
                .set("color", "#D6AD55")
                .set("font-size", "27px")
                .set("margin", "0")
                .set("overflow-wrap", "anywhere");

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

    private void estilizarBoton(Button boton) {

        boton.getStyle()
                .set("background", "#D6AD55")
                .set("color", "#111111")
                .set("font-weight", "bold")
                .set("border-radius", "8px")
                .set("padding", "10px 18px");
    }
}
