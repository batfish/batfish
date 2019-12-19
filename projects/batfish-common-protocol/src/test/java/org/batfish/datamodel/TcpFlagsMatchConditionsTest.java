package org.batfish.datamodel;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.testing.EqualsTester;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests of {@link org.batfish.datamodel.TcpFlagsMatchConditions} */
@RunWith(JUnit4.class)
public class TcpFlagsMatchConditionsTest {

  @Test
  public void testEquals() {
    TcpFlags flags1 = TcpFlags.builder().setUrg(true).setRst(true).build();
    TcpFlags flags2 = TcpFlags.builder().setUrg(false).setRst(true).build();
    new EqualsTester()
        .addEqualityGroup(
            TcpFlagsMatchConditions.builder().setTcpFlags(flags1).setUseUrg(true).build(),
            TcpFlagsMatchConditions.builder().setTcpFlags(flags1).setUseUrg(true).build())
        .addEqualityGroup(
            TcpFlagsMatchConditions.builder().setTcpFlags(flags2).setUseUrg(true).build())
        .addEqualityGroup(
            TcpFlagsMatchConditions.builder().setTcpFlags(flags1).setUseUrg(false).build())
        .testEquals();
  }

  @Test
  public void testMatchFlow() {
    TcpFlags flags = TcpFlags.builder().setUrg(true).setRst(true).build();
    TcpFlagsMatchConditions conditions =
        TcpFlagsMatchConditions.builder().setTcpFlags(flags).setUseUrg(true).build();
    assertThat(
        conditions.match(Flow.builder().setTcpFlagsUrg(1).setIngressNode("n").build()),
        equalTo(true));
    assertThat(
        conditions.match(Flow.builder().setTcpFlagsUrg(0).setIngressNode("n").build()),
        equalTo(false));
  }

  @Test
  public void testMatchFlowIgnoreFields() {
    TcpFlags flags = TcpFlags.builder().setUrg(true).setRst(true).build();
    // All fields ignored by default
    TcpFlagsMatchConditions conditions =
        TcpFlagsMatchConditions.builder().setTcpFlags(flags).build();
    assertThat(
        conditions.match(Flow.builder().setTcpFlagsUrg(1).setIngressNode("n").build()),
        equalTo(true));
    // Fields that do not match, but are ignored do not break match
    conditions =
        TcpFlagsMatchConditions.builder()
            .setTcpFlags(flags)
            .setUseUrg(false)
            .setUseRst(true)
            .build();
    assertThat(
        conditions.match(
            Flow.builder().setTcpFlagsUrg(0).setTcpFlagsRst(1).setIngressNode("n").build()),
        equalTo(true));
  }

  @Test
  public void testBuilderDefaults() {
    TcpFlagsMatchConditions conditions = TcpFlagsMatchConditions.builder().build();
    assertThat(conditions.getTcpFlags(), equalTo(TcpFlags.builder().build()));
    assertThat(conditions.getUseAck(), equalTo(false));
    assertThat(conditions.getUseCwr(), equalTo(false));
    assertThat(conditions.getUseEce(), equalTo(false));
    assertThat(conditions.getUseFin(), equalTo(false));
    assertThat(conditions.getUsePsh(), equalTo(false));
    assertThat(conditions.getUseRst(), equalTo(false));
    assertThat(conditions.getUseSyn(), equalTo(false));
    assertThat(conditions.getUseUrg(), equalTo(false));
  }
}
