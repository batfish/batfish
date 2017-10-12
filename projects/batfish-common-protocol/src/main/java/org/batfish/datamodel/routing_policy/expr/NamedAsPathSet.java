package org.batfish.datamodel.routing_policy.expr;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.common.base.MoreObjects;
import java.util.Objects;
import org.batfish.datamodel.AsPath;
import org.batfish.datamodel.AsPathAccessList;
import org.batfish.datamodel.BgpRoute;
import org.batfish.datamodel.routing_policy.Environment;

public class NamedAsPathSet extends AsPathSetExpr {

  /** */
  private static final long serialVersionUID = 1L;

  private String _name;

  @JsonCreator
  private NamedAsPathSet() {}

  public NamedAsPathSet(String name) {
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
    AsPathAccessList list = environment.getConfiguration().getAsPathAccessLists().get(_name);
    if (list != null) {
      boolean match = false;
      AsPath inputAsPath = null;
      if (environment.getUseOutputAttributes()
          && environment.getOutputRoute() instanceof BgpRoute.Builder) {
        BgpRoute.Builder bgpRouteBuilder = (BgpRoute.Builder) environment.getOutputRoute();
        inputAsPath = new AsPath(bgpRouteBuilder.getAsPath());
      } else if (environment.getReadFromIntermediateBgpAttributes()) {
        inputAsPath = new AsPath(environment.getIntermediateBgpAttributes().getAsPath());
      } else if (environment.getOriginalRoute() instanceof BgpRoute) {
        BgpRoute bgpRoute = (BgpRoute) environment.getOriginalRoute();
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

  public void setName(String name) {
    _name = name;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(NamedAsPathSet.class)
        .omitNullValues()
        .add("name", _name)
        .toString();
  }
}
