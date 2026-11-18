package org.batfish.datamodel.routing_policy.expr;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.batfish.common.util.BatfishObjectMapper;
import org.junit.jupiter.api.Test;

/** Tests of {@link MatchSourceVrf} */
class MatchSourceVrfTest {
  @Test
  void testJsonSerialization() {
    MatchSourceVrf m = new MatchSourceVrf("vrfName");
    assertThat(BatfishObjectMapper.clone(m, MatchSourceVrf.class), equalTo(m));
  }
}
