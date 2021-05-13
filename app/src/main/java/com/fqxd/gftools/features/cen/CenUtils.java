package com.fqxd.gftools.features.cen;

import android.annotation.SuppressLint;

import com.fqxd.gftools.global.Global;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

public class CenUtils {
    protected static int checkDatabase(String Package) throws IOException, InterruptedException {
        if (!new File(Global.Storage + "/GF_Tool/").exists()) new File(Global.Storage + "/GF_Tool/").mkdir();
        else if (new File(Global.Storage + "/GF_Tool/newXML.xml").exists())
            new File(Global.Storage + "/GF_Tool/newXML.xml").delete();

        Process p = Runtime.getRuntime().exec("su");
        DataOutputStream dos = new DataOutputStream(p.getOutputStream());
        dos.writeBytes("mount -o remount,rw /data\n");
        dos.writeBytes("cp -f /data/data/" + Package + "/shared_prefs/" + Package + ".v2.playerprefs.xml /sdcard/GF_Tool/\n");
        dos.writeBytes("exit\n");
        dos.flush();
        dos.close();
        p.waitFor();

        File Xml = new File(Global.Storage + "/GF_Tool/" + Package + ".v2.playerprefs.xml");
        while (true) {
            if (Xml.exists()) break;
        }
        String line;

        BufferedReader bufferedReader = new BufferedReader(new FileReader(Xml));
        while ((line = bufferedReader.readLine()) != null) {
            if (line.contains("Normal")) {
                if (line.contains("value=\"1\"")) {
                    Xml.delete();
                    return 0;
                } else if (line.contains("value=\"0\"")) {
                    Xml.delete();
                    return 1;
                }
            }
        }
        Xml.delete();
        return -1;
    }

    protected static int checkRootAndRunTask(boolean istrue, String Package) {
        try {
            if (isDataAvailable(Package)) {
                editTask(istrue, Package);
            } else {
                return -1;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return -2;
        }
        return 0;
    }

    @SuppressLint("SdCardPath")
    protected static boolean isDataAvailable(String Package) throws InterruptedException, IOException {
        Process process = Runtime.getRuntime().exec("su");
        DataOutputStream dos = new DataOutputStream(process.getOutputStream());
        dos.writeBytes("find /data/data/" + Package + "/shared_prefs/" + Package + ".v2.playerprefs.xml\n");
        dos.writeBytes("exit\n");
        dos.flush();
        dos.close();
        process.waitFor();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        boolean value = false;
        while ((line = reader.readLine()) != null) {
            if (line.contains("/data/data/" + Package + "/shared_prefs/" + Package + ".v2.playerprefs.xml"))
                value = true;
        }
        process.waitFor();
        return value;
    }

    private static void editTask(boolean istrue, String Package) {
        try {
            if (!new File(Global.Storage + "/GF_Tool/").exists()) new File(Global.Storage + "/GF_Tool/").mkdir();
            else if (new File(Global.Storage + "/GF_Tool/newXML.xml").exists())
                new File(Global.Storage + "/GF_Tool/newXML.xml").delete();

            Process p = Runtime.getRuntime().exec("su");
            DataOutputStream dos = new DataOutputStream(p.getOutputStream());
            dos.writeBytes("mount -o remount,rw /data\n");
            dos.writeBytes("cp -f /data/data/" + Package + "/shared_prefs/" + Package + ".v2.playerprefs.xml /sdcard/GF_Tool/\n");
            dos.writeBytes("exit\n");
            dos.flush();
            dos.close();
            p.waitFor();

            Xmledit(istrue, Package);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void copyXml(String Package) {
        try {
            Process p = Runtime.getRuntime().exec("su");
            DataOutputStream dos = new DataOutputStream(p.getOutputStream());
            dos.writeBytes("mount -o remount,rw /data\n");
            dos.writeBytes("cp -fp /sdcard/GF_Tool/" + Package + ".v2.playerprefs.xml /data/data/" + Package + "/shared_prefs/\n");
            dos.writeBytes("exit\n");
            dos.flush();
            dos.close();
            p.waitFor();

            new File(Global.Storage + "/GF_Tool/" + Package + ".v2.playerprefs.xml").delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void Xmledit(boolean istrue, String Package) {
        final int cen = istrue ? 1 : 0;

        File newXml = new File(Global.Storage + "/GF_Tool/newXML.xml");
        File Xml = new File(Global.Storage + "/GF_Tool/" + Package + ".v2.playerprefs.xml");

        String line;
        try {
            while (true) {
                if (Xml.exists()) break;
            }

            FileWriter Xmlwrite = new FileWriter(newXml);
            BufferedReader bufferedReader = new BufferedReader(new FileReader(Xml));

            while ((line = bufferedReader.readLine()) != null) {
                if (line.contains("Normal")) {
                    Xmlwrite.append("    <int name=\"Normal\" value=\"" + cen + "\" />\n");
                    Xmlwrite.flush();
                    continue;
                } else Xmlwrite.append(line + "\n");
                Xmlwrite.flush();
            }

            bufferedReader.close();
            Xmlwrite.close();

            Xml.delete();
            newXml.renameTo(Xml);
            copyXml(Package);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
