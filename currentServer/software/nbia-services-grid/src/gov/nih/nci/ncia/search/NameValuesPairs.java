package gov.nih.nci.ncia.search;

import java.io.Serializable;

/**
 * <P>This object should be considered immutable.... even though
 * it doesn't enforce that.
 *
 * <P><b>WARNING!</b> This object is serialized so if you change it, you risk
 * breaking remote search and the grid interface
 */
public class NameValuesPairs  implements Serializable {
    public NameValuesPairs() {
    }


    /**
     * Gets the name value for this Model.
     *
     * @return name
     */
    public String getName() {
        return name;
    }


    /**
     * Sets the name value for this Model.
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the versions value for this Model.
     *
     * @return versions
     */
    public String[] getValues() {
        return values;
    }


    /**
     * Sets the versions value for this Model.
     *
     * @param versions
     */
    public void setValues(String[] values) {
        this.values = values;
    }


    /**
     * This is necessary for the web services serializer to recognize
     * this property is an indexed property.
     */
    public String getValues(int i) {
        return this.values[i];
    }


    /**
     * This is necessary for the web services serializer to recognize
     * this property is an indexed property.
     */
    public void setValues(int i, String _value) {
        this.values[i] = _value;
    }

    ////////////////////////////////////////PRIVATE////////////////////////////////////
    private String name;
    private String[] values;
}
