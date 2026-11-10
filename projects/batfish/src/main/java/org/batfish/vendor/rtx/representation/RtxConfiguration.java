package org.batfish.vendor.rtx.representation;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.LinkedHashMap;
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

/** Vendor-specific datamodel for a Yamaha RTX router configuration. */
public final class RtxConfiguration extends VendorConfiguration {

  private final @Nonnull Map<String, RtxInterface> _interfaces;
  private final @Nonnull Map<Long, RtxFilter> _filters;
  private final @Nonnull List<RtxStaticRoute> _staticRoutes;
  private final @Nonnull Map<Integer, RtxNatDescriptor> _natDescriptors;
  private @Nullable String _hostname;
  private @Nullable String _rawHostname;
  private @Nullable RtxBgpProcess _bgpProcess;

  public RtxConfiguration() {
    _interfaces = new LinkedHashMap<>();
    _filters = new LinkedHashMap<>();
    _staticRoutes = new ArrayList<>();
    _natDescriptors = new LinkedHashMap<>();
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

  @Override
  public void setVendor(ConfigurationFormat format) {}

  /** Interfaces, keyed by RTX interface name ({@code lan1}, {@code lan3/1}, {@code tunnel1}). */
  public @Nonnull Map<String, RtxInterface> getInterfaces() {
    return _interfaces;
  }

  /** {@code ip filter} definitions, keyed by filter number. */
  public @Nonnull Map<Long, RtxFilter> getFilters() {
    return _filters;
  }

  /** Static routes, one per {@code gateway} clause of an {@code ip route} command. */
  public @Nonnull List<RtxStaticRoute> getStaticRoutes() {
    return _staticRoutes;
  }

  /** {@code nat descriptor} definitions, keyed by descriptor number. */
  public @Nonnull Map<Integer, RtxNatDescriptor> getNatDescriptors() {
    return _natDescriptors;
  }

  /** Returns the descriptor numbered {@code number}, creating it if this is its first mention. */
  public @Nonnull RtxNatDescriptor getOrCreateNatDescriptor(int number) {
    return _natDescriptors.computeIfAbsent(number, RtxNatDescriptor::new);
  }

  public @Nullable RtxBgpProcess getBgpProcess() {
    return _bgpProcess;
  }

  /** Returns the BGP process, creating it if this is the first {@code bgp} command seen. */
  public @Nonnull RtxBgpProcess getOrCreateBgpProcess() {
    if (_bgpProcess == null) {
      _bgpProcess = new RtxBgpProcess();
    }
    return _bgpProcess;
  }

  @Override
  public @Nonnull List<Configuration> toVendorIndependentConfigurations()
      throws VendorConversionException {
    return ImmutableList.of(toVendorIndependentConfiguration());
  }

  private @Nonnull Configuration toVendorIndependentConfiguration() {
    Configuration c = new Configuration(_hostname, ConfigurationFormat.YAMAHA_RTX);
    c.setHumanName(_rawHostname);
    c.setDeviceModel(DeviceModel.YAMAHA_RTX_UNSPECIFIED);
    c.setDefaultCrossZoneAction(LineAction.PERMIT);
    c.setDefaultInboundAction(LineAction.PERMIT);

    // RTX has no VRFs.
    c.setVrfs(ImmutableMap.of(DEFAULT_VRF_NAME, new Vrf(DEFAULT_VRF_NAME)));

    // Filters must exist before interfaces, which reference them.
    RtxConversions.convertFilters(this, c);
    RtxConversions.convertInterfaces(this, c);
    RtxConversions.convertStaticRoutes(this, c);
    RtxConversions.convertNat(this, c);
    RtxConversions.convertBgp(this, c);

    markConcreteStructure(RtxStructureType.BGP_EXPORT_FILTER);
    markConcreteStructure(RtxStructureType.BGP_IMPORT_FILTER);
    markConcreteStructure(RtxStructureType.FILTER);
    markConcreteStructure(RtxStructureType.NAT_DESCRIPTOR);

    return c;
  }
}
