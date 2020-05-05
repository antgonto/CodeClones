/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package type3;

/**
 *
 * @author Jordy
 */
public class cloneType3_9 {
    
    public static void insercionDirecta(int A[]) {
        int i, j;
        int result;
        int totals = 0;
        for (i = 1; i < A.length; i++) {
            result = A[i];
            j = i - 1;
            while ((j >= 0) && (result < A[j])) {

                A[j + 1] = A[j];
                j--;
                totals ++;
            }// comment AAAA
            A[j + 1] = result + totals;
        }
    }
    
}
