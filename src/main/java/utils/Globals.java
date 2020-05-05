/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import domain.ClassInfo;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;

/**
 *
 * @author Fede
 */
public final class Globals implements Serializable{

    private static SparkConf sparkConf;
    private static JavaSparkContext sc;
    private static Map<String, ClassInfo> results = new LinkedHashMap<>();
    
    public static JavaSparkContext getSparkContext() {
        // configure spark
        if (sparkConf == null) {
            sparkConf = new SparkConf().setAppName("CodeClones").setMaster("local[*]").set("spark.executor.memory", "2g").
                    set("spark.driver.host", "127.0.0.1").set("spark.driver.bindAddress", "127.0.0.1");
            // start a spark context
            sc = new JavaSparkContext(sparkConf);
        }
        return sc;
    }
    
    public static Map<String, ClassInfo> getProcessResults(){
        return results;
    }
    
    public static Map<String, ClassInfo> resetProcessResults(){
        return results = new LinkedHashMap<>();
    }
   
}
