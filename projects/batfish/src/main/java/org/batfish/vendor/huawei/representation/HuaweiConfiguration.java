package org.batfish.vendor.huawei.representation;

import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Vrf;
import org.batfish.vendor.VendorConfiguration;

/** Huawei device configuration. */
public class HuaweiConfiguration extends VendorConfiguration {

  private String _hostname;
  private String _rawHostname;
  private ConfigurationFormat _vendor;
  private final Map<String, HuaweiInterface> _interfaces;
  private final Map<Integer, HuaweiVlan> _vlans;
  private final Map<String, HuaweiAcl> _acls;
  private final Map<String, HuaweiVrf> _vrfs;
  private final List<HuaweiStaticRoute> _staticRoutes;
  private HuaweiBgpProcess _bgpProcess;
  private HuaweiOspfProcess _ospfProcess;

  public HuaweiConfiguration() {
    _interfaces = new java.util.HashMap<>();
    _vlans = new java.util.HashMap<>();
    _acls = new java.util.HashMap<>();
    _vrfs = new java.util.HashMap<>();
    _staticRoutes = new ArrayList<>();
  }

  @Override
  public String getHostname() {
    return _hostname;
  }

  @Override
  public void setHostname(String hostname) {
    _hostname = hostname.toLowerCase();
    _rawHostname = hostname;
  }

  public String getRawHostname() {
    return _rawHostname;
  }

  @Override
  public void setVendor(ConfigurationFormat format) {
    _vendor = format;
  }

  public ConfigurationFormat getVendor() {
    return _vendor;
  }

  public Map<String, HuaweiInterface> getInterfaces() {
    return _interfaces;
  }

  public Map<Integer, HuaweiVlan> getVlans() {
    return _vlans;
  }

  public Map<String, HuaweiAcl> getAcls() {
    return _acls;
  }

  public Map<String, HuaweiVrf> getVrfs() {
    return _vrfs;
  }

  public List<HuaweiStaticRoute> getStaticRoutes() {
    return _staticRoutes;
  }

  public HuaweiBgpProcess getBgpProcess() {
    return _bgpProcess;
  }

  public void setBgpProcess(HuaweiBgpProcess bgpProcess) {
    _bgpProcess = bgpProcess;
  }

  public HuaweiOspfProcess getOspfProcess() {
    return _ospfProcess;
  }

  public void setOspfProcess(HuaweiOspfProcess ospfProcess) {
    _ospfProcess = ospfProcess;
  }

  @Override
  public @Nonnull List<Configuration> toVendorIndependentConfigurations() {
    String hostname = getHostname();
    Configuration c =
        Configuration.builder()
            .setHostname(hostname)
            .setConfigurationFormat(_vendor)
            .setDefaultCrossZoneAction(LineAction.PERMIT)
            .setDefaultInboundAction(LineAction.PERMIT)
            .build();

    // Set human name
    c.setHumanName(_rawHostname);

    // Create default VRF
    Vrf defaultVrf = new Vrf(DEFAULT_VRF_NAME);
    c.setVrfs(ImmutableMap.of(DEFAULT_VRF_NAME, defaultVrf));

    // Convert interfaces
    for (HuaweiInterface iface : _interfaces.values()) {
      HuaweiConversions.convertInterface(c, defaultVrf, iface, getWarnings());
    }

    // Convert BGP if present
    if (_bgpProcess != null) {
      org.batfish.datamodel.BgpProcess viBgpProcess =
          HuaweiConversions.convertBgpProcess(c, defaultVrf, _bgpProcess, _interfaces);
      defaultVrf.setBgpProcess(viBgpProcess);
    }

    // Convert static routes
    HuaweiConversions.convertStaticRoutes(defaultVrf, _staticRoutes);

    // TODO: Convert OSPF when needed (requires more complex setup)

    return ImmutableList.of(c);
  }
}
