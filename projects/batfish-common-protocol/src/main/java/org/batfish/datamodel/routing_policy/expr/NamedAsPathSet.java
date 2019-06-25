package org.batfish.datamodel.routing_policy.expr;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import org.batfish.datamodel.AsPath;
import org.batfish.datamodel.AsPathAccessList;
import org.batfish.datamodel.BgpRoute;
import org.batfish.datamodel.routing_policy.Environment;

public final class NamedAsPathSet extends AsPathSetExpr {

  private static final String PROP_NAME = "name";

  private String _name;

  @JsonCreator
  public NamedAsPathSet(@JsonProperty(PROP_NAME) String name) {
    _name = name;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof NamedAsPathSet)) {
      return false;
    }
    NamedAsPathSet other = (NamedAsPathSet) obj;
    return Objects.equals(_name, other._name);
  }

  @JsonProperty(PROP_NAME)
  public String getName() {
    return _name;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_name == null) ? 0 : _name.hashCode());
    return result;
  }

  @Override
  public boolean matches(Environment environment) {
    AsPathAccessList list = environment.getAsPathAccessLists().get(_name);
    if (list != null) {
      boolean match = false;
      AsPath inputAsPath = null;
      if (environment.getUseOutputAttributes()
          && environment.getOutputRoute() instanceof BgpRoute.Builder<?, ?>) {
        BgpRoute.Builder<?, ?> bgpRouteBuilder =
            (BgpRoute.Builder<?, ?>) environment.getOutputRoute();
        inputAsPath = bgpRouteBuilder.getAsPath();
      } else if (environment.getReadFromIntermediateBgpAttributes()) {
        inputAsPath = environment.getIntermediateBgpAttributes().getAsPath();
      } else if (environment.getOriginalRoute() instanceof BgpRoute) {
        BgpRoute<?, ?> bgpRoute = (BgpRoute<?, ?>) environment.getOriginalRoute();
        inputAsPath = bgpRoute.getAsPath();
      }
      if (inputAsPath != null) {
        match = list.permits(inputAsPath);
      }
      return match;
    } else {
      environment.setError(true);
      return false;
    }
  }

  @JsonProperty(PROP_NAME)
  public void setName(String name) {
    _name = name;
  }

  @Override
  public String toString() {
    return toStringHelper().add("name", _name).toString();
  }
}
