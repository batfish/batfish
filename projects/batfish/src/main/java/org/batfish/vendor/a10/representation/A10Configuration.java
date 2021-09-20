package org.batfish.vendor.a10.representation;

import static com.google.common.base.MoreObjects.firstNonNull;
import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;
import static org.batfish.vendor.a10.representation.Interface.DEFAULT_MTU;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.batfish.common.VendorConversionException;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.DeviceModel;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.SwitchportMode;
import org.batfish.datamodel.Vrf;
import org.batfish.vendor.VendorConfiguration;

/** Datamodel class representing an A10 device configuration. */
public final class A10Configuration extends VendorConfiguration {

  public A10Configuration() {
    _interfacesEthernet = new HashMap<>();
    _interfacesLoopback = new HashMap<>();
  }

  @Override
  public String getHostname() {
    return _hostname;
  }

  public Map<Integer, Interface> getInterfacesEthernet() {
    return _interfacesEthernet;
  }

  public Map<Integer, Interface> getInterfacesLoopback() {
    return _interfacesLoopback;
  }

  @Override
  public void setHostname(String hostname) {
    _hostname = hostname;
  }

  @Override
  public void setVendor(ConfigurationFormat format) {
    _vendor = format;
  }

  @VisibleForTesting
  public static boolean getInterfaceEnabledEffective(Interface iface) {
    Boolean enabled = iface.getEnabled();
    if (enabled != null) {
      return enabled;
    }
    switch (iface.getType()) {
      case LOOPBACK:
        return true;
      case ETHERNET:
      default:
        return false;
    }
  }

  @VisibleForTesting
  public static int getInterfaceMtuEffective(Interface iface) {
    return firstNonNull(iface.getMtu(), DEFAULT_MTU);
  }

  public static InterfaceType getInterfaceType(Interface iface) {
    switch (iface.getType()) {
      case ETHERNET:
        return InterfaceType.PHYSICAL;
      case LOOPBACK:
        return InterfaceType.LOOPBACK;
      default:
        assert false;
        return InterfaceType.UNKNOWN;
    }
  }

  @VisibleForTesting
  public static String getInterfaceName(Interface iface) {
    String typeStr = iface.getType().toString();
    // Only the first letter should be capitalized, like in A10 `show` data
    return String.format(
        "%s%s %s", typeStr.substring(0, 1), typeStr.substring(1).toLowerCase(), iface.getNumber());
  }

  @Override
  public List<Configuration> toVendorIndependentConfigurations() throws VendorConversionException {
    String hostname = getHostname();
    _c = new Configuration(hostname, _vendor);
    _c.setHumanName(hostname);
    _c.setDeviceModel(DeviceModel.A10);
    _c.setDefaultCrossZoneAction(LineAction.DENY);
    _c.setDefaultInboundAction(LineAction.PERMIT);

    // Generated default VRF
    Vrf vrf = new Vrf(DEFAULT_VRF_NAME);
    _c.setVrfs(ImmutableMap.of(DEFAULT_VRF_NAME, vrf));

    _interfacesLoopback.forEach((num, iface) -> convertInterface(iface, vrf));
    _interfacesEthernet.forEach((num, iface) -> convertInterface(iface, vrf));

    return ImmutableList.of(_c);
  }

  private void convertInterface(Interface iface, Vrf vrf) {
    String name = getInterfaceName(iface);
    org.batfish.datamodel.Interface.Builder newIface =
        org.batfish.datamodel.Interface.builder()
            .setActive(getInterfaceEnabledEffective(iface))
            .setAddress(iface.getIpAddress())
            .setMtu(getInterfaceMtuEffective(iface))
            .setType(getInterfaceType(iface))
            .setName(name)
            .setVrf(vrf)
            .setOwner(_c);
    ImmutableList.Builder<String> names = ImmutableList.<String>builder().add(name);
    if (iface.getName() != null) {
      names.add(iface.getName());
    }
    newIface.setDeclaredNames(names.build());

    newIface.setSwitchportMode(SwitchportMode.NONE);

    newIface.build();
  }

  /**
   * Finalize configuration after it is finished being built. Does things like making structures
   * immutable.
   *
   * <p>This should only be called once, at the end of parsing and extraction.
   */
  public void finalizeStructures() {
    _interfacesEthernet = ImmutableMap.copyOf(_interfacesEthernet);
    _interfacesLoopback = ImmutableMap.copyOf(_interfacesLoopback);
  }

  private Configuration _c;
  private String _hostname;
  private Map<Integer, Interface> _interfacesEthernet;
  private Map<Integer, Interface> _interfacesLoopback;
  private ConfigurationFormat _vendor;
}
