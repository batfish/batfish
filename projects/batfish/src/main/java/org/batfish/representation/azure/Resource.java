package org.batfish.representation.azure;

import java.io.Serializable;

public class Resource implements Serializable {

    private final String _name;
    private final String _id;
    private final String _type;

    // Batfish doesn't accept '/' in hostnames
    private final String _cleanId;

    public static Resource create(
            String id,
            String type,
            String name) {
        return new Resource(id, type, name);
    }

    public Resource(String name, String id, String type) {
        _name = name;
        _id = id;
        _type = type;
        _cleanId = convertId(id);
    }

    private static String convertId(String id){
        return id.replace('/', '_');
    }

    public String getName() {
        return _name;
    }

    public String getId() {
        return _id;
    }

    public String getType() {
        return _type;
    }

    public String getCleanId() {
        return _cleanId;
    }


}
