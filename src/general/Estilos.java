package general;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicScrollBarUI;

public class Estilos {

    // Paleta de colores para modo oscuro
    public static final Color PRIMARY_COLOR = new Color(20, 20, 20);
    public static final Color SECONDARY_COLOR = new Color(40, 40, 40);


    public static final Color ACCENT_COLOR = new Color(241, 234, 234, 160);


    public static final Color BORDER_COLOR = new Color(243, 237, 237);

    public static final Color TEXT_COLOR = new Color(255, 255, 255);

    public static final Color Button_color_principal = new Color(7, 42, 200, 255);

    public static final Color Button_color_zeno = new Color(7, 42, 200, 255);



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
        button.setForeground(Button_color_principal);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // Efecto hover
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(ACCENT_COLOR.darker());
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(ACCENT_COLOR);
            }
        });
    }

    public static void styleMainButton(JButton button) {
        button.setFont(DEFAULT_FONT);
        button.setBackground(Button_color_principal);
        button.setForeground(TEXT_COLOR);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // Efecto hover
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(Button_color_principal.darker());
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(Button_color_principal);
            }
        });
    }



    public static void applyDarkMode(JComponent component) {
        component.setBackground(SECONDARY_COLOR);
        component.setForeground(TEXT_COLOR);
        if (component instanceof JScrollPane) {
            JScrollPane scrollPane = (JScrollPane) component;
            scrollPane.setBorder(FIELD_BORDER);
            scrollPane.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
                @Override
                protected void configureScrollBarColors() {
                    this.thumbColor = ACCENT_COLOR;
                    this.trackColor = SECONDARY_COLOR;
                }
            });
            scrollPane.getHorizontalScrollBar().setUI(new BasicScrollBarUI() {
                @Override
                protected void configureScrollBarColors() {
                    this.thumbColor = ACCENT_COLOR;
                    this.trackColor = SECONDARY_COLOR;
                }
            });
        } else if (component instanceof JTextField || component instanceof JTextArea) {
            component.setBackground(PRIMARY_COLOR);
            component.setForeground(TEXT_COLOR);
            ((JComponent) component).setBorder(FIELD_BORDER);
        } else if (component instanceof JComboBox) {
            component.setBackground(PRIMARY_COLOR);
            component.setForeground(TEXT_COLOR);
            ((JComboBox<?>) component).setBorder(FIELD_BORDER);
        } else if (component instanceof JLabel) {
            component.setForeground(TEXT_COLOR);
        }

        for (Component child : component.getComponents()) {
            if (child instanceof JComponent) {
                applyDarkMode((JComponent) child);
            }
        }
    }



}