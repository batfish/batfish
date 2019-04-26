package org.batfish.z3.expr;

import javax.annotation.Nullable;
import org.batfish.datamodel.transformation.Transformation;
import org.batfish.z3.state.visitors.GenericStateExprVisitor;

/**
 * A {@link StateExpr} for a {@link Transformation} on an edge or interface. Outgoing
 * transformations are parameterized by an edge and incoming transformations are parameterized by an
 * interface.
 */
public class TransformationExpr extends StateExpr {
  public static class State {

    private State() {}
  }

  private final String _node1;

  private final String _iface1;

  /** For egress transformations, the second node in the edge. Null for ingress transformations. */
  @Nullable private final String _node2;

  /**
   * For egress transformations, the second interface in the edge. Null for ingress transformations.
   */
  @Nullable private final String _iface2;

  private final String _tag;
  private final int _id;

  public TransformationExpr(
      String node1,
      String iface1,
      @Nullable String node2,
      @Nullable String iface2,
      String tag,
      int id) {
    _node1 = node1;
    _iface1 = iface1;
    _node2 = node2;
    _iface2 = iface2;
    _tag = tag;
    _id = id;
  }

  @Override
  public <R> R accept(GenericStateExprVisitor<R> visitor) {
    return visitor.visitTransformation(this);
  }

  public String getNode1() {
    return _node1;
  }

  public String getIface1() {
    return _iface1;
  }

  @Nullable
  public String getNode2() {
    return _node2;
  }

  @Nullable
  public String getIface2() {
    return _iface2;
  }

  public String getTag() {
    return _tag;
  }

  public int getId() {
    return _id;
  }
}
