package org.batfish.datamodel.bgp;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import java.io.IOException;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Tests of {@link Ipv4UnicastAddressFamily} */
public class Ipv4UnicastAddressFamilyTest {
  @Test
  public void testEquals() {
    Ipv4UnicastAddressFamily af = Ipv4UnicastAddressFamily.instance();
    new EqualsTester()
        .addEqualityGroup(af, Ipv4UnicastAddressFamily.instance())
        .addEqualityGroup(new Object())
        .testEquals();
  }

  @Test
  public void testJavaSerialization() {
    Ipv4UnicastAddressFamily af = Ipv4UnicastAddressFamily.instance();
    assertThat(SerializationUtils.clone(af), equalTo(af));
  }

  @Test
  public void testJsonSerialization() throws IOException {
    Ipv4UnicastAddressFamily af = Ipv4UnicastAddressFamily.instance();
    assertThat(BatfishObjectMapper.clone(af, Ipv4UnicastAddressFamily.class), equalTo(af));
  }
}
