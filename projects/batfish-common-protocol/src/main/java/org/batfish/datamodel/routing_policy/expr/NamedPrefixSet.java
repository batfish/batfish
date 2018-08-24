package org.batfish.datamodel.routing_policy.expr;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RouteFilterList;
import org.batfish.datamodel.routing_policy.Environment;

/** Expression for matching a {@link Prefix} against a named {@link RouteFilterList}. */
public final class NamedPrefixSet extends PrefixSetExpr {

  private static final String PROP_NAME = "name";

  private static final long serialVersionUID = 1L;

  private final String _name;

  @JsonCreator
  private static NamedPrefixSet create(@JsonProperty(PROP_NAME) @Nullable String name) {
    return new NamedPrefixSet(requireNonNull(name));
  }

  public NamedPrefixSet(@Nonnull String name) {
    _name = name;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof NamedPrefixSet)) {
      return false;
    }
    NamedPrefixSet other = (NamedPrefixSet) obj;
    return Objects.equals(_name, other._name);
  }

  @JsonProperty(PROP_NAME)
  public String getName() {
    return _name;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_name);
  }

  @Override
  public boolean matches(Prefix prefix, Environment environment) {
    RouteFilterList list = environment.getConfiguration().getRouteFilterLists().get(_name);
    if (list != null) {
      return list.permits(prefix);
    } else {
      environment.setError(true);
      return false;
    }
  }
}
