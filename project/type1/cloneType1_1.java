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
public class cloneType1_1 {
    static void bubbleSort(int[] arr) {  
        int n = arr.length;  
        int temp = 0;  
        //comment BBBB
         for(int i=0; i < n; i++){  
                 for(int j=1; j < (n-i); j++){  
                          if(arr[j-1] > arr[j]){  
                                 //comment AAAA
                                 temp = arr[j-1];  
                                 arr[j-1] = arr[j];  
                                 arr[j] = temp; //comment CCCC 
                         }  
                          
                 }  
         }  
    }
    
}
