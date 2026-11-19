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
    private final Usuario usuario;

    public RestaurantesPlataformaView(RestauranteService restauranteService) {
        this.restauranteService = restauranteService;
        usuario = (Usuario) VaadinSession.getCurrent().getAttribute("usuarioActual");
        if (usuario == null || !"ADMIN".equalsIgnoreCase(usuario.getRol())
                || !"jdptls10@gmail.com".equalsIgnoreCase(usuario.getCorreo())) {
            Notification.show("Solo el administrador general puede gestionar la plataforma.");
            getUI().ifPresent(ui -> ui.navigate("login"));
            return;
        }
        setSizeFull(); setPadding(true); setSpacing(true);
        getStyle().set("background", "#101010").set("color", "white");
        H2 titulo = new H2("Administración de restaurantes");
        titulo.getStyle().set("color", "#D6AD55");
        Paragraph ayuda = new Paragraph("Crea restaurantes independientes. Cada uno tendrá su propia dirección, menú, pedidos y personalización.");
        TextField nombre = new TextField("Nombre del restaurante");
        nombre.setPlaceholder("Ej. Restaurante del Centro");
        nombre.setWidth("min(100%, 320px)");
        TextField slug = new TextField("Identificador del enlace");
        slug.setPlaceholder("restaurante-del-centro");
        slug.setHelperText("Dirección: /restaurante/restaurante-del-centro");
        slug.setWidth("min(100%, 320px)");
        Button crear = new Button("Crear restaurante", event -> {
            try {
                Restaurante nuevo = restauranteService.crear(nombre.getValue(), slug.getValue(), usuario);
                Notification.show("Restaurante creado: /restaurante/" + nuevo.getSlug());
                nombre.clear(); slug.clear(); actualizarTabla();
            } catch (Exception ex) {
                Notification.show(ex.getMessage(), 5000, Notification.Position.MIDDLE);
            }
        });
        TextField slugAsignacion = new TextField("Slug del restaurante");
        slugAsignacion.setPlaceholder("restaurante-del-centro");
        TextField correoResponsable = new TextField("Correo del responsable");
        correoResponsable.setPlaceholder("responsable@correo.com");
        Button asignar = new Button("Asignar responsable", event -> {
            try {
                restauranteService.asignarAdministrador(slugAsignacion.getValue(), correoResponsable.getValue(), usuario);
                Notification.show("Responsable asignado. Su cuenta tendrá permisos de administrador para ese restaurante.");
                actualizarTabla();
            } catch (Exception ex) {
                Notification.show(ex.getMessage(), 5000, Notification.Position.MIDDLE);
            }
        });
        grid.addColumn(Restaurante::getNombre).setHeader("Restaurante").setAutoWidth(true);
        grid.addColumn(Restaurante::getSlug).setHeader("Identificador").setAutoWidth(true);
        grid.addColumn(r -> r.getAdministrador() == null ? "Sin responsable" : r.getAdministrador().getCorreo())
                .setHeader("Responsable").setAutoWidth(true);
        grid.addColumn(r -> r.isActivo() ? "Activo" : "Inactivo").setHeader("Estado").setAutoWidth(true);
        grid.addComponentColumn(r -> new Anchor("/restaurante/" + r.getSlug(), "Abrir página"))
                .setHeader("Página pública").setAutoWidth(true);
        grid.addComponentColumn(r -> new Anchor("/restaurante-admin/" + r.getSlug(), "Administrar"))
                .setHeader("Panel").setAutoWidth(true);
        grid.setWidthFull();
        HorizontalLayout formulario = new HorizontalLayout(nombre, slug, crear);
        formulario.setWidthFull(); formulario.setAlignItems(Alignment.END); formulario.setFlexWrap(FlexWrap.WRAP);
        HorizontalLayout responsables = new HorizontalLayout(slugAsignacion, correoResponsable, asignar);
        responsables.setWidthFull(); responsables.setAlignItems(Alignment.END); responsables.setFlexWrap(FlexWrap.WRAP);
        Button volver = new Button("Volver al panel", event -> getUI().ifPresent(ui -> ui.navigate("admin")));
        add(titulo, ayuda, formulario, new H2("Asignación de responsables"), responsables, grid, volver);
        actualizarTabla();
    }

    private void actualizarTabla() {
        grid.setItems(restauranteService.listar());
    }
}