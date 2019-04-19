package org.batfish.datamodel.eigrp;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import java.io.IOException;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Tests of {@link EigrpInterface} */
public class EigrpInterfaceTest {
  @Test
  public void testEquals() {
    EigrpInterface ei = new EigrpInterface("h", "i", "v");
    new EqualsTester()
        .addEqualityGroup(ei, ei, new EigrpInterface("h", "i", "v"))
        .addEqualityGroup(new EigrpInterface("h1", "i", "v"))
        .addEqualityGroup(new EigrpInterface("h", "i1", "v"))
        .addEqualityGroup(new EigrpInterface("h", "i", "v1"))
        .addEqualityGroup(new Object())
        .testEquals();
  }

  @Test
  public void testJavaSerialization() {
    EigrpInterface ei = new EigrpInterface("h", "i", "v");
    assertThat(SerializationUtils.clone(ei), equalTo(ei));
  }

  @Test
  public void testJsonSerialization() throws IOException {
    EigrpInterface ei = new EigrpInterface("h", "i", "v");
    assertThat(BatfishObjectMapper.clone(ei, EigrpInterface.class), equalTo(ei));
  }
}
