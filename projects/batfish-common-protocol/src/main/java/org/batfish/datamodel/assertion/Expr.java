package org.batfish.datamodel.assertion;

public interface Expr {

  Object evaluate(Environment env);
}
