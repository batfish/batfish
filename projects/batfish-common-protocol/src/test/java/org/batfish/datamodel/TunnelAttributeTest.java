package org.batfish.datamodel;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Test for {@link TunnelAttribute}. */
public final class TunnelAttributeTest {

  @Test
  public void testJsonSerialization() {
    TunnelAttribute ta = new TunnelAttribute(Ip.parse("1.2.3.4"));
    TunnelAttribute clone = BatfishObjectMapper.clone(ta, TunnelAttribute.class);
    assertEquals(ta, clone);
  }

  @Test
  public void testJavaSerialization() {
    TunnelAttribute ta = new TunnelAttribute(Ip.parse("1.2.3.4"));
    assertThat(SerializationUtils.clone(ta), equalTo(ta));
  }

  @Test
  public void testEquals() {
    TunnelAttribute ta = new TunnelAttribute(Ip.parse("1.2.3.4"));
    new EqualsTester()
        .addEqualityGroup(ta, ta, new TunnelAttribute(Ip.parse("1.2.3.4")))
        .addEqualityGroup(new TunnelAttribute(Ip.parse("5.6.7.8")))
        .testEquals();
  }
}
