package org.batfish.vendor.cool_nos;

import static org.batfish.vendor.cool_nos.CoolNosConversions.convertStaticRoutes;

import com.google.common.collect.ImmutableList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import org.batfish.common.VendorConversionException;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Prefix;
import org.batfish.vendor.VendorConfiguration;

/** Vendor-specific data model for example Cool NOS configuration. */
public final class CoolNosConfiguration extends VendorConfiguration {

  public CoolNosConfiguration() {
    _staticRoutes = new HashMap<>();
  }

  private @Nonnull Configuration toVendorIndependentConfiguration() {
    Configuration c =
        Configuration.builder()
            .setHostname(_hostname)
            .setDefaultCrossZoneAction(LineAction.PERMIT)
            .setDefaultInboundAction(LineAction.PERMIT)
            .build();

    convertStaticRoutes(this, c);

    return c;
  }

  @Override
  public String getHostname() {
    return _hostname;
  }

  @Override
  public void setHostname(String hostname) {
    _hostname = hostname;
  }

  public @Nonnull Map<Prefix, StaticRoute> getStaticRoutes() {
    return _staticRoutes;
  }

  @Override
  public void setVendor(ConfigurationFormat format) {}

  @Override
  public List<Configuration> toVendorIndependentConfigurations() throws VendorConversionException {
    return ImmutableList.of(toVendorIndependentConfiguration());
  }

  // Note: For simplicity, in Cool NOS, you can only have one static route per prefix.
  private @Nonnull Map<Prefix, StaticRoute> _staticRoutes;
  private String _hostname;
}
