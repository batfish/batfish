package org.batfish.z3.node;

public class NumberedQueryExpr extends PacketRelExpr {

  public static final String NAME = "query_relation";

  public NumberedQueryExpr(int number) {
    super(NAME + "_" + number);
  }
}
