package org.batfish.representation.azure;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableList;
import org.batfish.common.BfConsts;
import org.batfish.common.VendorConversionException;
import org.batfish.common.topology.Layer1Edge;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.vendor.VendorConfiguration;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Set;

public class AzureConfiguration extends VendorConfiguration {

    // only one resource group to start testing
    // next, we will be able to define multiple resource group based on folder structure (Batfish.java)
    private static final ResourceGroup rgp = new ResourceGroup("test");

    private ConvertedConfiguration _convertedConfiguration = null;

    public AzureConfiguration() {
    }

    @Override
    public String getFilename() {
        return BfConsts.RELPATH_AZURE_CONFIGS_DIR;
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

    private void convertConfigurations(){
        _convertedConfiguration = new ConvertedConfiguration();
        rgp.toConfigurationNode(_convertedConfiguration);
    }

    @Override
    public List<Configuration> toVendorIndependentConfigurations() throws VendorConversionException {
        if (_convertedConfiguration == null) {
            convertConfigurations();
        }
        return ImmutableList.copyOf(_convertedConfiguration.getAllNodes());
    }

    public @Nonnull Set<Layer1Edge> getLayer1Edges() {
        return _convertedConfiguration.getLayer1Edges();
    }
}
