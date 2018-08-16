package org.batfish.datamodel.routing_policy.expr;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RouteFilterList;
import org.batfish.datamodel.routing_policy.Environment;

public class NamedPrefixSet extends PrefixSetExpr {

  private static final String PROP_NAME = "name";

  /** */
  private static final long serialVersionUID = 1L;

  private String _name;

  @JsonCreator
  private NamedPrefixSet() {}

  public NamedPrefixSet(String name) {
    _name = name;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    NamedPrefixSet other = (NamedPrefixSet) obj;
    if (_name == null) {
      if (other._name != null) {
        return false;
      }
    } else if (!_name.equals(other._name)) {
      return false;
    }
    return true;
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
  public boolean matches(Prefix prefix, Environment environment) {
    RouteFilterList list = environment.getConfiguration().getRouteFilterLists().get(_name);
    if (list != null) {
      return list.permits(prefix);
    } else {
      environment.setError(true);
      return false;
    }
  }

  @JsonProperty(PROP_NAME)
  public void setName(String name) {
    _name = name;
  }
}
