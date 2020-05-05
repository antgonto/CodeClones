/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package process;

import analyze.CompareAST;
import analyze.GenerateAST;
import domain.ClassInfo;
import domain.CloneMethod;
import domain.MethodInfo;
import enums.CloneTypeEnum;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JEditorPane;
import org.eclipse.jdt.core.dom.ASTNode;
import parser.CustomProjectClassParser;
import utils.ResultsLogger;

/**
 *
 * @author Fede
 */
public class Process implements Serializable{
    public static Map<String,ClassInfo> process(JEditorPane peditor, String projectUrl) {
        List<ClassInfo> loadClasses = CustomProjectClassParser.loadClasses(projectUrl);
        Map<String,ClassInfo> results = new LinkedHashMap<>();

        ResultsLogger.doLog("DETECTING CLONES IN PROJECT... " + ResultsLogger.LINE_BREAK);
        for (ClassInfo classInfo : loadClasses) {
            ResultsLogger.doLog("Class " + classInfo.getName() + ResultsLogger.LINE_BREAK);

            for (MethodInfo method : classInfo.getMethodList()) {
                ResultsLogger.doLog(ResultsLogger.SINGLE_TAB + "Method " + method.getMethodName() + ResultsLogger.LINE_BREAK);
                ASTNode AST_A = GenerateAST.parse(method.getContent());
                for (ClassInfo classInfoInner : loadClasses) {
                    for (MethodInfo methodInner : classInfoInner.getMethodList()) {
                        //Same class and same method, ignore it
                        if (classInfo.getName().equals(classInfoInner.getName())
                                && method.getMethodName().equals(methodInner.getMethodName())) {
                            continue;
                        }

                        ASTNode AST_B = GenerateAST.parse(methodInner.getContent());
                        CloneTypeEnum codeCloneType = CompareAST.codeCloneTypeDetector(AST_A, AST_B, method.getLineQty(), methodInner.getLineQty());
                        //
                        if (codeCloneType.getType() > 0) {
                            CloneMethod cloneMethod = new CloneMethod(methodInner.getMethodName(), methodInner.getPath(), codeCloneType);
                            ResultsLogger.paintResults(codeCloneType, method, methodInner);

                            if (results.containsKey(classInfo.getFileUrl())) {
                                //Get all the declared methods for the class
                                List<MethodInfo> classMethodList = results.get(classInfo.getFileUrl()).getMethodList();
                                int cont = 0;
                                boolean found = false;
                                for (MethodInfo meth : classMethodList) {
                                    if (meth.equals(method)) {
                                        results.get(classInfo.getFileUrl()).getMethodList().get(cont).getClones().add(cloneMethod);
                                        found = true;
                                        break;
                                    }
                                    cont++;
                                }
                                if (!found) {
                                    method.getClones().add(cloneMethod);
                                    results.get(classInfo.getFileUrl()).getMethodList().add(method);
                                }
                                //Class not yet on the results
                            } else {
                                results.put(classInfo.getFileUrl(), classInfo);
                                results.get(classInfo.getFileUrl()).getMethodList().get(0).getClones().add(cloneMethod);
                            }
                        }//

                    }
                }
                //System.out.println(method.getContent()); 
            }//End For for method list
        }

        return results;
    }

   
}
