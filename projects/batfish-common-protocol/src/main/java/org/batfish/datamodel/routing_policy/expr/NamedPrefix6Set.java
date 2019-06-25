package org.batfish.datamodel.routing_policy.expr;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Prefix6;
import org.batfish.datamodel.Route6FilterList;
import org.batfish.datamodel.routing_policy.Environment;

@ParametersAreNonnullByDefault
public final class NamedPrefix6Set extends Prefix6SetExpr {
  private static final String PROP_NAME = "name";

  @Nonnull private final String _name;

  @JsonCreator
  private static NamedPrefix6Set jsonCreator(@Nullable @JsonProperty(PROP_NAME) String name) {
    checkArgument(name != null, "%s must be provided", PROP_NAME);
    return new NamedPrefix6Set(name);
  }

  public NamedPrefix6Set(String name) {
    _name = name;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    } else if (!(obj instanceof NamedPrefix6Set)) {
      return false;
    }
    NamedPrefix6Set other = (NamedPrefix6Set) obj;
    return _name.equals(other._name);
  }

  @JsonProperty(PROP_NAME)
  @Nonnull
  public String getName() {
    return _name;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _name.hashCode();
    return result;
  }

  @Override
  public boolean matches(Prefix6 prefix, Environment environment) {
    Route6FilterList list = environment.getRoute6FilterLists().get(_name);
    if (list != null) {
      return list.permits(prefix);
    } else {
      environment.setError(true);
      return false;
    }
  }
}
