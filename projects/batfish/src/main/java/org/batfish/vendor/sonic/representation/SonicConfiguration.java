package org.batfish.vendor.sonic.representation;

import static com.google.common.base.Preconditions.checkState;
import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;
import static org.batfish.representation.frr.FrrConversions.convertFrr;
import static org.batfish.vendor.sonic.representation.SonicConversions.convertAcls;
import static org.batfish.vendor.sonic.representation.SonicConversions.convertLoopbacks;
import static org.batfish.vendor.sonic.representation.SonicConversions.convertPorts;
import static org.batfish.vendor.sonic.representation.SonicConversions.convertSnmpServer;
import static org.batfish.vendor.sonic.representation.SonicConversions.convertVlans;
import static org.batfish.vendor.sonic.representation.SonicConversions.getAclRulesByTableName;
import static org.batfish.vendor.sonic.representation.SonicStructureType.fromFrrStructureType;
import static org.batfish.vendor.sonic.representation.SonicStructureUsage.fromFrrStructureUsage;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.SortedSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.antlr.v4.runtime.ParserRuleContext;
import org.batfish.common.VendorConversionException;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.DeviceModel;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Vrf;
import org.batfish.representation.frr.FrrConfiguration;
import org.batfish.representation.frr.FrrStructureType;
import org.batfish.representation.frr.FrrStructureUsage;
import org.batfish.representation.frr.FrrVendorConfiguration;
import org.batfish.representation.frr.Vxlan;
import org.batfish.vendor.sonic.representation.SonicConversions.AclRuleWithName;

/**
 * Represents configuration of a SONiC device, containing information in both its configdb.json and
 * frr.conf files
 */
public class SonicConfiguration extends FrrVendorConfiguration {

  // If MGMT_VRF is not explicitly defined, we use this name.
  // TODO: confirm device behavior
  @VisibleForTesting public static final String DEFAULT_MGMT_VRF_NAME = "vrf_global";

  private @Nullable String _hostname;

  // these are set by the extractor
  private ConfigDb _configDb;
  private @Nullable ResolvConf _resolvConf;
  private @Nullable SnmpYml _snmpYml;

  private final @Nonnull FrrConfiguration _frr;

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
    if (_configDb.getMgmtVrfs().isEmpty()) {
      // Create a management VRF with the default name.
      Vrf.builder().setName(DEFAULT_MGMT_VRF_NAME).setOwner(c).build();
    } else if (_configDb.getMgmtVrfs().size() > 1) {
      // warn about multiple management VRFs
      _w.redFlagf(
          "Multiple management VRFs defined.  Putting management ports in VRF '%s'",
          getMgmtVrfName(_configDb.getMgmtVrfs()));
    }

    // all ports under the PORT object are in the default VRF
    // (haven't seen VRF definitions in configdb model)
    convertPorts(c, _configDb.getPorts(), _configDb.getInterfaces(), c.getDefaultVrf());
    convertPorts(
        c,
        _configDb.getMgmtPorts(),
        _configDb.getMgmtInterfaces(),
        c.getVrfs().get(getMgmtVrfName(_configDb.getMgmtVrfs())));

    convertLoopbacks(
        c, _configDb.getLoopbacks(), _configDb.getLoopbackInterfaces(), c.getDefaultVrf());

    convertVlans(
        c,
        _configDb.getVlans(),
        _configDb.getVlanMembers(),
        _configDb.getVlanInterfaces(),
        c.getDefaultVrf(),
        _w);

    Map<String, SortedSet<AclRuleWithName>> aclNameToRules =
        getAclRulesByTableName(_configDb.getAclTables(), _configDb.getAclRules(), _w);
    convertAcls(c, _configDb.getAclTables(), aclNameToRules, _w);

    c.setNtpServers(_configDb.getNtpServers());
    c.setLoggingServers(_configDb.getSyslogServers());
    c.setTacacsServers(_configDb.getTacplusServers());
    c.setTacacsSourceInterface(_configDb.getTacplusSourceInterface().orElse(null));

    if (_resolvConf != null) {
      c.setDnsServers(
          _resolvConf.getNameservers().stream()
              .map(Ip::toString)
              .collect(ImmutableSet.toImmutableSet()));
    }

    if (_snmpYml != null && _snmpYml.getRoCommunity() != null) {
      convertSnmpServer(c, _snmpYml.getRoCommunity(), _configDb.getAclTables(), aclNameToRules, _w);
    }

    convertFrr(c, this);

    markStructures();

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

  private void markStructures() {
    SonicStructureType.CONCRETE_STRUCTURES.forEach(this::markConcreteStructure);
    SonicStructureType.ABSTRACT_STRUCTURES.asMap().forEach(this::markAbstractStructureAllUsages);
  }

  /* Overrides for FrrVendorConfiguration follow. They are called during FRR control plane extraction, to get information on what is in ConfigDb. */

  @Override
  public boolean hasInterface(String ifaceName) {
    return _configDb.getPorts().containsKey(ifaceName)
        || _configDb.getLoopbackInterfaces().containsKey(ifaceName)
        || _configDb.getMgmtPorts().containsKey(ifaceName)
        || (_configDb.getVlans().containsKey(ifaceName)
            && _configDb.getVlanInterfaces().containsKey(ifaceName));
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
    if (_configDb.getLoopbackInterfaces().containsKey(ifaceName)) {
      return DEFAULT_VRF_NAME; // only have default VRF for loopbacks
    }
    if (_configDb.getMgmtPorts().containsKey(ifaceName)) {
      return getMgmtVrfName(_configDb.getMgmtVrfs());
    }
    if (_configDb.getVlans().containsKey(ifaceName)
        && _configDb.getVlanInterfaces().containsKey(ifaceName)) {
      return DEFAULT_VRF_NAME;
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
          .map(iface -> ImmutableList.copyOf(iface.getAddresses().keySet()))
          .orElse(ImmutableList.of());
    }
    if (_configDb.getLoopbackInterfaces().containsKey(ifaceName)) {
      return Optional.ofNullable(_configDb.getLoopbackInterfaces().get(ifaceName))
          .map(iface -> ImmutableList.copyOf(iface.getAddresses().keySet()))
          .orElse(ImmutableList.of());
    }
    if (_configDb.getMgmtPorts().containsKey(ifaceName)) {
      return Optional.ofNullable(_configDb.getMgmtInterfaces().get(ifaceName))
          .map(iface -> ImmutableList.copyOf(iface.getAddresses().keySet()))
          .orElse(ImmutableList.of());
    }
    if (_configDb.getVlans().containsKey(ifaceName)
        && _configDb.getVlanInterfaces().containsKey(ifaceName)) {
      return ImmutableList.copyOf(
          _configDb.getVlanInterfaces().get(ifaceName).getAddresses().keySet());
    }
    // should never get here
    throw new NoSuchElementException("Interface " + ifaceName + " does not exist");
  }

  @Override
  public Map<String, Vxlan> getVxlans() {
    // we don't have vxlan support for Sonic yet, so this method shouldn't be called
    throw new UnsupportedOperationException();
  }

  @Override
  public void referenceStructure(
      FrrStructureType frrStructureType, String name, FrrStructureUsage usage, int line) {
    super.referenceStructure(
        fromFrrStructureType(frrStructureType), name, fromFrrStructureUsage(usage), line);
  }

  @Override
  public void defineStructure(
      FrrStructureType frrStructureType, String name, ParserRuleContext ctx) {
    super.defineStructure(fromFrrStructureType(frrStructureType), name, ctx);
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

  public @Nullable ResolvConf getResolvConf() {
    return _resolvConf;
  }

  public void setResolveConf(@Nullable ResolvConf resolveConf) {
    _resolvConf = resolveConf;
  }

  public @Nullable SnmpYml getSnmpYml() {
    return _snmpYml;
  }

  public void setSnmpYml(@Nullable SnmpYml snmpYml) {
    _snmpYml = snmpYml;
  }
}
