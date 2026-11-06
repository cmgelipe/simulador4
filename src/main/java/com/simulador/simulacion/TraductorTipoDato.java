package com.simulador.simulacion;

/**
 * Traduce el nombre de un tipo de dato de la base de datos (según lo
 * reportan los distintos motores) a la categoría interna que usan los
 * generadores de log (antes Modelo.BD#transTipoDato).
 */
public class TraductorTipoDato {

    private TraductorTipoDato() {
    }

    public static int traducir(String tipo) {
        tipo = tipo.toUpperCase();
        switch (tipo) {
            case "CHAR":
            case "VARCHAR":
            case "BIT":
            case "TINYINT":
            case "STRING":
                return 0;
            case "INTEGER":
            case "SMALLINT":
            case "BIGINT":
            case "NUMERIC":
            case "DECIMAL":
            case "FLOAT":
            case "DOUBLE":
            case "INT":
            case "LONG":
                return 1;
            case "DATE":
            case "TIME":
            case "TIMESTAMP":
            case "DATETIME":
                return 2;
            case "OBJECTID":
                return 4;
            case "ARRAY":
                return 5;
            case "BOOL":
            case "NULL":
            case "OBJECT":
                // Tipos de MongoDB sin un operador de rango natural: solo
                // igualdad/desigualdad (misma categoría operacional que 0).
                return 0;
            default:
                return 3;
        }
    }
}
