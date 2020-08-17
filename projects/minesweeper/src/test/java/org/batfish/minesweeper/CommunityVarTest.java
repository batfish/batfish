package org.batfish.minesweeper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import com.google.common.testing.EqualsTester;
import dk.brics.automaton.RegExp;
import java.util.List;
import org.batfish.datamodel.bgp.community.ExtendedCommunity;
import org.batfish.datamodel.bgp.community.LargeCommunity;
import org.batfish.datamodel.bgp.community.StandardCommunity;
import org.junit.Test;

/** Test of {@link CommunityVar} */
public class CommunityVarTest {
  @Test
  public void testToAutomaton() {
    CommunityVar cv0 = CommunityVar.from(StandardCommunity.of(20, 30));
    CommunityVar cv1 = CommunityVar.from("^.*$");
    CommunityVar cv2 = CommunityVar.from(ExtendedCommunity.of(10, 20, 30));
    CommunityVar cv3 = CommunityVar.from(LargeCommunity.of(10, 20, 30));

    assertEquals(new RegExp("20:30").toAutomaton(), cv0.toAutomaton());
    assertEquals(CommunityVar.COMMUNITY_FSM, cv1.toAutomaton());
    assertEquals(new RegExp("20:30").toAutomaton(), cv2.toAutomaton());
    assertEquals(new RegExp("large:10:20:30").toAutomaton(), cv3.toAutomaton());
  }

  @Test
  public void testEquals() {
    CommunityVar cv = CommunityVar.from(StandardCommunity.of(1, 1));
    new EqualsTester()
        .addEqualityGroup(cv, cv, CommunityVar.from(StandardCommunity.of(1, 1)))
        .addEqualityGroup(CommunityVar.from("1:1"))
        .addEqualityGroup(CommunityVar.other("1:1"))
        .addEqualityGroup(CommunityVar.from(StandardCommunity.of(1, 2)))
        .addEqualityGroup(CommunityVar.from("1:2"))
        .addEqualityGroup(new Object())
        .testEquals();
  }

  @Test
  public void testCompare() {
    List<CommunityVar> ordered =
        ImmutableList.of(
            CommunityVar.from(StandardCommunity.of(1, 1)),
            CommunityVar.from(StandardCommunity.of(1, 2)),
            CommunityVar.from("1:1"),
            CommunityVar.from("1:2"),
            CommunityVar.other("1:1"),
            CommunityVar.other("1:2"));

    for (int i = 0; i < ordered.size(); i++) {
      for (int j = 0; j < ordered.size(); j++) {
        assertThat(
            Integer.signum(ordered.get(i).compareTo(ordered.get(j))),
            equalTo(Integer.signum(i - j)));
      }
    }
  }
}
