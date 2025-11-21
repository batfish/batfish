package org.batfish.representation.juniper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.Test;

public class PsThenAigpOriginateTest {

  @Test
  public void testSerialization() {
    PsThenAigpOriginate objWithNull = new PsThenAigpOriginate(null);
    assertThat(SerializationUtils.clone(objWithNull), equalTo(objWithNull));

    PsThenAigpOriginate objWithDistance = new PsThenAigpOriginate(100L);
    assertThat(SerializationUtils.clone(objWithDistance), equalTo(objWithDistance));
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(5)
        .addEqualityGroup(new PsThenAigpOriginate(null), new PsThenAigpOriginate(null))
        .addEqualityGroup(new PsThenAigpOriginate(100L), new PsThenAigpOriginate(100L))
        .addEqualityGroup(new PsThenAigpOriginate(200L))
        .testEquals();
  }
}
