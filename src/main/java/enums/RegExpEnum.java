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
public enum RegExpEnum {
    /**
     * EXTRAC PACKAGE NAME
     */
    PACKAGE("package\\s+([a-zA_Z_][\\.\\w]*);"),
    /**
     * GET ALL THE IMPORTS LINES
     */
    IMPORTS("(?<=import (?:static )?+)[^;]+"),
    /**
     * CLASS NAME DETECTION
     */
    CLASS_NAME("(?:public|protected|private|static)\\s+(?:class|interface)\\s+\\w+\\s*\\{"),
    /**
     * METHOD DETECTION
     */
    REG_EXP_METHOD("(public|protected|private|static|\\s) +[\\w\\<\\>\\[\\],\\s]+\\s+(\\w+) *\\([^\\)]*\\) *(\\{?|[^;])"),
    /**
     * COMMENTS DETECTION
     */
    COMMENTS("//.*|(\"(?:\\\\[^\"]|\\\\\"|.)*?\")|(?s)/\\*.*?\\*/"),
    /**
     * EXTRACT THE METHOD NAME
     */
    EXTRACT_CLASS_NAME("(?<=\\n|\\A)(?:public\\s)?(class|interface|enum)\\s([^\\n\\s]*)"),
    /**
     * EXTRACT THE METHOD NAME
     */
    EXTRACT_METHOD_NAME("([a-zA-Z0-9_]+) *\\(");

    private String m_type;

    RegExpEnum(String p_type) {
        setType(p_type);
    }

    public String getType() {
        return m_type;
    }

    private void setType(String p_type) {
        this.m_type = p_type;
    }

}
