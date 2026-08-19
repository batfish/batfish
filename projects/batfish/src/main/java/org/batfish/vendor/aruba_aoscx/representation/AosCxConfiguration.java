package org.batfish.vendor.aruba_aoscx.representation;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.batfish.common.VendorConversionException;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.LineAction;
import org.batfish.vendor.VendorConfiguration;

/** Vendor-specific configuration for Aruba AOS-CX. */
public class AosCxConfiguration extends VendorConfiguration {

  private transient Configuration _c;
  private String _hostname;
  private String _rawHostname;
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
    _rawHostname = hostname;
  }

  @Override
  public void setVendor(ConfigurationFormat format) {
    _vendor = format;
  }

  @Override
  public List<Configuration> toVendorIndependentConfigurations() throws VendorConversionException {
    _c = new Configuration(_hostname, _vendor);
    _c.setHumanName(_rawHostname);
    _c.setDefaultCrossZoneAction(LineAction.PERMIT);
    _c.setDefaultInboundAction(LineAction.PERMIT);
    return ImmutableList.of(_c);
  }
}
