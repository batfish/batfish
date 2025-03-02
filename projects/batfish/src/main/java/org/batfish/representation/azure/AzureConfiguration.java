package org.batfish.representation.azure;

import com.fasterxml.jackson.databind.JsonNode;
import org.batfish.common.VendorConversionException;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.vendor.VendorConfiguration;

import java.util.ArrayList;
import java.util.List;

public class AzureConfiguration extends VendorConfiguration {

    // only one resource group to start testing
    // next, we will be able to define multiple resource group based on folder structure (Batfish.java)
    private static final ResourceGroup rgp = new ResourceGroup("test");

    public AzureConfiguration() {
    }

    @Override
    public String getHostname() {
        return "";
    }

    @Override
    public void setHostname(String hostname) {
        throw new IllegalStateException("Setting the hostname is not allowed for AWS configs");
    }

    @Override
    public void setVendor(ConfigurationFormat format) {
        throw new IllegalStateException("Setting the format is not allowed for AWS configs");
    }

    /** Adds a config subtree */
    public void addConfigElement(JsonNode node){
        rgp.addConfigElement(node);
    }

    @Override
    public List<Configuration> toVendorIndependentConfigurations() throws VendorConversionException {
        rgp.toConfigurationNode();

        // return configuration of each elements inside each resource group
        return new ArrayList<>();
    }
}
