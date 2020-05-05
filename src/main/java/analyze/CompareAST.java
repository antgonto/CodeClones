package analyze;

import enums.CloneTypeEnum;
import java.util.Iterator;
import java.util.List;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;

public class CompareAST {

    static boolean gap = false;
    static boolean type3 = false;
    static long codeAlines;
    static long codeBlines;

    public static CloneTypeEnum codeCloneTypeDetector(ASTNode codeAST_A, ASTNode codeAST_B, long CodeAlines, long CodeBlines) {
        codeAlines = CodeAlines;
        codeBlines = CodeBlines;
        boolean isEquals = compareAST(codeAST_A, codeAST_B);
        if (isEquals && !gap && !type3) {
            return CloneTypeEnum.TYPE_1;
        } else if (isEquals && gap && !type3) {
            gap = false;
            return CloneTypeEnum.TYPE_2;
        } else if (isEquals && type3) {
            gap = false;
            type3 = false;
            return CloneTypeEnum.TYPE_3;
        } else {
            gap = false;
            type3 = false;
            return CloneTypeEnum.NO_CLONE;
        }
    }

    private static boolean compareAST(ASTNode codeAST_A, ASTNode codeAST_B) {
        // Si ambos arboles son nulos, son iguales, pero si solo uno es nulo, no lo son clones
        if (codeAST_A == null && codeAST_B == null) {
            return true;
        } else if (codeAST_A == null || codeAST_B == null) {
            return false;
        }

        //Si los tipos de nodos son los mismos, se asume que tendrán las mismas propiedades,
        // pues previamente el codigo es compilado
        if (codeAST_A.getNodeType() != codeAST_B.getNodeType()) {
            // Si no son del mismo tipo, son diferentes y son mas largo que uno,
            // entonces es una linea insertada con posiblidad de se clone tipo 3
            if (!codeAST_A.equals(codeAST_B) && codeAST_A.getLength() == 1 && codeAST_B.getLength() == 1) {
                return false;
            } else {
                type3 = true;
                return false;
            }

        }
        List<StructuralPropertyDescriptor> props = codeAST_A.structuralPropertiesForType();
        for (StructuralPropertyDescriptor property : props) {
            //Se obtiene las propiedades structurales de cada de ASTNode
            Object structuralPropertyA = codeAST_A.getStructuralProperty(property);
            Object structuralPropertyB = codeAST_B.getStructuralProperty(property);
            // Si es una propiedad simple entonces se analizan los operadores/operandos
            if (property.isSimpleProperty()) {
                // Comprueba las propiedades simples (tipos primitivos, cadenas, ...)
                // Si es diferente existe la posibilidad de ser clone tipo 2 o tipo 3
                if (!structuralPropertyA.equals(structuralPropertyB)) {
                    // Si los oper son alfanumericos, efectivamente puede ser clone tipo 2 o tipo 3
                    // Si no son alfanumericos no son clones
                    if (isAlphaNumeric(structuralPropertyA.toString()) && isAlphaNumeric(structuralPropertyB.toString())) {
                        gap = true;
                        return true; // Se retorna true para que siga analizando
                    } else {
                        return false;
                    }
                }
            } else if (property.isChildProperty()) {
                // Llamar recursivamente esta función en nodos secundarios
                if (!compareAST((ASTNode) structuralPropertyA, (ASTNode) structuralPropertyB)) {
                    return false;
                }
            } else if (property.isChildListProperty()) {
                Iterator<ASTNode> codeAST_AValIt = ((Iterable<ASTNode>) structuralPropertyA)
                        .iterator();
                Iterator<ASTNode> codeAST_BValIt = ((Iterable<ASTNode>) structuralPropertyB)
                        .iterator();
                while (codeAST_AValIt.hasNext() && codeAST_BValIt.hasNext()) {
                    ASTNode left = codeAST_AValIt.next();
                    ASTNode right = codeAST_BValIt.next();
                    // Llamar recursivamente esta función en nodos secundarios
                    if (!compareAST(left, right)) {
                        if (type3) {
                            if (codeAST_AValIt.hasNext() && codeAST_BValIt.hasNext()) {
                                ASTNode leftNext = codeAST_AValIt.next();
                                ASTNode rightNext = codeAST_BValIt.next();
                                if (codeAlines > codeBlines) {
                                    type3 = true;
                                    if (!compareAST(leftNext, right)) {
                                        return false;
                                    }
                                } else if (codeAlines < codeBlines) {
                                    type3 = true;
                                    if (!compareAST(left, rightNext)) {
                                        return false;
                                    }
                                } else {
                                    return false;
                                }
                            } else {
                                break;
                                //return false;
                            }
                        } else {
                            return false;
                        }
                    }
                }
                // Una de las listas de valores tiene elementos adicionales
                if (codeAST_AValIt.hasNext() || codeAST_BValIt.hasNext()) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean isAlphaNumeric(String s) {
        String pattern = "^[a-zA-Z_]*$";
        return s.matches(pattern);
    }

    //método para calcular el número de veces que se repite un carácter en un String
    public static int contarCaracteres(String cadena, char caracter) {
        int posicion, contador = 0;
        //se busca la primera vez que aparece
        posicion = cadena.indexOf(caracter);
        while (posicion != -1) { //mientras se encuentre el caracter
            contador++;           //se cuenta
            //se sigue buscando a partir de la posición siguiente a la encontrada
            posicion = cadena.indexOf(caracter, posicion + 1);
        }
        return contador;
    }

}
