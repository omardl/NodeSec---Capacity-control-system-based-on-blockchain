package com.example.demo;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;


@Service
public class ServiceNodo implements ApplicationListener <WebServerInitializedEvent> {

	private final ServiceBloques servicioBloques;
	private final ServiceTransacciones servicioTransacciones;

	// URL del nodo actual (host + port)
	private URL miUrlNodo;
	public byte[] clavePublica;
	private byte[] clavePrivada;
	
	// Resto de nodos en la red
	private Set<URL> nodosVecinos = new HashSet<>(); 

	private RestTemplate restTemplate = new RestTemplate();

	@Autowired
	public ServiceNodo(ServiceBloques servicioCadenaDeBloques, ServiceTransacciones servicioTransacciones) {
		this.servicioBloques = servicioCadenaDeBloques;
		this.servicioTransacciones = servicioTransacciones;
	}


	/**
	 * Al iniciar el nodo se debe: - Obtener la lista de nodos en la red -
	 * Obtener la cadena de bloques - Obtener transactiones en el pool - Dar de alta
	 * mi nodo en el resto de nodos
	 * 
	 * @param webServerInitializedEvent WebServer para obtener el puerto
	 */
	
    @Override 
	public void onApplicationEvent(WebServerInitializedEvent webServerInitializedEvent) {
    
		// Obtener la url del nodo master
		URL urlNodoMaster = getNodoMaster();

		// Calcular la url (host y puerto)
		String host = getIpPublica(urlNodoMaster, restTemplate);
		int port = webServerInitializedEvent.getWebServer().getPort();

		miUrlNodo = getMiUrlNodo(host, port);

		// Descargar cadena de bloques y transacciones en pool si no soy nodo master
		if (miUrlNodo.equals(urlNodoMaster)) {
			System.out.println("Ejecutando nodo master");

		} else {
			nodosVecinos.add(urlNodoMaster);

			// Obtener lista de nodos, bloques y transacciones
			obtenerNodosVecinos(urlNodoMaster, restTemplate);
			servicioBloques.obtenerCadenaDeBloques(urlNodoMaster, restTemplate);
			servicioTransacciones.obtenerPoolTransacciones(urlNodoMaster, restTemplate);

			// Dar de alta este nodo en el resto de nodos en la red
			emitirPeticionPostNodosVecinos("nodo", miUrlNodo);
		}
			
		clavePublica = "MIIBuDCCASwGByqGSM44BAEwggEfAoGBAP1/U4EddRIpUt9KnC7s5Of2EbdSPO9EAMMeP4C2USZpRV1AIlH7WT2NWPq/xfW6MPbLm1Vs14E7gB00b/JmYLdrmVClpJ+f6AR7ECLCT7up1/63xhv4O1fnxqimFQ8E+4P208UewwI1VBNaFpEy9nXzrith1yrv8iIDGZ3RSAHHAhUAl2BQjxUjC8yykrmCouuEC/BYHPUCgYEA9+GghdabPd7LvKtcNrhXuXmUr7v6OuqC+VdMCz0HgmdRWVeOutRZT+ZxBxCBgLRJFnEj6EwoFhO3zwkyjMim4TwWeotUfI0o4KOuHiuzpnWRbqN/C/ohNWLx+2J6ASQ7zKTxvqhRkImog9/hWuWfBpKLZl6Ae1UlZAFMO/7PSSoDgYUAAoGBAJOpKdIMrUgi6vjPNv9iXAaLKzihwQO1764tKIlaOengc3+ScNM36U8vk0PhwUJJcDYHjc/Alb4C0RyNipYVLcQEJYTy6AYHFb0sEOZmlmv/TVdFZIOryPh9m49n/tJSNf7hixd80t5PF5gUKEte9BVnRR3jZK1ddoD6IznrnR6E".getBytes();
		clavePrivada = "MIICeAIBADANBgkqhkiG9w0BAQEFAASCAmIwggJeAgEAAoGBAMHtd271DAsW3eWk\\n+vg7mOQTRZRazYrh+2X6fg6Y/QYLRLWCTQknc15Qbs4g2jxTYiza33L9oL6Exw2M\\nXOOJn/4LqGw0mXzNhcsgl/Mi4qdQ7xGFKWRW1qMVgsK0VCMZPRBhrVugBFUUcNSm\\nLoEKQ/t7TVpRMFHHZoSZYxMEAZtJAgMBAAECgYEAtGebzr3pyTspjCPRUTHoBmyj\\nMuLSCZw2ieTgdeX+pCvBbHVeAuEPYzLCEcHgI3SShRXL/yZQ7kuI/WKRWx9BlK1E\\n4eDZst5MFOcpAMzTJeHCEQnGxG/G2nCcnqK80Xpke30KJltIav+P7rDUfY4QpbTb\\noq81XRHflWSVKcb7QwECQQDv6FYwkcGfARp6MHrBGBm1xemIIvBFze242wjp5B1F\\n4b/+yV+z1MvYLecc9XBH1tsnmoo6NbAOGSs6X01qj3ghAkEAzu+RTbHlhBb9kgKT\\ntsZdIQ3km5jHbDOKnT5sH+xIW5bH2G86urSo5mv1mV4F/Jo7mfrT1xH1zbCQ5Mvo\\nLoOeKQJAG6YaMNTLjMiyxXxK2XAunl1l0bO9Hz5hdFmCCHqqyQAAgZuxtOcEazC3\\nHwZGX3EqpsDPx2+ij61k5wBOysYoIQJBALnJXmQp/ozr8zaet63WRBCEH6YOsaSU\\nVbO9Mmgqw//uzHelzU2JG0bc0ICkaK2Ub0RcbMrf5haIml3AMDFqm5kCQQDNepc8\\n63w4aDCng9mRje+f4FzI7etziLD3YnkcHj71CBBNItVV+qc5462L0wyYz1ZKA+OD\\nNl+WFY49M0RcN50b".getBytes();	 
	}


	/**
	 * Dar de baja el nodo del resto de nodos antes de pararlo completamente
	 */
	@PreDestroy  
	public void shutdown() {
		System.out.println("Parando nodo...");
		// Enviar peticion para que el resto de nodos den de baja el nodo
		emitirPetitionDeleteNodosVecinos("nodo", miUrlNodo);
	}


	/**
	 * Obtener nodos vecinos en la red
	 */
	public Set<URL> getNodosVecinos() { 
		return nodosVecinos;
	}


	/**
	 * Dar de alta un nodo
	 */
	public synchronized void altaNodo(URL urlNodo) {
		nodosVecinos.add(urlNodo);
	}



	/**
	 * Dar de baja un nodo
	 */
	public synchronized void bajaNodo(URL urlNodo) {
		nodosVecinos.remove(urlNodo);
	}

	/**
	 * Enviar peticion de tipo PUT al resto de nodos en la red (nodos vecinos)
	 * 
	 * @param endpoint el endpoint para esta peticion
	 * @param datos    los datos que se quieren enviar con la peticion
	 */
	public void emitirPeticionPutNodosVecinos(String endpoint, Object datos) {
		nodosVecinos.parallelStream().forEach(urlNodo -> restTemplate.put(urlNodo + "/" + endpoint, datos));
	}


	/**
	 * Enviar peticion de tipo POST al resto de nodos en la red (nodos vecinos)
	 * 
	 * @param endpoint el endpoint para esta peticion
	 * @param datos    los datos que se quieren enviar con la peticion
	 */
	public void emitirPeticionPostNodosVecinos(String endpoint, Object data) {
		nodosVecinos.parallelStream().forEach(urlNodo -> restTemplate.postForLocation(urlNodo + "/" + endpoint, data));
	}


	/**
	 * Enviar peticion de tipo DELETE al resto de nodos en la red (nodos vecinos)
	 * 
	 * @param endpoint el endpoint para esta peticion
	 * @param datos    los datos que se quieren enviar con la peticion
	 */
	public void emitirPetitionDeleteNodosVecinos(String endpoint, Object data) {
		nodosVecinos.parallelStream().forEach(urlNodo -> restTemplate.delete(urlNodo + "/" + endpoint, data));
	}


	/**
	 * Obtener la lista de nodos en la red
	 * 
	 * @param urlNodoVecino Nodo vecino al que hacer la peticion
	 * @param restTemplate  RestTemplate a usar
	 */
	public void obtenerNodosVecinos(URL urlNodoVecino, RestTemplate restTemplate) {
		URL[] nodos = restTemplate.getForObject(urlNodoVecino + "/nodo", URL[].class);
		Collections.addAll(nodosVecinos, nodos);
	}


	/**
	 * Obtener la IP publica con la que me conecto a la red
	 * 
	 * @param urlNodoVecino Nodo vecino al que hacer la peticion
	 * @param restTemplate  RestTemplate a usar
	 */
	private String getIpPublica(URL urlNodoVecino, RestTemplate restTemplate) {
		return restTemplate.getForObject(urlNodoVecino + "/nodo/ip", String.class);
	}
	
	
	public byte[] getPublicKeyNodo(URL miUrlNodo, RestTemplate restTemplate) {
		return restTemplate.getForObject(miUrlNodo + "/nodo/Kp", byte[].class);
	}


	/**
	 * Construir mi url a partir de mi host y puerto
	 * 
	 * @param host Mi host publico
	 * @param port Puerto en el que se lanza el servicio
	 */
	private URL getMiUrlNodo(String host, int port) {
		try {
			return new URL("http", host, port, "");
		} catch (MalformedURLException e) {
			System.out.println("Invalida URL Nodo:" + e);
			return null;
		}
	}


	/**
	 * Obtener URL del nodo master del archivo de configuracion o iterando la red
	 */
	private URL getNodoMaster() {

		URL dir=nodeExist(Configuracion.getInstancia().getUrlNodoMaster());

		if (dir==null) {
			int port=8080;
			String	base= "http://localhost:%d";

			for (int i=0; i<10;i++) {
				port++;
				String dest= String.format(base, port);
				URL dir2= nodeExist(dest);

				if (dir2!=null) {
					return dir2;
				}
			}
			return null;	

		}else {
			return dir;
		}
	}
	

	/**
	 * Comprobar si una URL respoinde a una petición
	 */
	public URL nodeExist(String node) {
		
		try {
	        restTemplate.getForEntity(node+ "/nodo/ip", Void.class);
	        System.out.println("Nodo existe: " + node);
	        return new URL(node);
	       
	    } catch (MalformedURLException e) {
	        System.out.println("Invalida URL Nodo Master: " + e);
	        return null;

	    } catch (RestClientException e) {
	        System.out.println("Error al conectar con el Nodo Master: " + e);
	        return null;
	    }	
	}
}