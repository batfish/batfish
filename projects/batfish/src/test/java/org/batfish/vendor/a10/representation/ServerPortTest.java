package org.batfish.vendor.a10.representation;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.Test;

/** Tests of {@link ServerPort} */
public class ServerPortTest {
  @Test
  public void testServerPortAndTypeEquality() {
    ServerPort.ServerPortAndType obj = new ServerPort.ServerPortAndType(10, ServerPort.Type.UDP);
    new EqualsTester()
        .addEqualityGroup(obj, new ServerPort.ServerPortAndType(10, ServerPort.Type.UDP))
        .addEqualityGroup(new ServerPort.ServerPortAndType(11, ServerPort.Type.UDP))
        .addEqualityGroup(new ServerPort.ServerPortAndType(11, ServerPort.Type.TCP))
        .testEquals();
  }

  @Test
  public void testServerPortAndTypeSerialization() {
    ServerPort.ServerPortAndType obj = new ServerPort.ServerPortAndType(10, ServerPort.Type.UDP);
    ServerPort.ServerPortAndType clone = SerializationUtils.clone(obj);
    assertThat(obj, equalTo(clone));
  }
}
