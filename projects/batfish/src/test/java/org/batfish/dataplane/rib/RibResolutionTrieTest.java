package org.batfish.dataplane.rib;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertThat;

import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.junit.Test;

/** Tests of {@link RibResolutionTrie} */
public final class RibResolutionTrieTest {

  @Test
  public void testGetAffectedNextHopIpsEmpty() {
    RibResolutionTrie trie = new RibResolutionTrie();
    assertThat(trie.getAffectedNextHopIps(Prefix.ZERO), empty());
  }

  @Test
  public void testGetAffectedNextHopIpsUnaffectedMoreSpecific() {
    {
      RibResolutionTrie trie = new RibResolutionTrie();
      trie.addNextHopIp(Ip.parse("10.0.0.1"));
      trie.addPrefix(Prefix.strict("10.0.0.0/24"));
      assertThat(trie.getAffectedNextHopIps(Prefix.strict("10.0.0.0/8")), empty());
    }
    {
      RibResolutionTrie trie = new RibResolutionTrie();
      trie.addNextHopIp(Ip.parse("10.0.0.1"));
      trie.addPrefix(Prefix.strict("10.0.0.1/32"));
      assertThat(trie.getAffectedNextHopIps(Prefix.strict("10.0.0.0/8")), empty());
    }
  }

  @Test
  public void testGetAffectedNextHopIpsAffectedNotMoreSpecific() {
    {
      RibResolutionTrie trie = new RibResolutionTrie();
      Ip nhip = Ip.parse("10.0.0.1");
      trie.addNextHopIp(nhip);
      trie.addPrefix(Prefix.ZERO);
      assertThat(trie.getAffectedNextHopIps(Prefix.ZERO), contains(nhip));
    }
    {
      RibResolutionTrie trie = new RibResolutionTrie();
      Ip nhip = Ip.parse("10.0.0.1");
      trie.addNextHopIp(nhip);
      trie.addPrefix(Prefix.strict("10.0.0.0/24"));
      assertThat(trie.getAffectedNextHopIps(Prefix.strict("10.0.0.0/24")), contains(nhip));
    }
    {
      RibResolutionTrie trie = new RibResolutionTrie();
      Ip nhip = Ip.parse("10.0.0.1");
      trie.addNextHopIp(nhip);
      trie.addPrefix(Prefix.strict("10.0.0.0/24"));
      assertThat(trie.getAffectedNextHopIps(Prefix.strict("10.0.0.0/25")), contains(nhip));
    }
    {
      RibResolutionTrie trie = new RibResolutionTrie();
      Ip nhip = Ip.parse("10.0.0.1");
      trie.addNextHopIp(nhip);
      trie.addPrefix(Prefix.strict("10.0.0.1/32"));
      assertThat(trie.getAffectedNextHopIps(Prefix.strict("10.0.0.1/32")), contains(nhip));
    }
  }
}
