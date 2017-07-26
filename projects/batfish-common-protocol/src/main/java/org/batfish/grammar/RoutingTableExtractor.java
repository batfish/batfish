package org.batfish.grammar;

import org.batfish.datamodel.collections.RoutesByVrf;

public interface RoutingTableExtractor extends BatfishExtractor {

  RoutesByVrf getRoutesByVrf();
}
