package org.batfish.datamodel.questions;

import static org.batfish.datamodel.questions.BgpPeerPropertySpecifier.getIsPassive;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.batfish.datamodel.BgpActivePeerConfig;
import org.batfish.datamodel.BgpPassivePeerConfig;
import org.junit.Test;

public class BgpPeerPropertySpecifierTest {

  @Test
  public void getIsPassiveTest() {
    assertThat(getIsPassive(BgpActivePeerConfig.builder().build()), equalTo(false));
    assertThat(getIsPassive(BgpPassivePeerConfig.builder().build()), equalTo(true));
  }
}
