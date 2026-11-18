package org.batfish.datamodel.routing_policy;

import com.google.common.testing.EqualsTester;
import org.junit.jupiter.api.Test;

/** Tests of {@link Result} */
class ResultTest {
  @Test
  void testEquals() {
    new EqualsTester()
        .addEqualityGroup(new Result(), new Result())
        .addEqualityGroup(new Result(true, false, false, false))
        .addEqualityGroup(new Result(false, true, false, false))
        .addEqualityGroup(new Result(false, false, true, false))
        .addEqualityGroup(new Result(false, false, false, true))
        .addEqualityGroup(new Object())
        .testEquals();
  }
}
