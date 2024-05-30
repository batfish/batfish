package org.batfish.datamodel.routing_policy.expr;

import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

public class DiscardNextHopTest {
  @Test
  public void testSerialization() {
    NextHopExpr first = DiscardNextHop.INSTANCE;
    assertThat(SerializationUtils.clone(first), sameInstance(first));
    assertThat(BatfishObjectMapper.clone(first, NextHopExpr.class), sameInstance(first));
  }
}
