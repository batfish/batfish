package org.batfish.representation.juniper;

import javax.annotation.Nonnull;
import org.batfish.common.VendorConversionException;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.BooleanExprs;
import org.batfish.datamodel.routing_policy.expr.MatchIpv4;

/** Represents a "from family" line in a {@link PsTerm} */
public class PsFromFamily extends PsFrom {

  private final AddressFamily _family;

  public PsFromFamily(@Nonnull AddressFamily family) {
    _family = family;
  }

  @Override
  public BooleanExpr toBooleanExpr(JuniperConfiguration jc, Configuration c, Warnings warnings) {
    switch (_family) {
      case IPV4:
        return MatchIpv4.instance();
      case IPV6:
        return BooleanExprs.FALSE;
      default:
        throw new VendorConversionException("Unsupported address family: \"" + _family + "\"");
    }
  }
}
