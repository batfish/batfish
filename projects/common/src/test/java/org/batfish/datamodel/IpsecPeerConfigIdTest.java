package org.batfish.datamodel;

import com.google.common.testing.EqualsTester;
import org.junit.jupiter.api.Test;

class IpsecPeerConfigIdTest {

  @Test
  void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            new IpsecPeerConfigId("peer1", "host1"), new IpsecPeerConfigId("peer1", "host1"))
        .addEqualityGroup(new IpsecPeerConfigId("peer3", "host2"))
        .addEqualityGroup(new IpsecPeerConfigId("peer3", "host1"))
        .addEqualityGroup(new IpsecPeerConfigId("peer4", "host1"))
        .addEqualityGroup(new IpsecPeerConfigId("peer5", "host1"))
        .testEquals();
  }
}
