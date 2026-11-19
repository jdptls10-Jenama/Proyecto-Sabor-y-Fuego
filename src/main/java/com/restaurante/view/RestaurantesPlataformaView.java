package com.restaurante.view;

import com.restaurante.model.Restaurante;
import com.restaurante.model.Usuario;
import com.restaurante.service.RestauranteService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

@Route("plataforma/restaurantes")
@PageTitle("Restaurantes | Plataforma")
public class RestaurantesPlataformaView extends VerticalLayout {

    private final RestauranteService restauranteService;
    private final Grid<Restaurante> grid = new Grid<>(Restaurante.class, false);

    public RestaurantesPlataformaView(RestauranteService restauranteService) {
        this.restauranteService = restauranteService;

        Usuario usuario = (Usuario) VaadinSession.getCurrent()
                .getAttribute("usuarioActual");

        if (usuario == null || !"ADMIN".equalsIgnoreCase(usuario.getRol())) {
            Notification.show("No tienes permisos para administrar restaurantes.");
            getUI().ifPresent(ui -> ui.navigate("login"));
            return;
        }

        setSizeFull();
        setPadding(true);
        setSpacing(true);
        getStyle().set("background", "#101010").set("color", "white");

        H2 titulo = new H2("Administración de restaurantes");
        titulo.getStyle().set("color", "#D6AD55");

        Paragraph ayuda = new Paragraph(
                "Registra los restaurantes que tendrán su propio enlace e identidad visual. " +
                "Ejemplo: /restaurante/mi-restaurante"
        );

        TextField nombre = new TextField("Nombre del restaurante");
        nombre.setPlaceholder("Ej. Sabor & Fuego");
        nombre.setWidth("min(100%, 320px)");

        TextField slug = new TextField("Identificador del enlace");
        slug.setPlaceholder("sabor-y-fuego");
        slug.setHelperText("Se convertirá en una dirección como /restaurante/sabor-y-fuego");
        slug.setWidth("min(100%, 320px)");

        Button crear = new Button("Crear restaurante", event -> {
            try {
                Restaurante nuevo = restauranteService.crear(
                        nombre.getValue(), slug.getValue()
                );
                Notification.show("Restaurante creado: /restaurante/" + nuevo.getSlug());
                nombre.clear();
                slug.clear();
                actualizarTabla();
            } catch (IllegalArgumentException ex) {
                Notification.show(ex.getMessage());
            } catch (Exception ex) {
                Notification.show("No fue posible crear el restaurante.");
            }
        });

        Button volver = new Button("Volver al panel", event ->
                getUI().ifPresent(ui -> ui.navigate("admin")));

        grid.addColumn(Restaurante::getNombre).setHeader("Restaurante").setAutoWidth(true);
        grid.addColumn(Restaurante::getSlug).setHeader("Identificador").setAutoWidth(true);
        grid.addColumn(r -> r.isActivo() ? "Activo" : "Inactivo")
                .setHeader("Estado").setAutoWidth(true);
        grid.addColumn(r -> "/restaurante/" + r.getSlug())
                .setHeader("Enlace previsto").setAutoWidth(true);
        grid.setWidthFull();

        HorizontalLayout formulario = new HorizontalLayout(nombre, slug, crear);
        formulario.setWidthFull();
        formulario.setAlignItems(Alignment.END);
        formulario.setFlexWrap(FlexWrap.WRAP);

        add(titulo, ayuda, formulario, grid, volver);
        actualizarTabla();
    }

    private void actualizarTabla() {
        grid.setItems(restauranteService.listar());
    }
}
