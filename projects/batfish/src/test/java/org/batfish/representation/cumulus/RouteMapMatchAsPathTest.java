package org.batfish.representation.cumulus;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.batfish.datamodel.AsPathAccessList;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.routing_policy.expr.BooleanExprs;
import org.batfish.datamodel.routing_policy.expr.MatchAsPath;
import org.batfish.datamodel.routing_policy.expr.NamedAsPathSet;
import org.junit.Test;

public class RouteMapMatchAsPathTest {

  @Test
  public void testToBooleanExpr() {
    String name = "name";
    RouteMapMatchAsPath matchLine = new RouteMapMatchAsPath(name);
    Configuration c = new Configuration("hostname", ConfigurationFormat.CUMULUS_CONCATENATED);

    // Configuration has no matching AS path: toBooleanExpr() should return a false expr
    assertThat(matchLine.toBooleanExpr(c, null, null), equalTo(BooleanExprs.FALSE));

    // Adding the AS path should make it create a MatchAsPath expr instead
    c.getAsPathAccessLists().put(name, new AsPathAccessList(name, null));
    assertThat(
        matchLine.toBooleanExpr(c, null, null), equalTo(new MatchAsPath(new NamedAsPathSet(name))));
  }
}
