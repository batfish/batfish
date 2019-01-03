package org.batfish.z3.expr;

import org.batfish.datamodel.transformation.TransformationStep;
import org.batfish.z3.state.visitors.GenericStateExprVisitor;
import org.batfish.z3.state.visitors.StateVisitor;

/** A {@link StateExpr} for a {@link TransformationStep}. */
public class TransformationStepExpr extends StateExpr {
  public static class State extends StateExpr.State {

    public static final State INSTANCE = new State();

    private State() {}

    @Override
    public void accept(StateVisitor visitor) {
      visitor.visitTransformationStep(this);
    }
  }

  private final String _node;
  private final String _iface;
  private final String _tag;
  private final int _transformationId;
  private final int _id;

  public TransformationStepExpr(
      String node, String iface, String tag, int transformationId, int id) {
    _node = node;
    _iface = iface;
    _tag = tag;
    _transformationId = transformationId;
    _id = id;
  }

  @Override
  public <R> R accept(GenericStateExprVisitor<R> visitor) {
    return visitor.visitTransformationStep(this);
  }

  @Override
  public State getState() {
    return State.INSTANCE;
  }

  public String getNode() {
    return _node;
  }

  public String getIface() {
    return _iface;
  }

  public String getTag() {
    return _tag;
  }

  public int getTransformationId() {
    return _transformationId;
  }

  public int getId() {
    return _id;
  }
}
