package general;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import org.json.JSONObject;

public class FunEcdsa {

    public static void main(String[] args) {
        // generar claves ECDSA
        JSONObject json = new JSONObject(generateECDSAKeys());
        String priv = json.getString("private");
        String pub = json.getString("public");
        System.out.println("Clave privada: " + priv + "\n" + "Clave publica: " + pub);

        // generar firma ECDSA
        String mensaje = "mensaje a firmar";
        String firma = firmarECDSA(mensaje, priv);
        System.out.println("Firma: " + firma);

        // verificar firma
        boolean verificado = verificarECDSA(mensaje, firma, pub);
        System.out.println("Firma verificada: " + verificado);
    }


    // ECDSA para firmar el formulario de consentimiento
    public static String generateECDSAKeys() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC");
            keyGen.initialize(new ECGenParameterSpec("secp256r1"), new SecureRandom());
            KeyPair pair = keyGen.generateKeyPair();
            PrivateKey priv = pair.getPrivate();
            PublicKey pub = pair.getPublic();

            byte[] encodedPrivateKey = priv.getEncoded();
            byte[] encodedPublicKey = pub.getEncoded();
            JSONObject json = new JSONObject();
            json.put("algoritmo", "ECDSA");
            json.put("private", Base64.getEncoder().encodeToString(encodedPrivateKey));
            json.put("public", Base64.getEncoder().encodeToString(encodedPublicKey));
            String jsonDatos = json.toString();

            return jsonDatos;
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException e) {
            System.out.println(e);
        }

        return "";
    }

    public static String firmarECDSA(String data, String privateKey) {
        if (privateKey == null) {
            return "";
        }
        try {

            PrivateKey priv = (PrivateKey) KeyFactory.getInstance("EC").
                    generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKey)));

            Signature ecdsa = Signature.getInstance("SHA256withECDSA");
            ecdsa.initSign(priv);
            byte[] strByte = data.getBytes("UTF-8");
            ecdsa.update(strByte);

            byte[] realSig = ecdsa.sign();
            BigInteger firma = new BigInteger(1, realSig);
            return firma.toString(16);

        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException | UnsupportedEncodingException |
                 InvalidKeySpecException e) {
            System.out.println(e);
        }
        return "";
    }


    public static boolean verificarECDSA(String mensaje, String firma, String pub) {
        try {
            PublicKey publicKey = KeyFactory.getInstance("EC")
                    .generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(pub)));

            Signature ecdsaVerify = Signature.getInstance("SHA256withECDSA");
            ecdsaVerify.initVerify(publicKey);
            ecdsaVerify.update(mensaje.getBytes("UTF-8"));
            byte[] signatureBytes = new BigInteger(firma, 16).toByteArray();
            return ecdsaVerify.verify(signatureBytes);
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException | UnsupportedEncodingException |
                 InvalidKeySpecException e) {
            System.out.println(e);
        }
        return false;
    }




}