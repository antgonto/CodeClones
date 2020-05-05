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
public class cloneType2_4 {
    
    static void bubbleSorting(int[] a) {  
        int b = a.length;  
        int value = 0;  
         for(int i=0; i < b; i++){  
                 for(int j=1; j < (b-i); j++){  
                          if(a[j-1] > a[j]){  
                                 //swap elements  
                                 value = a[j-1];  
                                 a[j-1] = a[j];  
                                 a[j] = value;  
                         }  
                          
                 }  
         }  
    }
    
         
         
}
