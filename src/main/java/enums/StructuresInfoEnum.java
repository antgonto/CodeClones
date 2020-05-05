package enums;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Fede
 */
public enum StructuresInfoEnum {
    /**
     * METHOD DETECTION
     */
    OPEN_STRUC("{"),
    CLOSE_STRUC("}"),
    COMMNET("");

    private String m_type;

    StructuresInfoEnum(String p_type) {
        setType(p_type);
    }

    public String getType() {
        return m_type;
    }

    private void setType(String p_type) {
        this.m_type = p_type;
    }
}
