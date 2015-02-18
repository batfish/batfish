package org.batfish.z3.node;

import java.util.List;

public interface ComplexExpr {

   List<Expr> getSubExpressions();

   // void optimize();

}
