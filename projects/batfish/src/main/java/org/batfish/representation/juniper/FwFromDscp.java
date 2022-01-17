package org.batfish.representation.juniper;

import java.util.Map;
import java.util.Optional;
import javax.annotation.Nonnull;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.AclLineMatchExprs;
import org.batfish.datamodel.acl.FalseExpr;
import org.batfish.representation.juniper.FwTerm.Field;

/** Class for firewall filter from dscp */
public final class FwFromDscp implements FwFrom {

  // either a decimal value, user-defined alias, or a builtin alias
  private final String _spec;

  public FwFromDscp(String spec) {
    _spec = spec;
  }

  @Override
  public @Nonnull Field getField() {
    return Field.DSCP;
  }

  @Override
  public @Nonnull AclLineMatchExpr toAclLineMatchExpr(
      JuniperConfiguration jc, Configuration c, Warnings w) {
    Optional<Integer> dscpValue =
        toDscpValue(_spec, jc.getMasterLogicalSystem().getDscpAliases(), w);

    return dscpValue
        .map(value -> AclLineMatchExprs.matchDscp(value, getTraceElement()))
        .orElse(new FalseExpr(getTraceElement()));
  }

  private @Nonnull TraceElement getTraceElement() {
    return TraceElement.of(String.format("Matched DSCP %s", _spec));
  }

  private @Nonnull static Optional<Integer> toDscpValue(
      String spec, Map<String, Integer> dscpAliases, Warnings w) {
    try {
      int value = Integer.parseInt(spec);
      if (value > 63) {
        w.redFlag("Illegal DSCP value \"" + spec + "\"");
        return Optional.empty();
      }
      return Optional.of(value);
    } catch (NumberFormatException ignored) {
      // not a number, so try if it is a known alias
      // no need to check if any returned value is <64 because only legal values are parsed for
      // custom
      // aliases, and builtin aliases are legal of course.
      return dscpAliases.containsKey(spec)
          ? Optional.of(dscpAliases.get(spec))
          : DscpUtil.defaultValue(spec);
    }
  }
}
