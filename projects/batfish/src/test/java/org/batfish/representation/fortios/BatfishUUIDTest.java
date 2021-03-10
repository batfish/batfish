package org.batfish.representation.fortios;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.Test;

public class BatfishUUIDTest {
  @Test
  public void testJavaSerialization() {
    BatfishUUID uuid = new BatfishUUID(1);
    assertThat(SerializationUtils.clone(uuid), equalTo(uuid));
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(new BatfishUUID(1), new BatfishUUID(1))
        .addEqualityGroup(new BatfishUUID(2))
        .addEqualityGroup(new Object())
        .testEquals();
  }
}
