package org.batfish.datamodel.routing_policy.expr;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.common.base.MoreObjects;
import org.batfish.datamodel.routing_policy.Environment;

/** An {@link IntExpr} composed of a named local variable reference to an int. */
public final class VarInt extends IntExpr {

  private String _var;

  @JsonCreator
  private VarInt() {}

  public VarInt(String var) {
    _var = var;
  }

  @Override
  public <T, U> T accept(IntExprVisitor<T, U> visitor, U arg) {
    return visitor.visitVarInt(this, arg);
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
    VarInt other = (VarInt) obj;
    if (_var == null) {
      if (other._var != null) {
        return false;
      }
    } else if (!_var.equals(other._var)) {
      return false;
    }
    return true;
  }

  @Override
  public int evaluate(Environment environment) {
    throw new UnsupportedOperationException("no implementation for generated method");
  }

  public String getVar() {
    return _var;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_var == null) ? 0 : _var.hashCode());
    return result;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("var", _var).toString();
  }

  public void setVar(String var) {
    _var = var;
  }
}
