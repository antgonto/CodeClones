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
import java.util.List;
import java.util.Map;
import javax.swing.JEditorPane;
import org.apache.spark.api.java.JavaRDD;
import org.eclipse.jdt.core.dom.ASTNode;
import parser.CustomProjectClassParser;
import utils.Globals;
import utils.ResultsLogger;

/**
 *
 * @author Fede
 */
public class ProcessSpark implements Serializable{

    public static Map<String, ClassInfo> process(JEditorPane peditor, String projectUrl) {
        List<ClassInfo> loadClasses = CustomProjectClassParser.loadClasses(projectUrl);

        //Spark
        JavaRDD<ClassInfo> distList = Globals.getSparkContext().parallelize(loadClasses);

        ResultsLogger.doLog("DETECTING CLONES IN PROJECT PARALLELISM... " + ResultsLogger.LINE_BREAK);
        // for (ClassInfo classInfo : loadClasses) {
        distList.foreach((ClassInfo classInfo) -> {
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
                            System.out.println(method.getPath() + "---" + methodInner.getPath() + "---" + codeCloneType);

                            if (Globals.getProcessResults().containsKey(classInfo.getFileUrl())) {
                                //Get all the declared methods for the class
                                List<MethodInfo> classMethodList = Globals.getProcessResults().get(classInfo.getFileUrl()).getMethodList();
                                int cont = 0;
                                boolean found = false;
                                for (MethodInfo meth : classMethodList) {
                                    if (meth.equals(method)) {
                                        Globals.getProcessResults().get(classInfo.getFileUrl()).getMethodList().get(cont).getClones().add(cloneMethod);
                                        found = true;
                                        break;
                                    }
                                    cont++;
                                }
                                if (!found) {
                                    method.getClones().add(cloneMethod);
                                    Globals.getProcessResults().get(classInfo.getFileUrl()).getMethodList().add(method);
                                }
                                //Class not yet on the results
                            } else {
                                Globals.getProcessResults().put(classInfo.getFileUrl(), classInfo);
                                Globals.getProcessResults().get(classInfo.getFileUrl()).getMethodList().get(0).getClones().add(cloneMethod);
                            }
                        }//

                    }
                }
                //System.out.println(method.getContent()); 
                //System.out.println(method.getClones().listIterator());
//                ListIterator<CloneMethod> it = method.getClones().listIterator();
//                System.out.println("Class");
//                  for (CloneMethod m : method.getClones()) {
//                      System.out.println(m.getPath());
//                  }
                
            }//End For for method list
        });

        return Globals.getProcessResults();
    }

}
