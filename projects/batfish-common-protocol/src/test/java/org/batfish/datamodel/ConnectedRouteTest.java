package org.batfish.datamodel;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import java.io.IOException;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Tests of {@link ConnectedRoute} */
public class ConnectedRouteTest {
  @Test
  public void testJavaSerialization() {
    ConnectedRoute cr = new ConnectedRoute(Prefix.parse("1.1.1.0/24"), "Ethernet0");
    assertThat(SerializationUtils.clone(cr), equalTo(cr));
  }

  @Test
  public void testJsonSerialization() throws IOException {
    ConnectedRoute cr = new ConnectedRoute(Prefix.parse("1.1.1.0/24"), "Ethernet0");
    assertThat(BatfishObjectMapper.clone(cr, ConnectedRoute.class), equalTo(cr));
  }

  @Test
  public void testToBuilder() {
    ConnectedRoute cr = new ConnectedRoute(Prefix.parse("1.1.1.0/24"), "Ethernet0");
    assertThat(cr, equalTo(cr.toBuilder().build()));
  }

  @Test
  public void testEquals() {
    ConnectedRoute cr = new ConnectedRoute(Prefix.parse("1.1.1.0/24"), "Ethernet0");
    new EqualsTester()
        .addEqualityGroup(cr, cr)
        .addEqualityGroup(new ConnectedRoute(Prefix.parse("1.1.2.0/24"), "Ethernet0"))
        .addEqualityGroup(new ConnectedRoute(Prefix.parse("1.1.2.0/24"), "Ethernet1"))
        .addEqualityGroup(new ConnectedRoute(Prefix.parse("1.1.2.0/24"), "Ethernet1", 123))
        .addEqualityGroup(new Object())
        .testEquals();
  }
}
