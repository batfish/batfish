package org.batfish.datamodel.routing_policy.expr;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.ArrayList;
import java.util.List;
import org.batfish.datamodel.routing_policy.Environment;

public class LiteralAsList extends AsPathListExpr {

  /** */
  private static final long serialVersionUID = 1L;

  private List<AsExpr> _list;

  @JsonCreator
  private LiteralAsList() {}

  public LiteralAsList(List<AsExpr> list) {
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
    LiteralAsList other = (LiteralAsList) obj;
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
  public List<Integer> evaluate(Environment environment) {
    List<Integer> list = new ArrayList<>(_list.size());
    for (AsExpr expr : _list) {
      int as = expr.evaluate(environment);
      list.add(as);
    }
    return list;
  }

  public List<AsExpr> getList() {
    return _list;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_list == null) ? 0 : _list.hashCode());
    return result;
  }

  public void setList(List<AsExpr> list) {
    _list = list;
  }
}
