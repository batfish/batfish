package org.batfish.minesweeper.question.comparelayerpolicies;

import com.google.common.collect.ImmutableList;
import org.batfish.datamodel.BgpActivePeerConfig;
import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.expr.CallExpr;
import org.batfish.datamodel.routing_policy.expr.Not;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.batfish.datamodel.routing_policy.statement.Statements;

public final class LayerPolicyUtils {
  /**
   * An attempt to build a composite policy, but it can't really work given the current
   * abstractions.
   */
  public static RoutingPolicy transitive(String fromRegex, String toRegex, Configuration c) {
    BgpActivePeerConfig fromPeer = null;
    BgpActivePeerConfig toPeer = null;
    // Find matching peers, both in the same VRF.
    for (Vrf v : c.getVrfs().values()) {
      BgpProcess bgpProcess = v.getBgpProcess();
      if (bgpProcess == null) {
        continue;
      }
      BgpActivePeerConfig from =
          bgpProcess.getActiveNeighbors().values().stream()
              .filter(p -> p.getDescription() != null && p.getDescription().matches(fromRegex))
              .findAny()
              .orElse(null);
      BgpActivePeerConfig to =
          bgpProcess.getActiveNeighbors().values().stream()
              .filter(p -> p.getDescription() != null && p.getDescription().matches(toRegex))
              .findAny()
              .orElse(null);
      if (from != null
          && to != null
          && from.getIpv4UnicastAddressFamily() != null
          && to.getIpv4UnicastAddressFamily() != null) {
        fromPeer = from;
        toPeer = to;
        break;
      }
    }
    if (fromPeer == null) {
      return null;
    }

    String name =
        String.format(
            "Transitive from %s to %s", fromPeer.getPeerAddress(), toPeer.getPeerAddress());
    ImmutableList.Builder<Statement> peerStatements = ImmutableList.builder();
    if (fromPeer.getIpv4UnicastAddressFamily().getImportPolicy() != null) {
      peerStatements.add(
          new If(
              new Not(new CallExpr(fromPeer.getIpv4UnicastAddressFamily().getImportPolicy())),
              ImmutableList.of(Statements.ReturnFalse.toStaticStatement())));
    }
    if (toPeer.getIpv4UnicastAddressFamily().getExportPolicy() != null) {
      peerStatements.add(
          new If(
              new Not(new CallExpr(toPeer.getIpv4UnicastAddressFamily().getImportPolicy())),
              ImmutableList.of(Statements.ReturnFalse.toStaticStatement())));
    }
    peerStatements.add(Statements.ReturnTrue.toStaticStatement());
    RoutingPolicy ret =
        RoutingPolicy.builder().setName(name).setStatements(peerStatements.build()).build();
    return ret;
  }

  /* Prevent instantiation of utility class.*/
  private LayerPolicyUtils() {}
}
