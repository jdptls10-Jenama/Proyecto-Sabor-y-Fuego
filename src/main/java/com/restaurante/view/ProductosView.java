
package com.restaurante.view;

import com.restaurante.model.Producto;
import com.restaurante.service.ProductoService;

import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.streams.UploadHandler;

import java.math.BigDecimal;
import java.util.Base64;

@Route("productos")
@PageTitle("Productos | Sabor & Fuego")
public class ProductosView extends VerticalLayout {

    private final ProductoService productoService;

    private final TextField nombre =
            new TextField("Nombre del producto");

    private final TextArea descripcion =
            new TextArea("Descripción");

    private final BigDecimalField precio =
            new BigDecimalField("Precio");

    private final IntegerField stock =
            new IntegerField("Cantidad disponible");

    private final Grid<Producto> grid =
            new Grid<>(Producto.class, false);

    private byte[] imagenSeleccionada;
    private String tipoImagenSeleccionada;

    private final Image vistaPrevia =
            new Image("", "Vista previa del producto");

    private final String dorado = "#D6AD55";
    private final String fondo = "#101010";
    private final String tarjeta = "#1B1B1B";

    public ProductosView(ProductoService productoService) {
        this.productoService = productoService;

        configurarPagina();
        configurarCampos();
        configurarTabla();

        Button volver = new Button(
                "Volver al panel",
                event -> getUI().ifPresent(ui -> ui.navigate("admin"))
        );
        estiloBotonSecundario(volver);

        H2 titulo = new H2("Administración de productos");
        titulo.getStyle()
                .set("color", "#FFFFFF")
                .set("margin", "0")
                .set("font-size", "26px");

        Span subtitulo = new Span(
                "Registra, organiza y administra el menú del restaurante"
        );
        subtitulo.getStyle()
                .set("color", "#BBBBBB")
                .set("font-size", "14px");

        VerticalLayout textoEncabezado =
                new VerticalLayout(titulo, subtitulo);
        textoEncabezado.setPadding(false);
        textoEncabezado.setSpacing(false);

        HorizontalLayout encabezado =
                new HorizontalLayout(textoEncabezado, volver);
        encabezado.setWidthFull();
        encabezado.setAlignItems(Alignment.CENTER);
        encabezado.setJustifyContentMode(JustifyContentMode.BETWEEN);
        encabezado.getStyle()
                .set("flex-wrap", "wrap")
                .set("padding", "5px 0 10px 0")
                .set("background", "transparent");

        VerticalLayout formulario = crearFormulario();

        VerticalLayout seccionTabla = new VerticalLayout(
                crearTituloSeccion("Productos registrados"),
                grid
        );
        estiloTarjeta(seccionTabla);
        seccionTabla.setWidthFull();
        seccionTabla.setPadding(true);
        seccionTabla.setSpacing(true);

        add(encabezado, formulario, seccionTabla);
        actualizarTabla();
    }

    private void configurarPagina() {
        setWidthFull();
        setMinHeight("100vh");
        setPadding(true);
        setSpacing(true);
        setAlignItems(Alignment.STRETCH);

        getElement().getThemeList().add("dark");

        getStyle()
                .set("background-color", fondo)
                .set("background-image", "none")
                .set("box-shadow", "none")
                .set("border", "none")
                .set("box-sizing", "border-box")
                .set("padding", "20px")
                .set("color", "#FFFFFF");
    }

    private void configurarCampos() {
        nombre.setWidthFull();
        precio.setWidthFull();
        stock.setWidthFull();

        descripcion.setWidthFull();
        descripcion.setHeight("70px");

        precio.setPrefixComponent(new Span("$"));
        stock.setMin(0);

        nombre.setPlaceholder("Ej. Hamburguesa especial");
        descripcion.setPlaceholder(
                "Describe los ingredientes del producto"
        );
        precio.setPlaceholder("0.00");
        stock.setPlaceholder("0");

        nombre.getElement().getThemeList().add("dark");
        precio.getElement().getThemeList().add("dark");
        stock.getElement().getThemeList().add("dark");
        descripcion.getElement().getThemeList().add("dark");

        estilizarCampo(nombre);
        estilizarCampo(descripcion);
        estilizarCampo(precio);
        estilizarCampo(stock);

        vistaPrevia.setWidth("130px");
        vistaPrevia.setHeight("95px");
        vistaPrevia.getStyle()
                .set("object-fit", "contain")
                .set("border-radius", "10px")
                .set("background-color", "#252525")
                .set("background-image", "none")
                .set("padding", "5px");
        vistaPrevia.setVisible(false);
    }

    private void estilizarCampo(HasStyle campo) {
        campo.getElement()
                .getClassList()
                .add("producto-admin-campo");

        campo.getStyle()
                .set("background", "transparent")
                .set("--vaadin-input-field-background", "#252525")
                .set("--vaadin-input-field-border-color", "#555555")
                .set("--vaadin-input-field-value-color", "#FFFFFF")
                .set("--vaadin-input-field-label-color", "#D6AD55")
                .set("--vaadin-input-field-placeholder-color", "#AAAAAA")
                .set("--lumo-primary-color", "#D6AD55")
                .set("--lumo-primary-color-50pct",
                        "rgba(214, 173, 85, 0.5)")
                .set("--lumo-primary-color-10pct",
                        "rgba(214, 173, 85, 0.1)")
                .set("--lumo-focus-ring-color", "transparent")
                .set("--vaadin-focus-ring-color", "transparent");
    }

    private VerticalLayout crearFormulario() {
        Upload subirImagen = crearSelectorImagen();

        Button guardar = new Button(
                "Guardar producto",
                event -> guardarProducto()
        );
        estiloBotonPrincipal(guardar);

        HorizontalLayout fila = new HorizontalLayout(
                nombre,
                precio,
                stock
        );
        fila.setWidthFull();
        fila.setPadding(false);
        fila.setSpacing(false);
        fila.addClassName("productos-formulario-fila");

        fila.getStyle()
                .set("display", "grid")
                .set("grid-template-columns",
                        "minmax(0, 2fr) minmax(0, 1fr) minmax(0, 1fr)")
                .set("column-gap", "30px")
                .set("row-gap", "8px")
                .set("align-items", "end")
                .set("background", "transparent")
                .set("min-width", "0");

        VerticalLayout imagenes = new VerticalLayout(
                new Span("Imagen del producto"),
                subirImagen,
                vistaPrevia
        );
        imagenes.setPadding(false);
        imagenes.setSpacing(false);
        imagenes.setAlignItems(Alignment.START);
        imagenes.addClassName("productos-imagenes");

        imagenes.getStyle()
                .set("background", "transparent")
                .set("box-shadow", "none")
                .set("margin-top", "100px")
                .set("gap", "2px");

        VerticalLayout formulario = new VerticalLayout(
                crearTituloSeccion("Registrar nuevo producto"),
                fila,
                descripcion,
                imagenes,
                guardar
        );

        estiloTarjeta(formulario);
        formulario.setWidthFull();
        formulario.setPadding(true);
        formulario.setSpacing(false);
        formulario.addClassName("productos-formulario");

        formulario.getStyle()
                .set("gap", "6px");

        return formulario;
    }

    private H3 crearTituloSeccion(String texto) {
        H3 titulo = new H3(texto);
        titulo.getStyle()
                .set("color", dorado)
                .set("margin-top", "0")
                .set("margin-bottom", "0")
                .set("font-size", "19px");
        return titulo;
    }

    private void estiloTarjeta(VerticalLayout contenedor) {
        contenedor.getStyle()
                .set("background-color", tarjeta)
                .set("background-image", "none")
                .set("border", "1px solid #333333")
                .set("border-radius", "16px")
                .set("padding", "12px 16px")
                .set("box-sizing", "border-box")
                .set("box-shadow", "none");
    }

    private void estiloBotonPrincipal(Button boton) {
        boton.getStyle()
                .set("background", dorado)
                .set("color", "#111111")
                .set("font-weight", "bold")
                .set("border-radius", "9px")
                .set("padding", "10px 20px")
                .set("cursor", "pointer")
                .set("box-shadow", "none");
    }

    private void estiloBotonSecundario(Button boton) {
        boton.getStyle()
                .set("background", "#252525")
                .set("color", dorado)
                .set("border", "1px solid " + dorado)
                .set("border-radius", "9px")
                .set("font-weight", "bold")
                .set("box-shadow", "none");
    }

    private boolean esImagenValida(String tipo) {
        return tipo != null
                && (tipo.equalsIgnoreCase("image/jpeg")
                || tipo.equalsIgnoreCase("image/png"));
    }

    private Upload crearSelectorImagen() {
        Upload upload = new Upload(
                UploadHandler.inMemory((metadata, data) -> {
                    String tipo = metadata.contentType();

                    if (!esImagenValida(tipo)) {
                        imagenSeleccionada = null;
                        tipoImagenSeleccionada = null;
                        vistaPrevia.setSrc("");
                        vistaPrevia.setVisible(false);

                        Notification.show(
                                "Selecciona una imagen JPG o PNG"
                        );
                        return;
                    }

                    imagenSeleccionada = data;
                    tipoImagenSeleccionada = tipo;

                    String base64 = Base64.getEncoder()
                            .encodeToString(data);

                    vistaPrevia.setSrc(
                            "data:" + tipo + ";base64," + base64
                    );
                    vistaPrevia.setVisible(true);

                    Notification.show(
                            "Imagen cargada correctamente"
                    );
                })
        );

        upload.setWidth("min(100%, 420px)");
        upload.getStyle()
                .set("box-sizing", "border-box")
                .set("margin-top", "4px")
                .set("background", "transparent")
                .set("--lumo-primary-color", "#D6AD55")
                .set("--lumo-primary-text-color", "#D6AD55");

        upload.setAcceptedFileTypes("image/jpeg", "image/png");
        upload.setMaxFiles(1);
        upload.setMaxFileSize(5 * 1024 * 1024);

        upload.addFileRejectedListener(event ->
                Notification.show(
                        "Imagen no válida o superior a 5 MB"
                )
        );

        return upload;
    }

    private String obtenerImagen(Producto producto) {
        if (producto.getImagen() == null
                || producto.getImagen().length == 0) {
            return null;
        }

        String tipo = producto.getImagenTipo();
        if (!esImagenValida(tipo)) {
            tipo = "image/jpeg";
        }

        String base64 = Base64.getEncoder()
                .encodeToString(producto.getImagen());

        return "data:" + tipo + ";base64," + base64;
    }

    private Image crearImagen(
            Producto producto,
            String ancho,
            String alto
    ) {
        String fuente = obtenerImagen(producto);

        Image imagen = new Image(
                fuente != null ? fuente : "",
                producto.getNombre()
        );

        imagen.setWidth(ancho);
        imagen.setHeight(alto);

        imagen.getStyle()
                .set("object-fit", "contain")
                .set("border-radius", "8px")
                .set("background-color", "#252525")
                .set("background-image", "none");

        return imagen;
    }

    private void configurarTabla() {
        grid.addColumn(Producto::getId)
                .setHeader("ID")
                .setAutoWidth(true);

        grid.addColumn(Producto::getNombre)
                .setHeader("Producto")
                .setAutoWidth(true);

        grid.addColumn(Producto::getDescripcion)
                .setHeader("Descripción")
                .setFlexGrow(2);

        grid.addColumn(producto ->
                        "$ " + producto.getPrecio().toPlainString()
                )
                .setHeader("Precio")
                .setAutoWidth(true);

        grid.addColumn(Producto::getStock)
                .setHeader("Stock")
                .setAutoWidth(true);

        grid.addComponentColumn(producto -> {
                    if (producto.getImagen() == null
                            || producto.getImagen().length == 0) {
                        Span sinImagen = new Span("Sin imagen");
                        sinImagen.getStyle().set("color", "#BBBBBB");
                        return sinImagen;
                    }
                    return crearImagen(producto, "60px", "50px");
                })
                .setHeader("Imagen")
                .setAutoWidth(true);

        grid.addComponentColumn(producto -> {
                    Button opciones = new Button("⋮");
                    opciones.getStyle()
                            .set("color", dorado)
                            .set("font-size", "22px")
                            .set("background", "#292929")
                            .set("border-radius", "8px")
                            .set("box-shadow", "none");

                    ContextMenu menu = new ContextMenu(opciones);
                    menu.setOpenOnClick(true);

                    menu.addItem("Cambiar imagen", event ->
                            abrirEditorImagen(producto)
                    );

                    menu.addItem("Eliminar imagen", event -> {
                        producto.setImagen(null);
                        producto.setImagenTipo(null);
                        productoService.guardar(producto);
                        actualizarTabla();
                        Notification.show(
                                "Imagen eliminada correctamente"
                        );
                    });

                    return opciones;
                })
                .setHeader("Opciones")
                .setAutoWidth(true);

        grid.addComponentColumn(producto -> {
                    Button editar = new Button(
                            "Editar",
                            event -> abrirEditorProducto(producto)
                    );
                    editar.getStyle()
                            .set("background", "#D6AD55")
                            .set("color", "#111111")
                            .set("border-radius", "7px")
                            .set("font-weight", "bold")
                            .set("box-shadow", "none");
                    return editar;
                })
                .setHeader("Editar")
                .setAutoWidth(true);

        grid.addComponentColumn(producto -> {
                    Button eliminar = new Button("Eliminar");
                    eliminar.getStyle()
                            .set("background", "#7D2929")
                            .set("color", "#FFFFFF")
                            .set("border-radius", "7px")
                            .set("font-weight", "bold")
                            .set("box-shadow", "none");

                    eliminar.addClickListener(event -> {
                        productoService.eliminar(producto.getId());
                        actualizarTabla();
                        Notification.show("Producto eliminado");
                    });
                    return eliminar;
                })
                .setHeader("Acciones")
                .setAutoWidth(true);

        grid.setWidthFull();
        grid.setHeight("430px");
        grid.addThemeVariants(
                GridVariant.LUMO_ROW_STRIPES,
                GridVariant.LUMO_WRAP_CELL_CONTENT
        );
        grid.getElement().getThemeList().add("dark");

        grid.getStyle()
                .set("background-color", tarjeta)
                .set("background-image", "none")
                .set("border", "none")
                .set("border-radius", "10px")
                .set("box-shadow", "none")
                .set("--lumo-primary-color", dorado)
                .set("--lumo-primary-color-50pct",
                        "rgba(214, 173, 85, 0.5)")
                .set("--lumo-primary-color-10pct",
                        "rgba(214, 173, 85, 0.1)")
                .set("--lumo-focus-ring-color", "transparent")
                .set("--lumo-base-color", tarjeta)
                .set("--lumo-body-text-color", "#FFFFFF")
                .set("--lumo-header-text-color", dorado);
    }

    private void abrirEditorProducto(Producto producto) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Editar producto");
        dialog.setWidth("500px");

        TextField nombreEditar =
                new TextField("Nombre del producto");
        nombreEditar.setWidthFull();
        nombreEditar.setValue(producto.getNombre());

        TextArea descripcionEditar = new TextArea("Descripción");
        descripcionEditar.setWidthFull();
        descripcionEditar.setHeight("90px");
        descripcionEditar.setValue(
                producto.getDescripcion() != null
                        ? producto.getDescripcion()
                        : ""
        );

        BigDecimalField precioEditar = new BigDecimalField("Precio");
        precioEditar.setWidthFull();
        precioEditar.setPrefixComponent(new Span("$"));
        precioEditar.setValue(producto.getPrecio());

        IntegerField stockEditar =
                new IntegerField("Cantidad disponible");
        stockEditar.setWidthFull();
        stockEditar.setMin(0);
        stockEditar.setValue(
                producto.getStock() != null
                        ? producto.getStock()
                        : 0
        );

        nombreEditar.getElement().getThemeList().add("dark");
        descripcionEditar.getElement().getThemeList().add("dark");
        precioEditar.getElement().getThemeList().add("dark");
        stockEditar.getElement().getThemeList().add("dark");

        estilizarCampo(nombreEditar);
        estilizarCampo(descripcionEditar);
        estilizarCampo(precioEditar);
        estilizarCampo(stockEditar);

        Button guardarCambios = new Button(
                "Guardar cambios",
                event -> {
                    if (nombreEditar.isEmpty()
                            || precioEditar.isEmpty()
                            || stockEditar.isEmpty()) {
                        Notification.show(
                                "Completa el nombre, precio y cantidad"
                        );
                        return;
                    }

                    if (precioEditar.getValue().compareTo(BigDecimal.ZERO) <= 0
                            || stockEditar.getValue() < 0) {
                        Notification.show(
                                "El precio debe ser mayor que cero y el stock no puede ser negativo"
                        );
                        return;
                    }

                    producto.setNombre(nombreEditar.getValue().trim());
                    producto.setDescripcion(
                            descripcionEditar.getValue()
                    );
                    producto.setPrecio(precioEditar.getValue());
                    producto.setStock(stockEditar.getValue());

                    productoService.guardar(producto);
                    actualizarTabla();
                    dialog.close();

                    Notification.show(
                            "Producto actualizado correctamente"
                    );
                }
        );
        estiloBotonPrincipal(guardarCambios);

        Button cancelar = new Button(
                "Cancelar",
                event -> dialog.close()
        );
        estiloBotonSecundario(cancelar);

        HorizontalLayout botones =
                new HorizontalLayout(guardarCambios, cancelar);
        botones.setWidthFull();
        botones.getStyle().set("flex-wrap", "wrap");

        VerticalLayout contenido = new VerticalLayout(
                nombreEditar,
                descripcionEditar,
                precioEditar,
                stockEditar,
                botones
        );
        contenido.setWidthFull();
        contenido.setPadding(true);
        contenido.setSpacing(false);
        contenido.getStyle().set("gap", "12px");

        contenido.getElement().getThemeList().add("dark");
        contenido.getStyle()
                .set("background-color", fondo)
                .set("color", "#FFFFFF");

        dialog.add(contenido);
        dialog.open();
    }

    private void abrirEditorImagen(Producto producto) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Imagen de " + producto.getNombre());
        dialog.setWidth("420px");

        Image preview = new Image(
                "",
                "Vista previa de la imagen"
        );
        preview.setWidth("220px");
        preview.setHeight("180px");
        preview.getStyle()
                .set("object-fit", "contain")
                .set("background-color", "#252525")
                .set("border-radius", "10px");

        String imagenActual = obtenerImagen(producto);
        if (imagenActual != null) {
            preview.setSrc(imagenActual);
        }

        byte[][] nuevaImagen = {null};
        String[] nuevoTipo = {null};

        Upload cambiarImagen = new Upload(
                UploadHandler.inMemory((metadata, data) -> {
                    String tipo = metadata.contentType();

                    if (!esImagenValida(tipo)) {
                        nuevaImagen[0] = null;
                        nuevoTipo[0] = null;
                        Notification.show(
                                "Selecciona una imagen JPG o PNG"
                        );
                        return;
                    }

                    nuevaImagen[0] = data;
                    nuevoTipo[0] = tipo;

                    String base64 = Base64.getEncoder()
                            .encodeToString(data);
                    preview.setSrc(
                            "data:" + tipo + ";base64," + base64
                    );

                    Notification.show("Vista previa actualizada");
                })
        );

        cambiarImagen.setAcceptedFileTypes(
                "image/jpeg", "image/png"
        );
        cambiarImagen.setMaxFiles(1);
        cambiarImagen.setMaxFileSize(5 * 1024 * 1024);

        Button guardarImagen = new Button(
                "Guardar imagen",
                event -> {
                    if (nuevaImagen[0] == null) {
                        Notification.show(
                                "Selecciona una imagen primero"
                        );
                        return;
                    }

                    producto.setImagen(nuevaImagen[0]);
                    producto.setImagenTipo(nuevoTipo[0]);
                    productoService.guardar(producto);
                    actualizarTabla();
                    dialog.close();

                    Notification.show(
                            "Imagen del producto actualizada"
                    );
                }
        );
        estiloBotonPrincipal(guardarImagen);

        Button cancelar = new Button(
                "Cancelar",
                event -> dialog.close()
        );
        estiloBotonSecundario(cancelar);

        VerticalLayout contenido = new VerticalLayout(
                new H2("Vista previa"),
                preview,
                new H3("Seleccionar nueva imagen"),
                cambiarImagen,
                new HorizontalLayout(guardarImagen, cancelar)
        );
        contenido.setPadding(true);
        contenido.setSpacing(true);
        contenido.setAlignItems(Alignment.CENTER);
        contenido.getElement().getThemeList().add("dark");
        contenido.getStyle()
                .set("background-color", fondo)
                .set("background-image", "none")
                .set("color", "#FFFFFF");

        dialog.add(contenido);
        dialog.open();
    }

    private void guardarProducto() {
        if (nombre.isEmpty()
                || precio.isEmpty()
                || stock.isEmpty()) {
            Notification.show(
                    "Completa el nombre, precio y cantidad"
            );
            return;
        }

        if (precio.getValue().compareTo(BigDecimal.ZERO) <= 0
                || stock.getValue() < 0) {
            Notification.show("Revisa el precio y el stock");
            return;
        }

        Producto producto = new Producto(
                nombre.getValue().trim(),
                descripcion.getValue(),
                precio.getValue(),
                stock.getValue()
        );

        producto.setImagen(imagenSeleccionada);
        producto.setImagenTipo(tipoImagenSeleccionada);

        productoService.guardar(producto);
        Notification.show("Producto guardado correctamente");

        nombre.clear();
        descripcion.clear();
        precio.clear();
        stock.clear();

        imagenSeleccionada = null;
        tipoImagenSeleccionada = null;
        vistaPrevia.setSrc("");
        vistaPrevia.setVisible(false);

        actualizarTabla();
    }

    private void actualizarTabla() {
        grid.setItems(productoService.listar());
    }
}
