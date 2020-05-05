/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package type2;

/**
 *
 * @author Jordy
 */
public class cloneType2_6 {
    
    public static void insercionDirecta(int A[]) {
        int a, b;
        int result;
        for (a = 1; a < A.length; a++) {
            result = A[a];
            b = a - 1;
            while ((b >= 0) && (result < A[b])) {

                A[b + 1] = A[b];
                b--;
            }// comment AAAA
            A[b + 1] = result;
        }
    }
    
}
