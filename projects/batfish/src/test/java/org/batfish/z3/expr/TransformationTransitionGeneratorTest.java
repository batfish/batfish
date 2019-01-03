package org.batfish.z3.expr;

import static org.batfish.datamodel.acl.AclLineMatchExprs.matchDst;
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
import org.batfish.datamodel.transformation.Transformation;
import org.batfish.z3.AclLineMatchExprToBooleanExpr;
import org.batfish.z3.Field;
import org.batfish.z3.state.Query;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/** Tests for {@link TransformationTransitionGenerator}. */
public class TransformationTransitionGeneratorTest {

  /** */
  @Rule public ExpectedException _exception = ExpectedException.none();

  private static final String NODE = "node";
  private static final String IFACE = "iface";
  private static final String TAG = "tag";
  private static final AclLineMatchExprToBooleanExpr TO_BOOLEAN_EXPR =
      new AclLineMatchExprToBooleanExpr(
          ImmutableMap.of(),
          ImmutableMap.of(),
          new Field("SOURCE_INTERFACE", 0),
          ImmutableMap.of());
  private static final StateExpr OUT_STATE = Query.INSTANCE;

  private static List<BasicRuleStatement> generateTransitions(Transformation transformation) {
    return TransformationTransitionGenerator.generateTransitions(
        NODE, IFACE, TAG, TO_BOOLEAN_EXPR, OUT_STATE, transformation);
  }

  private static TransformationExpr stateExpr(int id) {
    return new TransformationExpr(NODE, IFACE, TAG, id);
  }

  private static TransformationStepExpr stepStateExpr(int transformationId, int stepId) {
    return new TransformationStepExpr(NODE, IFACE, TAG, transformationId, stepId);
  }

  @Test
  public void testSimple() {
    Transformation transformation = Transformation.always().build();
    List<BasicRuleStatement> stmts = generateTransitions(transformation);
    assertThat(
        stmts,
        containsInAnyOrder(
            new BasicRuleStatement(TrueExpr.INSTANCE, stateExpr(0), OUT_STATE),
            new BasicRuleStatement(FalseExpr.INSTANCE, stateExpr(0), OUT_STATE)));
  }

  @Test
  public void testGuard() {
    Ip ip = Ip.parse("1.2.3.4");
    Transformation transformation = Transformation.when(matchDst(ip)).build();
    List<BasicRuleStatement> stmts = generateTransitions(transformation);
    assertThat(
        stmts,
        containsInAnyOrder(
            new BasicRuleStatement(matchIp(ip, Field.DST_IP), stateExpr(0), OUT_STATE),
            new BasicRuleStatement(
                new NotExpr(matchIp(ip, Field.DST_IP)), stateExpr(0), OUT_STATE)));
  }

  @Test
  public void testShiftDestIpStep() {
    Prefix prefix = Prefix.parse("1.1.0.0/16");
    Transformation transformation =
        Transformation.always().apply(shiftDestinationIp(prefix)).build();
    List<BasicRuleStatement> stmts = generateTransitions(transformation);
    assertThat(
        stmts,
        containsInAnyOrder(
            new BasicRuleStatement(TrueExpr.INSTANCE, stateExpr(0), stepStateExpr(0, 0)),
            new BasicRuleStatement(
                shiftIntoSubnetExpr(Field.DST_IP, prefix), stepStateExpr(0, 0), OUT_STATE),
            new BasicRuleStatement(FalseExpr.INSTANCE, stateExpr(0), OUT_STATE)));
  }

  @Test
  public void testAssignDestIpFromPoolStep() {
    Ip startIp = Ip.parse("1.1.1.0");
    Ip endIp = Ip.parse("1.1.1.10");
    Transformation transformation =
        Transformation.always().apply(assignDestinationIp(startIp, endIp)).build();
    List<BasicRuleStatement> stmts = generateTransitions(transformation);
    assertThat(
        stmts,
        containsInAnyOrder(
            new BasicRuleStatement(TrueExpr.INSTANCE, stateExpr(0), stepStateExpr(0, 0)),
            new BasicRuleStatement(
                assignFromPoolExpr(Field.DST_IP, startIp, endIp), stepStateExpr(0, 0), OUT_STATE),
            new BasicRuleStatement(FalseExpr.INSTANCE, stateExpr(0), OUT_STATE)));
  }

  @Test
  public void testTwoSteps() {
    Prefix prefix = Prefix.parse("1.1.0.0/16");
    Ip startIp = Ip.parse("1.1.1.0");
    Ip endIp = Ip.parse("1.1.1.10");
    Transformation transformation =
        Transformation.always()
            .apply(shiftSourceIp(prefix), assignDestinationIp(startIp, endIp))
            .build();
    List<BasicRuleStatement> stmts = generateTransitions(transformation);
    assertThat(
        stmts,
        containsInAnyOrder(
            new BasicRuleStatement(TrueExpr.INSTANCE, stateExpr(0), stepStateExpr(0, 0)),
            new BasicRuleStatement(
                shiftIntoSubnetExpr(Field.SRC_IP, prefix),
                stepStateExpr(0, 0),
                stepStateExpr(0, 1)),
            new BasicRuleStatement(
                assignFromPoolExpr(Field.DST_IP, startIp, endIp), stepStateExpr(0, 1), OUT_STATE),
            new BasicRuleStatement(FalseExpr.INSTANCE, stateExpr(0), OUT_STATE)));
  }

  @Test
  public void testBranch() {
    Ip match1 = Ip.parse("1.1.1.1");
    Ip match2 = Ip.parse("1.1.1.2");
    Ip match3 = Ip.parse("1.1.1.3");
    Transformation transformation =
        Transformation.when(matchDst(match1))
            .setAndThen(Transformation.when(matchDst(match2)).build())
            .setOrElse(Transformation.when(matchDst(match3)).build())
            .build();
    List<BasicRuleStatement> stmts = generateTransitions(transformation);
    assertThat(
        stmts,
        containsInAnyOrder(
            // root
            new BasicRuleStatement(matchIp(match1, Field.DST_IP), stateExpr(0), stateExpr(1)),
            new BasicRuleStatement(
                new NotExpr(matchIp(match1, Field.DST_IP)), stateExpr(0), stateExpr(2)),
            // true branch
            new BasicRuleStatement(matchIp(match2, Field.DST_IP), stateExpr(1), OUT_STATE),
            new BasicRuleStatement(
                new NotExpr(matchIp(match2, Field.DST_IP)), stateExpr(1), OUT_STATE),
            // else branch
            new BasicRuleStatement(matchIp(match3, Field.DST_IP), stateExpr(2), OUT_STATE),
            new BasicRuleStatement(
                new NotExpr(matchIp(match3, Field.DST_IP)), stateExpr(2), OUT_STATE)));
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
