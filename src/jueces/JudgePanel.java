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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.PrivateKey;


import javax.imageio.ImageIO;




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
        Estilos.applyDarkMode(titlePanel);

        JLabel titleLabel = new JLabel("Bienvenido, Juez");
        titleLabel.setFont(new Font(Estilos.DEFAULT_FONT.getName(), Font.BOLD, 24));
        titleLabel.setForeground(Estilos.TEXT_COLOR);
        titlePanel.add(titleLabel);
        add(titlePanel, BorderLayout.NORTH);

        // Panel para la navegación y la card
        JPanel contentPanel = new JPanel(new BorderLayout());
        Estilos.applyDarkMode(contentPanel);

        // Panel para los botones de navegación
        JPanel navigationPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        Estilos.applyDarkMode(navigationPanel);

        JButton prevButton = new JButton("");
        Estilos.styleButton(prevButton);
        JButton nextButton = new JButton("");
        Estilos.styleButton(nextButton);

        int iconWidth = 30;
        int iconHeight = 30;

        // Carga y escala la imagen prev.png
        ImageIcon prevIcon = new ImageIcon("src/assets/prev.png");
        Image prevImage = prevIcon.getImage().getScaledInstance(iconWidth, iconHeight, Image.SCALE_SMOOTH);
        prevButton.setIcon(new ImageIcon(prevImage));

        // Carga y escala la imagen next.png
        ImageIcon nextIcon = new ImageIcon("src/assets/next.png");
        Image nextImage = nextIcon.getImage().getScaledInstance(iconWidth, iconHeight, Image.SCALE_SMOOTH);
        nextButton.setIcon(new ImageIcon(nextImage));


        navigationPanel.add(prevButton);
        navigationPanel.add(nextButton);
        contentPanel.add(navigationPanel, BorderLayout.NORTH); // Navegación en la parte superior

        // Panel para las cards
        cardsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        Estilos.applyDarkMode(cardsPanel);
        contentPanel.add(cardsPanel, BorderLayout.CENTER); // Panel de cards en el centro



        // Agregar contentPanel al panel principal
        add(contentPanel, BorderLayout.CENTER);

        // Panel inferior para el botón de cerrar sesión
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        Estilos.applyDarkMode(bottomPanel);
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
                System.out.println("pinturas: "+paintingsArray);
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
            System.out.println("si debo mostrar algo");
            try {
                JSONObject painting = paintingsArray.getJSONObject(currentCardIndex);
                boolean isEvaluated = painting.has("stars");

                JPanel cardPanel = new PaintingCard(painting, privateKey, principal, judgeId, isEvaluated);
                cardsPanel.add(cardPanel);

            } catch (Exception e) {
                JOptionPane.showMessageDialog(principal.getFrame(), "Error al crear la card de la pintura.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }else{
            System.out.println("no debo mostrar nada");
        }
        cardsPanel.revalidate();
        cardsPanel.repaint();
    }






}