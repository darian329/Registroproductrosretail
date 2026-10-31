import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

/**
 * Montos en pesos chilenos: se muestran como $1.500 o $1.500,50 y se aceptan
 * escritos como 1500, 1.500, $1.500, 1500,50, 1.500,50 o 1500.50.
 */
public final class Moneda {

    private static final DecimalFormatSymbols SIMBOLOS = new DecimalFormatSymbols();
    static {
        SIMBOLOS.setGroupingSeparator('.');
        SIMBOLOS.setDecimalSeparator(',');
    }

    private Moneda() {}

    public static String formatear(BigDecimal monto) {
        BigDecimal m = monto.setScale(2, RoundingMode.HALF_UP);
        String patron = m.stripTrailingZeros().scale() <= 0 ? "#,##0" : "#,##0.00";
        return "$" + new DecimalFormat(patron, SIMBOLOS).format(m);
    }

    /** Devuelve el monto escrito por el usuario, o null si no es un monto válido (letras, negativos, NaN, etc.). */
    public static BigDecimal leer(String texto) {
        if (texto == null) return null;
        String t = texto.replace("$", "").replace(" ", "").trim();
        if (!t.matches("[0-9.,]+")) return null;

        if (t.indexOf(',') >= 0) {
            // Coma = decimales, puntos = miles (1.500,50)
            if (t.indexOf(',') != t.lastIndexOf(',')) return null;
            t = t.replace(".", "").replace(',', '.');
        } else if (t.matches("[1-9]\\d{0,2}(\\.\\d{3})+")) {
            // Solo puntos de miles (1.500 o 20.000.000)
            t = t.replace(".", "");
        } else if (t.indexOf('.') != t.lastIndexOf('.')) {
            return null;
        }

        try {
            return new BigDecimal(t);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** true si el monto tiene más de 2 decimales (p.ej. 0,001). */
    public static boolean tieneMasDeDosDecimales(BigDecimal monto) {
        return monto.stripTrailingZeros().scale() > 2;
    }
}
