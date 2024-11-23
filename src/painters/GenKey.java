package painters;

import java.io.IOException;
import java.security.*;
import java.security.spec.ECGenParameterSpec;




public class GenKey {

    // funciones para generar claves ECDSA y AES para el pintor

    // ECDSA para firmar el formulario de consentimiento

    public static void generateECDSAKeys() {
        // implementar la generacion de claves ECDSA
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC");
            keyGen.initialize(new ECGenParameterSpec("secp256r1"), new SecureRandom());
            KeyPair pair = keyGen.generateKeyPair();
            PrivateKey priv = pair.getPrivate();
            PublicKey pub = pair.getPublic();


        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException e) {
            e.printStackTrace();

        }

    }




}
