
package com.restaurante.service;

import com.restaurante.model.Actividad;
import com.restaurante.model.Usuario;
import com.restaurante.repository.ActividadRepository;
import org.springframework.stereotype.Service;

@Service
public class ActividadService {

    private final ActividadRepository actividadRepository;

    public ActividadService(ActividadRepository actividadRepository) {
        this.actividadRepository = actividadRepository;
    }

    public void registrar(String tipo, Usuario usuario,
                          String pagina, String sesionId,
                          String detalle) {

        Actividad actividad = new Actividad(
                tipo,
                pagina,
                sesionId,
                detalle,
                usuario
        );

        actividadRepository.save(actividad);
    }
}
