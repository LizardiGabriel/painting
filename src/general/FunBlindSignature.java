package general;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;

public class FunBlindSignature {

    //'Blind' del mensaje(evaluacion) para que el chairman lo firme
    public static String[] blind(String evaluacion, RSAPublicKey publicKey) throws Exception{
        byte[] message = evaluacion.getBytes("UTF-8");

        BigInteger publicExponent = publicKey.getPublicExponent();
        BigInteger modulus = publicKey.getModulus();

        byte[] encoded_message;
        do {
            encoded_message = emsaPSSEncode(message, modulus.bitLength());
        }while(encoded_message[0] >= (byte) (0x90));

        BigInteger m = new BigInteger(1, encoded_message);
        if(!m.gcd(modulus).equals(BigInteger.ONE)) {
            return null;
        }

        BigInteger r;
        SecureRandom random = SecureRandom.getInstanceStrong();
        do {
            r = new BigInteger(modulus.bitLength(), random);
        }while(r.compareTo(BigInteger.ONE) <= 0 || r.compareTo(modulus) >= 0 || !r.gcd(modulus).equals(BigInteger.ONE));

        BigInteger inv = r.modInverse(modulus);
        BigInteger x = r.modPow(publicExponent, modulus).mod(modulus);
        BigInteger z = m.multiply(x).mod(modulus);

        String blinded_msg = Base64.getEncoder().encodeToString(z.toByteArray());
        String invString = Base64.getEncoder().encodeToString(inv.toByteArray());

        String[] res = new String[2];
        res[0] = blinded_msg;
        res[1] = invString;

        return res;
    }

    //Metodo del chairman para que firme
    public static String blindSign(RSAPrivateKey privateKey, String blindedMessage) throws Exception {
        byte[] blinded_msg = Base64.getDecoder().decode(blindedMessage);
        BigInteger m = new BigInteger(1, blinded_msg);
        BigInteger privateExponent = privateKey.getPrivateExponent();
        BigInteger modulus = privateKey.getModulus();

        BigInteger s = m.modPow(privateExponent, modulus).mod(modulus);

        return Base64.getEncoder().encodeToString(s.toByteArray());
    }

    //Transformar el blind signature en una firma valida.
    public static String finalize(RSAPublicKey key, String message, String blindSig, String inv) throws Exception {
        byte[] blind_sig = Base64.getDecoder().decode(blindSig);
        byte[] invBytes = Base64.getDecoder().decode(inv);

        BigInteger z = new BigInteger(1, blind_sig);
        BigInteger invInteger = new BigInteger(1, invBytes);

        BigInteger modulus = key.getModulus();

        BigInteger s = z.multiply(invInteger).mod(modulus);

        return Base64.getEncoder().encodeToString(s.toByteArray());
    }


    public static String RSASSA_PSS_Verify(RSAPublicKey key, String message, String sig) throws Exception {
        byte[] sigBytes = Base64.getDecoder().decode(sig);

        BigInteger s = new BigInteger(1, sigBytes);
        BigInteger publicExpontent = key.getPublicExponent();
        BigInteger modulus = key.getModulus();

        BigInteger m = s.modPow(publicExpontent, modulus).mod(modulus);
        byte[] em = m.toByteArray();

        String res = emsaPSSVerify(message, em, modulus.bitLength() - 1);
        return res;
    }

    public static byte[] emsaPSSEncode(byte[] m, int emBits) throws Exception{
        MessageDigest sha384 = MessageDigest.getInstance("SHA-384");
        byte[] mhash = sha384.digest(m);
        int hlen = sha384.getDigestLength();

        SecureRandom random = SecureRandom.getInstanceStrong();
        byte[] salt = new byte[hlen];
        random.nextBytes(salt);

        byte[] m_aux = new byte[8 + hlen + hlen];
        for (int i = 0; i < 8; i++) {
            m_aux[i] = 0;
        }

        System.arraycopy(mhash, 0, m_aux, 8, hlen);
        System.arraycopy(salt, 0, m_aux, 8 + hlen, hlen);

        byte[] h = sha384.digest(m_aux);

        int emlen = emBits / 8;
        byte[] ps = new byte[emlen - hlen - hlen - 2];
        for (int i = 0; i < ps.length; i++) {
            ps[i] = 0;
        }

        byte[] db = new byte[ps.length + hlen + 1];
        System.arraycopy(ps, 0, db, 0, ps.length);
        System.arraycopy(salt, 0, db, ps.length + 1, hlen);
        db[ps.length] = (byte) 0x01;

        byte[] dbMask = MGF1(h, emlen - hlen - 1);
        byte[] maskedDb = new byte[dbMask.length];

        for (int i = 0; i < dbMask.length; i++) {
            maskedDb[i] = (byte) (db[i] ^ dbMask[i]);
        }

        byte[] em = new byte[emlen];
        System.arraycopy(maskedDb, 0, em, 0, maskedDb.length);
        System.arraycopy(h, 0, em, maskedDb.length, h.length);
        em[emlen - 1] = (byte) 0xBC;

        return em;
    }

    private static String emsaPSSVerify(String message, byte[] em, int emBits) throws Exception {
        MessageDigest sha384 = MessageDigest.getInstance("SHA-384");
        byte[] mhash = sha384.digest(message.getBytes("UTF-8"));
        int hlen = sha384.getDigestLength();
        int emlen = emBits / 8;

        if(em[0] == 0) {
            byte[] newEm = new byte[em.length - 1];
            System.arraycopy(em, 1, newEm, 0, em.length-1);
            em = newEm;
        }

        if(emlen < hlen + hlen + 2) {
            System.out.println("Length");
            return "inconsistent";
        }

        if(em[em.length - 1] != (byte) (0xBC)) {
            System.out.println(String.format("0xBC != %02x",  em[em.length - 1]));
            return "inconsistent";
        }

        byte[] maskedDb = new byte[emlen - hlen];
        byte[] h = new byte[hlen];
        System.arraycopy(em, 0, maskedDb, 0, maskedDb.length);
        System.arraycopy(em, maskedDb.length, h, 0, hlen);

        byte[] dbMask = MGF1(h, emlen - hlen);
        byte[] db = new byte[dbMask.length];

        for (int i = 0; i < db.length; i++) {
            db[i] = (byte) (dbMask[i] ^ maskedDb[i]);
        }

        for (int i = 0; i < emlen - hlen - hlen - 2; i++) {
            if(db[i] != 0) {
                System.out.println("Zeros");
                return "inconsistent";
            }

        }

        if(db[emlen - hlen - hlen - 1] != (byte) (0x01)) {
            System.out.println("0x01");
            return "inconsistent";
        }

        byte[] salt = new byte[hlen];
        System.arraycopy(db, db.length - hlen, salt, 0, hlen);

        byte[] m_aux = new byte[8 + hlen + hlen];
        for (int i = 0; i < 8; i++) {
            m_aux[i] = 0;
        }

        System.arraycopy(mhash, 0, m_aux, 8, hlen);
        System.arraycopy(salt, 0, m_aux, 8 + hlen, hlen);

        byte[] h_aux = sha384.digest(m_aux);

        for (int i = 0; i < h_aux.length; i++) {
            if(h[i] != h_aux[i]) {
                System.out.println("hash");
                return "inconsistent";
            }

        }

        return "consistent";
    }

    private static byte[] MGF1(byte[] seed, int len) throws Exception{
        MessageDigest sha384 = MessageDigest.getInstance("SHA-384");
        byte[] mask = new byte[len];
        int generatedBytes = 0;
        int counter = 0;

        while (generatedBytes < len) {
            sha384.update(seed);
            sha384.update((byte) (counter >> 24));;
            sha384.update((byte) (counter >> 16));;
            sha384.update((byte) (counter >> 8));;
            sha384.update((byte) counter);
            byte[] hash = sha384.digest();
            int bytesToCopy = Math.min(hash.length, len - generatedBytes);
            System.arraycopy(hash, 0, mask, generatedBytes, bytesToCopy);
            generatedBytes += bytesToCopy;
            counter++;
        }

        return mask;
    }
}
