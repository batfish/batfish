package org.batfish.vendor.sros.representation;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.VendorConversionException;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.vendor.VendorConfiguration;

/**
 * Vendor-specific data model for a Nokia SR-OS (MD-CLI) configuration.
 *
 * <p>The configuration is first reduced (P3) to a canonical absolute-path statement tree that
 * unifies the brace/hierarchical form, the flat {@code /configure ...} form, and a mix of the two.
 * P4 extraction populates the typed feature maps below (hardware, routers/interfaces, BGP, policy)
 * from that tree. Conversion to the vendor-independent {@link Configuration} model is P5 work.
 */
public final class SrosConfiguration extends VendorConfiguration {

  public SrosConfiguration() {
    _statements = new ArrayList<>();
    _cards = new HashMap<>();
    _ports = new HashMap<>();
    _routers = new HashMap<>();
    _prefixLists = new HashMap<>();
    _policyStatements = new HashMap<>();
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

  /** Provisioned line cards, keyed by slot number. */
  public @Nonnull Map<Integer, Card> getCards() {
    return _cards;
  }

  /** Provisioned ports, keyed by port path string (e.g. {@code 1/1/c1}, {@code 1/1/c1/1}). */
  public @Nonnull Map<String, Port> getPorts() {
    return _ports;
  }

  /** Routing instances, keyed by router-name (e.g. {@code Base}). */
  public @Nonnull Map<String, Router> getRouters() {
    return _routers;
  }

  /** Routing-policy prefix-lists, keyed by name. */
  public @Nonnull Map<String, PrefixList> getPrefixLists() {
    return _prefixLists;
  }

  /** Routing-policy policy-statements, keyed by name. */
  public @Nonnull Map<String, PolicyStatement> getPolicyStatements() {
    return _policyStatements;
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
  private final @Nonnull Map<Integer, Card> _cards;
  private final @Nonnull Map<String, Port> _ports;
  private final @Nonnull Map<String, Router> _routers;
  private final @Nonnull Map<String, PrefixList> _prefixLists;
  private final @Nonnull Map<String, PolicyStatement> _policyStatements;
  private @Nullable String _hostname;

  @SuppressWarnings("unused")
  private @Nullable ConfigurationFormat _format;
}
