package org.batfish.vendor.check_point_gateway.representation;

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
import org.batfish.datamodel.Vrf;
import org.batfish.vendor.VendorConfiguration;

public class CheckPointGatewayConfiguration extends VendorConfiguration {

  public static final String VRF_NAME = "default";

  public CheckPointGatewayConfiguration() {
    _interfaces = new HashMap<>();
  }

  @Override
  public String getHostname() {
    return _hostname;
  }

  public Map<String, Interface> getInterfaces() {
    return _interfaces;
  }

  @Override
  public void setHostname(String hostname) {
    _hostname = hostname;
  }

  @Override
  public void setVendor(ConfigurationFormat format) {
    _vendor = format;
  }

  @Override
  public List<Configuration> toVendorIndependentConfigurations() throws VendorConversionException {
    String hostname = getHostname();
    _c = new Configuration(hostname, _vendor);
    _c.setDeviceModel(DeviceModel.CHECK_POINT_GATEWAY);
    _c.setDefaultCrossZoneAction(LineAction.DENY);
    _c.setDefaultInboundAction(LineAction.PERMIT);

    // Gateways don't have VRFs, so put everything in a generated default VRF
    Vrf vrf = new Vrf(VRF_NAME);
    _c.setVrfs(ImmutableMap.of(VRF_NAME, vrf));

    _interfaces.forEach((ifaceName, iface) -> toInterface(iface, vrf));
    return ImmutableList.of(_c);
  }

  InterfaceType getInterfaceType(Interface iface) {
    String name = iface.getName();
    if (name.startsWith("eth")) {
      return InterfaceType.PHYSICAL;
    } else if (name.startsWith("lo")) {
      return InterfaceType.LOOPBACK;
    }
    return InterfaceType.UNKNOWN;
  }

  org.batfish.datamodel.Interface toInterface(Interface iface, Vrf vrf) {
    String ifaceName = iface.getName();
    org.batfish.datamodel.Interface.Builder newIface =
        org.batfish.datamodel.Interface.builder()
            .setName(ifaceName)
            .setOwner(_c)
            .setVrf(vrf)
            .setActive(iface.getState())
            .setAddress(iface.getAddress())
            .setType(getInterfaceType(iface))
            .setMtu(iface.getMtuEffective());
    return newIface.build();
  }

  private Configuration _c;
  private String _hostname;
  private Map<String, Interface> _interfaces;
  private ConfigurationFormat _vendor;
}
