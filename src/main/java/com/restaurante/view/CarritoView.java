
package com.restaurante.view;

import com.restaurante.model.Producto;
import com.restaurante.service.ProductoService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Route("carrito")
@PageTitle("Mi carrito | Sabor & Fuego")
public class CarritoView extends Div {

    private final ProductoService productoService;

    public CarritoView(ProductoService productoService) {
        this.productoService = productoService;
        addClassName("carrito-page");
        mostrarCarrito();
    }

    @SuppressWarnings("unchecked")
    private Map<Long, Integer> obtenerCarrito() {

        Map<Long, Integer> carrito =
                (Map<Long, Integer>) VaadinSession.getCurrent()
                        .getAttribute("carrito");

        if (carrito == null) {
            carrito = new HashMap<>();
            VaadinSession.getCurrent()
                    .setAttribute("carrito", carrito);
        }

        return carrito;
    }

    private void mostrarCarrito() {

        removeAll();

        H1 titulo = new H1("Mi carrito");
        titulo.addClassName("carrito-titulo");

        add(titulo);

        Map<Long, Integer> carrito = obtenerCarrito();
        List<Producto> productos = productoService.listar();

        if (carrito.isEmpty()) {
            add(new Paragraph("Tu carrito está vacío."));
            add(new Anchor("/", "Volver al menú"));
            return;
        }

        Div lista = new Div();
        lista.addClassName("carrito-lista");

        BigDecimal total = BigDecimal.ZERO;

        for (Map.Entry<Long, Integer> entrada :
                new HashMap<>(carrito).entrySet()) {

            Producto producto = productos.stream()
                    .filter(p -> p.getId().equals(entrada.getKey()))
                    .findFirst()
                    .orElse(null);

            if (producto == null) {
                carrito.remove(entrada.getKey());
                continue;
            }

            int cantidad = entrada.getValue();

            BigDecimal subtotal = producto.getPrecio()
                    .multiply(BigDecimal.valueOf(cantidad));

            total = total.add(subtotal);

            Div fila = new Div();
            fila.addClassName("carrito-producto");

            H2 nombre = new H2(producto.getNombre());

            Span precio = new Span(
                    "Precio: " + formatoPrecio(producto.getPrecio())
            );

            Span cantidadTexto = new Span(
                    "Cantidad: " + cantidad
            );

            Span subtotalTexto = new Span(
                    "Subtotal: " + formatoPrecio(subtotal)
            );

            Button disminuir = new Button("-");
            Button aumentar = new Button("+");
            Button eliminar = new Button("Eliminar");

            disminuir.addClickListener(event -> {
                int nuevaCantidad = carrito.get(entrada.getKey()) - 1;

                if (nuevaCantidad <= 0) {
                    carrito.remove(entrada.getKey());
                } else {
                    carrito.put(entrada.getKey(), nuevaCantidad);
                }

                mostrarCarrito();
            });

            aumentar.addClickListener(event -> {

                int nuevaCantidad = carrito.get(entrada.getKey()) + 1;

                if (producto.getStock() != null
                        && nuevaCantidad > producto.getStock()) {
                    Notification.show("No hay más unidades disponibles");
                    return;
                }

                carrito.put(entrada.getKey(), nuevaCantidad);
                mostrarCarrito();
            });

            eliminar.addClickListener(event -> {
                carrito.remove(entrada.getKey());
                mostrarCarrito();
            });

            fila.add(
                    nombre,
                    precio,
                    cantidadTexto,
                    subtotalTexto,
                    disminuir,
                    aumentar,
                    eliminar
            );

            lista.add(fila);
        }

        Span totalTexto = new Span(
                "TOTAL: " + formatoPrecio(total)
        );

        totalTexto.addClassName("carrito-total");

        Button vaciar = new Button("Vaciar carrito");
        vaciar.addClickListener(event -> {
            carrito.clear();
            mostrarCarrito();
        });

        Button continuar = new Button("Seguir comprando");
        continuar.addClickListener(event ->
                getUI().ifPresent(ui -> ui.navigate(""))
        );
        Button comprar = new Button("Finalizar compra");

        comprar.addClickListener(event -> {

            Object usuario = VaadinSession.getCurrent()
                    .getAttribute("usuarioActual");

            if (usuario == null) {

                VaadinSession.getCurrent().setAttribute(
                        "mensajeAccesoCompra",
                        "Para finalizar tu compra, regístrate o inicia sesión."
                );

                getUI().ifPresent(ui -> ui.navigate("registro"));

            } else {

                getUI().ifPresent(ui -> ui.navigate("finalizar-compra"));

            }
        });
        comprar.addClassName("gold-button");

        Div acciones = new Div(vaciar, continuar, comprar);
        acciones.addClassName("carrito-acciones");

        add(lista, totalTexto, acciones);
    }

    private String formatoPrecio(BigDecimal precio) {

        NumberFormat formato =
                NumberFormat.getCurrencyInstance(
                        Locale.forLanguageTag("es-CO")
                );

        formato.setMaximumFractionDigits(0);
        formato.setMinimumFractionDigits(0);

        return formato.format(precio);
    }
}
