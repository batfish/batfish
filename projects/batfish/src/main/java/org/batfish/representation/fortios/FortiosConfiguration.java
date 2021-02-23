package org.batfish.representation.fortios;

import com.google.common.collect.ImmutableList;
import java.util.List;
import javax.annotation.Nonnull;
import org.batfish.common.VendorConversionException;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.LineAction;
import org.batfish.vendor.VendorConfiguration;

public class FortiosConfiguration extends VendorConfiguration {

  @Override
  public String getHostname() {
    return _hostname;
  }

  @Override
  public void setHostname(String hostname) {
    _hostname = hostname;
  }

  @Override
  public void setVendor(ConfigurationFormat format) {}

  @Override
  public List<Configuration> toVendorIndependentConfigurations() throws VendorConversionException {
    return ImmutableList.of(toVendorIndependentConfiguration());
  }

  private String _hostname;

  private @Nonnull Configuration toVendorIndependentConfiguration() {
    Configuration c = new Configuration(_hostname, ConfigurationFormat.FORTIOS);
    // TODO: verify
    c.setDefaultCrossZoneAction(LineAction.DENY);
    // TODO: verify
    c.setDefaultInboundAction(LineAction.DENY);
    return c;
  }
}
