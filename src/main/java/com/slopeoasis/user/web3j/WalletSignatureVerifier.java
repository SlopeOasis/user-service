package com.slopeoasis.user.web3j;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

import org.web3j.crypto.Keys;
import org.web3j.crypto.Sign;
import org.web3j.crypto.Sign.SignatureData;
import org.web3j.utils.Numeric;

public class WalletSignatureVerifier {
    public boolean verify(String message, String signature, String expectedAddress) {
        byte[] msgBytes = message.getBytes(StandardCharsets.UTF_8);
        byte[] sigBytes = Numeric.hexStringToByteArray(signature);
        SignatureData sig = new SignatureData(
                sigBytes[64],
                java.util.Arrays.copyOfRange(sigBytes, 0, 32),
                java.util.Arrays.copyOfRange(sigBytes, 32, 64)
        );
        try {
            BigInteger recoveredKey =
                    Sign.signedPrefixedMessageToKey(msgBytes, sig);

            String recoveredAddress =
                    "0x" + Keys.getAddress(recoveredKey);

            return recoveredAddress.equalsIgnoreCase(expectedAddress);
        } catch (Exception e) {
            return false;
        }
    }
}

