package org.batfish.vendor.sros.representation;

import static com.google.common.base.MoreObjects.firstNonNull;

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
import org.batfish.datamodel.DeviceModel;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Vrf;
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
    Configuration c =
        new Configuration(_hostname, firstNonNull(_format, ConfigurationFormat.NOKIA_SROS));
    c.setDeviceModel(DeviceModel.NOKIA_SROS_UNSPECIFIED);
    c.setDefaultCrossZoneAction(LineAction.PERMIT);
    c.setDefaultInboundAction(LineAction.PERMIT);
    // SR-OS runs the BGP export pipeline from the main RIB (Junos-like): a route is advertised by
    // an export policy matching a main-RIB route, not by origination into the BGP RIB. (The
    // operational `show router bgp routes` table holds only routes learned from peers — the local
    // entries that the `info /state … bgp rib local-rib` tree additionally lists are a state-tree
    // display artifact, not BGP-originated routes; see findings.)
    c.setExportBgpFromBgpRib(false);

    // Routing policy is referenced by BGP, so convert it before BGP. Prefix-lists before the
    // policy-statements that reference them.
    SrosConversions.convertPrefixLists(this, c, getWarnings());
    SrosConversions.convertPolicyStatements(this, c);

    // Each SR-OS router instance is a VRF; the "Base" instance is the Batfish default VRF.
    for (Router router : _routers.values()) {
      Vrf vrf = vrfForRouter(router.getName(), c);
      SrosConversions.convertInterfaces(this, router, c, vrf);
      SrosConversions.convertBgp(router, c, vrf, getWarnings());
    }

    warnUnconvertedHardware();

    // Mark all SR-OS structure types concrete so the structure manager reports definitions,
    // undefined references, and unused structures (defined-but-never-referenced).
    SrosStructureType.CONCRETE_STRUCTURES.forEach(this::markConcreteStructure);

    return ImmutableList.of(c);
  }

  /**
   * Returns the {@link Vrf} for an SR-OS router instance, creating it on {@code c} if needed. The
   * {@code Base} instance maps to the Batfish default VRF ("default"); any other instance maps to a
   * VRF of the same name.
   */
  private static @Nonnull Vrf vrfForRouter(String routerName, Configuration c) {
    String vrfName = routerName.equals("Base") ? Configuration.DEFAULT_VRF_NAME : routerName;
    return c.getVrfs().computeIfAbsent(vrfName, Vrf::new);
  }

  /**
   * Hardware provisioning (cards/MDAs/ports) is modeled in the vendor representation but does not
   * map to the vendor-independent model — Batfish derives interfaces from the router instance, not
   * the physical port tree. Emit one warning so the data is not silently dropped.
   */
  private void warnUnconvertedHardware() {
    if (!_cards.isEmpty() || !_ports.isEmpty()) {
      getWarnings()
          .redFlagf(
              "SR-OS: hardware provisioning (%d card(s), %d port(s)) is parsed but not converted to"
                  + " the vendor-independent model",
              _cards.size(), _ports.size());
    }
  }

  private final @Nonnull List<String> _statements;
  private final @Nonnull Map<Integer, Card> _cards;
  private final @Nonnull Map<String, Port> _ports;
  private final @Nonnull Map<String, Router> _routers;
  private final @Nonnull Map<String, PrefixList> _prefixLists;
  private final @Nonnull Map<String, PolicyStatement> _policyStatements;
  private @Nullable String _hostname;
  private @Nullable ConfigurationFormat _format;
}
