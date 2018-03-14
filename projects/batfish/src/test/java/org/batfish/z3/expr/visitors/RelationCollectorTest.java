package org.batfish.z3.expr.visitors;

import static org.batfish.z3.expr.visitors.BoolExprTransformer.getNodName;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.Set;
import org.batfish.z3.SynthesizerInput;
import org.batfish.z3.TestSynthesizerInput;
import org.batfish.z3.expr.BasicRuleStatement;
import org.batfish.z3.expr.QueryStatement;
import org.batfish.z3.expr.StateExpr;
import org.batfish.z3.expr.Statement;
import org.batfish.z3.expr.TransformationRuleStatement;
import org.batfish.z3.expr.TrueExpr;
import org.batfish.z3.state.StateParameter;
import org.batfish.z3.state.StateParameter.Type;
import org.batfish.z3.state.visitors.GenericStateExprVisitor;
import org.batfish.z3.state.visitors.Parameterizer;
import org.batfish.z3.state.visitors.StateVisitor;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

public class RelationCollectorTest {

  public static class TestStateExpr extends StateExpr {

    public static class State extends StateExpr.State {

      public static final State INSTANCE = new State();

      private State() {}

      @Override
      public void accept(StateVisitor visitor) {
        throw new UnsupportedOperationException(
            String.format(
                "Unsupported %s: %s",
                StateVisitor.class.getName(), visitor.getClass().getCanonicalName()));
      }
    }

    private final int _number;

    public TestStateExpr(int number) {
      _number = number;
    }

    @Override
    public <R> R accept(GenericStateExprVisitor<R> visitor) {
      if (visitor instanceof Parameterizer) {
        return visitor.castToGenericStateExprVisitorReturnType(
            ImmutableList.of(new StateParameter(Integer.toString(_number), Type.QUERY_NUMBER)));
      }
      throw new UnsupportedOperationException(
          String.format(
              "Unsupported %s: %s",
              GenericStateExprVisitor.class.getName(), visitor.getClass().getCanonicalName()));
    }

    @Override
    public State getState() {
      return State.INSTANCE;
    }
  }

  private static Set<String> collectRelations(SynthesizerInput input, Statement statement) {
    return RelationCollector.collectRelations(input, statement).keySet();
  }

  private int _atomCounter;

  private SynthesizerInput _input;

  private StateExpr newStateExpr() {
    return new TestStateExpr(_atomCounter++);
  }

  @Before
  public void setup() {
    _input = TestSynthesizerInput.builder().build();
  }

  @Test
  public void testTestStateExpr() {
    StateExpr b1 = newStateExpr();
    StateExpr b2 = newStateExpr();
    assertThat(b1, Matchers.not(equalTo(b2)));
  }

  /** Test that collectRelations traverses the child of a BasicRuleStatement. */
  @Test
  public void testVisitBasicRuleStatement() {
    StateExpr p1 = newStateExpr();
    StateExpr p2 = newStateExpr();
    BasicRuleStatement expr1 = new BasicRuleStatement(p1);
    Set<String> expectedRelations1 = ImmutableSet.of(getNodName(_input, p1));
    BasicRuleStatement expr2 = new BasicRuleStatement(p1, p2);
    Set<String> expectedRelations2 =
        ImmutableSet.of(getNodName(_input, p1), getNodName(_input, p2));

    assertThat(collectRelations(_input, expr1), equalTo(expectedRelations1));
    assertThat(collectRelations(_input, expr2), equalTo(expectedRelations2));
  }

  /** Test that collectRelations traverses the child of a QueryStatement. */
  @Test
  public void testVisitQueryStatement() {
    StateExpr p1 = newStateExpr();
    QueryStatement expr = new QueryStatement(p1);
    Set<String> expectedRelations = ImmutableSet.of(getNodName(_input, p1));

    assertThat(collectRelations(_input, expr), equalTo(expectedRelations));
  }

  /** Test that collectRelations traverses the child of a TransformationRuleStatement. */
  @Test
  public void testVisitTransformationRuleStatement() {
    StateExpr b1 = newStateExpr();
    StateExpr b2 = newStateExpr();
    StateExpr t1 = newStateExpr();
    TransformationRuleStatement expr1 = new TransformationRuleStatement(t1);
    Set<String> expectedRelations1 = ImmutableSet.of(getNodName(_input, t1));
    TransformationRuleStatement expr2 =
        new TransformationRuleStatement(
            TrueExpr.INSTANCE, ImmutableSet.of(b1), ImmutableSet.of(b2), t1);
    Set<String> expectedRelations2 =
        ImmutableSet.of(getNodName(_input, b1), getNodName(_input, b2), getNodName(_input, t1));

    assertThat(collectRelations(_input, expr1), equalTo(expectedRelations1));
    assertThat(collectRelations(_input, expr2), equalTo(expectedRelations2));
  }
}
