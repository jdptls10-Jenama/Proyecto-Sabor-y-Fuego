package com.restaurante.view;

import com.restaurante.model.Producto;
import com.restaurante.model.Restaurante;
import com.restaurante.model.Usuario;
import com.restaurante.service.ProductoService;
import com.restaurante.service.CategoriaService;
import com.restaurante.service.PedidoService;
import com.restaurante.model.Pedido;
import com.vaadin.flow.component.combobox.ComboBox;
import com.restaurante.service.RestauranteService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
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
    private final CategoriaService categoriaService;
    private final PedidoService pedidoService;
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
    private final TextField imagenProducto = new TextField("URL de imagen del producto");

    public RestauranteAdminView(RestauranteService restauranteService, ProductoService productoService, CategoriaService categoriaService, PedidoService pedidoService) {
        this.restauranteService = restauranteService;
        this.productoService = productoService;
        this.categoriaService = categoriaService;
        this.pedidoService = pedidoService;
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
                crearSeccionPersonalizacion(), crearSeccionProductos(), crearSeccionPedidos());
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
        stock.setMin(0);
        Button agregar = new Button("Agregar producto", e -> agregarProducto());
        HorizontalLayout campos = new HorizontalLayout(productoNombre, precio, stock, categoria);
        campos.setWidthFull();
        campos.setFlexWrap(FlexWrap.WRAP);
        productos.addColumn(Producto::getNombre).setHeader("Producto").setAutoWidth(true);
        productos.addColumn(p -> p.getCategoria() == null ? "-" : p.getCategoria()).setHeader("Categoría").setAutoWidth(true);
        productos.addColumn(p -> p.getPrecio() == null ? "$0" : p.getPrecio().toString()).setHeader("Precio").setAutoWidth(true);
        productos.addColumn(Producto::getStock).setHeader("Stock").setAutoWidth(true);
        productos.addComponentColumn(p -> new HorizontalLayout(
                new Button("Editar", e -> editarProducto(p)),
                new Button("Eliminar", e -> eliminarProducto(p))))
                .setHeader("Acciones").setAutoWidth(true);
        productos.setWidthFull();
        productos.setHeight("360px");
        VerticalLayout seccion = new VerticalLayout(titulo, new Paragraph("Las categorías se crean automáticamente al escribir un nombre nuevo en el campo Categoría."), campos, productoDescripcion, imagenProducto, agregar, productos);
        seccion.getStyle().set("background", "#1B1B1B").set("padding", "20px")
                .set("border-radius", "12px").set("border", "1px solid #393939");
        actualizarProductos();
        return seccion;
    }

    private VerticalLayout crearSeccionPedidos() {
        H2 titulo = new H2("Pedidos de este restaurante");
        Grid<Pedido> tabla = new Grid<>(Pedido.class, false);
        tabla.addColumn(Pedido::getId).setHeader("Pedido").setAutoWidth(true);
        tabla.addColumn(p -> p.getUsuario() == null ? "-" : p.getUsuario().getNombre())
                .setHeader("Cliente").setAutoWidth(true);
        tabla.addColumn(p -> p.getFechaPedido() == null ? "-" : p.getFechaPedido().toString())
                .setHeader("Fecha").setAutoWidth(true);
        tabla.addColumn(p -> p.getTotal() == null ? "0" : p.getTotal().toString())
                .setHeader("Total").setAutoWidth(true);
        tabla.addColumn(Pedido::getEstado).setHeader("Estado").setAutoWidth(true);
        tabla.addComponentColumn(pedido -> {
            ComboBox<String> estado = new ComboBox<>();
            estado.setItems("PENDIENTE", "EN_PREPARACION", "EN_CAMINO", "ENTREGADO");
            estado.setValue(pedido.getEstado());
            estado.setWidth("180px");
            Button guardar = new Button("Actualizar", e -> {
                try {
                    pedidoService.actualizarEstado(pedido.getId(), estado.getValue(), restaurante.getId());
                    actualizarPedidos(tabla);
                    Notification.show("Estado del pedido actualizado.");
                } catch (Exception ex) {
                    Notification.show(ex.getMessage(), 5000, Notification.Position.MIDDLE);
                }
            });
            return new HorizontalLayout(estado, guardar);
        }).setHeader("Gestionar").setAutoWidth(true);
        tabla.setWidthFull();
        tabla.setHeight("360px");
        VerticalLayout seccion = new VerticalLayout(titulo, tabla);
        seccion.getStyle().set("background", "#1B1B1B").set("padding", "20px")
                .set("border-radius", "12px").set("border", "1px solid #393939");
        actualizarPedidos(tabla);
        return seccion;
    }

    private void actualizarPedidos(Grid<Pedido> tabla) {
        tabla.setItems(pedidoService.listarPedidos(restaurante.getId()));
    }

    private void agregarProducto() {
        try {
            if (productoNombre.isEmpty() || precio.isEmpty() || precio.getValue() == null
                    || precio.getValue().compareTo(BigDecimal.ZERO) < 0 || stock.getValue() == null || stock.getValue() < 0) {
                Notification.show("Completa nombre, precio y existencias.");
                return;
            }
            Producto p = new Producto();
            p.setNombre(productoNombre.getValue().trim());
            p.setDescripcion(productoDescripcion.getValue());
            p.setPrecio(precio.getValue());
            p.setStock(stock.getValue());
            p.setCategoria(categoria.getValue());
            if (!imagenProducto.isEmpty()) {
                String urlImagen = imagenProducto.getValue().trim();
                if (!urlImagen.startsWith("https://") || urlImagen.indexOf(34) >= 0 || urlImagen.indexOf(39) >= 0 || urlImagen.indexOf(92) >= 0 || urlImagen.contains("(") || urlImagen.contains(")")) {
                    throw new IllegalArgumentException("La imagen debe tener una URL HTTPS válida.");
                }
                p.setImagenUrl(urlImagen);
            }
            p.setCategoriaRelacion(categoriaService.obtenerOCrear(categoria.getValue(), restaurante.getId()));
            productoService.guardar(p, restaurante.getId());
            productoNombre.clear(); productoDescripcion.clear(); precio.clear();
            stock.clear(); categoria.clear(); imagenProducto.clear();
            actualizarProductos();
            Notification.show("Producto agregado.");
        } catch (Exception ex) {
            Notification.show(ex.getMessage(), 5000, Notification.Position.MIDDLE);
        }
    }

    private void editarProducto(Producto producto) {
        Dialog dialogo = new Dialog();
        dialogo.setHeaderTitle("Editar producto");
        TextField nombreEditar = new TextField("Nombre");
        nombreEditar.setValue(producto.getNombre() == null ? "" : producto.getNombre());
        TextArea descripcionEditar = new TextArea("Descripción");
        descripcionEditar.setValue(producto.getDescripcion() == null ? "" : producto.getDescripcion());
        BigDecimalField precioEditar = new BigDecimalField("Precio");
        precioEditar.setValue(producto.getPrecio());
        IntegerField stockEditar = new IntegerField("Existencias");
        stockEditar.setValue(producto.getStock());
        TextField categoriaEditar = new TextField("Categoría");
        categoriaEditar.setValue(producto.getCategoria() == null ? "" : producto.getCategoria());
        TextField imagenEditar = new TextField("URL de imagen HTTPS");
        imagenEditar.setValue(producto.getImagenUrl() == null ? "" : producto.getImagenUrl());
        Button guardar = new Button("Guardar cambios", e -> {
            try {
                if (nombreEditar.isEmpty() || precioEditar.getValue() == null
                        || precioEditar.getValue().compareTo(BigDecimal.ZERO) < 0
                        || stockEditar.getValue() == null || stockEditar.getValue() < 0) {
                    Notification.show("Completa nombre, precio y existencias.");
                    return;
                }
                producto.setNombre(nombreEditar.getValue().trim());
                producto.setDescripcion(descripcionEditar.getValue());
                producto.setPrecio(precioEditar.getValue());
                producto.setStock(stockEditar.getValue());
                producto.setCategoria(categoriaEditar.getValue());
                producto.setCategoriaRelacion(categoriaService.obtenerOCrear(categoriaEditar.getValue(), restaurante.getId()));
                String url = imagenEditar.getValue() == null ? "" : imagenEditar.getValue().trim();
                if (!url.isBlank() && (!url.startsWith("https://") || url.indexOf(34) >= 0
                        || url.indexOf(39) >= 0 || url.indexOf(92) >= 0 || url.contains("(") || url.contains(")"))) {
                    throw new IllegalArgumentException("La imagen debe tener una URL HTTPS válida.");
                }
                producto.setImagenUrl(url.isBlank() ? null : url);
                productoService.guardar(producto, restaurante.getId());
                dialogo.close();
                actualizarProductos();
                Notification.show("Producto actualizado.");
            } catch (Exception ex) {
                Notification.show(ex.getMessage(), 5000, Notification.Position.MIDDLE);
            }
        });
        Button cancelar = new Button("Cancelar", e -> dialogo.close());
        VerticalLayout formulario = new VerticalLayout(nombreEditar, descripcionEditar, precioEditar,
                stockEditar, categoriaEditar, imagenEditar, new HorizontalLayout(guardar, cancelar));
        formulario.setWidth("min(520px, 90vw)");
        dialogo.add(formulario);
        dialogo.open();
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