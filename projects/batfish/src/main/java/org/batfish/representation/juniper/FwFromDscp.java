package org.batfish.representation.juniper;

import com.google.common.primitives.Ints;
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

    // try in following order: number, defined alias, builtin

    @SuppressWarnings("UnstableApiUsage")
    Integer value = Ints.tryParse(_spec);
    if (value != null) {
      return value < 64
          ? AclLineMatchExprs.matchDscp(value, getTraceElement())
          : new FalseExpr(
              TraceElement.of(
                  String.format("Treated illegal DSCP value '%d' as not matching", value)));
    }

    if (jc.getMasterLogicalSystem().getDscpAliases().containsKey(_spec)) {
      // no need to check if defined value is <64 because only legal values are parsed
      return AclLineMatchExprs.matchDscp(
          jc.getMasterLogicalSystem().getDscpAliases().get(_spec), getTraceElement());
    }

    return DscpUtil.defaultValue(_spec)
        .map(integer -> AclLineMatchExprs.matchDscp(integer, getTraceElement()))
        .orElseGet(
            () ->
                new FalseExpr(
                    TraceElement.of(
                        String.format(
                            "Treated undefined DSCP alias '%s' as not matching", _spec))));
  }

  private @Nonnull TraceElement getTraceElement() {
    return TraceElement.of(String.format("Matched DSCP %s", _spec));
  }
}
