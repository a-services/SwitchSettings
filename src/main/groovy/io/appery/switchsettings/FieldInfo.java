package io.appery.switchsettings;

public class FieldInfo {

    private String name;
    private boolean notNull;
    private String referencedCollection;
    private String type;
    private boolean system;
    private boolean unique;

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
     * @return the notNull
     */
    public boolean isNotNull() {
        return notNull;
    }

    /**
     * @param notNull the notNull to set
     */
    public void setNotNull(boolean notNull) {
        this.notNull = notNull;
    }

    /**
     * @return the referencedCollection
     */
    public String getReferencedCollection() {
        return referencedCollection;
    }

    /**
     * @param referencedCollection the referencedCollection to set
     */
    public void setReferencedCollection(String referencedCollection) {
        this.referencedCollection = referencedCollection;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
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
     * @return the unique
     */
    public boolean isUnique() {
        return unique;
    }

    /**
     * @param unique the unique to set
     */
    public void setUnique(boolean unique) {
        this.unique = unique;
    }

}