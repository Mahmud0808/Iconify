package com.drdisagree.iconify.utils.apksigner;

/* From
 * https://github.com/topjohnwu/Magisk/blob/master/app/signing/src/main/java/com/topjohnwu/signing/CryptoUtils.java
 */

import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;

public class CryptoUtils {

    public static X509Certificate readCertificate(InputStream input)
            throws IOException, GeneralSecurityException {
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            return (X509Certificate) cf.generateCertificate(input);
        } finally {
            input.close();
        }
    }

    /**
     * Read a PKCS#8 format private key.
     */
    public static PrivateKey readPrivateKey(InputStream input)
            throws IOException, GeneralSecurityException {
        try {
            ByteArrayStream buf = new ByteArrayStream();
            buf.readFrom(input);
            byte[] bytes = buf.toByteArray();
            /* Check to see if this is in an EncryptedPrivateKeyInfo structure. */
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(bytes);
            /*
             * Now it's in a PKCS#8 PrivateKeyInfo structure. Read its Algorithm
             * OID and use that to construct a KeyFactory.
             */
            ASN1InputStream bIn = new ASN1InputStream(new ByteArrayInputStream(spec.getEncoded()));
            PrivateKeyInfo pki = PrivateKeyInfo.getInstance(bIn.readObject());
            String algOid = pki.getPrivateKeyAlgorithm().getAlgorithm().getId();
            return KeyFactory.getInstance(algOid).generatePrivate(spec);
        } finally {
            input.close();
        }
    }
}