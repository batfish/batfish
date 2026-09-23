package org.batfish.representation.juniper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Test;

public final class BgpGroupVpnApplyExportTest {

  @Test
  public void testCascadeInheritance() {
    BgpGroup parent = new BgpGroup();
    parent.setVpnApplyExport(true);
    BgpGroup child = new BgpGroup();
    child.setParent(parent);

    child.cascadeInheritance();

    assertThat(child.getVpnApplyExport(), equalTo(true));
  }
}
