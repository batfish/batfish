package org.batfish.datamodel.acl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import org.batfish.datamodel.FilterResult;
import org.batfish.datamodel.LineAction;

public final class DefaultDeniedByIpAccessList implements TerminalTraceEvent {

  private static final String PROP_NAME = "name";

  private static final long serialVersionUID = 1L;

  private final String _name;

  @JsonCreator
  public DefaultDeniedByIpAccessList(@JsonProperty(PROP_NAME) @Nonnull String name) {
    _name = name;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof DefaultDeniedByIpAccessList)) {
      return false;
    }
    DefaultDeniedByIpAccessList rhs = (DefaultDeniedByIpAccessList) obj;
    return _name.equals(rhs._name);
  }

  @JsonProperty(PROP_NAME)
  public @Nonnull String getName() {
    return _name;
  }

  @Override
  public int hashCode() {
    return _name.hashCode();
  }

  @Override
  public FilterResult toFilterResult() {
    return new FilterResult(null, LineAction.REJECT);
  }

  @Override
  public String toString() {
    return String.format("Did not match ACL '%s'", _name);
  }
}
