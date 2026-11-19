package com.restaurante.view;

import com.restaurante.model.Producto;
import com.restaurante.model.Restaurante;
import com.restaurante.model.Usuario;
import com.restaurante.service.PedidoService;
import com.restaurante.service.ProductoService;
import com.restaurante.service.RestauranteService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Route("restaurante")
@PageTitle("Restaurante")
public class RestaurantePublicoView extends VerticalLayout implements HasUrlParameter<String> {
    private final RestauranteService restauranteService;
    private final ProductoService productoService;
    private final PedidoService pedidoService;
    private Restaurante restaurante;
    private final NumberFormat moneda = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));

    public RestaurantePublicoView(RestauranteService restauranteService,
                                  ProductoService productoService,
                                  PedidoService pedidoService) {
        this.restauranteService = restauranteService;
        this.productoService = productoService;
        this.pedidoService = pedidoService;
        setSizeFull();
        setPadding(false);
        setSpacing(false);
    }

    @Override
    public void setParameter(BeforeEvent event, String slug) {
        removeAll();
        restaurante = restauranteService.buscarPorSlug(slug).orElse(null);
        if (restaurante == null) {
            add(new H1("Restaurante no disponible"),
                new Paragraph("El enlace no existe o el restaurante está temporalmente inactivo."),
                new Anchor("/", "Ir al inicio"));
            return;
        }
        construirPagina();
    }

    private void construirPagina() {
        String primario = restaurante.getColorPrimario();
        String secundario = restaurante.getColorSecundario();
        String fondo = restaurante.getColorFondo();
        getStyle().set("background", fondo).set("color", "#FFFFFF")
                .set("min-height", "100vh").set("font-family", "Arial, sans-serif");

        Div header = new Div();
        header.getStyle().set("display", "flex").set("align-items", "center")
                .set("justify-content", "space-between").set("gap", "16px")
                .set("flex-wrap", "wrap").set("padding", "18px 5%")
                .set("background", secundario).set("border-bottom", "1px solid " + primario);

        Div identidad = new Div();
        identidad.getStyle().set("display", "flex").set("align-items", "center").set("gap", "12px");
        if (restaurante.getLogoUrl() != null) {
            Image logo = new Image(restaurante.getLogoUrl(), "Logo de " + restaurante.getNombre());
            logo.setWidth("54px"); logo.setHeight("54px");
            logo.getStyle().set("object-fit", "contain").set("border-radius", "8px");
            identidad.add(logo);
        }
        H2 nombre = new H2(restaurante.getNombre());
        nombre.getStyle().set("color", primario).set("margin", "0");
        identidad.add(nombre);

        HorizontalLayout acciones = new HorizontalLayout();
        acciones.setAlignItems(Alignment.CENTER);
        Anchor inicio = new Anchor("/restaurante/" + restaurante.getSlug(), "Inicio");
        inicio.getStyle().set("color", "#FFFFFF");
        Button carrito = boton("Mi carrito", primario, secundario);
        carrito.addClickListener(e -> mostrarCarrito());
        Button cuenta = boton("Cuenta ▾", "#292929", primario);
        cuenta.addClickListener(e -> mostrarCuenta());
        acciones.add(inicio, carrito, cuenta);
        header.add(identidad, acciones);

        Div hero = new Div();
        hero.getStyle().set("padding", "64px 7%").set("background", "#181818")
                .set("background-image", restaurante.getImagenInicioUrl() == null ? "none"
                        : "linear-gradient(90deg, rgba(0,0,0,.82), rgba(0,0,0,.25)), url('" + restaurante.getImagenInicioUrl() + "')")
                .set("background-size", "cover").set("background-position", "center")
                .set("border-bottom", "1px solid #333");
        H1 titulo = new H1(restaurante.getTituloInicio() == null ? "Bienvenido a " + restaurante.getNombre() : restaurante.getTituloInicio());
        titulo.getStyle().set("color", primario).set("max-width", "760px").set("font-size", "clamp(30px, 5vw, 54px)");
        Paragraph descripcion = new Paragraph(restaurante.getDescripcionInicio() == null ? "Explora nuestro menú y realiza tu pedido." : restaurante.getDescripcionInicio());
        descripcion.getStyle().set("color", "#EEEEEE").set("max-width", "680px").set("font-size", "18px");
        hero.add(titulo, descripcion);

        H2 tituloMenu = new H2("Nuestro menú");
        tituloMenu.getStyle().set("color", primario);
        Div grid = new Div();
        grid.getStyle().set("display", "grid")
                .set("grid-template-columns", "repeat(auto-fit, minmax(230px, 1fr))")
                .set("gap", "18px").set("width", "100%");
        List<Producto> productos = productoService.listar(restaurante.getId());
        if (productos.isEmpty()) {
            grid.add(new Paragraph("Este restaurante está preparando su menú. Vuelve pronto."));
        }
        for (Producto producto : productos) grid.add(tarjetaProducto(producto, primario, secundario));
        Div contenido = new Div(tituloMenu, grid);
        contenido.getStyle().set("padding", "34px 5% 60px");
        add(header, hero, contenido);
    }

    private Div tarjetaProducto(Producto producto, String primario, String secundario) {
        Div tarjeta = new Div();
        tarjeta.getStyle().set("background", secundario).set("border", "1px solid #383838")
                .set("border-radius", "14px").set("padding", "16px")
                .set("display", "flex").set("flex-direction", "column").set("gap", "10px");
        if ((producto.getImagen() != null && producto.getImagen().length > 0) || producto.getImagenUrl() != null) {
            String fuente = producto.getImagen() != null && producto.getImagen().length > 0
                    ? "data:" + (producto.getImagenTipo() == null ? "image/jpeg" : producto.getImagenTipo()) + ";base64," + Base64.getEncoder().encodeToString(producto.getImagen())
                    : producto.getImagenUrl();
            Image imagen = new Image(fuente, producto.getNombre());
            imagen.setWidthFull(); imagen.setHeight("160px");
            imagen.getStyle().set("object-fit", "cover").set("border-radius", "9px");
            tarjeta.add(imagen);
        }
        H2 nombre = new H2(producto.getNombre());
        nombre.getStyle().set("color", "#FFFFFF").set("font-size", "19px").set("margin", "0");
        Paragraph descripcion = new Paragraph(producto.getDescripcion() == null ? "" : producto.getDescripcion());
        descripcion.getStyle().set("color", "#BBBBBB").set("margin", "0").set("min-height", "42px");
        Span precio = new Span(moneda.format(producto.getPrecio()));
        precio.getStyle().set("color", primario).set("font-weight", "bold").set("font-size", "18px");
        Span stock = new Span(producto.getStock() == null || producto.getStock() <= 0 ? "Agotado" : "Disponibles: " + producto.getStock());
        stock.getStyle().set("color", "#AAAAAA").set("font-size", "12px");
        Button agregar = boton("Agregar al carrito", primario, "#111111");
        agregar.setEnabled(producto.getStock() != null && producto.getStock() > 0);
        agregar.setWidthFull();
        agregar.addClickListener(e -> {
            Map<Long, Integer> carrito = obtenerCarrito();
            int actual = carrito.getOrDefault(producto.getId(), 0);
            if (actual + 1 > producto.getStock()) {
                Notification.show("No hay más unidades disponibles.");
                return;
            }
            carrito.put(producto.getId(), actual + 1);
            Notification.show(producto.getNombre() + " agregado al carrito.");
        });
        tarjeta.add(nombre, descripcion, precio, stock, agregar);
        return tarjeta;
    }

    @SuppressWarnings("unchecked")
    private Map<Long, Integer> obtenerCarrito() {
        String clave = "carrito_restaurante_" + restaurante.getId();
        Map<Long, Integer> carrito = (Map<Long, Integer>) VaadinSession.getCurrent().getAttribute(clave);
        if (carrito == null) {
            carrito = new HashMap<>();
            VaadinSession.getCurrent().setAttribute(clave, carrito);
        }
        return carrito;
    }

    private void mostrarCarrito() {
        Map<Long, Integer> carrito = obtenerCarrito();
        Dialog dialogo = new Dialog();
        dialogo.setHeaderTitle("Carrito · " + restaurante.getNombre());
        dialogo.setWidth("min(560px, 95vw)");
        VerticalLayout contenido = new VerticalLayout();
        BigDecimal total = BigDecimal.ZERO;
        List<Producto> productos = productoService.listar(restaurante.getId());
        for (Map.Entry<Long, Integer> linea : new HashMap<>(carrito).entrySet()) {
            Producto p = productos.stream().filter(x -> x.getId().equals(linea.getKey())).findFirst().orElse(null);
            if (p == null) { carrito.remove(linea.getKey()); continue; }
            BigDecimal subtotal = p.getPrecio().multiply(BigDecimal.valueOf(linea.getValue()));
            total = total.add(subtotal);
            contenido.add(new Paragraph(p.getNombre() + " × " + linea.getValue() + " — " + moneda.format(subtotal)));
        }
        H2 totalTexto = new H2("Total: " + moneda.format(total));
        Button vaciar = new Button("Vaciar", e -> { carrito.clear(); dialogo.close(); Notification.show("Carrito vacío."); });
        Button pagar = boton("Continuar al pedido", restaurante.getColorPrimario(), "#111111");
        pagar.setEnabled(!carrito.isEmpty());
        pagar.addClickListener(e -> { dialogo.close(); mostrarFormularioPedido(); });
        contenido.add(totalTexto, new HorizontalLayout(vaciar, pagar));
        dialogo.add(contenido);
        dialogo.open();
    }

    private void mostrarFormularioPedido() {
        Usuario usuario = (Usuario) VaadinSession.getCurrent().getAttribute("usuarioActual");
        if (usuario == null) {
            VaadinSession.getCurrent().setAttribute("rutaDespuesLogin", "restaurante/" + restaurante.getSlug());
            Notification.show("Inicia sesión para completar tu pedido.");
            getUI().ifPresent(ui -> ui.navigate("login"));
            return;
        }
        Dialog dialogo = new Dialog();
        dialogo.setHeaderTitle("Datos de entrega");
        TextField direccion = new TextField("Dirección");
        direccion.setWidthFull();
        TextField telefono = new TextField("Teléfono");
        telefono.setWidthFull();
        TextArea observaciones = new TextArea("Observaciones");
        observaciones.setWidthFull();
        Button confirmar = boton("Confirmar pedido", restaurante.getColorPrimario(), "#111111");
        confirmar.addClickListener(e -> {
            try {
                pedidoService.crearPedido(usuario, direccion.getValue(), telefono.getValue(),
                        observaciones.getValue(), obtenerCarrito(), restaurante.getId());
                obtenerCarrito().clear();
                dialogo.close();
                Notification.show("Pedido registrado correctamente en " + restaurante.getNombre() + ".");
            } catch (Exception ex) {
                Notification.show(ex.getMessage() == null ? "No se pudo registrar el pedido." : ex.getMessage(), 5000, Notification.Position.MIDDLE);
            }
        });
        Button cancelar = new Button("Cancelar", e -> dialogo.close());
        dialogo.add(new VerticalLayout(direccion, telefono, observaciones, new HorizontalLayout(confirmar, cancelar)));
        dialogo.open();
    }

    private void mostrarCuenta() {
        Dialog dialogo = new Dialog();
        dialogo.setHeaderTitle("Mi cuenta");
        Usuario usuario = (Usuario) VaadinSession.getCurrent().getAttribute("usuarioActual");
        VerticalLayout opciones = new VerticalLayout();
        if (usuario == null) {
            VaadinSession.getCurrent().setAttribute("rutaDespuesLogin", "restaurante/" + restaurante.getSlug());
            opciones.add(new Anchor("/login", "Iniciar sesión"), new Anchor("/registro", "Crear cuenta"));
        } else {
            opciones.add(new Paragraph(usuario.getNombre()), new Paragraph(usuario.getCorreo()),
                    new Anchor("/restaurante-pedidos/" + restaurante.getSlug(), "Mis pedidos en este restaurante"));
            boolean puedeAdministrar = "jdptls10@gmail.com".equalsIgnoreCase(usuario.getCorreo())
                    || (restaurante.getAdministrador() != null
                    && restaurante.getAdministrador().getId().equals(usuario.getId()));
            if (puedeAdministrar) {
                opciones.add(new Anchor("/restaurante-admin/" + restaurante.getSlug(), "Administrar este restaurante"));
            }
            Button salir = new Button("Cerrar sesión", e -> {
                VaadinSession.getCurrent().setAttribute("usuarioActual", null);
                VaadinSession.getCurrent().setAttribute("carrito", null);
                VaadinSession.getCurrent().setAttribute("carrito_restaurante_" + restaurante.getId(), null);
                VaadinSession.getCurrent().setAttribute("rutaDespuesLogin", null);
                dialogo.close();
                getUI().ifPresent(ui -> ui.getPage().setLocation("/restaurante/" + restaurante.getSlug()));
            });
            opciones.add(salir);
        }
        dialogo.add(opciones);
        dialogo.open();
    }

    private Button boton(String texto, String fondo, String color) {
        Button b = new Button(texto);
        b.getStyle().set("background", fondo).set("color", color)
                .set("font-weight", "bold").set("border-radius", "8px");
        return b;
    }

}
