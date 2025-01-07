package president;

import general.Estilos;
import general.Principal;
import general.SocketHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class PresidentApp {
    private Principal principal;
    private String token;

    public PresidentApp(Principal principal) {
        this.principal = principal;
    }

    public JPanel presidentPanel(String token) {
        this.token = token;

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Estilos.SECONDARY_COLOR);

        // Panel de título
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(Estilos.SECONDARY_COLOR);
        JLabel titleLabel = new JLabel("Bienvenido, Presidente");
        titleLabel.setFont(new Font(Estilos.DEFAULT_FONT.getName(), Font.BOLD, 24));
        titleLabel.setForeground(Estilos.PRIMARY_COLOR);
        titlePanel.add(titleLabel);
        mainPanel.add(titlePanel, BorderLayout.NORTH);

        // TODO: lista de evaluaciones y el botón para firmar

        // Panel inferior para el botón de cerrar sesión
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setBackground(Estilos.SECONDARY_COLOR);
        JButton logOutButton = new JButton("Cerrar Sesión");
        Estilos.styleButton(logOutButton);
        bottomPanel.add(logOutButton);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        // Acción del botón de cerrar sesión
        logOutButton.addActionListener(e -> {
            SocketHandler.authToken = "";
            principal.showLoginFromAnyPanel();
        });

        return mainPanel;
    }
}