package org.batfish.representation.f5_bigip;

import static com.google.common.base.Predicates.notNull;
import static org.apache.commons.lang3.ObjectUtils.firstNonNull;
import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.VendorConversionException;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.SwitchportMode;
import org.batfish.datamodel.Vrf;
import org.batfish.vendor.VendorConfiguration;

@ParametersAreNonnullByDefault
public class F5BigipConfiguration extends VendorConfiguration {

  private static final long serialVersionUID = 1L;

  private transient Configuration _c;
  private ConfigurationFormat _format;
  private String _hostname;
  private final @Nonnull Map<String, Interface> _interfaces;
  private final @Nonnull Map<String, Self> _selves;
  private final @Nonnull Map<String, Vlan> _vlans;

  public F5BigipConfiguration() {
    _interfaces = new HashMap<>();
    _selves = new HashMap<>();
    _vlans = new HashMap<>();
  }

  @Override
  public String getHostname() {
    return _hostname;
  }

  public @Nonnull Map<String, Interface> getInterfaces() {
    return _interfaces;
  }

  public @Nonnull Map<String, Self> getSelves() {
    return _selves;
  }

  public @Nonnull Map<String, Vlan> getVlans() {
    return _vlans;
  }

  private void processVlanSettings(Vlan vlan) {
    Integer tag = vlan.getTag();
    if (tag == null) {
      return;
    }
    _interfaces.keySet().stream()
        .map(ifaceName -> _c.getAllInterfaces().get(ifaceName))
        .filter(notNull())
        .forEach(
            iface -> {
              iface.setSwitchport(true);
              iface.setSwitchportMode(SwitchportMode.TRUNK);
              // TODO: something else for configs with no concept of native VLAN
              iface.setNativeVlan(null);
              iface.setAllowedVlans(iface.getAllowedVlans().union(IntegerSpace.of(tag)));
            });
    ;
  }

  @Override
  public void setHostname(String hostname) {
    _hostname = hostname;
  }

  @Override
  public void setVendor(ConfigurationFormat format) {
    _format = format;
  }

  private org.batfish.datamodel.Interface toInterface(Interface iface) {
    org.batfish.datamodel.Interface newIface =
        new org.batfish.datamodel.Interface(iface.getName(), _c);
    Double speed = iface.getSpeed();
    newIface.setSpeed(speed);
    newIface.setBandwidth(firstNonNull(iface.getBandwidth(), speed, Interface.DEFAULT_BANDWIDTH));
    // Assume all interfaces are in default VRF for now
    newIface.setVrf(_c.getDefaultVrf());
    return newIface;
  }

  private org.batfish.datamodel.Interface toInterface(Self self) {
    org.batfish.datamodel.Interface newIface =
        new org.batfish.datamodel.Interface(self.getName(), _c, InterfaceType.VLAN);
    Vlan vlan = _vlans.get(self.getVlan());
    if (vlan != null) {
      newIface.setVlan(vlan.getTag());
    }
    InterfaceAddress address = self.getAddress();
    newIface.setAddress(address);
    newIface.setAllAddresses(ImmutableSortedSet.of(address));
    newIface.setBandwidth(Interface.DEFAULT_BANDWIDTH);
    newIface.setVrf(_c.getDefaultVrf());
    return newIface;
  }

  private @Nonnull Configuration toVendorIndependentConfiguration() {
    _c = new Configuration(_hostname, _format);

    // TODO: alter as behavior fleshed out
    _c.setDefaultCrossZoneAction(LineAction.PERMIT);
    _c.setDefaultInboundAction(LineAction.PERMIT);

    // Add default VRF
    _c.getVrfs().computeIfAbsent(DEFAULT_VRF_NAME, Vrf::new);

    // Add self interfaces

    // Add interfaces
    _interfaces.forEach(
        (name, iface) -> {
          org.batfish.datamodel.Interface newIface = toInterface(iface);
          _c.getAllInterfaces().put(name, newIface);
          // Assume all interfaces are in default VRF for now
          _c.getDefaultVrf().getInterfaces().put(name, newIface);
        });

    // Process vlans:
    // - configure interface switchport parameters
    _vlans.values().forEach(this::processVlanSettings);

    // Add self interfaces
    _selves.forEach(
        (name, self) -> {
          org.batfish.datamodel.Interface newIface = toInterface(self);
          _c.getAllInterfaces().put(name, newIface);
          // Assume all interfaces are in default VRF for now
          _c.getDefaultVrf().getInterfaces().put(name, newIface);
        });

    return _c;
  }

  @Override
  public @Nonnull List<Configuration> toVendorIndependentConfigurations()
      throws VendorConversionException {
    return ImmutableList.of(toVendorIndependentConfiguration());
  }
}
