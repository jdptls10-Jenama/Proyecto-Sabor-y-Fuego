
package com.restaurante.view;

import com.restaurante.model.Producto;
import com.restaurante.service.ProductoService;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.Base64;

@Route("menu")
@PageTitle("Menú - Sabor & Fuego")
public class MenuView extends VerticalLayout {

    private final ProductoService productoService;

    public MenuView(ProductoService productoService) {

        this.productoService = productoService;

        setWidthFull();
        setPadding(true);
        setSpacing(true);

        getStyle()
                .set("box-sizing", "border-box")
                .set("background", "#f5f5f5");

        H2 titulo = new H2("Nuestro menú");
        add(titulo);

        Div contenedor = new Div();

        contenedor.getStyle()
                .set("display", "grid")
                .set("grid-template-columns",
                        "repeat(auto-fill, minmax(220px, 1fr))")
                .set("gap", "20px")
                .set("width", "100%");

        for (Producto producto : productoService.listar()) {

            VerticalLayout tarjeta = new VerticalLayout();

            tarjeta.setPadding(true);
            tarjeta.setSpacing(true);

            tarjeta.setWidthFull();

            tarjeta.getStyle()
                    .set("background", "white")
                    .set("border-radius", "12px")
                    .set("box-shadow", "0 2px 8px #00000020")
                    .set("box-sizing", "border-box");

            // IMAGEN DEL PRODUCTO
            String imagenSrc = obtenerImagen(producto);

            if (imagenSrc != null) {

                Image imagen = new Image(
                        imagenSrc,
                        producto.getNombre()
                );

                imagen.setWidthFull();
                imagen.setHeight("180px");

                imagen.getStyle()
                        .set("object-fit", "contain")
                        .set("border-radius", "8px")
                        .set("display", "block");

                tarjeta.add(imagen);

            } else {

                Span sinImagen = new Span("Sin imagen");

                sinImagen.setWidthFull();

                sinImagen.getStyle()
                        .set("height", "180px")
                        .set("display", "flex")
                        .set("align-items", "center")
                        .set("justify-content", "center")
                        .set("background", "#eeeeee");

                tarjeta.add(sinImagen);
            }

            // NOMBRE
            H3 nombre = new H3(producto.getNombre());

            // DESCRIPCIÓN
            Paragraph descripcion = new Paragraph(
                    producto.getDescripcion() != null
                            ? producto.getDescripcion()
                            : ""
            );

            // PRECIO
            Span precio = new Span(
                    "$ " + producto.getPrecio()
            );

            precio.getStyle()
                    .set("font-size", "20px")
                    .set("font-weight", "bold")
                    .set("color", "#b8860b");

            tarjeta.add(nombre, descripcion, precio);

            contenedor.add(tarjeta);
        }

        add(contenedor);
    }

    // CONVERTIR IMAGEN A BASE64
    private String obtenerImagen(Producto producto) {

        if (producto.getImagen() == null
                || producto.getImagen().length == 0) {

            return null;
        }

        String tipo = producto.getImagenTipo();

        if (tipo == null
                || (!tipo.equalsIgnoreCase("image/jpeg")
                && !tipo.equalsIgnoreCase("image/png"))) {

            tipo = "image/jpeg";
        }

        String base64 = Base64.getEncoder()
                .encodeToString(producto.getImagen());

        return "data:" + tipo + ";base64," + base64;
    }
}
