package com.fqxd.gftools.features.xapk;

import net.lingala.zip4j.ZipFile;

import java.io.File;

public class OBBextrack {
    public boolean unZip(String zipFile, String targetPath)
    {
        if ((zipFile == null) || (zipFile.equals(""))) {
            System.out.println("Invalid source file");
            return false;
        }
        System.out.println("Zip file extracted!");
        return unzip(zipFile, targetPath);
    }

    private static boolean unzip(String zipFile, String targetPath){
        try {
            File temp = new File(targetPath);
            if(!temp.exists()) temp.mkdirs();
            ZipFile zip = new ZipFile(zipFile);
            zip.extractAll(targetPath);
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void deleteDirectory(String path) {
        File directory = new File(path);

        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (files == null) {
                return;
            }
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file.getAbsolutePath());
                } else {
                    file.delete();
                }
            }
        }
        directory.delete();
    }

    public void deleteFile(String path) {
        File file = new File(path);
        file.delete();
    }
}
