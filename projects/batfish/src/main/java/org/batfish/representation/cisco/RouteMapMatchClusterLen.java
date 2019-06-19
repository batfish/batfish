package org.batfish.representation.cisco;

import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.IntComparator;
import org.batfish.datamodel.routing_policy.expr.LiteralInt;
import org.batfish.datamodel.routing_policy.expr.MatchClusterLen;

public class RouteMapMatchClusterLen extends RouteMapMatchLine {
    private static final long serialVersionUID = 1L;

    private final Integer _clusterLen;

    public RouteMapMatchClusterLen(Integer clusterLen) {
        _clusterLen = clusterLen;
    }

    public Integer getClusterLen() {
        return _clusterLen;
    }

    @Override
    public BooleanExpr toBooleanExpr(Configuration c, CiscoConfiguration cc, Warnings w) {
        return new MatchClusterLen(IntComparator.EQ, _clusterLen);
    }
}
