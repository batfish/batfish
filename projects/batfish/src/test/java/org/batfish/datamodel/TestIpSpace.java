package org.batfish.datamodel;

import org.batfish.datamodel.visitors.GenericIpSpaceVisitor;
import org.batfish.z3.expr.TestBooleanAtom;
import org.batfish.z3.expr.visitors.IpSpaceBooleanExprTransformer;

public class TestIpSpace implements IpSpace {

  private final int _num;

  public TestIpSpace(int num) {
    _num = num;
  }

  @Override
  public <R> R accept(GenericIpSpaceVisitor<R> visitor) {
    if (visitor instanceof IpSpaceBooleanExprTransformer) {
      return visitor.castToGenericIpSpaceVisitorReturnType(new TestBooleanAtom(_num));
    } else {
      throw new UnsupportedOperationException(
          String.format(
              "No implementation for %s: %s",
              GenericIpSpaceVisitor.class.getSimpleName(), visitor.getClass().getSimpleName()));
    }
  }

  @Override
  public boolean contains(Ip ip) {
    throw new UnsupportedOperationException(
        "no implementation for generated method"); // TODO Auto-generated method stub
  }
}
