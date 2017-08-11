package org.batfish.datamodel;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.BitSet;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class PrefixSpaceTest {
  @Test
  public void getAddressBits() {
    assertThat(PrefixSpace.getAddressBits(Ip.ZERO), equalTo(new BitSet()));
    assertThat(
        PrefixSpace.getAddressBits(Ip.MAX), equalTo(BitSet.valueOf(new long[] {0xff_ff_ff_ffL})));
    assertThat(
        PrefixSpace.getAddressBits(new Ip("128.255.31.3")),
        equalTo(BitSet.valueOf(new long[] {0xc0_f8_ff_01L})));
  }
}
