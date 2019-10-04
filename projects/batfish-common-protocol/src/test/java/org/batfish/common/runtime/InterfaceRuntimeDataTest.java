package org.batfish.common.runtime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import java.io.IOException;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Tests of {@link InterfaceRuntimeData} */
public class InterfaceRuntimeDataTest {

  @Test
  public void testInterfaceRuntimeDataEquals() {
    double bandwidth = 1;
    boolean lineUp = true;
    double speed = 3;
    InterfaceRuntimeData.Builder irdBuilder =
        InterfaceRuntimeData.builder().setBandwidth(bandwidth).setLineUp(lineUp).setSpeed(speed);
    new EqualsTester()
        .addEqualityGroup(irdBuilder.build(), new InterfaceRuntimeData(bandwidth, lineUp, speed))
        .addEqualityGroup(irdBuilder.setBandwidth(bandwidth + 1).build())
        .addEqualityGroup(irdBuilder.setLineUp(!lineUp).build())
        .addEqualityGroup(irdBuilder.setSpeed(speed + 1).build())
        .addEqualityGroup(irdBuilder.setBandwidth(null).build())
        .addEqualityGroup(irdBuilder.setLineUp(null).build())
        .addEqualityGroup(irdBuilder.setSpeed(null).build(), InterfaceRuntimeData.builder().build())
        .testEquals();
  }

  @Test
  public void testInterfaceRuntimeDataJsonSerialization() throws IOException {
    InterfaceRuntimeData.Builder irdBuilder =
        InterfaceRuntimeData.builder().setBandwidth(1d).setLineUp(true).setSpeed(2d);
    assertThat(
        BatfishObjectMapper.clone(irdBuilder.build(), InterfaceRuntimeData.class),
        equalTo(irdBuilder.build()));
  }
}
