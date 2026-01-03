package org.batfish.datamodel.bgp;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Ip;
import org.junit.Test;

/** Test for {@link TunnelEncapsulationAttribute}. */
public final class TunnelEncapsulationAttributeTest {

  @Test
  public void testJsonSerialization() {
    TunnelEncapsulationAttribute ta = new TunnelEncapsulationAttribute(Ip.parse("1.2.3.4"));
    TunnelEncapsulationAttribute clone =
        BatfishObjectMapper.clone(ta, TunnelEncapsulationAttribute.class);
    assertEquals(ta, clone);
  }

  @Test
  public void testJavaSerialization() {
    TunnelEncapsulationAttribute ta = new TunnelEncapsulationAttribute(Ip.parse("1.2.3.4"));
    assertThat(SerializationUtils.clone(ta), equalTo(ta));
  }

  @Test
  public void testEquals() {
    TunnelEncapsulationAttribute ta = new TunnelEncapsulationAttribute(Ip.parse("1.2.3.4"));
    new EqualsTester()
        .addEqualityGroup(ta, ta, new TunnelEncapsulationAttribute(Ip.parse("1.2.3.4")))
        .addEqualityGroup(new TunnelEncapsulationAttribute(Ip.parse("5.6.7.8")))
        .testEquals();
  }
}
