package org.batfish.datamodel.routing_policy.expr;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.common.collect.ImmutableList;
import java.util.List;
import org.batfish.datamodel.routing_policy.Environment;

public class MultipliedAs extends AsPathListExpr {

  /** */
  private static final long serialVersionUID = 1L;

  private AsExpr _expr;

  private IntExpr _number;

  @JsonCreator
  private MultipliedAs() {}

  public MultipliedAs(AsExpr expr, IntExpr number) {
    _expr = expr;
    _number = number;
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
    MultipliedAs other = (MultipliedAs) obj;
    if (_expr == null) {
      if (other._expr != null) {
        return false;
      }
    } else if (!_expr.equals(other._expr)) {
      return false;
    }
    if (_number == null) {
      if (other._number != null) {
        return false;
      }
    } else if (!_number.equals(other._number)) {
      return false;
    }
    return true;
  }

  @Override
  public List<Long> evaluate(Environment environment) {
    ImmutableList.Builder<Long> listBuilder = ImmutableList.builder();
    long as = _expr.evaluate(environment);
    int number = _number.evaluate(environment);
    for (int i = 0; i < number; i++) {
      listBuilder.add(as);
    }
    return listBuilder.build();
  }

  public AsExpr getExpr() {
    return _expr;
  }

  public IntExpr getNumber() {
    return _number;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_expr == null) ? 0 : _expr.hashCode());
    result = prime * result + ((_number == null) ? 0 : _number.hashCode());
    return result;
  }

  public void setExpr(AsExpr expr) {
    _expr = expr;
  }

  public void setNumber(IntExpr number) {
    _number = number;
  }
}
