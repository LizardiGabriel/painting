package jueces;

import java.math.BigInteger;
import java.security.*;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PSSParameterSpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;

public class BlindSignatureClient {

    public static String prepareBlindMessage(String evaluation, PublicKey publicKey) throws Exception {
        // 1. Codificar la evaluación en bytes (se asume codificación UTF-8).
        byte[] evaluationBytes = evaluation.getBytes("UTF-8");

        // 2. Obtener los parámetros de la clave pública RSA.
        RSAPublicKeySpec publicSpec = KeyFactory.getInstance("RSA").getKeySpec(publicKey, RSAPublicKeySpec.class);
        BigInteger modulus = publicSpec.getModulus();
        BigInteger publicExponent = publicSpec.getPublicExponent();

        // 3. Generar un factor de cegado aleatorio que sea coprimo con el módulo.
        SecureRandom random = new SecureRandom();
        BigInteger blindingFactor;
        do {
            blindingFactor = new BigInteger(modulus.bitLength() - 1, random);
        } while (blindingFactor.compareTo(BigInteger.ONE) <= 0 || blindingFactor.compareTo(modulus) >= 0 || !blindingFactor.gcd(modulus).equals(BigInteger.ONE));

        // 4. Cegar el mensaje (hash de la evaluación).
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        byte[] hash = sha256.digest(evaluationBytes);
        BigInteger messageInt = new BigInteger(1, hash);
        BigInteger blindedMessageInt = (messageInt.multiply(blindingFactor.modPow(publicExponent, modulus))).mod(modulus);

        // 5. Codificar el mensaje cegado y el factor de cegado en Base64 y concatenarlos con una barra baja (_).
        return Base64.getEncoder().encodeToString(blindedMessageInt.toByteArray()) + "_" + Base64.getEncoder().encodeToString(blindingFactor.toByteArray());
    }

    public static String unblindSignature(String blindSignature, String blindingFactorBase64, PublicKey publicKey) throws Exception {
        // 1. Decodificar la firma a ciegas de Base64.
        byte[] blindSignatureBytes = Base64.getDecoder().decode(blindSignature);

        // 2. Convertir la firma a ciegas a un entero.
        BigInteger blindSignatureInt = new BigInteger(1, blindSignatureBytes);

        // 3. Obtener el módulo de la clave pública.
        RSAPublicKeySpec publicSpec = KeyFactory.getInstance("RSA").getKeySpec(publicKey, RSAPublicKeySpec.class);
        BigInteger modulus = publicSpec.getModulus();

        // 4. Decodificar el blinding factor de Base64.
        BigInteger blindingFactor = new BigInteger(1, Base64.getDecoder().decode(blindingFactorBase64));

        // 5. Desenmascarar la firma.
        BigInteger signatureInt = blindSignatureInt.multiply(blindingFactor.modInverse(modulus)).mod(modulus);

        // 6. Convertir la firma a bytes.
        byte[] signatureBytes = signatureInt.toByteArray();

        // 7. Codificar la firma en Base64.
        return Base64.getEncoder().encodeToString(signatureBytes);
    }

    /**
     * Firma un mensaje cegado utilizando la clave privada del presidente.
     *
     * @param blindedMessage El mensaje cegado a firmar, en formato Base64.
     * @param privateKey    La clave privada del presidente.
     * @return La firma a ciegas en formato Base64.
     * @throws Exception Si ocurre algún error durante el proceso de firma.
     */
    public static String blindSign(String blindedMessage, PrivateKey privateKey) throws Exception {
        // 1. Decodificar el mensaje cegado de Base64.
        byte[] blindedMessageBytes = Base64.getDecoder().decode(blindedMessage);

        // 2. Firmar el mensaje cegado con la clave privada del presidente.
        Signature signature = Signature.getInstance("RSASSA-PSS");
        signature.setParameter(new PSSParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, 32, 1));
        signature.initSign(privateKey);
        signature.update(blindedMessageBytes);
        byte[] blindSignatureBytes = signature.sign();

        // 3. Codificar la firma a ciegas en Base64.
        return Base64.getEncoder().encodeToString(blindSignatureBytes);
    }




}