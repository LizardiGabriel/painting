package servidor;

import java.math.BigInteger;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.Base64;

public class BlindSignatureServer {

    /**
     * Realiza la firma a ciegas de un mensaje.
     *
     * @param blindedMessage   El mensaje cegado, en formato Base64.
     * @param privateKey      La clave privada del firmante (presidente).
     * @return                La firma a ciegas, en formato Base64.
     * @throws Exception      Si ocurre algún error durante el proceso.
     */
    public static String blindSign(String blindedMessage, PrivateKey privateKey) throws Exception {
        byte[] blindedMessageBytes = Base64.getDecoder().decode(blindedMessage);

        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(blindedMessageBytes);
        byte[] blindSignatureBytes = signature.sign();

        return Base64.getEncoder().encodeToString(blindSignatureBytes);
    }

    /**
     * Verifica una firma digital.
     *
     * @param evaluation    La evaluación original (sin cegar), en formato String.
     * @param signature     La firma, en formato Base64.
     * @param publicKey     La clave pública del firmante (presidente).
     * @return              `true` si la firma es válida, `false` en caso contrario.
     * @throws Exception    Si ocurre algún error durante el proceso.
     */
    public static boolean verifySignature(String evaluation, String signature, PublicKey publicKey) throws Exception {
        byte[] evaluationBytes = evaluation.getBytes("UTF-8");
        byte[] signatureBytes = Base64.getDecoder().decode(signature);

        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initVerify(publicKey);
        sig.update(evaluationBytes);
        return sig.verify(signatureBytes);
    }
}