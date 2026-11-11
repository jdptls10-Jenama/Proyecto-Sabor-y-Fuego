
package com.restaurante.view;

import com.restaurante.model.Pedido;
import com.restaurante.model.DetallePedido;
import com.restaurante.model.Usuario;
import com.restaurante.service.PedidoService;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.streams.DownloadHandler;
import com.vaadin.flow.server.streams.DownloadResponse;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

@Route("finalizar-compra")
@PageTitle("Finalizar compra | Sabor & Fuego")
public class FinalizarCompraView extends Div {

    private final PedidoService pedidoService;
    private Pedido pedidoActual;

    public FinalizarCompraView(PedidoService pedidoService) {

        this.pedidoService = pedidoService;

        addClassName("finalizar-compra-page");

        VaadinSession sesion = VaadinSession.getCurrent();

        Usuario usuario =
                (Usuario) sesion.getAttribute("usuarioActual");

        if (usuario == null) {

            Div tarjeta = new Div();
            tarjeta.addClassName("checkout-card");

            H2 titulo = new H2("Inicia sesión para continuar");
            titulo.addClassName("checkout-title");

            Paragraph descripcion = new Paragraph(
                    "Necesitas iniciar sesión para completar tu pedido."
            );

            Button iniciarSesion = new Button("Iniciar sesión");
            iniciarSesion.addClassName("checkout-submit");

            iniciarSesion.addClickListener(event -> {

                sesion.setAttribute(
                        "rutaDespuesLogin",
                        "finalizar-compra"
                );

                getUI().ifPresent(ui -> ui.navigate("login"));
            });

            Anchor registrarse = new Anchor(
                    "/registro",
                    "¿No tienes cuenta? Regístrate"
            );

            registrarse.addClassName("checkout-link");

            tarjeta.add(
                    titulo,
                    descripcion,
                    iniciarSesion,
                    registrarse
            );

            add(tarjeta);
            return;
        }

        @SuppressWarnings("unchecked")
        Map<Long, Integer> carrito =
                (Map<Long, Integer>) sesion.getAttribute("carrito");

        if (carrito == null || carrito.isEmpty()) {

            Div tarjeta = new Div();
            tarjeta.addClassName("checkout-card");

            H2 titulo = new H2("Tu carrito está vacío");

            Paragraph mensaje = new Paragraph(
                    "Agrega productos al carrito antes de finalizar tu compra."
            );

            Anchor volver = new Anchor("/", "Volver al menú");
            volver.addClassName("checkout-link");

            tarjeta.add(titulo, mensaje, volver);

            add(tarjeta);
            return;
        }

        mostrarFormulario(usuario, carrito, sesion);
    }

    private void mostrarFormulario(
            Usuario usuario,
            Map<Long, Integer> carrito,
            VaadinSession sesion
    ) {

        Div tarjeta = new Div();
        tarjeta.addClassName("checkout-card");

        Paragraph etiqueta = new Paragraph("SABOR & FUEGO");
        etiqueta.addClassName("checkout-etiqueta");

        H2 titulo = new H2("Finalizar compra");
        titulo.addClassName("checkout-title");

        Paragraph descripcion = new Paragraph(
                "Completa tus datos para recibir tu pedido."
        );

        H3 subtitulo = new H3("Datos de entrega");

        Div formulario = new Div();
        formulario.addClassName("checkout-form");

        TextField direccion =
                new TextField("Dirección de entrega");

        direccion.setPlaceholder(
                "Ej. Calle 20 # 15-30, barrio Centro"
        );

        direccion.setWidthFull();
        direccion.addClassName("checkout-field");

        TextField telefono =
                new TextField("Teléfono de contacto");

        telefono.setPlaceholder("Ej. 3001234567");
        telefono.setWidthFull();
        telefono.addClassName("checkout-field");

        TextArea observaciones =
                new TextArea("Observaciones");

        observaciones.setPlaceholder(
                "Indicaciones adicionales para tu pedido..."
        );

        observaciones.setWidthFull();
        observaciones.setHeight("110px");
        observaciones.addClassName("checkout-field");

        Paragraph nota = new Paragraph(
                "Verifica que tus datos sean correctos antes de confirmar."
        );

        nota.addClassName("checkout-nota");

        Button confirmar = new Button("Confirmar pedido");
        confirmar.addClassName("checkout-submit");
        confirmar.setWidthFull();

        confirmar.addClickListener(event -> {

            if (direccion.isEmpty() || telefono.isEmpty()) {

                Notification.show(
                        "Completa la dirección y el teléfono."
                );

                return;
            }

            confirmar.setEnabled(false);

            try {

                pedidoActual = pedidoService.crearPedido(
                        usuario,
                        direccion.getValue(),
                        telefono.getValue(),
                        observaciones.getValue(),
                        carrito
                );

                if (pedidoActual == null) {

                    Notification.show(
                            "No fue posible registrar el pedido."
                    );

                    confirmar.setEnabled(true);
                    return;
                }

                sesion.setAttribute("carrito", null);

                mostrarFactura();

            } catch (Exception e) {

                Notification.show(
                        "Error al registrar el pedido: " + e.getMessage(),
                        5000,
                        Notification.Position.MIDDLE
                );

                confirmar.setEnabled(true);
                e.printStackTrace();
            }
        });

        formulario.add(
                direccion,
                telefono,
                observaciones,
                nota,
                confirmar
        );

        tarjeta.add(
                etiqueta,
                titulo,
                descripcion,
                subtitulo,
                formulario
        );

        add(tarjeta);
    }

    private void mostrarFactura() {

        removeAll();

        Div tarjeta = new Div();
        tarjeta.addClassName("checkout-card");

        Paragraph etiqueta = new Paragraph("SABOR & FUEGO");
        etiqueta.addClassName("checkout-etiqueta");

        H2 titulo = new H2("¡Pedido realizado!");
        titulo.addClassName("checkout-title");

        Paragraph descripcion = new Paragraph(
                "Tu pedido fue registrado correctamente. Descarga tu factura y compártela con nosotros por WhatsApp."
        );

        H3 numeroPedido = new H3(
                "Pedido número: " + pedidoActual.getId()
        );

        try {

            byte[] imagenFactura =
                    generarImagenFactura(pedidoActual);

            String nombreFactura =
                    "factura-" + pedidoActual.getId() + ".png";

            // Vista previa de la factura
            DownloadHandler manejadorImagen =
                    crearManejadorImagen(
                            imagenFactura,
                            nombreFactura
                    );

            Image facturaImagen = new Image(
                    manejadorImagen,
                    "Factura de Sabor & Fuego"
            );

            facturaImagen.setWidth("100%");
            facturaImagen.setMaxWidth("900px");
            facturaImagen.addClassName("factura-imagen");

            // Botón para descargar factura
            DownloadHandler manejadorDescarga =
                    crearManejadorImagen(
                            imagenFactura,
                            nombreFactura
                    );

            Anchor descargarFactura = new Anchor(
                    manejadorDescarga,
                    "Descargar factura"
            );

            descargarFactura.addClassName("checkout-submit");
            descargarFactura.setWidthFull();

            // Botón para abrir WhatsApp
            Button botonWhatsApp = new Button(
                    "Ir al chat de WhatsApp"
            );

            botonWhatsApp.addClassName("checkout-whatsapp");
            botonWhatsApp.setWidthFull();

            botonWhatsApp.addClickListener(event -> {

                getUI().ifPresent(ui ->
                        ui.getPage().open(
                                "https://wa.me/573013041675",
                                "_blank"
                        )
                );

            });

            // Mensaje debajo de los botones
            Paragraph mensaje = new Paragraph(
                    "Por favor, comparte la factura por WhatsApp para evitar inconvenientes con tu pedido."
            );

            mensaje.addClassName("checkout-nota");

            tarjeta.add(
                    etiqueta,
                    titulo,
                    descripcion,
                    numeroPedido,
                    facturaImagen,
                    descargarFactura,
                    botonWhatsApp,
                    mensaje
            );

        } catch (Exception e) {

            tarjeta.add(
                    etiqueta,
                    titulo,
                    descripcion,
                    new Paragraph(
                            "El pedido fue registrado, pero no se pudo generar la factura."
                    )
            );

            e.printStackTrace();
        }

        Anchor volver = new Anchor(
                "/",
                "Volver al menú"
        );

        volver.addClassName("checkout-link");

        tarjeta.add(volver);

        add(tarjeta);
    }

    private DownloadHandler crearManejadorImagen(
            byte[] contenido,
            String nombreArchivo
    ) {

        return DownloadHandler.fromInputStream(
                event -> new DownloadResponse(
                        new ByteArrayInputStream(contenido),
                        nombreArchivo,
                        "image/png",
                        contenido.length
                )
        );
    }

    private byte[] generarImagenFactura(Pedido pedido)
            throws IOException {

        List<DetallePedido> detalles = pedido.getDetalles();

        int ancho = 900;
        int alto = 560 + (detalles.size() * 95);

        BufferedImage imagen = new BufferedImage(
                ancho,
                alto,
                BufferedImage.TYPE_INT_RGB
        );

        Graphics2D g = imagen.createGraphics();

        Color negro = new Color(18, 18, 18);
        Color dorado = new Color(218, 174, 75);
        Color blanco = new Color(245, 245, 245);
        Color gris = new Color(190, 190, 190);

        g.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        // Fondo negro
        g.setColor(negro);
        g.fillRect(0, 0, ancho, alto);

        // Franja dorada
        g.setColor(dorado);
        g.fillRect(0, 0, ancho, 12);

        // Encabezado
        g.setFont(new Font("Arial", Font.BOLD, 42));
        g.drawString("SABOR & FUEGO", 45, 85);

        g.setFont(new Font("Arial", Font.PLAIN, 21));
        g.drawString("COMPROBANTE DE COMPRA", 47, 125);

        g.setColor(dorado);
        g.drawLine(45, 155, 855, 155);

        // Datos del pedido
        g.setColor(blanco);
        g.setFont(new Font("Arial", Font.BOLD, 21));

        g.drawString(
                "Pedido: #" + pedido.getId(),
                45,
                205
        );

        g.setFont(new Font("Arial", Font.PLAIN, 18));

        g.drawString(
                "Cliente: " + recortarTexto(
                        pedido.getUsuario().getNombre(), 45
                ),
                45,
                245
        );

        g.drawString(
                "Fecha: " + pedido.getFechaPedido(),
                45,
                280
        );

        g.drawString(
                "Dirección: " + recortarTexto(
                        pedido.getDireccionEntrega(), 65
                ),
                45,
                315
        );

        g.drawString(
                "Teléfono: " + pedido.getTelefonoContacto(),
                45,
                350
        );

        // Encabezado de productos
        g.setColor(dorado);
        g.fillRoundRect(40, 385, 820, 48, 8, 8);

        g.setColor(negro);
        g.setFont(new Font("Arial", Font.BOLD, 18));

        g.drawString("PRODUCTO", 60, 416);
        g.drawString("CANTIDAD", 440, 416);
        g.drawString("SUBTOTAL", 675, 416);

        int y = 475;

        g.setColor(blanco);
        g.setFont(new Font("Arial", Font.PLAIN, 18));

        for (DetallePedido detalle : detalles) {

            String nombre = detalle.getProducto().getNombre();

            g.drawString(
                    recortarTexto(nombre, 35),
                    60,
                    y
            );

            g.drawString(
                    String.valueOf(detalle.getCantidad()),
                    465,
                    y
            );

            g.drawString(
                    "$ " + formatoPrecio(detalle.getSubtotal()),
                    675,
                    y
            );

            g.setColor(gris);
            g.drawLine(55, y + 20, 845, y + 20);

            g.setColor(blanco);

            y += 95;
        }

        // Total
        g.setColor(dorado);
        g.drawLine(45, y + 10, 855, y + 10);

        g.setColor(blanco);
        g.setFont(new Font("Arial", Font.BOLD, 30));

        g.drawString(
                "TOTAL: $ " + formatoPrecio(pedido.getTotal()),
                500,
                y + 65
        );

        // Estado
        g.setFont(new Font("Arial", Font.PLAIN, 18));

        g.drawString(
                "Estado: " + pedido.getEstado(),
                45,
                y + 115
        );

        // Pie de factura
        g.setColor(dorado);
        g.setFont(new Font("Arial", Font.BOLD, 21));

        g.drawString(
                "¡Gracias por comprar en Sabor & Fuego!",
                45,
                alto - 45
        );

        g.dispose();

        ByteArrayOutputStream salida =
                new ByteArrayOutputStream();

        ImageIO.write(imagen, "png", salida);

        return salida.toByteArray();
    }

    private String formatoPrecio(BigDecimal precio) {

        if (precio == null) {
            return "0";
        }

        DecimalFormat formato =
                new DecimalFormat("#,##0");

        return formato.format(precio);
    }

    private String recortarTexto(String texto, int maximo) {

        if (texto == null) {
            return "";
        }

        if (texto.length() <= maximo) {
            return texto;
        }

        return texto.substring(0, maximo - 3) + "...";
    }
}
