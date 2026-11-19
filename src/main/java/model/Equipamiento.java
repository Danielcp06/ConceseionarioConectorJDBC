package model;

public class Equipamiento {
    private String extra;

    public Equipamiento() {
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    @Override
    public String toString() {
        return "Equipamiento{" +
                "extra='" + extra + '\'' +
                '}';
    }
}
