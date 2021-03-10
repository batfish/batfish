package org.batfish.representation.fortios;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.Test;

public class BatfishUUIDTest {
  @Test
  public void testJavaSerialization() {
    BatfishUUID uuid = new BatfishUUID();
    assertThat(SerializationUtils.clone(uuid), equalTo(uuid));
  }

  @Test
  public void testEquals() {
    BatfishUUID uuid1 = new BatfishUUID();
    BatfishUUID uuid2 = new BatfishUUID();
    new EqualsTester()
        .addEqualityGroup(uuid1)
        .addEqualityGroup(uuid2)
        .addEqualityGroup(new Object())
        .testEquals();
  }
}
