public class Producto {
    String nombre;
    int existencias;

    public Producto(String nombre, int existencias) {
        this.nombre = nombre;
        this.existencias = existencias;
    }

    public void vender(int cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero.");
        }
        if (cantidad > existencias) {
            throw new IllegalArgumentException("No hay suficientes existencias.");
        }
        existencias = existencias - cantidad;
    }

    public static void main(String[] args) {
        Producto cuaderno = new Producto("Cuaderno", 10);

        System.out.println("Producto: " + cuaderno.nombre);
        System.out.println("Existencias: " + cuaderno.existencias);

        cuaderno.vender(3);
        System.out.println("Existencias después de vender 3: " + cuaderno.existencias);
    }
}
