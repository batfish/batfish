package org.batfish.vendor.arista.representation.eos;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.Range;
import org.batfish.datamodel.BgpPeerConfig;
import org.batfish.datamodel.LongSpace;
import org.junit.Test;

/** Tests for {@link AristaBgpPeerFilter} */
public class AristaBgpPeerFilterTest {

  @Test
  public void testEmpty() {
    AristaBgpPeerFilter pf = new AristaBgpPeerFilter("name");
    assertThat(pf.toLongSpace(), equalTo(BgpPeerConfig.ALL_AS_NUMBERS));
  }

  @Test
  public void testAddNoSeq() {
    AristaBgpPeerFilter pf = new AristaBgpPeerFilter("name");
    pf.addLine(LongSpace.of(10), AristaBgpPeerFilterLine.Action.ACCEPT);
    pf.addLine(LongSpace.of(20), AristaBgpPeerFilterLine.Action.ACCEPT);
    assertThat(
        pf.toLongSpace(), equalTo(LongSpace.builder().including(10L).including(20L).build()));
  }

  @Test
  public void testAddWithSeq() {
    AristaBgpPeerFilter pf = new AristaBgpPeerFilter("name");
    pf.addLine(1, LongSpace.of(10), AristaBgpPeerFilterLine.Action.ACCEPT);
    pf.addLine(LongSpace.of(Range.closed(11L, 20L)), AristaBgpPeerFilterLine.Action.ACCEPT);
    pf.addLine(5, LongSpace.of(15), AristaBgpPeerFilterLine.Action.REJECT);
    assertThat(
        pf.toLongSpace(),
        equalTo(
            LongSpace.builder()
                .including(Range.closed(10L, 14L))
                .including(Range.closed(16L, 20L))
                .build()));
  }

  @Test
  public void testUnreachableReject() {
    AristaBgpPeerFilter pf = new AristaBgpPeerFilter("name");
    pf.addLine(LongSpace.of(10), AristaBgpPeerFilterLine.Action.ACCEPT);
    pf.addLine(LongSpace.of(Range.closed(11L, 20L)), AristaBgpPeerFilterLine.Action.ACCEPT);
    pf.addLine(LongSpace.of(15), AristaBgpPeerFilterLine.Action.REJECT);
    assertThat(
        pf.toLongSpace(), equalTo(LongSpace.builder().including(Range.closed(10L, 20L)).build()));
  }

  @Test
  public void testUnreachableAccept() {
    AristaBgpPeerFilter pf = new AristaBgpPeerFilter("name");
    pf.addLine(LongSpace.of(Range.closed(10L, 20L)), AristaBgpPeerFilterLine.Action.ACCEPT);
    pf.addLine(LongSpace.of(15), AristaBgpPeerFilterLine.Action.REJECT);
    pf.addLine(LongSpace.of(15), AristaBgpPeerFilterLine.Action.ACCEPT);
    assertThat(
        pf.toLongSpace(), equalTo(LongSpace.builder().including(Range.closed(10L, 20L)).build()));
  }
}
