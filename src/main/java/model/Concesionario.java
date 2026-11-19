package model;

import java.util.List;

public class Concesionario {
    private List<Coche> coches;

    public Concesionario() {
    }

    public List<Coche> getCoches() {
        return coches;
    }

    public void setCoches(List<Coche> coches) {
        this.coches = coches;
    }

    @Override
    public String toString() {
        return "Concesionario{" +
                "coches=" + coches +
                '}';
    }
}
