/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package domain;

import enums.CloneTypeEnum;
import java.io.Serializable;
import java.util.Objects;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;

/**
 *
 * @author Fede
 */
@NodeEntity
public class CloneMethod implements Serializable{

    @Id @GeneratedValue
    private Long id;
    private String path;
    private String name;
    private CloneTypeEnum cloneType = CloneTypeEnum.NO_CLONE;
    
    public CloneMethod(){
        
    }
    public CloneMethod(String name, String path, CloneTypeEnum cloneType) {
        setName(name);
        setPath(path);
        setCloneType(cloneType);
    }

   
    /**
     * @return the path
     */
    public String getPath() {
        return path;
    }

    /**
     * @param path the path to set
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the cloneType
     */
    public CloneTypeEnum getCloneType() {
        return cloneType;
    }

    /**
     * @param cloneType the cloneType to set
     */
    public void setCloneType(CloneTypeEnum cloneType) {
        this.cloneType = cloneType;
    }
    
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 29 * hash + Objects.hashCode(this.getPath());
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CloneMethod other = (CloneMethod) obj;
        if (!Objects.equals(this.path, other.path)) {
            return false;
        }
        return true;
    }

    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(Long id) {
        this.id = id;
    }
}
