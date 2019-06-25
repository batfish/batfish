package org.batfish.representation.juniper;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;
import javax.annotation.Nonnull;
import org.batfish.datamodel.Prefix;

/** A Juniper RIB */
public class RoutingInformationBase implements Serializable {

  public static final String RIB_IPV4_MPLS = "inet.3";
  public static final String RIB_IPV4_MULTICAST = "inet.1";
  public static final String RIB_IPV4_UNICAST = "inet.0";
  public static final String RIB_IPV6_UNICAST = "inet6.0";
  public static final String RIB_ISIS = "iso.0";
  public static final String RIB_MPLS = "mpls.0";

  private final Map<Prefix, AggregateRoute> _aggregateRoutes;
  private final Map<Prefix, GeneratedRoute> _generatedRoutes;
  private final String _name;
  private final Map<Prefix, StaticRoute> _staticRoutes;

  public RoutingInformationBase(@Nonnull String name) {
    _name = name;
    _aggregateRoutes = new TreeMap<>();
    _generatedRoutes = new TreeMap<>();
    _staticRoutes = new TreeMap<>();
  }

  @Nonnull
  public Map<Prefix, AggregateRoute> getAggregateRoutes() {
    return _aggregateRoutes;
  }

  @Nonnull
  public Map<Prefix, GeneratedRoute> getGeneratedRoutes() {
    return _generatedRoutes;
  }

  @Nonnull
  public String getName() {
    return _name;
  }

  @Nonnull
  public Map<Prefix, StaticRoute> getStaticRoutes() {
    return _staticRoutes;
  }
}
