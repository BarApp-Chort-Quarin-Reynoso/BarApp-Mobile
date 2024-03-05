package com.barapp.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.barapp.util.Maps;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class MapsTest {

    @Test
    @DisplayName("Debería retornar 800 ya que es un valor mayor que el factor de redondeo y menor a la distancia maxima a mostrar")
    public void adaptarDistanciasTest() {
        Integer distancia = 841;
        assertEquals(800, Maps.adaptarDistancia(distancia));
    }

    @Test
    @DisplayName("Debería retornar null porque la distancia es mayor a la distancia máxima a mostrar")
    public void adaptarDistanciasTest2() {
        Integer distancia = 1500;
        assertNull(Maps.adaptarDistancia(distancia));
    }

    @Test
    @DisplayName("Debería retornar 50 ya que la distancia es menor a la mínima distancia a mostrar, definida por el factor de redondeo")
    public void adaptarDistanciasTest3() {
        Integer distancia = 24;
        assertEquals(50, Maps.adaptarDistancia(distancia));
    }
}
