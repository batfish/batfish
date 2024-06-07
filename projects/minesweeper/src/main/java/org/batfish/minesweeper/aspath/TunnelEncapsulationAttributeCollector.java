package org.batfish.minesweeper.aspath;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.bgp.TunnelEncapsulationAttribute;
import org.batfish.datamodel.routing_policy.expr.LiteralTunnelEncapsulationAttribute;
import org.batfish.datamodel.routing_policy.expr.TunnelEncapsulationAttributeExpr;
import org.batfish.datamodel.routing_policy.statement.SetTunnelEncapsulationAttribute;
import org.batfish.minesweeper.utils.Tuple;

/**
 * Collects {@link TunnelEncapsulationAttribute} from {@link
 * org.batfish.datamodel.routing_policy.RoutingPolicy}.
 */
@ParametersAreNonnullByDefault
public class TunnelEncapsulationAttributeCollector
    extends RoutingPolicyCollector<TunnelEncapsulationAttribute> {

  public static TunnelEncapsulationAttributeCollector instance() {
    return INSTANCE;
  }

  @Override
  public Set<TunnelEncapsulationAttribute> visitSetTunnelEncapsulationAttribute(
      SetTunnelEncapsulationAttribute setTunnelAttribute, Tuple<Set<String>, Configuration> arg) {
    TunnelEncapsulationAttributeExpr expr =
        setTunnelAttribute.getTunnelEncapsulationAttributeExpr();
    assert expr instanceof LiteralTunnelEncapsulationAttribute;
    // TODO otherwise.
    return ImmutableSet.of(
        ((LiteralTunnelEncapsulationAttribute) expr).getTunnelEncapsulationAttribute());
  }

  private static final TunnelEncapsulationAttributeCollector INSTANCE =
      new TunnelEncapsulationAttributeCollector();
}
