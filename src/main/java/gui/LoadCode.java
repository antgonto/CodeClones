/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import analyze.CompareAST;
import analyze.GenerateAST;
import enums.CloneTypeEnum;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileNameExtensionFilter;
import jp.naist.se.simplecc.CloneDetectionMain;
import org.eclipse.jdt.core.dom.ASTNode;

/**
 *
 * @author Ronny
 */
public class LoadCode extends javax.swing.JFrame {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private static int MIN_TOKENS = 20;

    String CodeAString;
    String CodeBString;

    String CodeADir;
    String CodeBDir;

    long CodeAlines;
    long CodeBlines;

    /**
     * Creates new form LoadCode
     */
    public LoadCode() {
        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">                          
    private void initComponents() {

        jButton_CodeA = new javax.swing.JButton();
        jButton_CodeB = new javax.swing.JButton();
        jScrollPane_CodeA = new javax.swing.JScrollPane();
        jTextArea_CodeA = new javax.swing.JTextArea();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea_CodeB = new javax.swing.JTextArea();
        jButton_Analyze = new javax.swing.JButton();

        jButton_Analyze.setText("Analyze");
        jButton_Analyze.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                jButton_AnalyzeActionPerformed(evt);
            }
        });

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jButton_CodeA.setText("Upload Code A");
        jButton_CodeA.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_CodeAActionPerformed(evt);
            }
        });

        jButton_CodeB.setText("Upload Code B");
        jButton_CodeB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_CodeBActionPerformed(evt);
            }
        });

        jTextArea_CodeA.setColumns(20);
        jTextArea_CodeA.setRows(5);
        jScrollPane_CodeA.setViewportView(jTextArea_CodeA);

        jTextArea_CodeB.setColumns(20);
        jTextArea_CodeB.setRows(5);
        jScrollPane1.setViewportView(jTextArea_CodeB);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addGap(25, 25, 25)
                                .addComponent(jScrollPane_CodeA, javax.swing.GroupLayout.PREFERRED_SIZE, 650, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(53, 53, 53)
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 650, Short.MAX_VALUE)
                                .addGap(33, 33, 33))
                        .addGroup(layout.createSequentialGroup()
                                .addGap(72, 72, 72)
                                .addComponent(jButton_CodeA, javax.swing.GroupLayout.PREFERRED_SIZE, 500, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jButton_CodeB, javax.swing.GroupLayout.PREFERRED_SIZE, 500, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(72, 72, 72))
                        .addGroup(layout.createSequentialGroup()
                                .addGap(107, 107, 107)
                                .addComponent(jButton_Analyze, javax.swing.GroupLayout.PREFERRED_SIZE, 1140, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGap(40, 40, 40)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jButton_CodeA)
                                        .addComponent(jButton_CodeB))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(jScrollPane_CodeA, javax.swing.GroupLayout.DEFAULT_SIZE, 530, Short.MAX_VALUE)
                                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 530, Short.MAX_VALUE))
                                .addGap(18, 18, 18)
                                .addComponent(jButton_Analyze, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(26, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>                        

    private void jButton_CodeAActionPerformed(java.awt.event.ActionEvent evt) {
        JFileChooser selector = new JFileChooser();
        selector.setDialogTitle("Select a java code");
        FileNameExtensionFilter CodeA = new FileNameExtensionFilter("JAVA", "java");
        selector.setFileFilter(CodeA);
        int flag = selector.showOpenDialog(null);
        if (flag == JFileChooser.APPROVE_OPTION) {
            try {
                File FileManager = selector.getSelectedFile();

                CodeAString = getFileContent(FileManager);
                //jTextArea_CodeA.append(CodeAString);
                jTextArea_CodeA.setText(CodeAString);
                Font font = new Font("Arial", Font.BOLD, 15);
                jTextArea_CodeA.setFont(font);
                jTextArea_CodeA.setForeground(Color.BLACK);

                CodeADir = FileManager.getAbsolutePath();
                FileReader fr = new FileReader(CodeADir);
                BufferedReader bf = new BufferedReader(fr);
                CodeAlines = bf.lines().count();
                bf.close();

            } catch (IOException e) {
                System.out.println(e);
            }
        }
    }

    private void jButton_AnalyzeActionPerformed(java.awt.event.ActionEvent evt) {
        if (CodeAString == null && CodeBString == null) {
            JOptionPane.showMessageDialog(null, "Source code hasn't been uploaded", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (CodeAString == null) {
            JOptionPane.showMessageDialog(null, "Source code 1 hasn't been uploaded", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (CodeBString == null) {
            JOptionPane.showMessageDialog(null, "Source code 2 hasn't been uploaded", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        long TInicio, TFin, tiempo; //Variables para determinar el tiempo de ejecuci�n
        TInicio = System.currentTimeMillis();

        //Con tokens
        String[] clones = {CodeADir, CodeBDir};
        CloneDetectionMain.main(clones);
        TFin = System.currentTimeMillis(); //Tomamos la hora en que finaliz� el algoritmo y la almacenamos en la variable T
        tiempo = TFin - TInicio; //Calculamos los milisegundos de diferencia
        System.out.println("Tiempo de ejecuci�n SimpleCC (tokens): " + tiempo); //Mostramos en pantalla el tiempo de ejecuci�n en milisegundos
        //
        
        //Con Arboles AST
        long TInicio1, TFin1, tiempo1; //Variables para determinar el tiempo de ejecuci�n
        TInicio1 = System.currentTimeMillis();
        ASTNode AST_A = GenerateAST.parse(CodeAString);
        ASTNode AST_B = GenerateAST.parse(CodeBString);
        CloneTypeEnum codeCloneType = CompareAST.codeCloneTypeDetector(AST_A, AST_B, CodeAlines, CodeBlines);
        TFin1 = System.currentTimeMillis(); //Tomamos la hora en que finaliz� el algoritmo y la almacenamos en la variable TFin
        //
        switch (codeCloneType.getType()) {
            case 0:
                JOptionPane.showMessageDialog(null, "Uploaded source codes do NOT correspond to Type-1, Type-2 or Type-3", "Result", JOptionPane.WARNING_MESSAGE);
                break;
            case 1:
                JOptionPane.showMessageDialog(null, "Code Clone Type 1", "Result", JOptionPane.INFORMATION_MESSAGE);
                break;
            case 2:
                JOptionPane.showMessageDialog(null, "Code Clone Type 2", "Result", JOptionPane.INFORMATION_MESSAGE);
                break;
            case 3:
                JOptionPane.showMessageDialog(null, "Code Clone Type 3", "Result", JOptionPane.INFORMATION_MESSAGE);
                break;
            default:
                break;
        }
        tiempo1 = TFin1 - TInicio1; //Calculamos los milisegundos de diferencia
        System.out.println("Tiempo de ejecuci�n AST: " + tiempo1); //Mostramos en pantalla el tiempo de ejecuci�n en milisegundos
        jTextArea_CodeA.setText("");
        jTextArea_CodeB.setText("");

    }

    private void jButton_CodeBActionPerformed(java.awt.event.ActionEvent evt) {

        JFileChooser selector = new JFileChooser();
        selector.setDialogTitle("Select a java code");
        FileNameExtensionFilter CodeA = new FileNameExtensionFilter("JAVA", "java");
        selector.setFileFilter(CodeA);
        int flag = selector.showOpenDialog(null);
        if (flag == JFileChooser.APPROVE_OPTION) {
            try {
                File FileManager = selector.getSelectedFile();
                CodeBString = getFileContent(FileManager);
                //jTextArea_CodeB.append(CodeBString);

                jTextArea_CodeB.setText(CodeBString);
                Font font = new Font("Arial", Font.BOLD, 15);
                jTextArea_CodeB.setFont(font);
                jTextArea_CodeB.setForeground(Color.BLACK);

                CodeBDir = FileManager.getAbsolutePath();
                FileReader fr = new FileReader(CodeBDir);
                BufferedReader bf = new BufferedReader(fr);
                CodeBlines = bf.lines().count();
                bf.close();

            } catch (IOException e) {
                System.out.println(e);
            }
        }

    }

    public static String getFileContent(File fileManager) throws FileNotFoundException, IOException {
        BufferedReader br = new BufferedReader(new FileReader(fileManager));
        StringBuilder sb = new StringBuilder();
        String line = br.readLine();
        while (line != null) {
            sb.append(line);
            sb.append(System.lineSeparator());
            line = br.readLine();
        }
        return sb.toString();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(LoadCode.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(LoadCode.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(LoadCode.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(LoadCode.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
                    Logger.getLogger(LoadCode.class.getName()).log(Level.SEVERE, null, ex);
                }
                new LoadCode().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify                     
    private javax.swing.JButton jButton_Analyze;
    private javax.swing.JButton jButton_CodeA;
    private javax.swing.JButton jButton_CodeB;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane_CodeA;
    private javax.swing.JTextArea jTextArea_CodeA;
    private javax.swing.JTextArea jTextArea_CodeB;
    // End of variables declaration                   
}
