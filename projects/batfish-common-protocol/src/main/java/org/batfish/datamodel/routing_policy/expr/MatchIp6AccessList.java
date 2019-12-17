package org.batfish.datamodel.routing_policy.expr;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.Ip6AccessList;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

public final class MatchIp6AccessList extends BooleanExpr {

  private static final String PROP_LIST = "list";

  private final String _list;

  @JsonCreator
  private static MatchIp6AccessList create(@JsonProperty(PROP_LIST) String list) {
    checkArgument(list != null, "%s must be provided", PROP_LIST);
    return new MatchIp6AccessList(list);
  }

  public MatchIp6AccessList(String list) {
    _list = list;
  }

  @Override
  public <T, U> T accept(BooleanExprVisitor<T, U> visitor, U arg) {
    return visitor.visitMatchIp6AccessList(this, arg);
  }

  @Override
  public Result evaluate(Environment environment) {
    Ip6AccessList list = environment.getIp6AccessLists().get(_list);
    if (list != null) {
      // TODO
    } else {
      environment.setError(true);
      return Result.builder().setBooleanValue(false).build();
    }
    throw new BatfishException("No implementation for MatchIp6AccessList.evaluate()");
  }

  public String getList() {
    return _list;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof MatchIp6AccessList)) {
      return false;
    }
    MatchIp6AccessList other = (MatchIp6AccessList) obj;
    return Objects.equals(_list, other._list);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(_list);
  }
}
