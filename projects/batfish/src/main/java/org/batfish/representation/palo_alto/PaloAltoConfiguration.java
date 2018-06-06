package org.batfish.representation.palo_alto;

import java.util.Set;
import org.batfish.common.VendorConversionException;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.LineAction;
import org.batfish.vendor.VendorConfiguration;

public class PaloAltoConfiguration extends VendorConfiguration {

  private static final long serialVersionUID = 1L;

  private Configuration _c;

  private String _hostname;

  private transient Set<String> _unimplementedFeatures;

  private ConfigurationFormat _vendor;

  public PaloAltoConfiguration(Set<String> unimplementedFeatures) {
    _unimplementedFeatures = unimplementedFeatures;
  }

  @Override
  public String getHostname() {
    return _hostname;
  }

  @Override
  public void setHostname(String hostname) {
    _hostname = hostname;
  }

  @Override
  public Set<String> getUnimplementedFeatures() {
    return _unimplementedFeatures;
  }

  @Override
  public void setVendor(ConfigurationFormat format) {
    _vendor = format;
  }

  @Override
  public Configuration toVendorIndependentConfiguration() throws VendorConversionException {
    String hostname = getHostname();
    _c = new Configuration(hostname, _vendor);
    _c.setDefaultCrossZoneAction(LineAction.REJECT);
    _c.setDefaultInboundAction(LineAction.ACCEPT);
    return _c;
  }
}
