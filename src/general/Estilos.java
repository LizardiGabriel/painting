package general;

import java.awt.Color;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.border.Border;

public class Estilos {

    // Paleta de colores
    public static final Color PRIMARY_COLOR = new Color(16, 32, 59); // Azul oscuro
    public static final Color SECONDARY_COLOR = new Color(245, 245, 245); // Gris claro (casi blanco)
    public static final Color ACCENT_COLOR = new Color(52, 152, 219); // Azul claro (para botones, etc.)
    public static final Color TEXT_COLOR = new Color(51, 51, 51); // Gris oscuro (para texto)
    public static final Color BORDER_COLOR = new Color(192, 192, 192); // Gris claro (para bordes)



    // Fuente
    public static final Font DEFAULT_FONT = new Font("Arial", Font.PLAIN, 14);

    // Bordes
    public static final Border FIELD_BORDER = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(5, 5, 5, 5));

    // Estilo para botones
    public static void styleButton(JButton button) {
        button.setFont(DEFAULT_FONT);
        button.setBackground(ACCENT_COLOR);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // Efecto hover
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(ACCENT_COLOR.brighter());
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(ACCENT_COLOR);
            }
        });
    }
}