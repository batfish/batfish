package org.batfish.datamodel.routing_policy.expr;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

public final class MatchIpAccessList extends BooleanExpr {

  private static final long serialVersionUID = 1L;
  private static final String PROP_LIST = "list";

  private final String _list;

  @JsonCreator
  private static MatchIpAccessList create(@JsonProperty(PROP_LIST) String list) {
    checkArgument(list != null, "%s must be provided", PROP_LIST);
    return new MatchIpAccessList(list);
  }

  public MatchIpAccessList(String list) {
    _list = list;
  }

  @Override
  public Result evaluate(Environment environment) {
    IpAccessList list = environment.getIpAccessLists().get(_list);
    if (list != null) {
      // TODO
    } else {
      environment.setError(true);
      return new Result(false);
    }
    throw new BatfishException("No implementation for MatchIpAccessList.evaluate()");
  }

  @JsonProperty(PROP_LIST)
  public String getList() {
    return _list;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof MatchIpAccessList)) {
      return false;
    }
    MatchIpAccessList other = (MatchIpAccessList) obj;
    return Objects.equals(_list, other._list);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_list);
  }
}
