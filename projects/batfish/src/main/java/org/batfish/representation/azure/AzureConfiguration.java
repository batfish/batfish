package org.batfish.representation.azure;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableList;
import org.batfish.common.BfConsts;
import org.batfish.common.VendorConversionException;
import org.batfish.common.topology.Layer1Edge;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Ip;
import org.batfish.vendor.VendorConfiguration;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AzureConfiguration extends VendorConfiguration {

    // only one resource group to start testing
    // next, we will be able to define multiple resource group based on folder structure (Batfish.java)
    private final Map<String, Region> _regions = new HashMap<>();
    public static final Ip LINK_LOCAL_IP = Ip.parse("169.254.0.1");

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

    private Region addOrGetRegion(String regionName) {
        Region region = _regions.get(regionName);
        if(region == null){
            region = new Region(regionName);
            _regions.put(regionName, region);
        }
        return region;
    }

    /** Adds a config subtree */
    public void addConfigElement(JsonNode node){
        JsonNode regionField = node.get(AzureEntities.JSON_KEY_LOCATION);
        if(regionField == null){
            return; // unable to parse
        }

        String regionName = regionField.asText();
        addOrGetRegion(regionName).addConfigElement(node);
    }

    private void convertConfigurations(){
        _convertedConfiguration = new ConvertedConfiguration();

        for(Region region : _regions.values()){
            region.toConfigurationNode(_convertedConfiguration);
        }
        //rgp.toConfigurationNode(_convertedConfiguration);
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
