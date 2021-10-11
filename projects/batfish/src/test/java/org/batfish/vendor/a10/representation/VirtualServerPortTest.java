package org.batfish.vendor.a10.representation;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.Test;

/** Tests of {@link VirtualServerPort} */
public class VirtualServerPortTest {
  @Test
  public void testPortAndTypeEquality() {
    VirtualServerPort.PortAndType obj =
        new VirtualServerPort.PortAndType(10, VirtualServerPort.Type.UDP);
    new EqualsTester()
        .addEqualityGroup(obj, new VirtualServerPort.PortAndType(10, VirtualServerPort.Type.UDP))
        .addEqualityGroup(new VirtualServerPort.PortAndType(11, VirtualServerPort.Type.UDP))
        .addEqualityGroup(new VirtualServerPort.PortAndType(11, VirtualServerPort.Type.TCP))
        .testEquals();
  }

  @Test
  public void testPortAndTypeSerialization() {
    VirtualServerPort.PortAndType obj =
        new VirtualServerPort.PortAndType(10, VirtualServerPort.Type.UDP);
    VirtualServerPort.PortAndType clone = SerializationUtils.clone(obj);
    assertThat(obj, equalTo(clone));
  }
}
