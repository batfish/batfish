package org.batfish.datamodel.routing_policy.expr;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

public class MatchIpAccessList extends BooleanExpr {

  /** */
  private static final long serialVersionUID = 1L;

  private String _list;

  @JsonCreator
  private MatchIpAccessList() {}

  public MatchIpAccessList(String list) {
    _list = list;
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
    MatchIpAccessList other = (MatchIpAccessList) obj;
    if (_list == null) {
      if (other._list != null) {
        return false;
      }
    } else if (!_list.equals(other._list)) {
      return false;
    }
    return true;
  }

  @Override
  public Result evaluate(Environment environment) {
    Result result = new Result();
    IpAccessList list = environment.getConfiguration().getIpAccessLists().get(_list);
    if (list != null) {
      // TODO
    } else {
      environment.setError(true);
      result.setBooleanValue(false);
      return result;
    }
    throw new UnsupportedOperationException("no implementation for generated method");
  }

  public String getList() {
    return _list;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_list == null) ? 0 : _list.hashCode());
    return result;
  }

  public void setList(String list) {
    _list = list;
  }
}
