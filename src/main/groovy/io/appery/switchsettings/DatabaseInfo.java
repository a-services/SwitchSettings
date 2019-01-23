package io.appery.switchsettings;

import java.util.List;

public class DatabaseInfo {

    private String name;
    private boolean secure;
    private boolean system;
    private List<FieldInfo> fields;

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
     * @return the secure
     */
    public boolean isSecure() {
        return secure;
    }

    /**
     * @param secure the secure to set
     */
    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    /**
     * @return the system
     */
    public boolean isSystem() {
        return system;
    }

    /**
     * @param system the system to set
     */
    public void setSystem(boolean system) {
        this.system = system;
    }

    /**
     * @return the fields
     */
    public List<FieldInfo> getFields() {
        return fields;
    }

    /**
     * @param fields the fields to set
     */
    public void setFields(List<FieldInfo> fields) {
        this.fields = fields;
    }
}