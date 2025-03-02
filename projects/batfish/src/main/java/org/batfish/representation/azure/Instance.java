package org.batfish.representation.azure;

import org.batfish.datamodel.Configuration;

import java.io.Serializable;


public abstract class Instance extends Resource implements Serializable {

    public Instance(String name, String id, String type) {
        super(name, id, type);
    }
    public abstract Configuration toConfigurationNode(Region rgp, ConvertedConfiguration convertedConfiguration);
}
