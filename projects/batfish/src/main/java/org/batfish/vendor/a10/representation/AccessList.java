package org.batfish.vendor.a10.representation;

import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.util.List;
import javax.annotation.Nonnull;

/** Datamodel class representing configuration of an A10 access-list. */
public final class AccessList implements Serializable {

  @Nonnull
  public String getName() {
    return _name;
  }

  @Nonnull
  public List<AccessListRule> getRules() {
    return _rules;
  }

  public void addRule(AccessListRule rule) {
    ImmutableList.Builder<AccessListRule> builder =
        ImmutableList.<AccessListRule>builder().addAll(_rules);
    _rules = builder.add(rule).build();
  }

  public AccessList(String name) {
    _name = name;
    _rules = ImmutableList.of();
  }

  @Nonnull private final String _name;

  @Nonnull private List<AccessListRule> _rules;
}
