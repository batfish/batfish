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
    ConjunctionChain dc = new ConjunctionChain(ImmutableList.of(BooleanExprs.TRUE));
    assertThat(SerializationUtils.clone(dc), equalTo(dc));
  }

  @Test
  public void testJsonSerialization() throws IOException {
    ConjunctionChain dc = new ConjunctionChain(ImmutableList.of(BooleanExprs.TRUE));
    assertThat(BatfishObjectMapper.clone(dc, ConjunctionChain.class), equalTo(dc));
  }

  @Test
  public void testEvaluate() {
    ConjunctionChain dc =
        new ConjunctionChain(ImmutableList.of(BooleanExprs.FALSE, BooleanExprs.TRUE));
    // Test that or is evaluated correctly
    assertThat(
        dc.evaluate(
                Environment.builder(new Configuration("host", ConfigurationFormat.JUNIPER))
                    .setVrf(Configuration.DEFAULT_VRF_NAME)
                    .build())
            .getBooleanValue(),
        equalTo(false));
  }

  @Test
  public void testToString() {
    ConjunctionChain dc = new ConjunctionChain(ImmutableList.of(BooleanExprs.TRUE));
    assertThat(dc.toString(), equalTo("ConjunctionChain{subroutines=[StaticBooleanExpr{}]}"));
  }
}
