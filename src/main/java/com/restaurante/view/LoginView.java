package com.restaurante.view;

import com.restaurante.service.UsuarioService;
import com.restaurante.model.Usuario;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

@Route("login")
@PageTitle("Iniciar sesión | Sabor & Fuego")
public class LoginView extends Div {

    public LoginView(UsuarioService usuarioService) {

        addClassName("auth-page");

        H2 titulo = new H2("Bienvenido de nuevo");

        Paragraph descripcion = new Paragraph(
                "Ingresa a tu cuenta de Sabor & Fuego."
        );

        EmailField correo = new EmailField("Correo electrónico");
        correo.setWidthFull();

        PasswordField password = new PasswordField("Contraseña");
        password.setWidthFull();

        Button ingresar = new Button("Iniciar sesión");
        ingresar.addClassName("auth-button");
        ingresar.setWidthFull();

        ingresar.addClickListener(event -> {

            if (correo.isEmpty() || password.isEmpty()) {
                Notification.show("Completa todos los campos");
                return;
            }

            var resultado = usuarioService.iniciarSesion(
                    correo.getValue(),
                    password.getValue()
            );

            if (resultado.isPresent()) {

                Usuario cliente = resultado.get();

                VaadinSession sesion = VaadinSession.getCurrent();

                sesion.setAttribute("usuarioActual", cliente);

                String rutaDestino = (String) sesion.getAttribute(
                        "rutaDespuesLogin"
                );

                sesion.setAttribute("rutaDespuesLogin", null);

                if (rutaDestino == null || rutaDestino.isBlank()) {
                    if ("ADMIN".equalsIgnoreCase(cliente.getRol())) {
                        rutaDestino = "admin";
                    } else {
                        rutaDestino = "";
                    }
                }

                final String destinoFinal = rutaDestino;

                getUI().ifPresent(ui -> ui.navigate(destinoFinal));

            } else {

                Notification.show("Correo o contraseña incorrectos");
            }
        });

        Anchor registrarse = new Anchor(
                "/registro",
                "¿No tienes cuenta? Regístrate"
        );

        Div formulario = new Div(
                titulo,
                descripcion,
                correo,
                password,
                ingresar,
                registrarse
        );

        formulario.addClassName("auth-formulario");

        add(formulario);
    }
}