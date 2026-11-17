package org.batfish.representation.juniper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.Test;

final class BgpGroupAcceptRemoteNexthopTest {

  @Test
  void testCascadeInheritance() {
    BgpGroup parent = new BgpGroup();
    parent.setAcceptRemoteNexthop(true);
    BgpGroup child = new BgpGroup();
    child.setParent(parent);

    child.cascadeInheritance();

    assertThat(child.getAcceptRemoteNexthop(), equalTo(true));
  }
}
