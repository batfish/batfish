package org.batfish.vendor.aruba_aoscx.representation;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.batfish.common.VendorConversionException;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Vrf;
import org.batfish.vendor.VendorConfiguration;

/** Vendor-specific configuration for Aruba AOS-CX. */
public class AosCxConfiguration extends VendorConfiguration {

  private transient Configuration _c;
  private String _hostname;
  private final Map<String, AosCxInterface> _interfaces = new HashMap<>();
  private String _rawHostname;
  private ConfigurationFormat _vendor;

  @Override
  public String getHostname() {
    return _hostname;
  }

  public ConfigurationFormat getVendor() {
    return _vendor;
  }

  public Map<String, AosCxInterface> getInterfaces() {
    return _interfaces;
  }

  public AosCxInterface getOrCreateInterface(String name) {
    return _interfaces.computeIfAbsent(name, AosCxInterface::new);
  }

  @Override
  public void setHostname(String hostname) {
    checkNotNull(hostname, "'hostname' cannot be null");
    _hostname = hostname.toLowerCase();
    _rawHostname = hostname;
  }

  @Override
  public void setVendor(ConfigurationFormat format) {
    _vendor = format;
  }

  private static InterfaceType getInterfaceType(AosCxInterface iface) {
    String name = iface.getName();
    if (name.startsWith("loopback ")) {
      return InterfaceType.LOOPBACK;
    }
    if (name.startsWith("vlan ")) {
      return InterfaceType.VLAN;
    }
    return InterfaceType.PHYSICAL;
  }

  private static boolean getInterfaceAdminUpEffective(AosCxInterface iface) {
    if (iface.getEnabled() != null) {
      return iface.getEnabled();
    }
    // Physical AOS-CX interfaces are disabled by default. Loopback and VLAN
    // interfaces commonly appear enabled without an explicit "no shutdown".
    return getInterfaceType(iface) != InterfaceType.PHYSICAL;
  }

  private void convertInterface(AosCxInterface iface, Vrf vrf) {
    String name = iface.getName();

    org.batfish.datamodel.Interface.Builder newIface =
        org.batfish.datamodel.Interface.builder()
            .setAdminUp(getInterfaceAdminUpEffective(iface))
            .setType(getInterfaceType(iface))
            .setName(name)
            .setVrf(vrf)
            .setOwner(_c);

    newIface.setHumanName(name);
    newIface.setDeclaredNames(ImmutableList.of(name));

    if (iface.getAddress() != null) {
      newIface.setAddress(iface.getAddress());
    }

    newIface.build();
  }

  @Override
  public List<Configuration> toVendorIndependentConfigurations() throws VendorConversionException {
    _c = new Configuration(_hostname, _vendor);
    _c.setHumanName(_rawHostname);
    _c.setDefaultCrossZoneAction(LineAction.PERMIT);
    _c.setDefaultInboundAction(LineAction.PERMIT);

    Vrf defaultVrf = new Vrf(DEFAULT_VRF_NAME);
    _c.setVrfs(ImmutableMap.of(DEFAULT_VRF_NAME, defaultVrf));

    _interfaces.values().forEach(iface -> convertInterface(iface, defaultVrf));

    return ImmutableList.of(_c);
  }
}
