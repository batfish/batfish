package org.batfish.representation.cisco_xr;

import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.util.List;
import javax.annotation.Nullable;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Prefix6;
import org.batfish.datamodel.routing_policy.expr.PrefixSetExpr;

public class RoutePolicyInlinePrefix6Set extends RoutePolicyPrefixSet {

  public static class Entry implements Serializable {
    private final Prefix6 _prefix;
    private final int _lower;
    private final int _upper;

    public Entry(Prefix6 prefix, int lower, int upper) {
      _prefix = prefix;
      _lower = lower;
      _upper = upper;
    }

    public Prefix6 getPrefix() {
      return _prefix;
    }

    public int getLower() {
      return _lower;
    }

    public int getUpper() {
      return _upper;
    }
  }

  private final List<Entry> _entries;

  public RoutePolicyInlinePrefix6Set(List<Entry> entries) {
    _entries = ImmutableList.copyOf(entries);
  }

  public List<Entry> getEntries() {
    return _entries;
  }

  @Override
  public @Nullable PrefixSetExpr toPrefixSetExpr(
      CiscoXrConfiguration cc, Configuration c, Warnings w) {
    return null;
  }
}
