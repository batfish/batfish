package org.batfish.datamodel.packet_policy;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Tests of {@link IngressInterfaceVrf} */
public class IngressInterfaceVrfTest {
  @Test
  public void testEquals() {
    IngressInterfaceVrf iiv = IngressInterfaceVrf.instance();
    new EqualsTester().addEqualityGroup(iiv, iiv).addEqualityGroup(new Object()).testEquals();
  }

  @Test
  public void testJavaSerialization() {
    IngressInterfaceVrf iiv = IngressInterfaceVrf.instance();
    assertThat(SerializationUtils.clone(iiv), equalTo(iiv));
  }

  @Test
  public void testJsonSerialization() {
    IngressInterfaceVrf iiv = IngressInterfaceVrf.instance();
    assertThat(BatfishObjectMapper.clone(iiv, IngressInterfaceVrf.class), equalTo(iiv));
  }
}
