
package com.restaurante.config;

import com.restaurante.model.Usuario;
import com.restaurante.service.ActividadService;
import com.restaurante.service.ErrorAplicacionService;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.server.ErrorEvent;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import com.vaadin.flow.server.VaadinSession;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class RegistroActividadListener
        implements VaadinServiceInitListener {

    private final ActividadService actividadService;
    private final ErrorAplicacionService errorService;

    public RegistroActividadListener(
            ActividadService actividadService,
            ErrorAplicacionService errorService) {

        this.actividadService = actividadService;
        this.errorService = errorService;
    }

    @Override
    public void serviceInit(ServiceInitEvent event) {

        event.getSource().addUIInitListener(uiEvent -> {

            UI ui = uiEvent.getUI();

            // Registrar visitas
            ui.addBeforeEnterListener(this::registrarVisita);

            // Capturar excepciones de la sesión Vaadin
            ui.getSession().setErrorHandler(this::registrarError);        });
    }

    private void registrarError(ErrorEvent errorEvent) {

        Throwable error = errorEvent.getThrowable();

        VaadinSession sesion = VaadinSession.getCurrent();

        Usuario usuario = null;
        String sesionId = null;

        if (sesion != null) {

            usuario = (Usuario) sesion.getAttribute(
                    "usuarioActual"
            );

            sesionId = (String) sesion.getAttribute(
                    "actividadSesionId"
            );
        }

        errorService.registrar(
                error,
                usuario,
                "INTERFAZ_VAADIN",
                sesionId
        );

        error.printStackTrace();
    }

    private void registrarVisita(BeforeEnterEvent event) {

        VaadinSession sesion = VaadinSession.getCurrent();

        if (sesion == null) {
            return;
        }

        String sesionId = (String) sesion.getAttribute(
                "actividadSesionId"
        );

        if (sesionId == null) {

            sesionId = UUID.randomUUID().toString();

            sesion.setAttribute(
                    "actividadSesionId",
                    sesionId
            );
        }

        Usuario usuario = (Usuario) sesion.getAttribute(
                "usuarioActual"
        );

        String pagina = event.getLocation().getPath();

        if (pagina == null || pagina.isBlank()) {
            pagina = "/";
        }

        actividadService.registrar(
                "VISITA",
                usuario,
                pagina,
                sesionId,
                "Visita a la página " + pagina
        );
    }
}
