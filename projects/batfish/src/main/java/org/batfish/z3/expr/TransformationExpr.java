package org.batfish.z3.expr;

import org.batfish.datamodel.transformation.Transformation;
import org.batfish.z3.state.visitors.GenericStateExprVisitor;
import org.batfish.z3.state.visitors.StateVisitor;

/** A {@link StateExpr} for a {@link Transformation} on an edge. */
public class TransformationExpr extends StateExpr {
  public static class State extends StateExpr.State {

    public static final State INSTANCE = new State();

    private State() {}

    @Override
    public void accept(StateVisitor visitor) {
      visitor.visitTransformation(this);
    }
  }

  /** source edge, on which the transformation is defined. */
  private final String _node1;

  private final String _iface1;

  /** destination edge. */
  private final String _node2;

  private final String _iface2;

  private final String _tag;
  private final int _id;

  public TransformationExpr(
      String node1, String iface1, String node2, String iface2, String tag, int id) {
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

  @Override
  public State getState() {
    return State.INSTANCE;
  }

  public String getNode1() {
    return _node1;
  }

  public String getIface1() {
    return _iface1;
  }

  public String getNode2() {
    return _node2;
  }

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
