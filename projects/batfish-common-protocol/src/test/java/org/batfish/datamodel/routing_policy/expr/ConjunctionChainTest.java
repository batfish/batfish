package org.batfish.datamodel.routing_policy.expr;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.testing.EqualsTester;
import java.io.IOException;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.routing_policy.Environment;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests of {@link ConjunctionChain} */
@RunWith(JUnit4.class)
public class ConjunctionChainTest {

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(new Object())
        .addEqualityGroup(
            new ConjunctionChain(ImmutableList.of(BooleanExprs.TRUE)),
            new ConjunctionChain(ImmutableList.of(BooleanExprs.TRUE)))
        .addEqualityGroup(ImmutableList.of())
        .addEqualityGroup(ImmutableList.of(BooleanExprs.FALSE))
        .testEquals();
  }

  @Test
  public void testJavaSerialization() {
    ConjunctionChain cc = new ConjunctionChain(ImmutableList.of(BooleanExprs.TRUE));
    assertThat(SerializationUtils.clone(cc), equalTo(cc));
  }

  @Test
  public void testJsonSerialization() throws IOException {
    ConjunctionChain cc = new ConjunctionChain(ImmutableList.of(BooleanExprs.TRUE));
    assertThat(BatfishObjectMapper.clone(cc, ConjunctionChain.class), equalTo(cc));
  }

  @Test
  public void testEvaluate() {
    ConjunctionChain cc =
        new ConjunctionChain(ImmutableList.of(BooleanExprs.FALSE, BooleanExprs.TRUE));
    // Test that AND is evaluated correctly
    assertThat(
        cc.evaluate(
                Environment.builder(new Configuration("host", ConfigurationFormat.JUNIPER))
                    .setVrf(Configuration.DEFAULT_VRF_NAME)
                    .build())
            .getBooleanValue(),
        equalTo(false));
  }

  @Test
  public void testToString() {
    ConjunctionChain cc = new ConjunctionChain(ImmutableList.of(BooleanExprs.TRUE));
    assertThat(cc.toString(), equalTo("ConjunctionChain{subroutines=[StaticBooleanExpr{}]}"));
  }
}
