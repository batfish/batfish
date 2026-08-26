package org.batfish.representation.palo_alto;

import com.google.common.collect.ImmutableList;
import com.google.common.testing.EqualsTester;
import org.batfish.datamodel.DiffieHellmanGroup;
import org.batfish.datamodel.IpsecProtocol;
import org.batfish.representation.palo_alto.CryptoProfile.Type;
import org.junit.Test;

/** Tests of {@link CryptoProfile} */
public class CryptoProfileTest {

  private static CryptoProfile ipsecProfile() {
    return new CryptoProfile("name", Type.IPSEC);
  }

  @Test
  public void testEquals() {
    CryptoProfile withProtocol = ipsecProfile();
    withProtocol.setProtocol(IpsecProtocol.AH);

    CryptoProfile withNoPfs = ipsecProfile();
    withNoPfs.setNoPfs(true);

    CryptoProfile withDhGroups = ipsecProfile();
    withDhGroups.setDhGroups(ImmutableList.of(DiffieHellmanGroup.GROUP14));

    new EqualsTester()
        .addEqualityGroup(ipsecProfile(), ipsecProfile())
        .addEqualityGroup(new CryptoProfile("other", Type.IPSEC))
        .addEqualityGroup(new CryptoProfile("name", Type.IKE))
        // protocol and no-pfs both distinguish otherwise-identical profiles
        .addEqualityGroup(withProtocol)
        .addEqualityGroup(withNoPfs)
        .addEqualityGroup(withDhGroups)
        .testEquals();
  }
}
