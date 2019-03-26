package org.batfish.representation.cumulus;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.VendorConversionException;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.LineAction;
import org.batfish.vendor.VendorConfiguration;
import org.parboiled.common.ImmutableList;

/** A {@link VendorConfiguration} for the Cumulus NCLU configuration language. */
public class CumulusNcluConfiguration extends VendorConfiguration {

  private static final long serialVersionUID = 1L;

  private @Nullable String _hostname;

  @Override
  public @Nullable String getHostname() {
    return _hostname;
  }

  @Override
  public void setHostname(@Nullable String hostname) {
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
