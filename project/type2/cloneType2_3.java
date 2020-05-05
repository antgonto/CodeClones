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
public class cloneType2_3 {
    
    
    static void bubbleSort(int[] c) {  
        int d = c.length;  
        int e = 0;  
         for(int i=0; i < d; i++){  
                 for(int j=1; j < (d-i); j++){  
                          if(c[j-1] > c[j]){  
                                 //swap elements  
                                 e = c[j-1];  
                                 c[j-1] = c[j];  
                                 c[j] = e;  
                         }  
                          
                 }  
         }  
    }
    
              
}
