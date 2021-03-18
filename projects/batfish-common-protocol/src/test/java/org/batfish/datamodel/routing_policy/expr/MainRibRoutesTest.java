package org.batfish.datamodel.routing_policy.expr;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.testing.EqualsTester;
import javax.annotation.ParametersAreNonnullByDefault;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Tests of {@link MainRibRoutes} */
@ParametersAreNonnullByDefault
public final class MainRibRoutesTest {

  @Test
  public void testJavaSerialization() {
    MainRibRoutes obj = MainRibRoutes.instance();
    assertThat(SerializationUtils.clone(obj), equalTo(obj));
  }

  @Test
  public void testJsonSerialization() {
    MainRibRoutes obj = MainRibRoutes.instance();
    assertThat(BatfishObjectMapper.clone(obj, MainRibRoutes.class), equalTo(obj));
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(MainRibRoutes.instance(), MainRibRoutes.instance())
        .testEquals();
  }
}
