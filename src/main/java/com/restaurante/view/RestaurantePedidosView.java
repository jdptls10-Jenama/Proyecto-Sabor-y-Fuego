package com.restaurante.view;

import com.restaurante.model.Pedido;
import com.restaurante.model.Restaurante;
import com.restaurante.model.Usuario;
import com.restaurante.service.PedidoService;
import com.restaurante.service.RestauranteService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Route("restaurante-pedidos")
@PageTitle("Mis pedidos")
public class RestaurantePedidosView extends VerticalLayout implements HasUrlParameter<String> {
    private final RestauranteService restauranteService;
    private final PedidoService pedidoService;
    private final NumberFormat moneda = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));
    private final DateTimeFormatter fecha = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public RestaurantePedidosView(RestauranteService restauranteService, PedidoService pedidoService) {
        this.restauranteService = restauranteService;
        this.pedidoService = pedidoService;
        setSizeFull(); setPadding(true); setSpacing(true);
    }

    @Override
    public void setParameter(BeforeEvent event, String slug) {
        removeAll();
        Usuario usuario = (Usuario) VaadinSession.getCurrent().getAttribute("usuarioActual");
        if (usuario == null) {
            VaadinSession.getCurrent().setAttribute("rutaDespuesLogin", "restaurante-pedidos/" + slug);
            Notification.show("Inicia sesión para consultar tus pedidos.");
            getUI().ifPresent(ui -> ui.navigate("login"));
            return;
        }
        Restaurante restaurante = restauranteService.buscarPorSlug(slug).orElse(null);
        if (restaurante == null) {
            add(new H2("Restaurante no encontrado"));
            return;
        }
        getStyle().set("background", restaurante.getColorFondo()).set("color", "#FFFFFF");
        H2 titulo = new H2("Mis pedidos · " + restaurante.getNombre());
        titulo.getStyle().set("color", restaurante.getColorPrimario());
        Grid<Pedido> tabla = new Grid<>(Pedido.class, false);
        tabla.addColumn(Pedido::getId).setHeader("Pedido").setAutoWidth(true);
        tabla.addColumn(p -> p.getFechaPedido() == null ? "-" : p.getFechaPedido().format(fecha))
                .setHeader("Fecha").setAutoWidth(true);
        tabla.addColumn(p -> moneda.format(p.getTotal())).setHeader("Total").setAutoWidth(true);
        tabla.addColumn(Pedido::getEstado).setHeader("Estado").setAutoWidth(true);
        tabla.addColumn(Pedido::getEstadoPago).setHeader("Pago").setAutoWidth(true);
        tabla.setItems(pedidoService.listarPedidosPorUsuario(usuario.getId(), restaurante.getId()));
        tabla.setWidthFull();
        tabla.setAllRowsVisible(true);
        add(titulo, new Paragraph("Este historial muestra únicamente tus pedidos en este restaurante."),
                tabla, new Anchor("/restaurante/" + slug, "Volver al restaurante"));
    }
}