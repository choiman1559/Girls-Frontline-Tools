package com.fqxd.gftools.alarm.palarm.decrypt;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import java.io.*;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Date;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

public class GfEncryptionInputStream extends InputStream implements AutoCloseable {

    private InputStream in;

    private Date date;

    public GfEncryptionInputStream(final InputStream in, final Sign sign) throws IOException {
        Cipher rc4;
        try {
            rc4 = Cipher.getInstance("RC4");
            rc4.init(
                    Cipher.DECRYPT_MODE,
                    sign.getSecretKeySpec(),
                    rc4.getParameters()
            );
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | InvalidAlgorithmParameterException e) {
            throw new IOException(e);
        }
        final byte[] buffer = new byte[in.available()];
        in.read(buffer);
        final byte[] rawData;
        try {
            rawData = rc4.doFinal(Base64.decodeBase64(buffer));
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new IOException(e);
        }

        final byte[] date = new byte[10];
        final byte[] check = new byte[16];
        {
            final ByteBuffer rawDataBuffer = ByteBuffer.wrap(rawData);
            rawDataBuffer.get(date);
            rawDataBuffer.get(check);
        }
        final byte[] generatedCheck = sign.generateCheck(rawData, 26, rawData.length - 26);

        this.date = new Date(
                Long.parseLong(new String(date))
        );

        if (!Arrays.equals(generatedCheck, check)) {
            Logger.getGlobal().warning("check doesn't match");
        }

        this.in = new ByteArrayInputStream(
                rawData,
                26,
                rawData.length
        );

        final int magic = rawData[26] & 0xFF | ((rawData[27] << 8) & 0xFF00);
        if (magic == GZIPInputStream.GZIP_MAGIC) {
            this.in = new GZIPInputStream(this.in);
        }
    }

    public Date getDate() {
        return date;
    }

    @Override
    public int read() throws IOException {
        return in.read();
    }

    @Override
    public int read(byte[] b) throws IOException {
        return in.read(b);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return in.read(b, off, len);
    }

    @Override
    public void close() throws IOException {
        in.close();
    }
}
