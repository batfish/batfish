package org.batfish.vendor.sonic.representation;

import static com.google.common.base.Preconditions.checkState;
import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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

/**
 * Represents configuration of a SONiC device, containing information in both its configdb.json and
 * frr.conf files
 */
public class SonicConfiguration extends FrrVendorConfiguration {

  private @Nullable String _hostname;
  private ConfigDb _configDb; // set via the extractor
  private @Nonnull final FrrConfiguration _frr;

  public SonicConfiguration() {
    _frr = new FrrConfiguration();
  }

  @Override
  public List<Configuration> toVendorIndependentConfigurations() throws VendorConversionException {
    checkState(_hostname != null, "Conversion called before hostname was set");
    checkState(_configDb != null, "Conversion called before configDb was set");

    Configuration c = new Configuration(_hostname, ConfigurationFormat.SONIC);
    c.setDeviceModel(DeviceModel.SONIC);
    c.setDefaultCrossZoneAction(LineAction.PERMIT); // TODO: confirm
    c.setDefaultInboundAction(LineAction.PERMIT); // TODO: confirm
    c.setExportBgpFromBgpRib(true);

    // create default VRF
    Vrf vrf = new Vrf(DEFAULT_VRF_NAME);
    c.setVrfs(ImmutableMap.of(DEFAULT_VRF_NAME, vrf));

    convertPorts(c, _configDb.getPorts(), _configDb.getInterfaces());

    return ImmutableList.of(c);
  }

  private void convertPorts(
      Configuration c, Map<String, Port> ports, Map<String, L3Interface> interfaces) {
    for (String portName : ports.keySet()) {
      Port port = ports.get(portName);
      Interface.Builder ib =
          Interface.builder()
              .setName(portName)
              .setOwner(c)
              .setVrf(c.getDefaultVrf()) // everything is default VRF at the moment
              .setType(InterfaceType.PHYSICAL)
              .setDescription(port.getDescription().orElse(null))
              .setMtu(port.getMtu().orElse(null))
              .setActive(port.getAdminStatusUp().orElse(true)); // default is active

      if (interfaces.containsKey(portName)) {
        L3Interface l3Interface = interfaces.get(portName);
        ib.setAddress(l3Interface.getAddress());
      }

      ib.build();
    }
  }

  public ConfigDb getConfigDb() {
    return _configDb;
  }

  public void setConfigDb(ConfigDb configDb) {
    _configDb = configDb;
  }

  @Override
  public @Nonnull FrrConfiguration getFrrConfiguration() {
    return _frr;
  }

  private @Nullable L3Interface getInterface(String ifaceName) {
    // This function will need to be extended if/when non-L3 interfaces appear in the VS model
    if (_configDb.getInterfaces().containsKey(ifaceName)) {
      return _configDb.getInterfaces().get(ifaceName);
    }
    if (_configDb.getLoopbacks().containsKey(ifaceName)) {
      return _configDb.getLoopbacks().get(ifaceName);
    }
    return null;
  }

  @Override
  public boolean hasInterface(String ifaceName) {
    return getInterface(ifaceName) != null;
  }

  @Override
  public boolean hasVrf(String vrfName) {
    return vrfName.equals(DEFAULT_VRF_NAME); // only have default VRF for now
  }

  @Override
  public @Nonnull String getInterfaceVrf(String ifaceName) {
    if (!hasInterface(ifaceName)) {
      throw new NoSuchElementException("Interface " + ifaceName + " does not exist");
    }
    return DEFAULT_VRF_NAME; // only have default VRF for now
  }

  @Override
  public @Nonnull List<ConcreteInterfaceAddress> getInterfaceAddresses(String ifaceName) {
    L3Interface l3Interface = getInterface(ifaceName);
    if (l3Interface == null) {
      throw new NoSuchElementException("Interface " + ifaceName + " does not exist");
    }
    return Optional.ofNullable(l3Interface.getAddress())
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
