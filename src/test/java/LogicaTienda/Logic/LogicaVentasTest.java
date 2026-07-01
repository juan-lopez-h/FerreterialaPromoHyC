package LogicaTienda.Logic;

import LogicaTienda.Model.Productos;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

class LogicaVentasTest {

    @Test
    void venderDebeReducirSoloLaCantidadTotal() {
        Productos producto = new Productos("1", "Martillo", 1000.0, 10, 0);

        boolean resultado = new LogicaVentas().vender(producto, 4);

        assertTrue(resultado);
        assertEquals(6, producto.getCantidad());
    }
}
