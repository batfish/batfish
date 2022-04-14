package org.batfish.representation.mrv;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.batfish.datamodel.topology.LegacyInterfaceTopologyUtils.legacyPopulateInterfaceTopologies;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.batfish.common.VendorConversionException;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.LineAction;
import org.batfish.vendor.VendorConfiguration;

public class MrvConfiguration extends VendorConfiguration {

  private transient Configuration _c;

  private String _hostname;

  private ConfigurationFormat _vendor;

  @Override
  public String getHostname() {
    return _hostname;
  }

  public ConfigurationFormat getVendor() {
    return _vendor;
  }

  @Override
  public void setHostname(String hostname) {
    checkNotNull(hostname, "'hostname' cannot be null");
    _hostname = hostname.toLowerCase();
  }

  @Override
  public void setVendor(ConfigurationFormat format) {
    _vendor = format;
  }

  @Override
  public List<Configuration> toVendorIndependentConfigurations() throws VendorConversionException {
    _c = new Configuration(_hostname, _vendor);
    _c.setDefaultCrossZoneAction(LineAction.PERMIT);
    _c.setDefaultInboundAction(LineAction.PERMIT);

    // TODO: instead, populate directly in conversion
    legacyPopulateInterfaceTopologies(_c);

    return ImmutableList.of(_c);
  }
}
