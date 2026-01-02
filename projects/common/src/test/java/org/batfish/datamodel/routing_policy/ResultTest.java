package org.batfish.datamodel.routing_policy;

import com.google.common.testing.EqualsTester;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests of {@link Result} */
@RunWith(JUnit4.class)
public class ResultTest {
  @Test
  public void testEquals() {
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
