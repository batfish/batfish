package org.batfish.vendor.nec_ix.representation;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.VendorConversionException;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.vendor.VendorConfiguration;

/** Vendor-specific datamodel for an NEC IX router configuration. */
public final class NecIxConfiguration extends VendorConfiguration {

  private final @Nonnull Map<String, NecIxInterface> _interfaces;
  private final @Nonnull Set<String> _shutdownDevices;
  private final @Nonnull Map<String, NecIxAccessList> _accessLists;
  private final @Nonnull Map<String, NecIxPrefixList> _prefixLists;
  private final @Nonnull Map<String, NecIxRouteMap> _routeMaps;
  private final @Nonnull List<NecIxStaticRoute> _staticRoutes;
  private @Nullable NecIxBgpProcess _bgpProcess;
  private @Nullable String _hostname;
  private @Nullable String _rawHostname;

  public NecIxConfiguration() {
    _interfaces = new LinkedHashMap<>();
    _shutdownDevices = new HashSet<>();
    _accessLists = new LinkedHashMap<>();
    _prefixLists = new LinkedHashMap<>();
    _routeMaps = new LinkedHashMap<>();
    _staticRoutes = new ArrayList<>();
  }

  @Override
  public @Nullable String getHostname() {
    return _hostname;
  }

  @Override
  public void setHostname(String hostname) {
    checkNotNull(hostname, "hostname cannot be null");
    _hostname = hostname.toLowerCase();
    _rawHostname = hostname;
  }

  public @Nullable String getRawHostname() {
    return _rawHostname;
  }

  @Override
  public void setVendor(ConfigurationFormat format) {}

  /** Interfaces keyed by name, e.g. {@code GigaEthernet0.0}. */
  public @Nonnull Map<String, NecIxInterface> getInterfaces() {
    return _interfaces;
  }

  /** Devices ({@code GigaEthernet0}, {@code USB0}, ...) with {@code shutdown} in device mode. */
  public @Nonnull Set<String> getShutdownDevices() {
    return _shutdownDevices;
  }

  public @Nonnull Map<String, NecIxAccessList> getAccessLists() {
    return _accessLists;
  }

  /** Prefix-lists keyed by lower-case name: IX does not distinguish case in prefix-list names. */
  public @Nonnull Map<String, NecIxPrefixList> getPrefixLists() {
    return _prefixLists;
  }

  public @Nonnull Map<String, NecIxRouteMap> getRouteMaps() {
    return _routeMaps;
  }

  public @Nonnull List<NecIxStaticRoute> getStaticRoutes() {
    return _staticRoutes;
  }

  public @Nullable NecIxBgpProcess getBgpProcess() {
    return _bgpProcess;
  }

  public void setBgpProcess(@Nullable NecIxBgpProcess bgpProcess) {
    _bgpProcess = bgpProcess;
  }

  @Override
  public @Nonnull List<Configuration> toVendorIndependentConfigurations()
      throws VendorConversionException {
    Configuration c = new NecIxConversions(this).convert();
    markConcreteStructure(NecIxStructureType.ACCESS_LIST);
    markConcreteStructure(NecIxStructureType.PREFIX_LIST);
    markConcreteStructure(NecIxStructureType.ROUTE_MAP);
    return ImmutableList.of(c);
  }
}
