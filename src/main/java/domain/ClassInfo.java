package domain;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

/**
 *
 * @author fsobrado
 */
@NodeEntity
public class ClassInfo implements Serializable {
    @Id @GeneratedValue
    private Long id;
    private String fileUrl;
    private String name;
    private String classPackage;
    @Relationship(type = "CONTIENE", direction = Relationship.OUTGOING)
    private List<MethodInfo> methodList;
    
    public ClassInfo(){
        
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
     * @return the classPackage
     */
    public String getClassPackage() {
        return classPackage;
    }

    /**
     * @param classPackage the classPackage to set
     */
    public void setClassPackage(String classPackage) {
        this.classPackage = classPackage;
    }

    /**
     * @return the fileUrl
     */
    public String getFileUrl() {
        return fileUrl;
    }

    /**
     * @param fileUrl the fileUrl to set
     */
    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    /**
     * @return the methodList
     */
    public List<MethodInfo> getMethodList() {
        return methodList;
    }

    /**
     * @param methodList the methodList to set
     */
    public void setMethodList(List<MethodInfo> methodList) {
        this.methodList = methodList;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + Objects.hashCode(this.fileUrl);
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
        final ClassInfo other = (ClassInfo) obj;
        if (!Objects.equals(this.fileUrl, other.fileUrl)) {
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
