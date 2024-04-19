package com.example.demo;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("nodo")
public class RestControllerNodo {

    private final ServiceNodo servicioNodo;

    @Autowired
    public RestControllerNodo(ServiceNodo servicioNodo) {
        this.servicioNodo = servicioNodo;
    }


    /**
     * Obtener la lista de nodos vecinos en la red
     * @return JSON lista de URLs
     */
    @RequestMapping(method = RequestMethod.GET)
    Set <URL>getNodosVecinos() {
        System.out.println("Request: getNodosVecinos");
        return servicioNodo.getNodosVecinos(); 
    }
    

    /**
     * Obtencion de la clave publica
     */
    @GetMapping(path = "Kp")
    @ResponseBody
    public Map<String, Object> getPublicKeyNodo() {
    	System.out.println("Request: getPublicKeyNodo");
    	Map<String, Object> response = new HashMap<>();
    	response.put("PublicKey", servicioNodo.clavePublica);
    	return response;
    }


    /**
     * Dar de alta un nodo en la red
     * @param nodo a ser dado de alta
     */
    @RequestMapping(method = RequestMethod.POST)
    void altaNodo(@RequestBody String urlNodo, HttpServletResponse response) {
        System.out.println("Request: altaNodo " + urlNodo);

        try {
		    servicioNodo.altaNodo(new URL(urlNodo.replaceAll("^\"|\"$", "")));
	        response.setStatus(HttpServletResponse.SC_OK);

	    } catch (MalformedURLException e) {
	        System.out.println("Error dando de alta nodo: " +urlNodo+" "+ e);
	        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
	    }
    }

    /**
     * Dar de baja a un nodo en la red
     * @param nodo a ser dado de baja
     */
    @RequestMapping(method = RequestMethod.DELETE)
    void bajaNodo(@RequestBody String urlNodo, HttpServletResponse response) {
    	System.out.println("Request: bajaNodo " + urlNodo);

        try {
		    servicioNodo.bajaNodo(new URL(urlNodo));
	        response.setStatus(HttpServletResponse.SC_OK);

        } catch (MalformedURLException e) {
                System.out.println("Error dando de baja nodo: " +urlNodo+" "+ e);
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }


    /**
     * Endpoint auxiliar para que un nodo pueda conocer su IP publica y con la que otros nodos se comunicaran con el
     * @param request HttpServletRequest
     * @return la IP publica
     */
    @RequestMapping(path = "ip", method = RequestMethod.GET) 
    String getIpPublica(HttpServletRequest request) {
    	System.out.println("Request: getIpPublica");
    	System.out.println(request.getRemoteAddr());
        return request.getRemoteAddr();
    }
}
