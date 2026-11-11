
package com.restaurante.view;

import com.restaurante.service.UsuarioService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

@Route("registro")
@PageTitle("Crear cuenta | Sabor & Fuego")
public class RegistroView extends Div {

    public RegistroView(UsuarioService usuarioService) {

        addClassName("registro-page");

        H2 titulo = new H2("Crear cuenta");

        Paragraph descripcion = new Paragraph(
                "Regístrate para realizar tus pedidos."
        );

        TextField nombre = new TextField("Nombre completo");
        nombre.setWidthFull();
        nombre.addClassName("registro-campo");

        EmailField correo = new EmailField("Correo electrónico");
        correo.setWidthFull();
        correo.addClassName("registro-campo");

        PasswordField password = new PasswordField("Contraseña");
        password.setWidthFull();
        password.addClassName("registro-campo");

        PasswordField confirmarPassword =
                new PasswordField("Confirmar contraseña");
        confirmarPassword.setWidthFull();
        confirmarPassword.addClassName("registro-campo");

        Button registrar = new Button("Crear cuenta");
        registrar.setWidthFull();

        registrar.addClickListener(event -> {

            registrar.setEnabled(false);

            try {
                if (nombre.isEmpty() || correo.isEmpty()
                        || password.isEmpty()
                        || confirmarPassword.isEmpty()) {

                    Notification.show("Completa todos los campos");
                    registrar.setEnabled(true);
                    return;
                }

                if (!password.getValue().equals(
                        confirmarPassword.getValue())) {

                    Notification.show("Las contraseñas no coinciden");
                    registrar.setEnabled(true);
                    return;
                }

                usuarioService.registrar(
                        nombre.getValue(),
                        correo.getValue(),
                        password.getValue()
                );

                var usuarioCreado = usuarioService.iniciarSesion(
                        correo.getValue(),
                        password.getValue()
                );

                if (usuarioCreado.isPresent()) {

                    VaadinSession.getCurrent().setAttribute(
                            "usuarioActual",
                            usuarioCreado.get()
                    );

                    Notification.show(
                            "Cuenta creada correctamente"
                    );

                    getUI().ifPresent(ui -> ui.navigate(""));

                } else {

                    Notification.show(
                            "La cuenta se creó, pero debes iniciar sesión."
                    );

                    getUI().ifPresent(ui -> ui.navigate("login"));
                }

            } catch (IllegalArgumentException e) {

                Notification.show(e.getMessage());
                registrar.setEnabled(true);

            } catch (Exception e) {

                Notification.show("No se pudo crear la cuenta");
                registrar.setEnabled(true);
            }
        });

        Anchor iniciarSesion = new Anchor(
                "/login",
                "¿Ya tienes una cuenta? Inicia sesión"
        );

        Div formulario = new Div(
                titulo,
                descripcion,
                nombre,
                correo,
                password,
                confirmarPassword,
                registrar,
                iniciarSesion
        );

        formulario.addClassName("registro-formulario");

        add(formulario);
    }
}