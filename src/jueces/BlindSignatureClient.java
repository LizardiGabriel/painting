package jueces;

import java.math.BigInteger;
import java.security.*;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PSSParameterSpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;

public class BlindSignatureClient {

    public static String prepareBlindMessage(String evaluation, PublicKey publicKey) throws Exception {
        byte[] evaluationBytes = evaluation.getBytes("UTF-8");

        RSAPublicKeySpec publicSpec = KeyFactory.getInstance("RSA").getKeySpec(publicKey, RSAPublicKeySpec.class);
        BigInteger modulus = publicSpec.getModulus();
        BigInteger publicExponent = publicSpec.getPublicExponent();

        SecureRandom random = new SecureRandom();
        BigInteger blindingFactor;
        do {
            blindingFactor = new BigInteger(modulus.bitLength() - 1, random);
        } while (blindingFactor.compareTo(BigInteger.ONE) <= 0 || blindingFactor.compareTo(modulus) >= 0 || !blindingFactor.gcd(modulus).equals(BigInteger.ONE));

        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        byte[] hash = sha256.digest(evaluationBytes);
        BigInteger messageInt = new BigInteger(1, hash);
        BigInteger blindedMessageInt = (messageInt.multiply(blindingFactor.modPow(publicExponent, modulus))).mod(modulus);

        return Base64.getEncoder().encodeToString(blindedMessageInt.toByteArray()) + "_" + Base64.getEncoder().encodeToString(blindingFactor.toByteArray());
    }

    public static String unblindSignature(String blindSignature, String blindingFactorBase64, PublicKey publicKey) throws Exception {
        byte[] blindSignatureBytes = Base64.getDecoder().decode(blindSignature);

        BigInteger blindSignatureInt = new BigInteger(1, blindSignatureBytes);

        RSAPublicKeySpec publicSpec = KeyFactory.getInstance("RSA").getKeySpec(publicKey, RSAPublicKeySpec.class);
        BigInteger modulus = publicSpec.getModulus();


        BigInteger blindingFactor = new BigInteger(1, Base64.getDecoder().decode(blindingFactorBase64));


        BigInteger signatureInt = blindSignatureInt.multiply(blindingFactor.modInverse(modulus)).mod(modulus);


        byte[] signatureBytes = signatureInt.toByteArray();

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
        byte[] blindedMessageBytes = Base64.getDecoder().decode(blindedMessage);

        Signature signature = Signature.getInstance("RSASSA-PSS");
        signature.setParameter(new PSSParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, 32, 1));
        signature.initSign(privateKey);
        signature.update(blindedMessageBytes);
        byte[] blindSignatureBytes = signature.sign();

        return Base64.getEncoder().encodeToString(blindSignatureBytes);
    }




}