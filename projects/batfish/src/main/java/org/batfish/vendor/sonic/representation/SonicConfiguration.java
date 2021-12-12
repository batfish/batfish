package org.batfish.vendor.sonic.representation;

import static com.google.common.base.Preconditions.checkState;
import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;
import static org.batfish.vendor.sonic.representation.SonicConversions.convertPorts;

import com.google.common.collect.ImmutableList;
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

  // If MGMT_VRF is not explicitly defined, we use this name.
  // TODO: confirm device behavior
  static final String DEFAULT_MGMT_VRF_NAME = "vrf_global";

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

    // create VRFs
    Vrf.builder().setName(DEFAULT_VRF_NAME).setOwner(c).build();
    _configDb
        .getMgmtVrfs()
        .keySet()
        .forEach(vrfName -> Vrf.builder().setName(vrfName).setOwner(c).build());

    // warn about multiple management VRFs
    if (_configDb.getMgmtVrfs().size() > 1) {
      _w.redFlag(
          String.format(
              "Multiple management VRFs defined.  Putting management ports in VRF '%s'",
              getMgmtVrfName(_configDb.getMgmtVrfs())));
    }

    // all ports under the PORT object are in the default VRF
    // (haven't seen VRF definitions in configdb model)
    convertPorts(c, _configDb.getPorts(), _configDb.getInterfaces(), c.getDefaultVrf());
    convertPorts(
        c,
        _configDb.getMgmtPorts(),
        _configDb.getMgmtInterfaces(),
        c.getVrfs().get(getMgmtVrfName(_configDb.getMgmtVrfs())));

    return ImmutableList.of(c);
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

  private static String getMgmtVrfName(Map<String, MgmtVrf> mgmtVrfs) {
    return mgmtVrfs.keySet().stream().sorted().findFirst().orElse(DEFAULT_MGMT_VRF_NAME);
  }

  /* Overrides for FrrVendorConfiguration follow. They are called during FRR control plane extraction, to get information on what is in ConfigDb. */

  @Override
  public boolean hasInterface(String ifaceName) {
    return _configDb.getPorts().containsKey(ifaceName)
        || _configDb.getLoopbacks().containsKey(ifaceName)
        || _configDb.getMgmtPorts().containsKey(ifaceName);
  }

  @Override
  public boolean hasVrf(String vrfName) {
    return vrfName.equals(DEFAULT_VRF_NAME) || _configDb.getMgmtVrfs().containsKey(vrfName);
  }

  @Override
  public @Nonnull String getInterfaceVrf(String ifaceName) {
    if (!hasInterface(ifaceName)) {
      throw new NoSuchElementException("Interface " + ifaceName + " does not exist");
    }
    if (_configDb.getPorts().containsKey(ifaceName)) {
      return DEFAULT_VRF_NAME; // only have default VRF for ports in PORT object
    }
    if (_configDb.getLoopbacks().containsKey(ifaceName)) {
      return DEFAULT_VRF_NAME; // only have default VRF for loopbacks
    }
    if (_configDb.getMgmtPorts().containsKey(ifaceName)) {
      return getMgmtVrfName(_configDb.getMgmtVrfs());
    }
    // should never get here
    throw new NoSuchElementException("Interface " + ifaceName + " does not exist");
  }

  @Override
  public @Nonnull List<ConcreteInterfaceAddress> getInterfaceAddresses(String ifaceName) {
    if (!hasInterface(ifaceName)) {
      throw new NoSuchElementException("Interface " + ifaceName + " does not exist");
    }
    if (_configDb.getPorts().containsKey(ifaceName)) {
      return Optional.ofNullable(_configDb.getInterfaces().get(ifaceName))
          .flatMap(iface -> Optional.ofNullable(iface.getAddress()).map(ImmutableList::of))
          .orElse(ImmutableList.of());
    }
    if (_configDb.getLoopbacks().containsKey(ifaceName)) {
      return Optional.ofNullable(_configDb.getLoopbacks().get(ifaceName))
          .flatMap(iface -> Optional.ofNullable(iface.getAddress()).map(ImmutableList::of))
          .orElse(ImmutableList.of());
    }
    if (_configDb.getMgmtPorts().containsKey(ifaceName)) {
      return Optional.ofNullable(_configDb.getMgmtInterfaces().get(ifaceName))
          .flatMap(iface -> Optional.ofNullable(iface.getAddress()).map(ImmutableList::of))
          .orElse(ImmutableList.of());
    }
    // should never get here
    throw new NoSuchElementException("Interface " + ifaceName + " does not exist");
  }

  @Override
  public Map<String, Vxlan> getVxlans() {
    // we don't have vxlan support for Sonic yet, so this method shouldn't be called
    throw new UnsupportedOperationException();
  }

  /* Overrides for VendorConfiguration follow */

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
