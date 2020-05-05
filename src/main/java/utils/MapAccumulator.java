/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import domain.ClassInfo;
import domain.MethodInfo;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.spark.util.AccumulatorV2;

/**
 *
 * @author Fede
 */
  public class MapAccumulator extends AccumulatorV2<Map<ClassInfo, List<MethodInfo>>,Map<ClassInfo, List<MethodInfo>>> implements Serializable{
    Map<ClassInfo,List<MethodInfo>> results = new HashMap<>();
    
   
    @Override
    public boolean isZero() {
        return results.isEmpty();
    }


    @Override
    public void reset() {
        results.clear();
    }

    @Override
    public void add(Map<ClassInfo, List<MethodInfo>> map) {
        results = map;
    }
    @Override
    public Map<ClassInfo, List<MethodInfo>> value() {
        return results;
    }

  

    @Override
    public void merge(AccumulatorV2<Map<ClassInfo, List<MethodInfo>>, Map<ClassInfo, List<MethodInfo>>> other) {
 //       throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public AccumulatorV2<Map<ClassInfo, List<MethodInfo>>, Map<ClassInfo, List<MethodInfo>>> copy() {
       return this;
    }
}
