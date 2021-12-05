package org.batfish.vendor.sonic.representation;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.VendorConversionException;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.DeviceModel;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Vrf;
import org.batfish.representation.frr.FrrConfiguration;
import org.batfish.representation.frr.FrrVendorConfiguration;
import org.batfish.representation.frr.Vxlan;
import org.batfish.vendor.sonic.representation.PortDb.Port;

@ParametersAreNonnullByDefault
public class SonicConfiguration extends FrrVendorConfiguration {

  private @Nullable String _hostname;
  private @Nullable ConfigDb _configDb;
  private @Nonnull final FrrConfiguration _frr;

  public SonicConfiguration() {
    _frr = new FrrConfiguration();
  }

  @Override
  public List<Configuration> toVendorIndependentConfigurations() throws VendorConversionException {
    checkArgument(_configDb != null, "Conversion called before configDb was set");
    checkArgument(_hostname != null, "Conversion called before hostname was set");

    Configuration c = new Configuration(_hostname, ConfigurationFormat.SONIC);
    c.setDeviceModel(DeviceModel.SONIC);
    c.setDefaultCrossZoneAction(LineAction.PERMIT); // TODO: confirm
    c.setDefaultInboundAction(LineAction.PERMIT); // TODO: confirm
    c.setExportBgpFromBgpRib(true);

    // create default VRF
    Vrf vrf = new Vrf(DEFAULT_VRF_NAME);
    c.setVrfs(ImmutableMap.of(DEFAULT_VRF_NAME, vrf));

    convertPorts(c, _configDb.getPortDb(), _configDb.getInterfaceDb());

    return ImmutableList.of(c);
  }

  private void convertPorts(Configuration c, PortDb portDb, InterfaceDb interfaceDb) {
    for (String portName : portDb.getPorts().keySet()) {
      Port port = portDb.getPorts().get(portName);
      Interface.Builder ib =
          Interface.builder()
              .setName(portName)
              .setOwner(c)
              .setVrf(c.getDefaultVrf()) // everything is default VRF at the moment
              .setType(InterfaceType.PHYSICAL)
              .setDescription(port.getDescription().orElse(null))
              .setMtu(port.getMtu().orElse(null))
              .setActive(port.getAdminStatus().orElse(true)); // default is active

      if (interfaceDb.getInterfaces().containsKey(portName)) {
        L3Interface l3Interface = interfaceDb.getInterfaces().get(portName);
        ib.setAddress(l3Interface.getAddress());
      }

      ib.build();
    }
  }

  @Nullable
  public ConfigDb getConfigDb() {
    return _configDb;
  }

  public void setConfigDb(@Nullable ConfigDb configDb) {
    _configDb = configDb;
  }

  @Override
  public FrrConfiguration getFrrConfiguration() {
    return _frr;
  }

  @Override
  public boolean hasInterface(String ifaceName) {
    return _configDb.getInterfaceDb().getInterfaces().containsKey(ifaceName);
  }

  @Override
  public boolean hasVrf(String vrfName) {
    return vrfName.equals(DEFAULT_VRF_NAME); // only have default VRF for now
  }

  @Override
  public String getInterfaceVrf(String ifaceName) {
    if (!_configDb.getInterfaceDb().getInterfaces().containsKey(ifaceName)) {
      throw new NoSuchElementException("Interface " + ifaceName + " does not exist");
    }
    return DEFAULT_VRF_NAME; // only have default VRF for now
  }

  @Nonnull
  @Override
  public List<ConcreteInterfaceAddress> getInterfaceAddresses(String ifaceName) {
    if (!_configDb.getInterfaceDb().getInterfaces().containsKey(ifaceName)) {
      throw new NoSuchElementException("Interface " + ifaceName + " does not exist");
    }
    return Optional.ofNullable(
            _configDb.getInterfaceDb().getInterfaces().get(ifaceName).getAddress())
        .map(ImmutableList::of)
        .orElse(ImmutableList.of());
  }

  @Override
  public Map<String, Vxlan> getVxlans() {
    throw new UnsupportedOperationException();
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
  public void setVendor(ConfigurationFormat format) {}
}
