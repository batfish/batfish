package org.batfish.representation.f5_bigip;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Represents a firewall rule-list in F5 BIG-IP security configuration. */
@ParametersAreNonnullByDefault
public final class FirewallRuleList implements Serializable {

  public static final class Builder {
    private @Nullable String _name;
    private final List<FirewallRule> _rules;

    private Builder() {
      _rules = new ArrayList<>();
    }

    public @Nonnull Builder setName(@Nonnull String name) {
      _name = checkNotNull(name);
      return this;
    }

    public @Nonnull Builder addRule(@Nonnull FirewallRule rule) {
      _rules.add(checkNotNull(rule));
      return this;
    }

    public @Nonnull FirewallRuleList build() {
      return new FirewallRuleList(
          checkNotNull(_name, "Name cannot be null"), new ArrayList<>(_rules));
    }
  }

  public static @Nonnull Builder builder() {
    return new Builder();
  }

  private final @Nonnull String _name;
  private final @Nonnull List<FirewallRule> _rules;

  private FirewallRuleList(@Nonnull String name, @Nonnull List<FirewallRule> rules) {
    _name = name;
    _rules = rules;
  }

  public @Nonnull String getName() {
    return _name;
  }

  public @Nonnull List<FirewallRule> getRules() {
    return _rules;
  }

  public @Nonnull Builder toBuilder() {
    Builder builder = builder().setName(_name);
    _rules.forEach(builder::addRule);
    return builder;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof FirewallRuleList)) {
      return false;
    }
    FirewallRuleList that = (FirewallRuleList) o;
    return _name.equals(that._name) && _rules.equals(that._rules);
  }

  @Override
  public int hashCode() {
    return java.util.Objects.hash(_name, _rules);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(String.format("name: \"%s\"", _name));
    if (!_rules.isEmpty()) {
      sb.append(",\nrules: {\n");
      for (FirewallRule rule : _rules) {
        sb.append("  ").append(rule).append("\n");
      }
      sb.append("}");
    }
    return sb.toString();
  }
}
