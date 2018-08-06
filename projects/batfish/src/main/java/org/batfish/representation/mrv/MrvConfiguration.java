package org.batfish.representation.mrv;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Set;
import org.batfish.common.VendorConversionException;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.LineAction;
import org.batfish.vendor.VendorConfiguration;

public class MrvConfiguration extends VendorConfiguration {

  /** */
  private static final long serialVersionUID = 1L;

  private transient Configuration _c;

  private String _hostname;

  private ConfigurationFormat _vendor;

  @Override public String getHostname() {
    return _hostname;
  }

  @Override public Set<String> getUnimplementedFeatures() {
    throw new UnsupportedOperationException("no implementation for generated method");
    // TODO Auto-generated method stub
  }

  public ConfigurationFormat getVendor() {
    return _vendor;
  }

  @Override public void setHostname(String hostname) {
    checkNotNull(hostname, "'hostname' cannot be null");
    _hostname = hostname.toLowerCase();
  }

  @Override public void setVendor(ConfigurationFormat format) {
    _vendor = format;
  }

  @Override public Configuration toVendorIndependentConfiguration()
      throws VendorConversionException {
    _c = new Configuration(_hostname, _vendor);
    _c.setDefaultCrossZoneAction(LineAction.ACCEPT);
    _c.setDefaultInboundAction(LineAction.ACCEPT);
    return _c;
  }
}
