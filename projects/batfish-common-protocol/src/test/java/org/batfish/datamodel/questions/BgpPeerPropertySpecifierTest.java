package org.batfish.datamodel.questions;

import static org.batfish.datamodel.questions.BgpPeerPropertySpecifier.getIsPassive;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.batfish.datamodel.BgpActivePeerConfig;
import org.batfish.datamodel.BgpPassivePeerConfig;
import org.batfish.datamodel.BgpUnnumberedPeerConfig;
import org.junit.Test;

public class BgpPeerPropertySpecifierTest {

  @Test
  public void getIsPassiveTest() {
    assertFalse(getIsPassive(BgpActivePeerConfig.builder().build()));
    assertFalse(getIsPassive(BgpUnnumberedPeerConfig.builder().setPeerInterface("i").build()));
    assertTrue(getIsPassive(BgpPassivePeerConfig.builder().build()));
  }
}
