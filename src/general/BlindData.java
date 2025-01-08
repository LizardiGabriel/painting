package general;

import java.math.BigInteger;

public class BlindData {
    private byte[] blinded_msg;
    private BigInteger inv;

    public BlindData(byte[] blinded_msg, BigInteger inv) {
        this.blinded_msg = blinded_msg;
        this.inv = inv;
    }

    public byte[] getBlinded_msg() {
        return blinded_msg;
    }

    public BigInteger getInv() {
        return inv;
    }
}
