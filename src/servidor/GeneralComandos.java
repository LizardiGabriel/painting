package servidor;

import org.json.JSONObject;

public class GeneralComandos {

    static String terminos = "Aqui van los terminos y condiciones";

    public static String obtenerTYC(JSONObject request) {
        JSONObject response = new JSONObject();
        response.put("response", "200");
        response.put("info", "OK");
        response.put("tyc", terminos);

        return response.toString();
    }
}