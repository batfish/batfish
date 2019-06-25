package org.batfish.datamodel.routing_policy.expr;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.PrefixSpace;
import org.batfish.datamodel.routing_policy.Environment;

public class ExplicitPrefixSet extends PrefixSetExpr {
  private static final String PROP_PREFIX_SPACE = "prefixSpace";

  private PrefixSpace _prefixSpace;

  @JsonCreator
  private ExplicitPrefixSet() {}

  public ExplicitPrefixSet(PrefixSpace prefixSpace) {
    _prefixSpace = prefixSpace;
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
    ExplicitPrefixSet other = (ExplicitPrefixSet) obj;
    if (_prefixSpace == null) {
      if (other._prefixSpace != null) {
        return false;
      }
    } else if (!_prefixSpace.equals(other._prefixSpace)) {
      return false;
    }
    return true;
  }

  @JsonProperty(PROP_PREFIX_SPACE)
  public PrefixSpace getPrefixSpace() {
    return _prefixSpace;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_prefixSpace == null) ? 0 : _prefixSpace.hashCode());
    return result;
  }

  @Override
  public boolean matches(Prefix prefix, Environment environment) {
    boolean value = _prefixSpace.containsPrefix(prefix);
    return value;
  }

  @JsonProperty(PROP_PREFIX_SPACE)
  public void setPrefixSpace(PrefixSpace prefixSpace) {
    _prefixSpace = prefixSpace;
  }
}
