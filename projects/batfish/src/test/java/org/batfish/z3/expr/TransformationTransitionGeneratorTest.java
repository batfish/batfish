package org.batfish.z3.expr;

import static org.batfish.datamodel.acl.AclLineMatchExprs.matchDst;
import static org.batfish.datamodel.flow.TransformationStep.TransformationType.SOURCE_NAT;
import static org.batfish.datamodel.transformation.Transformation.always;
import static org.batfish.datamodel.transformation.Transformation.when;
import static org.batfish.datamodel.transformation.TransformationStep.assignDestinationIp;
import static org.batfish.datamodel.transformation.TransformationStep.shiftDestinationIp;
import static org.batfish.datamodel.transformation.TransformationStep.shiftSourceIp;
import static org.batfish.z3.expr.ExtractExpr.newExtractExpr;
import static org.batfish.z3.expr.HeaderSpaceMatchExpr.matchIp;
import static org.batfish.z3.expr.TransformationTransitionGenerator.assignFromPoolExpr;
import static org.batfish.z3.expr.TransformationTransitionGenerator.shiftIntoSubnetExpr;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;
import java.util.List;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.transformation.Noop;
import org.batfish.datamodel.transformation.Transformation;
import org.batfish.z3.AclLineMatchExprToBooleanExpr;
import org.batfish.z3.Field;
import org.batfish.z3.state.Debug;
import org.batfish.z3.state.Query;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/** Tests for {@link TransformationTransitionGenerator}. */
public class TransformationTransitionGeneratorTest {

  /** */
  @Rule public ExpectedException _exception = ExpectedException.none();

  private static final String NODE1 = "node1";
  private static final String IFACE1 = "iface1";
  private static final String NODE2 = "node2";
  private static final String IFACE2 = "iface2";
  private static final String TAG = "tag";
  private static final AclLineMatchExprToBooleanExpr TO_BOOLEAN_EXPR =
      new AclLineMatchExprToBooleanExpr(
          ImmutableMap.of(),
          ImmutableMap.of(),
          new Field("SOURCE_INTERFACE", 0),
          ImmutableMap.of());
  private static final StateExpr PRE_STATE = Debug.INSTANCE;
  private static final StateExpr POST_STATE = Query.INSTANCE;

  private static List<BasicRuleStatement> generateTransitions(Transformation transformation) {
    return TransformationTransitionGenerator.generateTransitions(
        NODE1, IFACE1, NODE2, IFACE2, TAG, TO_BOOLEAN_EXPR, PRE_STATE, POST_STATE, transformation);
  }

  private static TransformationExpr stateExpr(int id) {
    return new TransformationExpr(NODE1, IFACE1, NODE2, IFACE2, TAG, id);
  }

  private static TransformationStepExpr stepStateExpr(int transformationId, int stepId) {
    return new TransformationStepExpr(NODE1, IFACE1, NODE2, IFACE2, TAG, transformationId, stepId);
  }

  @Test
  public void testSimple() {
    Transformation transformation = always().apply(new Noop(SOURCE_NAT)).build();
    List<BasicRuleStatement> stmts = generateTransitions(transformation);
    assertThat(
        stmts,
        containsInAnyOrder(
            new BasicRuleStatement(PRE_STATE, stateExpr(0)),
            new BasicRuleStatement(TrueExpr.INSTANCE, stateExpr(0), stepStateExpr(0, 0)),
            new BasicRuleStatement(stepStateExpr(0, 0), POST_STATE),
            new BasicRuleStatement(FalseExpr.INSTANCE, stateExpr(0), POST_STATE)));
  }

  @Test
  public void testGuard() {
    Ip ip = Ip.parse("1.2.3.4");
    Transformation transformation = when(matchDst(ip)).apply(new Noop(SOURCE_NAT)).build();
    List<BasicRuleStatement> stmts = generateTransitions(transformation);
    assertThat(
        stmts,
        containsInAnyOrder(
            new BasicRuleStatement(PRE_STATE, stateExpr(0)),
            new BasicRuleStatement(matchIp(ip, Field.DST_IP), stateExpr(0), stepStateExpr(0, 0)),
            new BasicRuleStatement(stepStateExpr(0, 0), POST_STATE),
            new BasicRuleStatement(
                new NotExpr(matchIp(ip, Field.DST_IP)), stateExpr(0), POST_STATE)));
  }

  @Test
  public void testShiftDestIpStep() {
    Prefix prefix = Prefix.parse("1.1.0.0/16");
    Transformation transformation = always().apply(shiftDestinationIp(prefix)).build();
    List<BasicRuleStatement> stmts = generateTransitions(transformation);
    assertThat(
        stmts,
        containsInAnyOrder(
            new BasicRuleStatement(PRE_STATE, stateExpr(0)),
            new BasicRuleStatement(TrueExpr.INSTANCE, stateExpr(0), stepStateExpr(0, 0)),
            new BasicRuleStatement(
                shiftIntoSubnetExpr(Field.DST_IP, prefix), stepStateExpr(0, 0), POST_STATE),
            new BasicRuleStatement(FalseExpr.INSTANCE, stateExpr(0), POST_STATE)));
  }

  @Test
  public void testAssignDestIpFromPoolStep() {
    Ip startIp = Ip.parse("1.1.1.0");
    Ip endIp = Ip.parse("1.1.1.10");
    Transformation transformation = always().apply(assignDestinationIp(startIp, endIp)).build();
    List<BasicRuleStatement> stmts = generateTransitions(transformation);
    assertThat(
        stmts,
        containsInAnyOrder(
            new BasicRuleStatement(PRE_STATE, stateExpr(0)),
            new BasicRuleStatement(TrueExpr.INSTANCE, stateExpr(0), stepStateExpr(0, 0)),
            new BasicRuleStatement(
                assignFromPoolExpr(Field.DST_IP, startIp, endIp), stepStateExpr(0, 0), POST_STATE),
            new BasicRuleStatement(FalseExpr.INSTANCE, stateExpr(0), POST_STATE)));
  }

  @Test
  public void testTwoSteps() {
    Prefix prefix = Prefix.parse("1.1.0.0/16");
    Ip startIp = Ip.parse("1.1.1.0");
    Ip endIp = Ip.parse("1.1.1.10");
    Transformation transformation =
        always().apply(shiftSourceIp(prefix), assignDestinationIp(startIp, endIp)).build();
    List<BasicRuleStatement> stmts = generateTransitions(transformation);
    assertThat(
        stmts,
        containsInAnyOrder(
            new BasicRuleStatement(PRE_STATE, stateExpr(0)),
            new BasicRuleStatement(stateExpr(0), stepStateExpr(0, 0)),
            new BasicRuleStatement(
                shiftIntoSubnetExpr(Field.SRC_IP, prefix),
                stepStateExpr(0, 0),
                stepStateExpr(0, 1)),
            new BasicRuleStatement(
                assignFromPoolExpr(Field.DST_IP, startIp, endIp), stepStateExpr(0, 1), POST_STATE),
            new BasicRuleStatement(FalseExpr.INSTANCE, stateExpr(0), POST_STATE)));
  }

  @Test
  public void testBranch() {
    Ip match1 = Ip.parse("1.1.1.1");
    Ip match2 = Ip.parse("1.1.1.2");
    Ip match3 = Ip.parse("1.1.1.3");
    Noop noop = new Noop(SOURCE_NAT);
    Transformation transformation =
        when(matchDst(match1))
            .apply(noop)
            .setAndThen(when(matchDst(match2)).apply(noop).build())
            .setOrElse(when(matchDst(match3)).apply(noop).build())
            .build();
    List<BasicRuleStatement> stmts = generateTransitions(transformation);
    assertThat(
        stmts,
        containsInAnyOrder(
            // entry
            new BasicRuleStatement(PRE_STATE, stateExpr(0)),
            // root
            new BasicRuleStatement(
                matchIp(match1, Field.DST_IP), stateExpr(0), stepStateExpr(0, 0)),
            new BasicRuleStatement(stepStateExpr(0, 0), stateExpr(1)),
            new BasicRuleStatement(
                new NotExpr(matchIp(match1, Field.DST_IP)), stateExpr(0), stateExpr(2)),
            // true branch
            new BasicRuleStatement(
                matchIp(match2, Field.DST_IP), stateExpr(1), stepStateExpr(1, 0)),
            new BasicRuleStatement(stepStateExpr(1, 0), POST_STATE),
            new BasicRuleStatement(
                new NotExpr(matchIp(match2, Field.DST_IP)), stateExpr(1), POST_STATE),
            // else branch
            new BasicRuleStatement(
                matchIp(match3, Field.DST_IP), stateExpr(2), stepStateExpr(2, 0)),
            new BasicRuleStatement(stepStateExpr(2, 0), POST_STATE),
            new BasicRuleStatement(
                new NotExpr(matchIp(match3, Field.DST_IP)), stateExpr(2), POST_STATE)));
  }

  @Test
  public void testShiftIntoSubnetExpr() {
    Prefix subnet = Prefix.parse("1.1.1.0/24");
    TransformedVarIntExpr transformedDst = new TransformedVarIntExpr(Field.DST_IP);

    // the high bits are transformed
    BooleanExpr transformedExpr =
        new EqExpr(
            newExtractExpr(transformedDst, 8, 31),
            new LitIntExpr(subnet.getStartIp().asLong(), 8, 31));

    // the low bits are preserved
    BooleanExpr preservedExpr =
        new EqExpr(newExtractExpr(transformedDst, 0, 7), newExtractExpr(Field.DST_IP, 0, 7));

    assertThat(
        shiftIntoSubnetExpr(Field.DST_IP, subnet),
        equalTo(new AndExpr(ImmutableList.of(transformedExpr, preservedExpr))));
  }

  @Test
  public void testShiftIntoSubnetExpr_32() {
    _exception.expect(IllegalArgumentException.class);
    shiftIntoSubnetExpr(Field.DST_IP, Prefix.parse("1.1.1.1/32"));
  }

  @Test
  public void testAssignFromPoolExpr() {
    Ip poolStart = Ip.parse("1.1.1.0");
    Ip poolEnd = Ip.parse("1.1.1.7");
    assertThat(
        assignFromPoolExpr(Field.DST_IP, poolStart, poolEnd),
        equalTo(
            new RangeMatchExpr(
                new TransformedVarIntExpr(Field.DST_IP),
                Field.DST_IP.getSize(),
                ImmutableSet.of(Range.closed(poolStart.asLong(), poolEnd.asLong())))));
  }
}
