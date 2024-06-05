package org.batfish.representation.juniper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

import com.google.common.collect.Iterables;
import org.batfish.common.Warnings;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.BooleanExprs;
import org.batfish.datamodel.routing_policy.expr.Disjunction;
import org.junit.Test;

/** Tests of {@link PsFromAsPathGroup}. */
public class PsFromAsPathGroupTest {

  @Test
  public void testUndefined() {
    Warnings w = new Warnings(true, true, true);
    BooleanExpr ret = PsFromAsPathGroup.toBooleanExpr(null, w);
    assertThat(ret, is(BooleanExprs.FALSE));
    assertThat(w.getRedFlagWarnings(), empty()); // Prefer undefined reference elsewhere
  }

  @Test
  public void testConversionError() {
    AsPathGroup asPathGroup = new AsPathGroup("Group");
    asPathGroup
        .getAsPaths()
        .put("not-aspath", new NamedAsPath("not-aspath", "this is not a valid as-path regex"));

    Warnings w = new Warnings(true, true, true);
    BooleanExpr ret = PsFromAsPathGroup.toBooleanExpr(asPathGroup, w);

    assertThat(ret, equalTo(new Disjunction(BooleanExprs.FALSE)));
    assertThat(w.getRedFlagWarnings(), hasSize(1));
    assertThat(
        Iterables.getOnlyElement(w.getRedFlagWarnings()).getText(),
        containsString("Error converting Juniper as-path-group regex Group"));
  }

  @Test
  public void testConversionErrorWithValidRegex() {
    AsPathGroup asPathGroup = new AsPathGroup("Group");
    asPathGroup.getAsPaths().put("aspath1", new NamedAsPath("aspath1", "1 2"));
    asPathGroup
        .getAsPaths()
        .put("not-aspath", new NamedAsPath("not-aspath", "this is not a valid as-path regex"));

    Warnings w = new Warnings(true, true, true);
    BooleanExpr ret = PsFromAsPathGroup.toBooleanExpr(asPathGroup, w);

    assertThat(ret, instanceOf(Disjunction.class));
    assertThat(w.getRedFlagWarnings(), hasSize(1));
  }

  @Test
  public void testConversion() {
    AsPathGroup asPathGroup = new AsPathGroup("Group");
    asPathGroup.getAsPaths().put("aspath1", new NamedAsPath("aspath1", "1 2"));
    asPathGroup.getAsPaths().put("aspath2", new NamedAsPath("aspath2", "3 4"));

    Warnings w = new Warnings(true, true, true);
    BooleanExpr ret = PsFromAsPathGroup.toBooleanExpr(asPathGroup, w);

    assertThat(ret, instanceOf(Disjunction.class));
    assertThat(w.getRedFlagWarnings(), empty());
  }
}
