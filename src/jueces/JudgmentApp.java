package jueces;

import general.Principal;

import javax.swing.*;

public class JudgmentApp {

    private Principal principal;

    public JudgmentApp(Principal principal) {
        this.principal = principal;
    }

    public JPanel judgePanel(String token) {
        return new JudgePanel(principal, token);
    }

    public JPanel clavesRsaPanel(String username, String nombre, String password) {
        return new RegisterPanel(principal, username, nombre, password);
    }
}