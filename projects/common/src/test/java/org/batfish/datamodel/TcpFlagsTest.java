package org.batfish.datamodel;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests of {@link org.batfish.datamodel.TcpFlags} */
@RunWith(JUnit4.class)
public class TcpFlagsTest {
  @Test
  public void testEquality() {
    new EqualsTester()
        .addEqualityGroup(
            TcpFlags.builder().setAck(true).setRst(true).build(),
            TcpFlags.builder().setAck(true).setRst(true).build())
        .addEqualityGroup(
            TcpFlags.builder().setFin(true).setUrg(false).build(),
            TcpFlags.builder().setFin(true).setUrg(false).build())
        .testEquals();
  }

  @Test
  public void testBuilderDefaults() {
    TcpFlags flags = TcpFlags.builder().build();
    assertThat(flags.getAck(), equalTo(false));
    assertThat(flags.getCwr(), equalTo(false));
    assertThat(flags.getEce(), equalTo(false));
    assertThat(flags.getFin(), equalTo(false));
    assertThat(flags.getPsh(), equalTo(false));
    assertThat(flags.getRst(), equalTo(false));
    assertThat(flags.getSyn(), equalTo(false));
    assertThat(flags.getUrg(), equalTo(false));
  }
}
