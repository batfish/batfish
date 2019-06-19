package org.batfish.datamodel.routing_policy.expr;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Tests of {@link MatchSourceVrf} */
public class MatchSourceVrfTest {
  @Test
  public void testJsonSerialization() throws IOException {
    MatchSourceVrf m = new MatchSourceVrf("vrfName");
    assertThat(BatfishObjectMapper.clone(m, MatchSourceVrf.class), equalTo(m));
  }
}
