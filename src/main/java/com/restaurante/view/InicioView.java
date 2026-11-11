
package com.restaurante.view;

import com.restaurante.model.Producto;
import com.restaurante.service.ProductoService;
import com.restaurante.model.Usuario;

import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Route("")
@PageTitle("Sabor & Fuego | Restaurante")
public class InicioView extends Div {

    private final ProductoService productoService;

    public InicioView(ProductoService productoService) {

        this.productoService = productoService;

        addClassName("restaurant-page");

        add(
                crearEncabezado(),
                crearPortada(),
                crearNosotros(),
                crearMenu(),
                crearPieDePagina()
        );
    }

    // ENCABEZADO Y NAVEGACIÓN


    private Div crearEncabezado() {

        Div encabezado = new Div();
        encabezado.addClassName("restaurant-header");

        Anchor logo = new Anchor("/", "SABOR & FUEGO");
        logo.addClassName("restaurant-logo");

        Div navegacion = new Div();
        navegacion.addClassName("restaurant-nav");

        navegacion.add(
                crearEnlace("Inicio", "#inicio"),
                crearEnlace("Menú", "#menu"),
                crearEnlace("Contacto", "#contacto")
        );

        Div acciones = new Div();
        acciones.addClassName("header-actions");

        Anchor carrito = new Anchor("/carrito", "Mi carrito");
        carrito.addClassName("login-button");

        acciones.add(carrito);

        Usuario usuarioActual = (Usuario) VaadinSession.getCurrent()
                .getAttribute("usuarioActual");

        if (usuarioActual == null) {

            VaadinSession.getCurrent()
                    .setAttribute("rutaDespuesLogin", "");

            Anchor iniciarSesion = new Anchor("/login", "Iniciar sesión");
            iniciarSesion.addClassName("login-button");

            Anchor registrarse = new Anchor("/registro", "Registrarse");
            registrarse.addClassName("gold-button");

            acciones.add(iniciarSesion, registrarse);

        } else if ("CLIENTE".equalsIgnoreCase(usuarioActual.getRol())) {

            Anchor misPedidos = new Anchor("/mis-pedidos", "Mis pedidos");
            misPedidos.addClassName("gold-button");

            acciones.add(misPedidos);

        } else if ("ADMIN".equalsIgnoreCase(usuarioActual.getRol())) {

            Anchor panelAdmin = new Anchor("/admin", "Panel administrativo");
            panelAdmin.addClassName("gold-button");

            acciones.add(panelAdmin);
        }

// BOTÓN CERRAR SESIÓN
        if (usuarioActual != null) {

            Button cerrarSesion = new Button("Cerrar sesión");

            cerrarSesion.addClassName("login-button");


            cerrarSesion.addClickListener(event -> {

                // Limpiar los datos de la sesión
                VaadinSession.getCurrent().setAttribute("usuarioActual", null);
                VaadinSession.getCurrent().setAttribute("carrito", null);
                VaadinSession.getCurrent().setAttribute("rutaDespuesLogin", null);

                // Recargar completamente la página principal
                getUI().ifPresent(ui -> ui.getPage().setLocation("/"));

            });


            acciones.add(cerrarSesion);
        }

        encabezado.add(logo, navegacion, acciones);

        return encabezado;
    }


    // PORTADA PRINCIPAL

    private Div crearPortada() {

        Div portada = new Div();
        portada.addClassName("restaurant-hero");
        portada.setId("inicio");

        Div contenido = new Div();
        contenido.addClassName("hero-content");

        Span etiqueta = new Span("COCINA • SABOR • EXPERIENCIA");
        etiqueta.addClassName("hero-label");

        H1 titulo = new H1(
                "Una experiencia gastronómica que despierta tus sentidos"
        );
        titulo.addClassName("hero-title");

        Paragraph descripcion = new Paragraph(
                "Descubre una propuesta gastronómica especial, " +
                        "con ingredientes seleccionados y platos preparados " +
                        "para disfrutar cada momento."
        );
        descripcion.addClassName("hero-description");

        Anchor boton = new Anchor("#menu", "DESCUBRIR EL MENÚ");
        boton.addClassName("gold-button");

        contenido.add(etiqueta, titulo, descripcion, boton);

        Image fotografia = new Image(
                "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?w=1400&q=85",
                "Interior elegante de un restaurante"
        );
        fotografia.addClassName("hero-image");

        portada.add(contenido, fotografia);

        return portada;
    }

    // CATÁLOGO DEL MENÚ

    private Div crearMenu() {

        Div seccion = new Div();
        seccion.addClassName("menu-section");
        seccion.setId("menu");

        Div etiqueta = new Div();
        etiqueta.setText("NUESTRO RESTAURANTE");
        etiqueta.addClassName("section-label");

        H1 titulo = new H1("Descubre nuestro menú");
        titulo.addClassName("section-title");

        Paragraph descripcion = new Paragraph(
                "Explora nuestros platos y encuentra tu comida favorita."
        );
        descripcion.addClassName("section-description");

        Div productos = new Div();
        productos.addClassName("menu-grid");

        Div contenedorFiltro = new Div();
        contenedorFiltro.addClassName("menu-filter");

        Button botonFiltro = new Button("Filtrar menú ▾");
        botonFiltro.addClassName("menu-filter-button");

        Div opcionesFiltro = new Div();
        opcionesFiltro.addClassName("menu-filter-options");
        opcionesFiltro.setVisible(false);

        // Consultar productos de PostgreSQL

        List<Producto> listaProductos = productoService.listar();

        String[] categorias = {
                "Todas",
                "Comidas rápidas",
                "Bebidas",
                "Comidas personales",
                "Comidas familiares"
        };

        // Crear un botón para cada categoría

        for (String categoria : categorias) {

            Button opcion = new Button(categoria);
            opcion.addClassName("menu-filter-option");

            opcion.addClickListener(event -> {

                mostrarProductos(listaProductos, categoria, productos);

                opcionesFiltro.setVisible(false);
                botonFiltro.setText("Filtrar: " + categoria + " ▾");
            });

            opcionesFiltro.add(opcion);
        }

        // Mostrar u ocultar las opciones

        botonFiltro.addClickListener(event -> {

            boolean mostrar = !opcionesFiltro.isVisible();

            opcionesFiltro.setVisible(mostrar);

            botonFiltro.setText(
                    mostrar ? "Ocultar filtros ▴" : "Filtrar menú ▾"
            );
        });

        contenedorFiltro.add(botonFiltro, opcionesFiltro);

        // Mostrar todos los productos al entrar

        mostrarProductos(listaProductos, "Todas", productos);

        seccion.add(
                etiqueta,
                titulo,
                descripcion,
                contenedorFiltro,
                productos
        );

        return seccion;
    }

    // METODO PARA FILTRAR Y MOSTRAR LOS PRODUCTOS

    private void mostrarProductos(
            List<Producto> listaProductos,
            String categoria,
            Div contenedor) {

        contenedor.removeAll();

        if (listaProductos == null || listaProductos.isEmpty()) {

            Paragraph mensaje = new Paragraph(
                    "Nuestro menú se está preparando."
            );

            contenedor.add(mensaje);
            return;
        }

        int indice = 0;

        for (Producto producto : listaProductos) {

            boolean coincide = categoria.equals("Todas")
                    || (producto.getCategoria() != null
                    && producto.getCategoria().equalsIgnoreCase(categoria));

            if (coincide) {

                contenedor.add(
                        crearTarjetaProducto(producto, indice)
                );

                indice++;
            }
        }

        if (indice == 0) {

            Paragraph mensaje = new Paragraph(
                    "No hay productos disponibles en esta categoría."
            );

            contenedor.add(mensaje);
        }
    }

    // SECCIÓN NOSOTROS

    private Div crearNosotros() {

        Div seccion = new Div();
        seccion.addClassName("about-section");
        seccion.setId("nosotros");

        Span etiqueta = new Span("CONÓCENOS");
        etiqueta.addClassName("section-label");

        H2 titulo = new H2("Más que un restaurante, una experiencia");

        Paragraph descripcion = new Paragraph(
                "En Sabor & Fuego buscamos ofrecer platos de calidad, " +
                        "preparados con dedicación y buenos ingredientes. " +
                        "Nuestro objetivo es que cada visita sea especial."
        );

        seccion.add(etiqueta, titulo, descripcion);

        return seccion;
    }

    // OBTENER IMAGEN DESDE POSTGRESQL

    private String obtenerImagenProducto(Producto producto) {

        if (producto.getImagen() == null
                || producto.getImagen().length == 0) {

            return null;
        }

        String tipo = producto.getImagenTipo();

        if (tipo == null || tipo.isBlank()) {
            tipo = "image/jpeg";
        }

        String base64 = Base64.getEncoder()
                .encodeToString(producto.getImagen());

        return "data:" + tipo + ";base64," + base64;
    }

    // TARJETA DE PRODUCTO

    private Div crearTarjetaProducto(Producto producto, int indice) {

        Div tarjeta = new Div();
        tarjeta.addClassName("food-card");

        String foto = obtenerImagenProducto(producto);

        Image imagen;

        if (foto != null) {

            imagen = new Image(
                    foto,
                    "Fotografía de " + producto.getNombre()
            );

        } else {

            imagen = new Image(
                    "https://placehold.co/800x600?text=Sin+imagen",
                    "Producto sin imagen"
            );
        }

        imagen.addClassName("food-image");

        Div contenido = new Div();
        contenido.addClassName("food-content");

        H2 nombre = new H2(producto.getNombre());
        nombre.addClassName("food-name");

        String descripcionTexto = producto.getDescripcion();

        if (descripcionTexto == null || descripcionTexto.isBlank()) {
            descripcionTexto = "Un plato preparado para disfrutar.";
        }

        Paragraph descripcion = new Paragraph(descripcionTexto);
        descripcion.addClassName("food-description");

        Span precio = new Span(formatearPrecio(producto));
        precio.addClassName("food-price");

        boolean disponible =
                producto.getStock() != null && producto.getStock() > 0;

        Span disponibilidad = new Span(
                disponible ? "Disponible" : "Agotado"
        );

        disponibilidad.addClassName("food-availability");

        Button agregarCarrito = new Button("Agregar al carrito");
        agregarCarrito.addClassName("gold-button");
        agregarCarrito.setEnabled(disponible);

        agregarCarrito.addClickListener(event -> {

            Map<Long, Integer> carrito =
                    (Map<Long, Integer>) VaadinSession.getCurrent()
                            .getAttribute("carrito");

            if (carrito == null) {
                carrito = new HashMap<>();
            }

            Long idProducto = producto.getId();

            carrito.put(
                    idProducto,
                    carrito.getOrDefault(idProducto, 0) + 1
            );

            VaadinSession.getCurrent()
                    .setAttribute("carrito", carrito);

            Notification.show(
                    producto.getNombre() + " agregado al carrito"
            );
        });

        contenido.add(
                nombre,
                descripcion,
                precio,
                disponibilidad,
                agregarCarrito
        );

        tarjeta.add(imagen, contenido);

        return tarjeta;
    }

    // FORMATO DE PRECIO COLOMBIANO

    private String formatearPrecio(Producto producto) {

        if (producto.getPrecio() == null) {
            return "Precio no disponible";
        }

        NumberFormat formato = NumberFormat.getCurrencyInstance(
                Locale.forLanguageTag("es-CO")
        );

        formato.setMaximumFractionDigits(0);
        formato.setMinimumFractionDigits(0);

        return formato.format(producto.getPrecio());
    }

    // CREAR ENLACES

    private Anchor crearEnlace(String texto, String destino) {

        Anchor enlace = new Anchor(destino, texto);
        enlace.addClassName("nav-link");

        return enlace;
    }

    // PIE DE PÁGINA

    private Div crearPieDePagina() {

        Div pie = new Div();
        pie.addClassName("restaurant-footer");
        pie.setId("contacto");

        H2 nombre = new H2("SABOR & FUEGO");

        Paragraph frase = new Paragraph(
                "Una experiencia gastronómica para disfrutar."
        );

        Paragraph contacto = new Paragraph(
                "Contáctanos para conocer nuestros servicios y realizar tus pedidos."
        );

        Paragraph derechos = new Paragraph(
                "© 2026 Sabor & Fuego. Todos los derechos reservados."
        );

        pie.add(nombre, frase, contacto, derechos);

        return pie;
    }
}
