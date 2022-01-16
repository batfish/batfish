package org.batfish.representation.juniper;

import com.google.common.collect.ImmutableList;
import java.util.Map;
import java.util.Optional;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.representation.juniper.FwTerm.Field;

/** Class for firewall filter from dscp */
public final class FwFromDscp implements FwFrom {

  // either a decimal value, user-defined alias, or one of the standard values
  private final String _spec;

  public FwFromDscp(String spec) {
    _spec = spec;
  }

  @Override
  public Field getField() {
    return Field.DSCP;
  }

  @Override
  public AclLineMatchExpr toAclLineMatchExpr(JuniperConfiguration jc, Configuration c, Warnings w) {
    return new MatchHeaderSpace(toHeaderspace(jc, c, w), getTraceElement());
  }

  private HeaderSpace toHeaderspace(JuniperConfiguration jc, Configuration c, Warnings w) {
    Optional<Integer> dscpValue =
        toDscpValue(_spec, jc.getMasterLogicalSystem().getDscpAliases(), w);

    if (!dscpValue.isPresent()) {
      // match nothing
      return HeaderSpace.builder().setDscps(ImmutableList.of()).build();
    }

    return HeaderSpace.builder().setDscps(ImmutableList.of(dscpValue.get())).build();
  }

  private TraceElement getTraceElement() {
    return TraceElement.of(String.format("Matched DSCP %s", _spec));
  }

  private static Optional<Integer> toDscpValue(
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
      Optional<Integer> value =
          dscpAliases.containsKey(spec)
              ? Optional.of(dscpAliases.get(spec))
              : DscpUtil.defaultValue(spec);

      if (!value.isPresent()) {
        w.redFlag("Reference to unknown DSCP alias \"" + spec + "\"");
      }

      // no need to check if this value is legal because only legal values are parsed for custom
      // aliases, and builtin aliases are legal of course.
      return value;
    }
  }
}
