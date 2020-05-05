package parser;

import domain.ClassInfo;
import domain.MethodInfo;
import enums.RegExpEnum;
import enums.StructuresInfoEnum;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import utils.RegExpUtils;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Fede
 */
public class CustomProjectClassParser implements Serializable{

    private static ArrayList<Map<String, String>> classList = new ArrayList<>();
    private static final String PROJECT_DEFAULT_FOLDER = ".\\project\\";
    private static final String GENERIC_CLASS_NAME = "public class test{";
    private static final List<String> IGNORE_FOLDERS_LIST = Arrays.asList(".git", "target");
    private static File tempFolder;
    
    public static List<ClassInfo> loadClasses(String projectUrl) {
        classList = new ArrayList<>();
        File projectFile = new File(projectUrl);
        //Check default file locations
        tempFolder = new File(PROJECT_DEFAULT_FOLDER);
        tempFolder.mkdirs();
        //
        loadFiles(projectUrl, projectFile);

        if (classList.isEmpty()) {
            return Collections.EMPTY_LIST;
        }
        List<ClassInfo> classInfoList = new ArrayList<>();

        for (Map<String, String> map : classList) {
            Map.Entry<String, String> entry = map.entrySet().iterator().next();
            String fileName = entry.getKey();
            String path = entry.getValue();

            ClassInfo classInfo = new ClassInfo();
            classInfo.setFileUrl(path);
            
            File file = new File(path);

            if (file == null) {
                throw new IllegalArgumentException("file is not found!");
            }

            Scanner sc = getScanner(file);
            if (sc == null) {
                throw new IllegalArgumentException("error reading file!");
            }

            classInfo.setName(fileName);

            boolean insideMethod = false;
            ArrayList<MethodInfo> methodList = new ArrayList<>();
            StringBuilder content = new StringBuilder();
            StringBuilder imports = new StringBuilder();
            String methodName = "";
            String packageName = "";
            String className = "";
            classInfo.setMethodList(methodList);
            int openKey = 0;
            int closeKey = 0;

            while (sc.hasNextLine()) {
                String nextLine = sc.nextLine() + System.lineSeparator();

                //Get the package name
                if (nextLine.trim().matches(RegExpEnum.PACKAGE.getType())) {
                    packageName = nextLine;
                    classInfo.setClassPackage(packageName);
                    continue;
                }
                //Get all the imports
                if (nextLine.trim().matches(RegExpEnum.IMPORTS.getType())) {
                    imports.append(nextLine);
                    continue;
                }
                //Get the class name
                if (nextLine.trim().matches(RegExpEnum.CLASS_NAME.getType())) {
                    className = nextLine;
                    continue;
                }
                //Ignore comments lines
                if (nextLine.trim().matches(RegExpEnum.COMMENTS.getType())) {
                    continue;
                }
                if (openKey == closeKey) {
                    insideMethod = false;
                    openKey = 0;
                    closeKey = 0;
                }
                
                //Check for method
                if (nextLine.trim().matches(RegExpEnum.REG_EXP_METHOD.getType())) {
                    openKey++;
                    insideMethod = true;
                    methodName = nextLine;
                    MethodInfo methodInfo = new MethodInfo();
                    methodInfo.setMethodName(methodName);
                    methodList.add(methodInfo);
                    //Body on the content, will include the class name definition
                    content = new StringBuilder();
                    //content.append(packageName);
                    //content.append(imports.toString());
                    content.append(GENERIC_CLASS_NAME).append(System.lineSeparator());
                    methodInfo.addLineQty();
                    content.append(methodName.replaceAll(RegExpUtils.getString(methodName,RegExpEnum.EXTRACT_METHOD_NAME),"test"));
                    methodInfo.addLineQty();
                    methodInfo.setPath(classInfo.getFileUrl()+methodName.trim());
                    continue;
                }

                if (insideMethod) {
                    content.append(nextLine);
                    if (nextLine.contains(StructuresInfoEnum.OPEN_STRUC.getType())) {
                        openKey++;
                    }
                    if (nextLine.contains(StructuresInfoEnum.CLOSE_STRUC.getType())) {
                        closeKey++;
                    }

                    for (MethodInfo method : methodList) {
                        if (method.getMethodName().equals(methodName)) {
                            method.setContent(content.toString());
                        }
                    }
                }
            }
            classInfoList.add(classInfo);
        }

        //Add missing  }
        classInfoList.forEach((classInfo) -> {
            classInfo.getMethodList().forEach((method) -> {
                method.setContent(method.getContent()+StructuresInfoEnum.CLOSE_STRUC.getType());
            });
        });
        
        
        return classInfoList;
    }

    private static Scanner getScanner(File file) {
        Scanner sc;
        try {
            sc = new Scanner(file);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(CustomProjectClassLoader.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        return sc;
    }

    private static void loadFiles(String projectUrl, File node) {
        if (node.isDirectory() && !IGNORE_FOLDERS_LIST.contains(node.getName())) {
            String[] subNode = node.list();
            for (String filename : subNode) {
                if (filename.contains(".java")) {

                    String path = copy(node.getPath(), filename);
                    Map<String, String> map = new HashMap<>();
                    map.put(filename, path + "\\");
                    classList.add(map);
                }
                loadFiles(projectUrl, new File(node, filename));
            }
        }

    }

    private static String copy(String projectUrl, String fileName) {
        InputStream inStream = null;
        OutputStream outStream = null;
        String packageName = "";
        File toFile = null;
        try {

            File fromFile = new File(projectUrl, fileName);
            Scanner sc = getScanner(fromFile);
            while (sc.hasNextLine()) {
                String nextLine = sc.nextLine();

                //Get the package name
                if (nextLine.trim().matches(RegExpEnum.PACKAGE.getType())) {
                    packageName = nextLine;
                    break;
                }
            }

            if (!packageName.isEmpty()) {
                packageName = packageName.substring(8, packageName.lastIndexOf(";"));
                if (packageName.contains(".")) {
                    packageName = packageName.replaceAll("\\.", "//");
                }
                packageName = packageName + "\\";
            }

            new File(PROJECT_DEFAULT_FOLDER + packageName).mkdirs();
            toFile = new File(PROJECT_DEFAULT_FOLDER + packageName + fileName);

            inStream = new FileInputStream(fromFile);
            outStream = new FileOutputStream(toFile);

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
            System.out.println("File copied successfully!");

        } catch (IOException e) {
            Logger.getLogger(CustomProjectClassLoader.class.getName()).log(Level.SEVERE, null, e);
        }
        return toFile != null ? toFile.getAbsolutePath() : "";
    }
    
    public static void deleteTempFolder(){
        if(tempFolder==null){
            return;
        }
        try{
             tempFolder.delete();
        }catch(Exception e){
            System.out.println("Error deleting temp folder!");
        }
       
    }
}
