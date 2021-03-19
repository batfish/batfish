package org.batfish.dataplane.ibdp;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.GenericRibReadOnly;
import org.batfish.datamodel.routing_policy.expr.MainRib;
import org.batfish.dataplane.rib.Rib;
import org.junit.Test;

@ParametersAreNonnullByDefault
public final class RibExprEvaluatorTest {

  @Test
  public void testVisitMainRibRoutesExpr() {
    GenericRibReadOnly<?> mainRib = new Rib();
    assertThat(
        new VirtualRouter.RibExprEvaluator(mainRib).visitMainRib(MainRib.instance(), null),
        is(mainRib));
  }
}
