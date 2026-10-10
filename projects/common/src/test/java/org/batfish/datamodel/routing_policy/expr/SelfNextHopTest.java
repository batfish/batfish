package org.batfish.datamodel.routing_policy.expr;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.sameInstance;

import com.google.common.collect.ImmutableList;
import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Ip;
import org.junit.Test;

public class SelfNextHopTest {
  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(SelfNextHop.getInstance())
        .addEqualityGroup(new IpNextHop(ImmutableList.of(Ip.ZERO)))
        .testEquals();
  }

  @Test
  public void testSerialization() {
    NextHopExpr first = SelfNextHop.getInstance();
    assertThat(SerializationUtils.clone(first), sameInstance(first));
    assertThat(BatfishObjectMapper.clone(first, NextHopExpr.class), sameInstance(first));
  }
}
