package com.example.demo;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;



@RestController
@RequestMapping("bloque")
public class RestControllerBloques {

    private final ServiceBloques servicioBloques;
    private final ServiceNodo servicioNodo;
    private final ServiceMinado servicioMinado;
    
    @Autowired
    public RestControllerBloques(ServiceBloques servicioCadenaDeBloques, ServiceNodo servicioNodo,ServiceMinado servicioMinado) {
        this.servicioBloques = servicioCadenaDeBloques;
        this.servicioNodo = servicioNodo;
        this.servicioMinado = servicioMinado;
        
        if (Configuracion.getInstancia().getMinar()) {
			servicioMinado.startMinado();
		}
    }


    /**
     * Obtener la cadena de bloques
     * @return JSON Lista de bloques
     */
    @RequestMapping(method = RequestMethod.GET)
    CadenaDeBloques getCadenaDeBloques() {
        System.out.println("Request: getCadenaDeBloques");
        return servicioBloques.getCadenaDeBloques();
    }


    /**
     * Añadir un bloque a la cadena
     * @param bloque El bloque a ser añadido
     * @param propagar Si el bloque debe ser propagado al resto de nodos en la red
     * @param response codigo 202 si el bloque es aceptado y añadido, codigo 406 en caso contrario
     */
    @RequestMapping(method = RequestMethod.POST)
    void añadirBloque(@RequestBody Bloque bloque, @RequestParam(required = false) Boolean propagar, HttpServletResponse response) {
        
        System.out.println("Request: Añadir bloque " + Base64.encodeBase64String(bloque.getHash()));
        boolean exito = servicioBloques.añadirBloque(bloque);
        
        if (exito) {
        	
            System.out.println("Bloque validado y añadido.");
            response.setStatus(HttpServletResponse.SC_ACCEPTED);

            if (propagar != null && propagar) {
                servicioNodo.emitirPeticionPostNodosVecinos("bloque", bloque);
            }

        } else {
            System.out.println("Bloque invalido y no añadido.");
            response.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
        }
    }
}