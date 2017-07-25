package org.batfish.datamodel.assertion;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.jayway.jsonpath.Configuration;
import java.util.concurrent.ConcurrentMap;
import org.batfish.common.plugin.IBatfish;

public class AssertionAst {

  private BooleanExpr _expr;

  public AssertionAst(BooleanExpr expr) {
    _expr = expr;
  }

  public boolean execute(
      IBatfish batfish,
      Object jsonObject,
      ConcurrentMap<String, ArrayNode> pathCache,
      Configuration c) {
    Environment env = new Environment(batfish, jsonObject, pathCache, c);
    boolean pass = _expr.evaluate(env);
    return pass;
  }
}
