package org.batfish.datamodel.routing_policy.expr;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

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

/** Tests of {@link DisjunctionChain} */
@RunWith(JUnit4.class)
public class DisjunctionChainTest {

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(new Object())
        .addEqualityGroup(
            new DisjunctionChain(ImmutableList.of(BooleanExprs.TRUE)),
            new DisjunctionChain(ImmutableList.of(BooleanExprs.TRUE)))
        .addEqualityGroup(ImmutableList.of())
        .addEqualityGroup(ImmutableList.of(BooleanExprs.FALSE))
        .testEquals();
  }

  @Test
  public void testJavaSerialization() {
    DisjunctionChain dc = new DisjunctionChain(ImmutableList.of(BooleanExprs.TRUE));
    assertThat(SerializationUtils.clone(dc), equalTo(dc));
  }

  @Test
  public void testJsonSerialization() throws IOException {
    DisjunctionChain dc = new DisjunctionChain(ImmutableList.of(BooleanExprs.TRUE));
    assertThat(BatfishObjectMapper.clone(dc, DisjunctionChain.class), equalTo(dc));
  }

  @Test
  public void testEvaluate() {
    DisjunctionChain dc =
        new DisjunctionChain(ImmutableList.of(BooleanExprs.FALSE, BooleanExprs.TRUE));
    // Test that OR is evaluated correctly
    assertThat(
        dc.evaluate(
                Environment.builder(new Configuration("host", ConfigurationFormat.JUNIPER))
                    .setVrf(Configuration.DEFAULT_VRF_NAME)
                    .build())
            .getBooleanValue(),
        equalTo(true));
  }

  @Test
  public void testToString() {
    DisjunctionChain dc = new DisjunctionChain(ImmutableList.of(BooleanExprs.TRUE));
    assertThat(dc.toString(), equalTo("DisjunctionChain{subroutines=[StaticBooleanExpr{}]}"));
  }
}
