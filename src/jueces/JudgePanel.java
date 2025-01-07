package jueces;

import general.Estilos;
import general.Principal;
import general.SocketHandler;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.security.PrivateKey;

public class JudgePanel extends JPanel {

    private Principal principal;
    private String token;
    private String judgeId;
    private PrivateKey privateKey;
    private JPanel cardsPanel;
    private int currentCardIndex = 0;
    private JSONArray paintingsArray;

    public JudgePanel(Principal principal, String token) {
        this.principal = principal;
        this.token = token;

        // Obtener el ID del juez a partir del token
        String[] tokenParts = token.split("_");
        this.judgeId = tokenParts[0];

        // Solicitar al juez que cargue su clave privada
        this.privateKey = RSAKeyLoader.loadPrivateKey(principal.getFrame());
        if (this.privateKey == null) {
            JOptionPane.showMessageDialog(principal.getFrame(), "Error al cargar la clave privada. Intente de nuevo.", "Error", JOptionPane.ERROR_MESSAGE);
            setLayout(new BorderLayout());
            add(new JPanel(), BorderLayout.CENTER); // Retorna un panel vacío
            return;
        }

        setLayout(new BorderLayout());
        setBackground(Estilos.SECONDARY_COLOR);

        // Panel de título
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(Estilos.SECONDARY_COLOR);
        JLabel titleLabel = new JLabel("Bienvenido, Juez");
        titleLabel.setFont(new Font(Estilos.DEFAULT_FONT.getName(), Font.BOLD, 24));
        titleLabel.setForeground(Estilos.PRIMARY_COLOR);
        titlePanel.add(titleLabel);
        add(titlePanel, BorderLayout.NORTH);

        // Panel para la navegación y la card
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(Estilos.SECONDARY_COLOR);

        // Panel para los botones de navegación
        JPanel navigationPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        navigationPanel.setBackground(Estilos.SECONDARY_COLOR);
        JButton prevButton = new JButton("Anterior");
        Estilos.styleButton(prevButton);
        JButton nextButton = new JButton("Siguiente");
        Estilos.styleButton(nextButton);
        navigationPanel.add(prevButton);
        navigationPanel.add(nextButton);
        contentPanel.add(navigationPanel, BorderLayout.NORTH); // Navegación en la parte superior

        // Panel para las cards
        cardsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        cardsPanel.setBackground(Estilos.SECONDARY_COLOR);
        contentPanel.add(cardsPanel, BorderLayout.CENTER); // Panel de cards en el centro

        // Agregar contentPanel al panel principal
        add(contentPanel, BorderLayout.CENTER);

        // Panel inferior para el botón de cerrar sesión
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setBackground(Estilos.SECONDARY_COLOR);
        JButton logOutButton = new JButton("Cerrar Sesión");
        Estilos.styleButton(logOutButton);
        bottomPanel.add(logOutButton);
        add(bottomPanel, BorderLayout.SOUTH);

        // Acción del botón de cerrar sesión
        logOutButton.addActionListener(e -> {
            SocketHandler.authToken = "";
            principal.showLoginFromAnyPanel();
        });

        // Acciones de los botones de navegación
        prevButton.addActionListener(e -> {
            if (paintingsArray != null && paintingsArray.length() > 0) {
                currentCardIndex = (currentCardIndex - 1 + paintingsArray.length()) % paintingsArray.length();
                updateCardsPanel();
            }
        });

        nextButton.addActionListener(e -> {
            if (paintingsArray != null && paintingsArray.length() > 0) {
                currentCardIndex = (currentCardIndex + 1) % paintingsArray.length();
                updateCardsPanel();
            }
        });

        // Cargar la lista de pinturas al inicio
        loadPaintings();
    }

    private void loadPaintings() {
        String paintingsJson = SocketHandler.getPaintingsForJudge(token);
        if (paintingsJson != null) {
            try {
                this.paintingsArray = new JSONArray(paintingsJson);
                this.currentCardIndex = 0;
                updateCardsPanel();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(principal.getFrame(), "Error al cargar la lista de pinturas.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(principal.getFrame(), "No se pudieron obtener las pinturas.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateCardsPanel() {
        cardsPanel.removeAll();
        if (paintingsArray != null && paintingsArray.length() > 0) {
            try {
                JSONObject painting = paintingsArray.getJSONObject(currentCardIndex);
                JPanel cardPanel = new PaintingCard(painting, privateKey, principal, judgeId);
                cardsPanel.add(cardPanel);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(principal.getFrame(), "Error al crear la card de la pintura.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        cardsPanel.revalidate();
        cardsPanel.repaint();
    }
}