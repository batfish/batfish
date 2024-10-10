package org.batfish.representation.juniper;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;
import javax.annotation.Nonnull;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Prefix6;

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
  private final Map<Prefix, StaticRouteV4> _staticRoutes;
  private final Map<Prefix6, StaticRouteV6> _staticRoutesV6;

  public RoutingInformationBase(@Nonnull String name) {
    _name = name;
    _aggregateRoutes = new TreeMap<>();
    _generatedRoutes = new TreeMap<>();
    _staticRoutes = new TreeMap<>();
    _staticRoutesV6 = new TreeMap<>();
  }

  public @Nonnull Map<Prefix, AggregateRoute> getAggregateRoutes() {
    return _aggregateRoutes;
  }

  public @Nonnull Map<Prefix, GeneratedRoute> getGeneratedRoutes() {
    return _generatedRoutes;
  }

  public @Nonnull String getName() {
    return _name;
  }

  public @Nonnull Map<Prefix, StaticRouteV4> getStaticRoutes() {
    return _staticRoutes;
  }

  public @Nonnull Map<Prefix6, StaticRouteV6> getStaticRoutesV6() {
    return _staticRoutesV6;
  }
}
