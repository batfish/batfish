package org.batfish.representation.cumulus;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.VendorConversionException;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.LineAction;
import org.batfish.vendor.VendorConfiguration;
import org.parboiled.common.ImmutableList;

@ParametersAreNonnullByDefault
public class CumulusNcluConfiguration extends VendorConfiguration {

  private static final long serialVersionUID = 1L;

  private String _hostname;

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

  private @Nonnull Configuration toVendorIndependentConfiguration() {
    Configuration c = new Configuration(getHostname(), ConfigurationFormat.CUMULUS_NCLU);
    c.setDefaultCrossZoneAction(LineAction.PERMIT);
    c.setDefaultInboundAction(LineAction.PERMIT);
    return c;
  }

  @Override
  public @Nonnull List<Configuration> toVendorIndependentConfigurations()
      throws VendorConversionException {
    return ImmutableList.of(toVendorIndependentConfiguration());
  }
}
