package org.batfish.vendor.sros.representation;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.VendorConversionException;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.vendor.VendorConfiguration;

/**
 * Vendor-specific data model for a Nokia SR-OS (MD-CLI) configuration.
 *
 * <p>As of P3 (parsing) this holds the set of canonical absolute-path statements extracted from the
 * configuration, regardless of whether the input was the brace/hierarchical form, the flat {@code
 * /configure ...} form, or a mix of the two. Feature-specific extraction (interfaces, BGP, policy,
 * etc.) and conversion to the vendor-independent {@link Configuration} model are P4/P5 work.
 */
public final class SrosConfiguration extends VendorConfiguration {

  public SrosConfiguration() {
    _statements = new ArrayList<>();
  }

  /**
   * The canonical absolute-path statements in the configuration, in input order. Each entry is the
   * space-joined path from the implicit root to a configured leaf or empty block, e.g. {@code
   * "configure router \"Base\" bgp router-id 1.1.1.1"}. Brace, flat {@code /configure ...}, and
   * mixed inputs that describe the same configuration produce the same list.
   */
  public @Nonnull List<String> getStatements() {
    return _statements;
  }

  @Override
  public @Nullable String getHostname() {
    return _hostname;
  }

  @Override
  public void setHostname(String hostname) {
    _hostname = hostname;
  }

  @Override
  public void setVendor(ConfigurationFormat format) {
    _format = format;
  }

  @Override
  public List<Configuration> toVendorIndependentConfigurations() throws VendorConversionException {
    // Conversion to the vendor-independent model is P5 work; nothing is produced yet.
    return ImmutableList.of();
  }

  private final @Nonnull List<String> _statements;
  private @Nullable String _hostname;

  @SuppressWarnings("unused")
  private @Nullable ConfigurationFormat _format;
}
