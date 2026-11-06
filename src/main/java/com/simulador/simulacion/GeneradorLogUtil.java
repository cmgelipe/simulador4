package com.simulador.simulacion;

import java.text.SimpleDateFormat;
import java.util.UUID;

/**
 * Utilidades compartidas por los generadores de log simulado (antes
 * métodos privados de Modelo.BD: fecha(), generaCadena(), generaNumero()).
 */
public class GeneradorLogUtil {

    private GeneradorLogUtil() {
    }

    public static String fecha() {
        java.util.Date d = new java.util.Date();
        SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat dt2 = new SimpleDateFormat("HH:mm:ss.SS");
        String ceros = "000000000000000000000000000";
        String x = dt.format(d) + "T" + dt2.format(d);
        ceros = ceros.substring(0, 27 - x.length() - 1);
        return x + ceros + "Z\t";
    }

    public static String generaCadena() {
        return "" + UUID.randomUUID();
    }

    public static String generaNumero() {
        return "" + (int) (Math.random() * 25 + 1);
    }

    /**
     * Identificador de sesión/operación nuevo en cada llamada (antes: los
     * generadores de log de Mongo reusaban siempre el mismo par de uuids
     * fijos para todas las operaciones, lo que hacía trivialmente
     * detectable que el log era simulado).
     */
    public static String nuevoUuid() {
        return UUID.randomUUID().toString();
    }

    /**
     * Entero aleatorio en [minimoInclusive, maximoInclusive], usado para
     * variar métricas del log (durationMillis, numYields, reslen...) en
     * vez de usar siempre la misma constante fija.
     */
    public static int enteroEntre(int minimoInclusive, int maximoInclusive) {
        return minimoInclusive + (int) (Math.random() * (maximoInclusive - minimoInclusive + 1));
    }
}
