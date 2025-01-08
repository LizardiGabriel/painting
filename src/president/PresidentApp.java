package president;

import general.Estilos;
import general.Principal;
import general.SocketHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.json.JSONArray;
import org.json.JSONObject;

public class PresidentApp {
    private Principal principal;
    private String token;
    private JList<String> evaluationsList;
    private DefaultListModel<String> listModel;

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
        titleLabel.setForeground(Estilos.TEXT_COLOR);
        titlePanel.add(titleLabel);
        mainPanel.add(titlePanel, BorderLayout.NORTH);

        // Panel central para la lista de evaluaciones y el botón de firmar
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(Estilos.SECONDARY_COLOR);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Lista de evaluaciones
        listModel = new DefaultListModel<>();
        evaluationsList = new JList<>(listModel);
        evaluationsList.setFont(Estilos.DEFAULT_FONT);
        JScrollPane scrollPane = new JScrollPane(evaluationsList);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        // Botón de firmar
        JButton signButton = new JButton("Firmar Evaluación");
        Estilos.styleButton(signButton);
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(Estilos.SECONDARY_COLOR);
        buttonPanel.add(signButton);
        centerPanel.add(buttonPanel, BorderLayout.SOUTH);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // Panel inferior para el botón de cerrar sesión
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setBackground(Estilos.SECONDARY_COLOR);
        JButton logOutButton = new JButton("Cerrar Sesión");
        Estilos.styleButton(logOutButton);
        bottomPanel.add(logOutButton);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        // Acción del botón de firmar
        signButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedEvaluation = evaluationsList.getSelectedValue();
                if (selectedEvaluation != null) {
                    // TODO: Implementar la lógica de firma ciega
                    JOptionPane.showMessageDialog(mainPanel, "Firmar a ciegas: " + selectedEvaluation);
                } else {
                    JOptionPane.showMessageDialog(mainPanel, "Selecciona una evaluación de la lista.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Acción del botón de cerrar sesión
        logOutButton.addActionListener(e -> {
            SocketHandler.authToken = "";
            principal.showLoginFromAnyPanel();
        });

        // Cargar la lista de evaluaciones al inicio
        updateEvaluationsList();

        return mainPanel;
    }

    private void updateEvaluationsList() {
        String evaluationsJson = SocketHandler.getEvaluationsForPresident(token);
        if (evaluationsJson != null) {
            try {
                JSONArray evaluationsArray = new JSONArray(evaluationsJson);
                listModel.clear();
                for (int i = 0; i < evaluationsArray.length(); i++) {
                    JSONObject evaluation = evaluationsArray.getJSONObject(i);
                    String evaluationInfo = "ID Evaluación: " + evaluation.getInt("evaluation_id") +
                            ", ID Pintura: " + evaluation.getInt("painting_id") +
                            ", Juez: " + evaluation.getString("judge_name");
                    listModel.addElement(evaluationInfo);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(principal.getFrame(), "Error al actualizar la lista de evaluaciones.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(principal.getFrame(), "No se pudieron obtener las evaluaciones.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}