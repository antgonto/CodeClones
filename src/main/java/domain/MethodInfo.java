package domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.annotation.Transient;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author fsobrado
 */
@NodeEntity
public class MethodInfo implements Serializable {
    
    @Id @GeneratedValue
    private Long id;
    private String path = "";
    private String methodName = "";
    @Transient
    private String content = "";
    @Transient
    private int lineQty = 0;
    @Relationship(direction = Relationship.INCOMING)
    private List<CloneMethod> clones = new ArrayList<>();
    
    public MethodInfo(){
        
    }
    /**
     * @return the methodName
     */
    public String getMethodName() {
        return methodName;
    }

    /**
     * @param methodName the methodName to set
     */
    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    /**
     * @return the content
     */
    public String getContent() {
        return content;
    }

    /**
     * @param content the content to set
     */
    public void setContent(String content) {
        this.content = content;
    }

     /**
     * @return the lineQty
     */
    public int getLineQty() {
        return lineQty;
    }
    
    public void addLineQty(){
        lineQty++;
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
     * @return the clones
     */
    public List<CloneMethod> getClones() {
        return clones;
    }

    /**
     * @param clones the clones to set
     */
    public void setClones(List<CloneMethod> clones) {
        this.clones = clones;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + Objects.hashCode(this.getPath());
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
        final MethodInfo other = (MethodInfo) obj;

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
