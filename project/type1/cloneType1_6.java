/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package type1;

/**
 *
 * @author Jordy
 */
public class cloneType1_6 {
    
    public static void insercionDirecta(int A[]){
    int i, j;
    int value;
    for (i = 1; i < A.length; i++){ //Ccomment AAAA
              value = A[i]; 
              j = i - 1; //comment BBBB
              while ((j >= 0) && (value < A[j])){ 
                                                                    
                             A[j + 1] = A[j];       
                             j--;                   
              }
              A[j + 1] = value; 
              //Comment CCCC
    }
}
    
}
