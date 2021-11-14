package org.batfish.representation.frr;

import com.google.common.annotations.VisibleForTesting;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Ip;
import org.batfish.vendor.VendorConfiguration;

public abstract class FrrContainer extends VendorConfiguration {

  public static final String LOOPBACK_INTERFACE_NAME = "lo";
  @VisibleForTesting public static final String CUMULUS_CLAG_DOMAIN_ID = "~CUMULUS_CLAG_DOMAIN~";

  abstract Map<String, Vxlan> getVxlans();

  abstract Vrf getVrf(String innerVrfName);

  abstract OspfProcess getOspfProcess();

  abstract FrrConfiguration getFrrConfiguration();

  abstract Optional<OspfInterface> getOspfInterface(String ifaceName);

  abstract Map<String, InterfaceClagSettings> getClagSettings();

  abstract String getVrfForVlan(Integer bridgeAccessVlan);

  abstract List<ConcreteInterfaceAddress> getInterfaceAddresses(String ifaceName);

  abstract String getSuperInterfaceName(String sourceInterfaceName);

  abstract Optional<Ip> getClagVxlanAnycastIp(String loopbackInterfaceName);

  abstract Optional<Ip> getVxlanLocalTunnelIp(String loopbackInterfaceName);
}
