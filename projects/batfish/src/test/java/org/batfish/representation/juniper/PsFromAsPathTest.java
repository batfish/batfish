package org.batfish.representation.juniper;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import com.google.common.collect.Iterables;
import org.batfish.common.Warnings;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.BooleanExprs;
import org.batfish.datamodel.routing_policy.expr.MatchAsPath;
import org.junit.Test;

/** Tests of {@link PsFromAsPath}. */
public class PsFromAsPathTest {
  @Test
  public void testUndefined() {
    Warnings w = new Warnings(true, true, true);
    BooleanExpr ret = PsFromAsPath.toBooleanExpr(null, w);
    assertThat(ret, is(BooleanExprs.FALSE));
    assertThat(w.getRedFlagWarnings(), empty()); // Prefer undefined reference elsewhere
  }

  @Test
  public void testConversionError() {
    AsPath unsupportedRegex = new AsPath("this is not a valid as-path regex");
    Warnings w = new Warnings(true, true, true);
    BooleanExpr ret = PsFromAsPath.toBooleanExpr(unsupportedRegex, w);

    assertThat(ret, is(BooleanExprs.FALSE));
    assertThat(w.getRedFlagWarnings(), hasSize(1));
    assertThat(
        Iterables.getOnlyElement(w.getRedFlagWarnings()).getText(),
        containsString("Error converting Juniper as-path regex"));
  }

  @Test
  public void testConversion() {
    AsPath regex = new AsPath("1 2");
    Warnings w = new Warnings(true, true, true);
    BooleanExpr ret = PsFromAsPath.toBooleanExpr(regex, w);

    assertThat(ret, instanceOf(MatchAsPath.class));
    assertThat(w.getRedFlagWarnings(), empty());
  }
}
