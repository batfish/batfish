package org.batfish.datamodel;

import static org.batfish.common.util.CommonUtil.communityStringToLong;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

/** Tests of {@link CommunityList} */
public class CommunityListTest {

  @Test
  public void testEmptyMatch() {
    CommunityList cl = new CommunityList("name", ImmutableList.of());

    assertThat(cl.permits(communityStringToLong("65111:34")), equalTo(false));
  }

  @Test
  public void testMatch() {
    CommunityList cl =
        new CommunityList(
            "name",
            ImmutableList.of(new CommunityListLine(LineAction.ACCEPT, "65[0-9][0-9][0-9]:*")));

    assertThat(cl.permits(communityStringToLong("65111:34")), equalTo(true));
    assertThat(cl.permits(communityStringToLong("64111:34")), equalTo(false));
  }

  @Test
  public void testInvertMatch() {
    CommunityList cl =
        new CommunityList(
            "name",
            ImmutableList.of(new CommunityListLine(LineAction.REJECT, "65[0-9][0-9][0-9]:*")));

    assertThat(cl.permits(communityStringToLong("65111:34")), equalTo(false));
    assertThat(cl.permits(communityStringToLong("64111:34")), equalTo(false));
  }
}
