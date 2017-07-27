package org.batfish.datamodel.assertion;

import com.fasterxml.jackson.databind.node.ArrayNode;

public class PathSize implements ComparableExpr {

  private StringExpr _pathExpr;

  public PathSize(StringExpr pathExpr) {
    _pathExpr = pathExpr;
  }

  @Override
  public Comparable<?> evaluate(Environment env) {
    String path = _pathExpr.evaluate(env);
    ArrayNode pathResult = env.computePath(path);
    return pathResult.size();
  }
}
