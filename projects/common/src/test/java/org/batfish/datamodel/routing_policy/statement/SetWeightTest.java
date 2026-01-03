package org.batfish.datamodel.routing_policy.statement;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.sameInstance;

import java.util.List;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.routing_policy.expr.LiteralInt;
import org.junit.Test;

public class SetWeightTest {
  @Test
  public void testSerialization() {
    Statement sw = new SetWeight(new LiteralInt(6));
    assertThat(BatfishObjectMapper.clone(sw, Statement.class), equalTo(sw));
    assertThat(SerializationUtils.clone(sw), equalTo(sw));
  }

  @Test
  public void testSimplifyIsNoop() {
    Statement sw = new SetWeight(new LiteralInt(6));
    List<Statement> simplified = sw.simplify();
    assertThat(simplified, hasSize(1));
    assertThat(simplified.get(0), sameInstance(sw));
    assertThat(sw.simplify(), sameInstance(simplified));
  }
}
