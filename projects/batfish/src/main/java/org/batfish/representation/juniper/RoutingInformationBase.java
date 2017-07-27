package org.batfish.representation.juniper;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;
import org.batfish.datamodel.Prefix;

public class RoutingInformationBase implements Serializable {

  public static final String RIB_IPV4_MPLS = "inet.3";

  public static final String RIB_IPV4_MULTICAST = "inet.1";

  public static final String RIB_IPV4_UNICAST = "inet.0";

  public static final String RIB_IPV6_UNICAST = "inet6.0";

  public static final String RIB_ISIS = "iso.0";

  public static final String RIB_MPLS = "mpls.0";

  /** */
  private static final long serialVersionUID = 1L;

  private Map<Prefix, AggregateRoute> _aggregateRoutes;

  private Map<Prefix, GeneratedRoute> _generatedRoutes;

  private String _name;

  private Map<Prefix, StaticRoute> _staticRoutes;

  public RoutingInformationBase(String name) {
    _name = name;
    _aggregateRoutes = new TreeMap<>();
    _generatedRoutes = new TreeMap<>();
    _staticRoutes = new TreeMap<>();
  }

  public Map<Prefix, AggregateRoute> getAggregateRoutes() {
    return _aggregateRoutes;
  }

  public Map<Prefix, GeneratedRoute> getGeneratedRoutes() {
    return _generatedRoutes;
  }

  public String getName() {
    return _name;
  }

  public Map<Prefix, StaticRoute> getStaticRoutes() {
    return _staticRoutes;
  }
}
