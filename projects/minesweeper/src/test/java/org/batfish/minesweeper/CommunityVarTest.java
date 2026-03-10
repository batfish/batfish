package org.batfish.minesweeper;

import static org.batfish.datamodel.routing_policy.Common.DEFAULT_UNDERSCORE_REPLACEMENT;
import static org.batfish.minesweeper.CommunityVar.COMMUNITY_NUM_REGEX;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import com.google.common.testing.EqualsTester;
import dk.brics.automaton.RegExp;
import java.util.List;
import org.apache.commons.text.StringEscapeUtils;
import org.batfish.datamodel.bgp.community.ExtendedCommunity;
import org.batfish.datamodel.bgp.community.LargeCommunity;
import org.batfish.datamodel.bgp.community.StandardCommunity;
import org.junit.Test;

/** Test of {@link CommunityVar} */
public class CommunityVarTest {
  @Test
  public void testToAutomaton() {

    // community literals
    CommunityVar cv0 = CommunityVar.from(StandardCommunity.of(20, 30));
    CommunityVar cv1 = CommunityVar.from(ExtendedCommunity.of(10, 20, 30));
    CommunityVar cv2 = CommunityVar.from(LargeCommunity.of(10, 20, 30));

    // community regexes
    CommunityVar cv3 = CommunityVar.from("^.*$");
    CommunityVar cv4 = CommunityVar.from("^40:");
    // the Java regex translation of _40:50_
    String underscore = StringEscapeUtils.unescapeJava(DEFAULT_UNDERSCORE_REPLACEMENT);
    CommunityVar cv5 = CommunityVar.from(underscore + "40:50" + underscore);

    // ensure we get well-formed numbers
    CommunityVar cv6 = CommunityVar.from("^0:.0$");

    // ensure that the ^ and $ markers are not treated as ordinary characters
    CommunityVar cv7 = CommunityVar.from("..:..");

    assertThat(cv0.toAutomaton(), equalTo(new RegExp("^20:30$").toAutomaton()));
    assertThat(cv1.toAutomaton(), equalTo(new RegExp("^20:30$").toAutomaton()));
    assertThat(cv2.toAutomaton(), equalTo(new RegExp("^large:10:20:30$").toAutomaton()));

    assertThat(cv3.toAutomaton(), equalTo(CommunityVar.COMMUNITY_FSM));
    assertThat(
        cv4.toAutomaton(), equalTo(new RegExp("^40:" + COMMUNITY_NUM_REGEX + "$").toAutomaton()));
    assertThat(cv5.toAutomaton(), equalTo(new RegExp("^40:50$").toAutomaton()));

    assertThat(cv6.toAutomaton(), equalTo(new RegExp("^0:[1-9]0$").toAutomaton()));

    String atLeastTwoDigits = "(" + COMMUNITY_NUM_REGEX + "&<10-65535>)";
    assertThat(
        cv7.toAutomaton(),
        equalTo(new RegExp("^" + atLeastTwoDigits + ":" + atLeastTwoDigits + "$").toAutomaton()));
  }

  @Test
  public void testEquals() {
    CommunityVar cv = CommunityVar.from(StandardCommunity.of(1, 1));
    new EqualsTester()
        .addEqualityGroup(cv, cv, CommunityVar.from(StandardCommunity.of(1, 1)))
        .addEqualityGroup(CommunityVar.from("1:1"))
        .addEqualityGroup(CommunityVar.from(StandardCommunity.of(1, 2)))
        .addEqualityGroup(CommunityVar.from("1:2"))
        .addEqualityGroup(new Object())
        .testEquals();
  }

  @Test
  public void testCompare() {
    List<CommunityVar> ordered =
        ImmutableList.of(CommunityVar.from("1:1"), CommunityVar.from("1:2"));

    for (int i = 0; i < ordered.size(); i++) {
      for (int j = 0; j < ordered.size(); j++) {
        assertThat(
            Integer.signum(ordered.get(i).compareTo(ordered.get(j))),
            equalTo(Integer.signum(i - j)));
      }
    }
  }
}
