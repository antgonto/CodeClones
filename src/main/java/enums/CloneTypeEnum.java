package enums;

/**
 *
 * @author fsobrado
 */
public enum CloneTypeEnum {
    /**
     * NO CLONE
     */
    NO_CLONE(0),
    /**
     * TYPE 1
     */
    TYPE_1(1),
    /**
     * TYPE 2
     */
    TYPE_2(2),
    /**
     * TYPE 3
     */
    TYPE_3(3);

    private int m_type;

    CloneTypeEnum(int p_type) {
        setType(p_type);
    }

    public int getType() {
        return m_type;
    }

    private void setType(int p_type) {
        this.m_type = p_type;
    }
}
