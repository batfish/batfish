package org.batfish.job;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;
import javax.annotation.Nonnull;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.vendor.VendorConfiguration;

/**
 * Exposes {@link ConvertConfigurationJob#finalizeConfiguration} to the engine.
 *
 * <p>Finalization is the conversion-time pass every parsed {@link Configuration} goes through:
 * reference verification, immutable maps, tenant VNI interfaces. A model that did not come from a
 * vendor parser must go through it too, or Batfish computes on objects that violate its invariants.
 * The pass only uses the vendor configuration inside an {@code assert}, so a stub carrying the
 * hostname is enough.
 */
public final class FinalizeShim {

  private FinalizeShim() {}

  /**
   * Finalizes {@code c} in place. Throws {@link org.batfish.common.BatfishException} or {@link
   * org.batfish.common.VendorConversionException} when the model is invalid, for example on an
   * undefined routing-policy reference.
   */
  public static void finalizeConfiguration(@Nonnull Configuration c, @Nonnull Warnings w) {
    checkNotNull(c);
    checkNotNull(w);
    ConvertConfigurationJob.finalizeConfiguration(c, new StubVendorConfiguration(c), w);
  }

  private static final class StubVendorConfiguration extends VendorConfiguration {
    private String _hostname;

    StubVendorConfiguration(Configuration c) {
      _hostname = c.getHostname();
      setFilename("engine/" + _hostname);
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
    public void setVendor(ConfigurationFormat format) {}

    @Override
    public List<Configuration> toVendorIndependentConfigurations() {
      throw new UnsupportedOperationException("engine stub");
    }
  }
}
