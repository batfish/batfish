package org.batfish.datamodel.packet_policy;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import java.io.IOException;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Tests of {@link TrueExpr} */
public class TrueExprTest {

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(TrueExpr.instance(), TrueExpr.instance())
        .addEqualityGroup(new Object())
        .testEquals();
  }

  @Test
  public void testJavaSerialization() {
    assertThat(SerializationUtils.clone(TrueExpr.instance()), equalTo(TrueExpr.instance()));
  }

  @Test
  public void testJsonSerialization() throws IOException {
    assertThat(
        BatfishObjectMapper.clone(TrueExpr.instance(), TrueExpr.class),
        equalTo(TrueExpr.instance()));
  }
}
