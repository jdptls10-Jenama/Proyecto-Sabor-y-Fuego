
package com.restaurante.service;

import com.restaurante.model.ErrorAplicacion;
import com.restaurante.model.Usuario;
import com.restaurante.repository.ErrorAplicacionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.PrintWriter;
import java.io.StringWriter;

@Service
public class ErrorAplicacionService {

    private final ErrorAplicacionRepository repository;

    public ErrorAplicacionService(
            ErrorAplicacionRepository repository) {
        this.repository = repository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrar(
            Throwable error,
            Usuario usuario,
            String ruta,
            String sesionId) {

        try {
            ErrorAplicacion registro = new ErrorAplicacion();

            registro.setTipoError(
                    error.getClass().getSimpleName()
            );

            registro.setMensaje(
                    error.getMessage() != null
                            ? error.getMessage()
                            : "Error sin mensaje"
            );

            StringWriter escritor = new StringWriter();
            error.printStackTrace(new PrintWriter(escritor));

            String detalle = escritor.toString();

            if (detalle.length() > 20000) {
                detalle = detalle.substring(0, 20000);
            }

            registro.setDetalleTecnico(detalle);
            registro.setRuta(ruta);
            registro.setUsuario(usuario);
            registro.setSesionId(sesionId);

            repository.save(registro);

        } catch (Exception registroError) {
            // No ocultar el error original si falla el registro.
            System.err.println(
                    "No se pudo guardar el error: "
                            + registroError.getMessage()
            );
        }
    }
}
