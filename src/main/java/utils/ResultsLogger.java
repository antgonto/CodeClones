/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import domain.ClassInfo;
import domain.CloneMethod;
import domain.MethodInfo;
import enums.CloneTypeEnum;
import java.awt.Color;
import java.io.Serializable;
import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

/**
 *
 * @author Fede
 */
public class ResultsLogger implements Serializable{

    public static final String SINGLE_TAB = "	";
    public static final String DOUBLE_TAB = "		";
    public static final String TRIPLE_TAB = "			";
    public static final String LINE_BREAK = "\n";
    private static JEditorPane editor;
    
    public static void init(JEditorPane peditor){
         editor = peditor;
    }
    
    public static void paintResults(CloneTypeEnum codeCloneType, MethodInfo methodInfo, MethodInfo methodInner) {
        switch (codeCloneType.getType()) {
            case 0:
                //doLog(DOUBLE_TAB + "No Code Clone Found" + LINE_BREAK);
                break;
            case 1:
                doLog(DOUBLE_TAB + "Code Clone Type 1" + LINE_BREAK);
                doLog(TRIPLE_TAB + "On " + methodInfo.getPath() + " method :" + methodInner.getPath() + LINE_BREAK, false, true);
                break;
            case 2:
                doLog(DOUBLE_TAB + "Code Clone Type 2" + LINE_BREAK);
                doLog(TRIPLE_TAB + "On " + methodInfo.getPath() + " method :" + methodInner.getPath() + LINE_BREAK, false, true);
                break;
            case 3:
                doLog(DOUBLE_TAB + "Code Clone Type 3" + LINE_BREAK);
                doLog(TRIPLE_TAB + "On " + methodInfo.getPath() + " method :" + methodInner.getPath() + LINE_BREAK, false, true);
                break;
            default:
                break;
        }
    }

    public static void doLog(String value) {
        doLog(value, false, false);
    }

    public static void doLog(String value, boolean errorMsg) {
        doLog(value, errorMsg, false);
    }

    private static void doLog(String value, boolean errorMsg, boolean result) {
        try {

            SimpleAttributeSet keyWord = new SimpleAttributeSet();
            if (errorMsg) {
                StyleConstants.setForeground(keyWord, Color.RED);
                StyleConstants.setBold(keyWord, true);
            }

            if (result) {
                StyleConstants.setForeground(keyWord, Color.BLUE);
                StyleConstants.setBold(keyWord, true);
            }

            javax.swing.text.Document doc = editor.getDocument();
            doc.insertString(doc.getLength(), value, keyWord);
            editor.setCaretPosition(editor.getDocument().getLength());
            editor.repaint();
        } catch (BadLocationException exc) {
        }
    }
}
