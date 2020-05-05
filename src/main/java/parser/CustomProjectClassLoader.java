package parser;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import org.apache.commons.io.FilenameUtils;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author FSOBRADO
 */
public class CustomProjectClassLoader extends ClassLoader {

    private static ArrayList<String> classList = new ArrayList<>();

    public void loadClasses(String projectUrl) {
        loadFiles(projectUrl, new File(projectUrl));

        ArrayList<File> javaClass = getJavaClassClassLoader();
        try {
            //COMPILE CLASSES
            for (File file : javaClass) {
                JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
                int result = compiler.run(null, null, null, file.getAbsolutePath());
                System.out.println("result " + result);
            }
            String folder = "";
            for (File file : javaClass) {
                folder = file.getAbsolutePath().substring(0, file.getAbsolutePath().lastIndexOf("\\"));
                if (!folder.equals("")) {
                    break;
                }
            }

            try {
                ArrayList<Class> javaClasses = loadJavaClasses(projectUrl);
                System.out.println(javaClasses);
            } catch (Exception ex) {
                Logger.getLogger(CustomProjectClassLoader.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (Exception e) {
            Logger.getLogger(CustomProjectClassLoader.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    private static void loadFiles(String projectUrl, File node) {

        if (node.isDirectory()) {
            String[] subNode = node.list();
            for (String filename : subNode) {
                if (filename.contains(".java")) {
                    classList.add(filename);
                    copy(projectUrl, node.getName() + "/" + filename, filename);
                }
                loadFiles(projectUrl, new File(filename));
            }
        }

    }

    private static void copy(String projectUrl, String fileNameA, String fileNameB) {
        InputStream inStream = null;
        OutputStream outStream = null;

        try {

            File afile = new File(projectUrl + fileNameA);
            File bfile = new File("project\\" + fileNameB);

            inStream = new FileInputStream(afile);
            outStream = new FileOutputStream(bfile);

            byte[] buffer = new byte[1024];

            int length;
            //copy the file content in bytes 
            while ((length = inStream.read(buffer)) > 0) {

                outStream.write(buffer, 0, length);

            }

            inStream.close();
            outStream.close();

            //delete the original file
            //afile.delete();
            System.out.println("File is copied successful!");

        } catch (IOException e) {
            Logger.getLogger(CustomProjectClassLoader.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    private static ArrayList<File> getJavaClassClassLoader() {
        ArrayList<File> javaClass = new ArrayList();
        for (int i = 0; i < classList.size(); i++) {
            String url = classList.get(i);
            javaClass.add(getFileFromResourcesUsingClassLoader("project\\" + url));
        }
        return javaClass;
    }

    private static File getFileFromResourcesUsingClassLoader(String fileName) {
        File file = new File(fileName);
        if (file == null) {
            throw new IllegalArgumentException("file is not found!");
        } else {
            return (file);
        }
    }

    private  ArrayList<Class> loadJavaClasses(String projectUrl) throws Exception {
        ArrayList<Class> classes = new ArrayList<>();
        File file = new File(projectUrl);
        File[] listOfFiles = file.listFiles();

        for (File fileName : listOfFiles) {
            String ext = FilenameUtils.getExtension(fileName.getName());
            if (ext.equals("class")) {
                // create FileInputStream object
                InputStream fileInputStream = new URLClassLoader(new URL[]{file.toURI().toURL()}).getResourceAsStream(fileName.getName());
                byte rawBytes[] = new byte[fileInputStream.available()];
                fileInputStream.read(rawBytes);
                Class<?> regeneratedClass = this.defineClass(rawBytes, 0, rawBytes.length);
                classes.add(regeneratedClass);
            }
        }
        return classes;
    }

}
