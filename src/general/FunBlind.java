package general;

import java.math.BigInteger;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

public class FunBlind {

    public static void main(String[] args) throws NoSuchAlgorithmException{
        System.out.println("Prueba:");
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");

        keyPairGenerator.initialize(1024);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();


        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();

        String evaluacion = "123;3;Define sus trazos de manera exquisita y refleja en su textura su tecnica.";
        System.out.print("Prepare Randomize...\t");
        byte[] payload = evaluacion.getBytes();
        payload = prepareRandomize(payload);
        if(payload == null) {
            System.out.println("Error");
            return;
        }
        System.out.println("Done");

        System.out.print("Blind...\t");
        BlindData data = blind(payload, publicKey);
        if(data == null) {
            System.out.println("Error");
            return;
        }
        System.out.println("Done");
        System.out.print("BlindSign...\t");
        byte[] BlindedSign = blindSign(publicKey, privateKey, data.getBlinded_msg());
        if(BlindedSign == null) {
            System.out.println("Error");
            return;
        }
        System.out.println("Done");
        System.out.print("Finalize...\t");
        byte[] sig = finalize(publicKey, payload, data.getBlinded_msg(), data.getInv());
        if(sig == null) {
            System.out.println("Error");
            return;
        }

        System.out.println("Exito");

    }

    public static byte[] prepareRandomize(byte[] payload) {
        try {
            byte[] prefix = new byte[32];
            SecureRandom rand = SecureRandom.getInstanceStrong();
            rand.nextBytes(prefix);
            int len = payload.length;

            byte[] output = new byte[len + 32];
            for (int i = 0; i < len + 32; i++) {
                if(i < 32)
                    output[i] = prefix[i];
                else
                    output[i] = payload[i - 32];
            }

            return output;
        }catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static BlindData blind(byte[] message, RSAPublicKey destKey) {
        try {

            byte[] encoded_msg = EMSA_PSS_encode(message, destKey.getModulus().bitLength());
            if(encoded_msg == null) {
                return null;
            }

            BigInteger m = OS2IP(encoded_msg);

            if (!m.gcd(destKey.getModulus()).equals(BigInteger.ONE)) {
                //invalid input error
                return null;
            }
            SecureRandom rand = SecureRandom.getInstanceStrong();
            BigInteger r;
            do {
                r = new BigInteger(destKey.getModulus().bitLength(), rand);
            }while (r.compareTo(BigInteger.ONE) <= 0 || r.compareTo(destKey.getModulus()) >= 0);

            BigInteger inv = r.modInverse(destKey.getModulus());

            BigInteger x = r.modPow(destKey.getPublicExponent(), destKey.getModulus());

            BigInteger z = m.multiply(x).mod(destKey.getModulus());

            int byteLen = (int) Math.ceil((double) destKey.getModulus().bitLength() / 8);
            byte[] blinded_msg = I2OSP(z, byteLen);

            return new BlindData(blinded_msg, inv);
        }catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] blindSign(RSAPublicKey p, RSAPrivateKey key, byte[] blinded_msg) {
        BigInteger m = OS2IP(blinded_msg);
        BigInteger s = m.modPow(key.getPrivateExponent(), key.getModulus());
        BigInteger m2 = s.modPow(p.getPublicExponent(), p.getModulus());

        if(!m.equals(m2)) {
            return null;
        }
        int byteLen = (int) Math.ceil((double) key.getModulus().bitLength() / 8);

        return I2OSP(s, byteLen);
    }

    public static byte[] finalize(RSAPublicKey key, byte[] msg, byte[] blind_sig, BigInteger inv) {
        int byteLen = (int) Math.ceil((double) key.getModulus().bitLength() / 8);
        if(blind_sig.length != byteLen) {
            return null;
        }

        BigInteger z = OS2IP(blind_sig);
        BigInteger s = z.multiply(inv).mod(key.getModulus());

        byte[] sig = I2OSP(s, byteLen);
        if(RSASSA_PSS_VERIFY(key, msg, sig).equals("invalid signature")) {
            return null;
        }

        return sig;
    }

    private static String RSASSA_PSS_VERIFY(RSAPublicKey key, byte[] msg, byte[] sig) {
        int byteLen = (int) Math.ceil((double) key.getModulus().bitLength() / 8);
        if(sig.length != byteLen) {
            return "invalid signature";
        }
        BigInteger s = OS2IP(sig);
        BigInteger m = s.modPow(key.getPublicExponent(), key.getModulus());

        int modBits = key.getModulus().bitLength();
        int emLen = (int) Math.ceil((double) (modBits-1)/8);
        byte[] em = I2OSP(m, emLen);

        if(!EMSA_PSS_Verify(msg, em, modBits - 1).equals("consistent")) {
            return "invalid signature";
        }

        return "valid signature";
    }

    private static String EMSA_PSS_Verify(byte[] msg, byte[] em, int emBits) {

        byte[] mhash = hash(msg);

        int emlen = (int) Math.ceil((double) emBits / 8);
        if(emlen < 98) {
            return "inconsistent";
        }

        if(em[em.length -1] != (byte)0xBC) {
            return "inconsistent";
        }

        int maskedDbLen = emlen - 48 - 1;
        byte[] maskedDb = new byte[maskedDbLen];
        for (int i = 0; i < maskedDbLen; i++) {
            maskedDb[i] = em[i];
        }

        byte[] h = new byte[48];
        for (int i = 0; i < 48; i++) {
            h[i] = em[i+maskedDbLen];
        }

        int bitsZero = 8*emlen - emBits;
        byte mask = (byte) 0xFF;
        mask <<= 8 - bitsZero;

        if((byte)(maskedDb[0] ^ mask) != 0) {
            return "inconsistent";
        }

        byte[] dbMask = MGF(h, emlen - 48 - 1);
        byte[] db = new byte[maskedDbLen];
        for (int i = 0; i < maskedDbLen; i++) {
            db[i] = (byte) (dbMask[i] ^ maskedDb[i]);
        }

        byte mask2 = (byte) 0xFF;
        mask2 >>= bitsZero;
        db[0] = (byte) (db[0] ^ mask2);

        int numBytes = emlen - 48 - 48 - 2;
        for(int i = 0; i < numBytes; i++) {
            if(db[i] != 0) {
                return "inconsistent";
            }

        }

        if(db[numBytes+1] != 0x01) {
            return "inconsistent";
        }


        byte[] salt = new byte[48];
        for (int i = 0; i < 48; i++) {
            salt[i] = db[db.length-48 + i];
        }

        byte[] m = new byte[8 + 48 + 48];
        for(int i = 0; i < 8 + 48+ 48; i++) {
            if(i < 8)
                m[i] = 0;
            else if(i < 48 + 8)
                m[i] = mhash[i - 8];
            else
                m[i] = salt[i - 48 - 8];
        }

        byte[] hv = hash(m);

        for (int i = 0; i < hv.length; i++) {
            if(hv[i] != h[i]) {
                return "inconsistent";
            }

        }

        return "consistent";
    }

    private static byte[] EMSA_PSS_encode(byte[] message, int emBits) {
        try {

            byte[] mhash = hash(message);
            int emlen = (int) Math.ceil((double)emBits/8);
            //if emLen < hlen + slen + 2: hlen = slen = 48
            if(emlen < 98) {
                //error: encoding error
                return null;
            }
            SecureRandom rand = SecureRandom.getInstanceStrong();
            byte[] salt = new byte[48];
            rand.nextBytes(salt);

            int mlen = 8 + 48 + 48;
            byte[] m = new byte[mlen];
            for (int i = 0; i < mlen; i++) {
                if(i < 8)
                    m[i] = 0;
                else if (i < 8 + 48) {
                    m[i] = mhash[i - 8];
                }else {
                    m[i] = salt[i - 8 - 48];
                }
            }

            byte[] h = hash(m);
            byte[] ps = new byte[emlen - 48 - 48 - 2];

            for (int i = 0; i < ps.length; i++) {
                ps[i] = 0;
            }

            byte[] db = new byte[ps.length + 1 + 48];
            for (int i = 0; i < ps.length + 1 + 48; i++) {
                if( i < ps.length)
                    db[i] = ps[i];
                else if ( i > ps.length)
                    db[i] = salt[i - ps.length - 1];
                else
                    db[i] = 0x01;
            }

            byte[] dbMask = MGF(h, emlen - 48 - 1);
            byte[] maskedDb = new byte[db.length];

            for (int i = 0; i < db.length; i++) {
                maskedDb[i] = (byte) (db[i] ^ dbMask[i]);
            }

            byte mask = (byte) 0xFF;
            mask >>= (8*emlen - emBits);

            maskedDb[0] = (byte) (maskedDb[0] & mask);

            int emlen2 = maskedDb.length + h.length + 1;
            byte[] em = new byte[emlen2];
            for (int i = 0; i < emlen2; i++) {
                if (i < maskedDb.length)
                    em[i] = maskedDb[i];
                else if (i < emlen2 - 1)
                    em[i] = h[i - maskedDb.length];
                else
                    em[i] = (byte) 0xbc;
            }

            return em;
        }catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static byte[] MGF(byte[] mgfseed, int maskLen) {
        byte[] T = new byte[0];
        for (int i = 0; i < (int) Math.ceil((double)maskLen / 48); i++) {
            byte[] c = I2OSP(BigInteger.valueOf(i), 4);
            T = concatenateArrays(T, hash(concatenateArrays(mgfseed, c)));
        }

        return truncate(T, maskLen);
    }

    private static byte[] truncate(byte[] t, int len) {
        if(t.length <= len) {
            return t;
        }else {
            byte[] truncated = new byte[len];
            System.arraycopy(t, 0, truncated, 0, len);
            return truncated;
        }
    }

    private static byte[] I2OSP(BigInteger x, int xlen) {
        byte[] X = new byte[xlen];
        byte[] xbytes = x.toByteArray();

        int offset = Math.max(xbytes.length - xlen, 0);
        System.arraycopy(xbytes, offset, X, xlen - xbytes.length + offset, xbytes.length - offset);
        return X;
    }

    private static BigInteger OS2IP(byte[] X) {
        BigInteger result = BigInteger.ZERO;
        for (int i = 0; i < X.length; i++) {
            result = result.shiftLeft(8).or(BigInteger.valueOf(X[i] & 0xFF));
        }
        return result;
    }

    private static byte[] hash(byte[] message) {
        try {
            MessageDigest sha384digest = MessageDigest.getInstance("SHA-384");
            sha384digest.update(message);
            return sha384digest.digest();
        }catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] concatenateArrays(byte[] array1, byte[] array2) {
        byte[] result = new byte[array1.length + array2.length];
        System.arraycopy(array1, 0, result, 0, array1.length);
        System.arraycopy(array2, 0, result, array1.length, array2.length);
        return result;
    }

}
