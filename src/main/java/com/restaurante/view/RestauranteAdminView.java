package com.restaurante.view;

import com.restaurante.model.Producto;
import com.restaurante.model.Restaurante;
import com.restaurante.model.Usuario;
import com.restaurante.service.ProductoService;
import com.restaurante.service.RestauranteService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import java.math.BigDecimal;

@Route("restaurante-admin")
@PageTitle("Administración del restaurante")
public class RestauranteAdminView extends VerticalLayout implements HasUrlParameter<String> {
    private final RestauranteService restauranteService;
    private final ProductoService productoService;
    private Restaurante restaurante;
    private Usuario usuario;
    private final Grid<Producto> productos = new Grid<>(Producto.class, false);

    private final TextField nombre = new TextField("Nombre del restaurante");
    private final TextField logo = new TextField("URL del logo");
    private final TextField colorPrimario = new TextField("Color principal");
    private final TextField colorSecundario = new TextField("Color secundario");
    private final TextField colorFondo = new TextField("Color de fondo");
    private final TextField imagen = new TextField("URL de imagen principal");
    private final TextField tituloInicio = new TextField("Título de portada");
    private final TextArea descripcionInicio = new TextArea("Descripción de portada");

    private final TextField productoNombre = new TextField("Nombre del producto");
    private final TextArea productoDescripcion = new TextArea("Descripción");
    private final BigDecimalField precio = new BigDecimalField("Precio");
    private final IntegerField stock = new IntegerField("Existencias");
    private final TextField categoria = new TextField("Categoría");

    public RestauranteAdminView(RestauranteService restauranteService, ProductoService productoService) {
        this.restauranteService = restauranteService;
        this.productoService = productoService;
        setSizeFull();
        setPadding(true);
        setSpacing(true);
    }

    @Override
    public void setParameter(BeforeEvent event, String slug) {
        removeAll();
        usuario = (Usuario) VaadinSession.getCurrent().getAttribute("usuarioActual");
        restaurante = restauranteService.buscarPorSlug(slug).orElse(null);
        if (restaurante == null) {
            add(new H2("Restaurante no encontrado"));
            return;
        }
        try {
            restauranteService.validarPermiso(restaurante, usuario);
        } catch (Exception ex) {
            add(new H2("Acceso denegado"), new Paragraph(ex.getMessage()));
            return;
        }
        construir();
    }

    private void construir() {
        getStyle().set("background", "#101010").set("color", "#FFFFFF");
        H2 encabezado = new H2("Administración · " + restaurante.getNombre());
        encabezado.getStyle().set("color", restaurante.getColorPrimario());
        add(encabezado, new Paragraph("Los cambios de esta pantalla afectan únicamente a este restaurante."),
                crearSeccionPersonalizacion(), crearSeccionProductos());
        Button volver = new Button("Volver a la página pública",
                e -> getUI().ifPresent(ui -> ui.navigate("restaurante/" + restaurante.getSlug())));
        add(volver);
    }

    private VerticalLayout crearSeccionPersonalizacion() {
        nombre.setValue(restaurante.getNombre());
        logo.setValue(valor(restaurante.getLogoUrl()));
        colorPrimario.setValue(restaurante.getColorPrimario());
        colorSecundario.setValue(restaurante.getColorSecundario());
        colorFondo.setValue(restaurante.getColorFondo());
        imagen.setValue(valor(restaurante.getImagenInicioUrl()));
        tituloInicio.setValue(valor(restaurante.getTituloInicio()));
        descripcionInicio.setValue(valor(restaurante.getDescripcionInicio()));
        Button guardar = new Button("Guardar diseño", e -> {
            try {
                restaurante = restauranteService.actualizarPersonalizacion(
                        restaurante.getSlug(), usuario, nombre.getValue(), logo.getValue(),
                        colorPrimario.getValue(), colorSecundario.getValue(), colorFondo.getValue(),
                        imagen.getValue(), tituloInicio.getValue(), descripcionInicio.getValue());
                Notification.show("Personalización guardada.");
                getUI().ifPresent(ui -> ui.getPage().reload());
            } catch (Exception ex) {
                Notification.show(ex.getMessage(), 5000, Notification.Position.MIDDLE);
            }
        });
        VerticalLayout seccion = new VerticalLayout(new H2("Personalización de la página"),
                new Paragraph("Usa enlaces de imágenes públicas y colores hexadecimales, por ejemplo #D6AD55."),
                nombre, logo, new HorizontalLayout(colorPrimario, colorSecundario, colorFondo),
                imagen, tituloInicio, descripcionInicio, guardar);
        seccion.getStyle().set("background", "#1B1B1B").set("padding", "20px")
                .set("border-radius", "12px").set("border", "1px solid #393939");
        return seccion;
    }

    private VerticalLayout crearSeccionProductos() {
        H2 titulo = new H2("Menú y productos");
        productoNombre.setWidthFull();
        productoDescripcion.setWidthFull();
        precio.setPrefixComponent(new com.vaadin.flow.component.html.Span("$"));
        precio.setMin(BigDecimal.ZERO);
        stock.setMin(0);
        Button agregar = new Button("Agregar producto", e -> agregarProducto());
        HorizontalLayout campos = new HorizontalLayout(productoNombre, precio, stock, categoria);
        campos.setWidthFull();
        campos.setFlexWrap(FlexWrap.WRAP);
        productos.addColumn(Producto::getNombre).setHeader("Producto").setAutoWidth(true);
        productos.addColumn(p -> p.getCategoria() == null ? "-" : p.getCategoria()).setHeader("Categoría").setAutoWidth(true);
        productos.addColumn(p -> p.getPrecio() == null ? "$0" : p.getPrecio().toString()).setHeader("Precio").setAutoWidth(true);
        productos.addColumn(Producto::getStock).setHeader("Stock").setAutoWidth(true);
        productos.addComponentColumn(p -> new Button("Eliminar", e -> eliminarProducto(p)))
                .setHeader("Acciones").setAutoWidth(true);
        productos.setWidthFull();
        productos.setHeight("360px");
        VerticalLayout seccion = new VerticalLayout(titulo, campos, productoDescripcion, agregar, productos);
        seccion.getStyle().set("background", "#1B1B1B").set("padding", "20px")
                .set("border-radius", "12px").set("border", "1px solid #393939");
        actualizarProductos();
        return seccion;
    }

    private void agregarProducto() {
        try {
            if (productoNombre.isEmpty() || precio.isEmpty() || precio.getValue() == null
                    || stock.getValue() == null) {
                Notification.show("Completa nombre, precio y existencias.");
                return;
            }
            Producto p = new Producto();
            p.setNombre(productoNombre.getValue().trim());
            p.setDescripcion(productoDescripcion.getValue());
            p.setPrecio(precio.getValue());
            p.setStock(stock.getValue());
            p.setCategoria(categoria.getValue());
            productoService.guardar(p, restaurante.getId());
            productoNombre.clear(); productoDescripcion.clear(); precio.clear();
            stock.clear(); categoria.clear();
            actualizarProductos();
            Notification.show("Producto agregado.");
        } catch (Exception ex) {
            Notification.show(ex.getMessage(), 5000, Notification.Position.MIDDLE);
        }
    }

    private void eliminarProducto(Producto producto) {
        try {
            productoService.eliminar(producto.getId(), restaurante.getId());
            actualizarProductos();
            Notification.show("Producto eliminado.");
        } catch (Exception ex) {
            Notification.show(ex.getMessage(), 5000, Notification.Position.MIDDLE);
        }
    }

    private void actualizarProductos() {
        productos.setItems(productoService.listar(restaurante.getId()));
    }

    private String valor(String texto) { return texto == null ? "" : texto; }
}