package org.batfish.datamodel;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import java.io.IOException;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Tests of {@link LocalRoute} */
public class LocalRouteTest {
  @Test
  public void testJavaSerialization() {
    LocalRoute lr = new LocalRoute(new InterfaceAddress("1.1.1.1/24"), "Ethernet0");
    assertThat(SerializationUtils.clone(lr), equalTo(lr));
  }

  @Test
  public void testJsonSerialization() throws IOException {
    LocalRoute lr = new LocalRoute(new InterfaceAddress("1.1.1.1/24"), "Ethernet0");
    assertThat(BatfishObjectMapper.clone(lr, LocalRoute.class), equalTo(lr));
  }

  @Test
  public void testToBuilder() {
    LocalRoute lr = new LocalRoute(new InterfaceAddress("1.1.1.1/24"), "Ethernet0");
    assertThat(lr, equalTo(lr.toBuilder().build()));
  }

  @Test
  public void testEquals() {
    LocalRoute lr = new LocalRoute(new InterfaceAddress("1.1.1.1/24"), "Ethernet0");
    new EqualsTester()
        .addEqualityGroup(lr, lr)
        .addEqualityGroup(new LocalRoute(new InterfaceAddress("1.1.2.1/24"), "Ethernet0"))
        .addEqualityGroup(new LocalRoute(new InterfaceAddress("1.1.2.1/24"), "Ethernet1"))
        .addEqualityGroup(new Object())
        .testEquals();
  }
}
