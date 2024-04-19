package com.example.demo;


import java.util.ArrayList;
import java.util.List;

/*
 * La cadena de bloques es esencialmente una lista de bloques enlazados ya que cada bloque tiene el identificador del bloque anterior.
 * */
public class CadenaDeBloques {

	// Lista de bloques en la cadena ordenados por altura (posicion en la cadena)
	
	private List<Bloque> bloques = new ArrayList<Bloque>();
	
	public CadenaDeBloques() {
	}

	public CadenaDeBloques(List<Bloque> bloques) {
		this.bloques = bloques;
	}
	
	public List<Bloque> getBloques() {
		return bloques;
	}

	public void setBloques(List<Bloque> bloques) {
		this.bloques = bloques;
	}


	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		CadenaDeBloques cadena = (CadenaDeBloques) o;
		
		if(bloques.size() != cadena.getBloques().size())
			return false;

		for(int i=0;i<bloques.size();i++) {
			if(bloques.get(i) != cadena.getBloques().get(i))
				return false;
		}
		
		return true;
	}


    @Override
    public String toString() {
        return bloques.toString();
    }

    
	public void añadirBloque(Bloque bloque) {
		bloques.add(bloque);
	}


	public boolean estaVacia() {
		return this.bloques == null || this.bloques.isEmpty();
	}
	

	public Bloque getUltimoBloque() {
		if (estaVacia()) {
			return null;
		}
		
		return this.bloques.get(this.bloques.size() - 1);
	}	
}