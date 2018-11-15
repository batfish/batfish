package org.batfish.datamodel.questions;

import static org.batfish.datamodel.questions.BgpPeerPropertySpecifier.getIsPassive;
import static org.batfish.datamodel.questions.BgpPeerPropertySpecifier.getRemoteAs;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import com.google.common.collect.ImmutableList;
import org.batfish.datamodel.BgpActivePeerConfig;
import org.batfish.datamodel.BgpPassivePeerConfig;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.answers.SelfDescribingObject;
import org.junit.Test;

public class BgpPeerPropertySpecifierTest {

  @Test
  public void getIsPassiveTest() {
    assertThat(getIsPassive(BgpActivePeerConfig.builder().build()), equalTo(false));
    assertThat(getIsPassive(BgpPassivePeerConfig.builder().build()), equalTo(true));
  }

  @Test
  public void getRemoteAsActivePeer() {
    BgpActivePeerConfig activePeerConfig = BgpActivePeerConfig.builder().setRemoteAs(100L).build();
    assertThat(getRemoteAs(activePeerConfig), equalTo(new SelfDescribingObject(Schema.LONG, 100L)));
  }

  @Test
  public void getRemoteAsPassivePeer() {
    BgpPassivePeerConfig passivePeerConfig =
        BgpPassivePeerConfig.builder().setRemoteAs(ImmutableList.of(100L)).build();
    assertThat(
        getRemoteAs(passivePeerConfig),
        equalTo(new SelfDescribingObject(Schema.list(Schema.LONG), ImmutableList.of(100L))));
  }
}
