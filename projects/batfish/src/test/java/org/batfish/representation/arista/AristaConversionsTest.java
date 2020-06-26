package org.batfish.representation.arista;

import static org.batfish.representation.arista.AristaConversions.getAsnSpace;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableMap;
import org.batfish.datamodel.BgpPeerConfig;
import org.batfish.datamodel.LongSpace;
import org.batfish.datamodel.Prefix;
import org.batfish.representation.arista.eos.AristaBgpPeerFilter;
import org.batfish.representation.arista.eos.AristaBgpPeerFilterLine;
import org.batfish.representation.arista.eos.AristaBgpV4DynamicNeighbor;
import org.junit.Test;

/** Tests of {@link AristaConversions} */
public class AristaConversionsTest {

  @Test
  public void testGetAsnSpaceSingularAs() {
    AristaBgpV4DynamicNeighbor neighbor =
        new AristaBgpV4DynamicNeighbor(Prefix.parse("1.1.1.0/24"));
    neighbor.setRemoteAs(1L);
    assertThat(getAsnSpace(neighbor, ImmutableMap.of()), equalTo(LongSpace.of(1)));
  }

  @Test
  public void testGetAsnSpaceNoAs() {
    AristaBgpV4DynamicNeighbor neighbor =
        new AristaBgpV4DynamicNeighbor(Prefix.parse("1.1.1.0/24"));
    assertThat(getAsnSpace(neighbor, ImmutableMap.of()), equalTo(LongSpace.EMPTY));
  }

  @Test
  public void testGetAsnSpaceUndefinedPeerList() {
    AristaBgpV4DynamicNeighbor neighbor =
        new AristaBgpV4DynamicNeighbor(Prefix.parse("1.1.1.0/24"));
    neighbor.setPeerFilter("PF");
    assertThat(getAsnSpace(neighbor, ImmutableMap.of()), equalTo(BgpPeerConfig.ALL_AS_NUMBERS));
  }

  @Test
  public void testGetAsnSpaceDefinedPeerList() {
    AristaBgpV4DynamicNeighbor neighbor =
        new AristaBgpV4DynamicNeighbor(Prefix.parse("1.1.1.0/24"));
    neighbor.setPeerFilter("PF");
    AristaBgpPeerFilter pf = new AristaBgpPeerFilter("PF");
    pf.addLine(LongSpace.of(1), AristaBgpPeerFilterLine.Action.ACCEPT);
    assertThat(getAsnSpace(neighbor, ImmutableMap.of(pf.getName(), pf)), equalTo(LongSpace.of(1)));
  }
}
