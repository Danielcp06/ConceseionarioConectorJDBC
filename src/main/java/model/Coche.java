package model;

import java.util.ArrayList;
import java.util.List;

public class Coche {
    private String dueño;
    private String matricula;
    private String modelo;
    private String marca;
    private List<Equipamiento> extra;

    public Coche(List<Equipamiento> extra) {
        this.extra = new ArrayList<>();
    }

    public String getDueño() {
        return dueño;
    }

    public void setDueño(String dueño) {
        this.dueño = dueño;
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public List<Equipamiento> getExtra() {
        return extra;
    }

    public void setExtra(List<Equipamiento> extra) {
        this.extra = extra;
    }

    @Override
    public String toString() {
        return "Coche{" +
                "dueño='" + dueño + '\'' +
                ", matricula='" + matricula + '\'' +
                ", modelo='" + modelo + '\'' +
                ", marca='" + marca + '\'' +
                ", extra=" + extra +
                '}';
    }
}
