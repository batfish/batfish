package org.batfish.specifier;

import java.util.Map;
import java.util.Set;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.RoutingProtocol;

/** A way to specify groups of RoutingProtocols */
@ParametersAreNonnullByDefault
public class RoutingProtocolSpecifier {

  private static final String ISIS = "isis";
  private static final String ISIS_L1
  private static final String OSPF = "ospf";
  private static final String OSPF_EXT = "ospf-ext";
  private static final String OSPF_EXT1 = "ospf-ext1";
  private static final String OSPF_EXT2 = "ospf-ext2";
  private static final String OSPF_INT = "ospf-int";
  private static final String OSPF_INTRA = "ospf-intra";
  private static final String OSPF_INTER = "ospf-inter";

  private static final Map<String, Set<RoutingProtocol>> _map = getMap();

  private static Map<String, Set<RoutingProtocol>> getMap() {

  }
}
