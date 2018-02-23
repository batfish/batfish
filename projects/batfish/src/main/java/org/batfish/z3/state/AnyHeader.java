
package org.batfish.z3.state;
import org.batfish.z3.expr.BasicStateExpr;
import org.batfish.z3.state.visitors.StateExprVisitor;
import org.batfish.z3.state.visitors.StateVisitor;

public class AnyHeader extends BasicStateExpr {

  public static class State extends BasicStateExpr.State {

    public static State INSTANCE = new State();
    
    private State() {}
    
    @Override
    public void accept(StateVisitor visitor) {
      visitor.visitAnyHeader(this);
    }
  }
  
  public static final AnyHeader INSTANCE = new AnyHeader();

  private AnyHeader() {}

  @Override
  public void accept(StateExprVisitor visitor) {
    visitor.visitAnyHeader(this);
  }

  @Override
  public State getState() {
    return State.INSTANCE;
  }
}
