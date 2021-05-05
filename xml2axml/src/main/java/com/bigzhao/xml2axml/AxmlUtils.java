package com.bigzhao.xml2axml;

import com.bigzhao.xml2axml.test.AXMLPrinter;

import java.io.*;
import java.nio.charset.StandardCharsets;

import androidstub.content.Context;

/**
 * Created by Roy on 16-4-27.
 */
public class AxmlUtils {

    public static String decode(byte[] data) {
        try(InputStream is = new ByteArrayInputStream(data)) {
            try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                AXMLPrinter.out = new PrintStream(os);
                AXMLPrinter.decode(is);
                byte[] bs = os.toByteArray();
                os.close();
                AXMLPrinter.out.close();
                return new String(bs, StandardCharsets.UTF_8);
            }
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public static String decode(File file) throws IOException {
        byte[] buffer = new byte[(int) file.length()];
        try (InputStream ios = new FileInputStream(file)) {
            if (ios.read(buffer) == -1) {
                throw new IOException("EOF reached while trying to read the whole file");
            }
        }
        return decode(buffer);
    }

    public static byte[] encode(String xml){
        try {
            Encoder encoder = new Encoder();
            return encoder.encodeString(null, xml);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public static byte[] encode(File file){
        try {
            Encoder encoder = new Encoder();
            return encoder.encodeFile(new Context(), file.getAbsolutePath());
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
