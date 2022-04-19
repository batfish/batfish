package org.batfish.datamodel;

import static org.batfish.datamodel.PrefixCompressor.buildDependentBitsTrie;
import static org.batfish.datamodel.PrefixCompressor.compress;
import static org.batfish.datamodel.PrefixCompressor.inflate;
import static org.batfish.datamodel.PrefixCompressor.leftExtension;
import static org.batfish.datamodel.PrefixCompressor.minEquivalentIp;
import static org.batfish.datamodel.PrefixCompressor.rightExtension;
import static org.junit.Assert.assertEquals;

import net.sf.javabdd.BDD;
import org.batfish.common.bdd.BDDPacket;
import org.junit.Test;

public class PrefixCompressorTest {
  @Test
  public void testLeftExtension() {
    Prefix p0 = Prefix.parse("10.0.0.0/8");
    assertEquals(Prefix.parse("10.0.0.0/9"), leftExtension(p0));
  }

  @Test
  public void testRightExtension() {
    Prefix p0 = Prefix.parse("10.0.0.0/8");
    assertEquals(Prefix.parse("10.128.0.0/9"), rightExtension(p0));
  }

  @Test
  public void testBitsBetween() {
    Prefix p1 = Prefix.parse("10.0.0.0/8");
    Prefix p2 = Prefix.parse("10.211.123.0/24");
    assertEquals(
        IpWildcard.ipWithWildcardMask(Ip.parse("0.211.123.0"), 0xFF0000FFL),
        PrefixCompressor.bitsBetween(p1, p2));
  }

  @Test
  public void testBuildDependentBitsTrie_empty() {
    PrefixTrieMultiMap prefixes = new PrefixTrieMultiMap();
    PrefixTrieMultiMap actual = PrefixCompressor.buildDependentBitsTrie(prefixes);
    PrefixTrieMultiMap expected = new PrefixTrieMultiMap();
    assertEquals(expected, actual);
  }

  @Test
  public void testBuildDependentBitsTrie_single() {
    PrefixTrieMultiMap prefixes = new PrefixTrieMultiMap();
    Prefix prefix = Prefix.parse("10.1.1.0/24");
    prefixes.put(prefix, Prefix.ZERO);

    PrefixTrieMultiMap depsTrie = PrefixCompressor.buildDependentBitsTrie(prefixes);
    PrefixTrieMultiMap expected = new PrefixTrieMultiMap();
    expected.put(Prefix.ZERO, IpWildcard.create(prefix));
    assertEquals(expected, depsTrie);
  }

  @Test
  public void testBuildDependentBitsTrie() {
    PrefixTrieMultiMap prefixes = new PrefixTrieMultiMap<>();
    prefixes.put(Prefix.parse("10.1.1.0/24"), Prefix.ZERO);
    prefixes.put(Prefix.parse("10.1.1.1/32"), Prefix.ZERO);
    prefixes.put(Prefix.parse("10.1.1.2/32"), Prefix.ZERO);
    prefixes.put(Prefix.parse("10.1.2.0/24"), Prefix.ZERO);
    prefixes.put(Prefix.parse("8.8.8.8/32"), Prefix.ZERO);

    PrefixTrieMultiMap depsTrie = PrefixCompressor.buildDependentBitsTrie(prefixes);
    PrefixTrieMultiMap expected = new PrefixTrieMultiMap<>();
    expected.put(
        Prefix.parse("0.0.0.0/0"), IpWildcard.parse("8.0.0.0/6")); // first 6 bits are constant
    expected.put(
        Prefix.parse("8.0.0.0/7"), IpWildcard.parse("0.8.8.8:254.0.0.0")); // 25 dependent bits
    expected.put(
        Prefix.parse("10.0.0.0/7"), IpWildcard.parse("0.1.0.0:254.0.3.255")); // 14 dependent bits
    expected.put(
        Prefix.parse("10.1.0.0/23"),
        IpWildcard.parse("0.0.1.0:255.255.254.255")); // 1 dependent bit
    expected.put(
        Prefix.parse("10.1.1.0/25"),
        IpWildcard.parse("0.0.0.0:255.255.255.131")); // 5 dependent bits
    expected.put(
        Prefix.parse("10.1.1.0/31"),
        IpWildcard.parse("0.0.0.1:255.255.255.254")); // 1 dependent bit
    expected.put(
        Prefix.parse("10.1.1.2/31"),
        IpWildcard.parse("0.0.0.0:255.255.255.254")); // 1 dependent bit
    expected.put(
        Prefix.parse("10.1.2.0/23"),
        IpWildcard.parse("0.0.0.0:255.255.254.255")); // 1 dependent bit
    assertEquals(expected, depsTrie);
  }

  @Test
  public void testCompress() {
    PrefixTrieMultiMap<Prefix> prefixes = new PrefixTrieMultiMap<>();
    prefixes.put(Prefix.parse("10.1.1.0/24"), Prefix.ZERO);
    prefixes.put(Prefix.parse("10.1.1.1/32"), Prefix.ZERO);
    prefixes.put(Prefix.parse("10.1.1.2/32"), Prefix.ZERO);
    prefixes.put(Prefix.parse("10.1.2.0/24"), Prefix.ZERO);
    prefixes.put(Prefix.parse("8.8.8.8/32"), Prefix.ZERO);

    PrefixTrieMultiMap<IpWildcard> trie = buildDependentBitsTrie(prefixes);
    // compresses to 4 bits
    assertEquals(
        IpWildcard.parse("2.0.0.0:253.255.253.125"), compress(trie, Prefix.parse("10.1.1.1/32")));
    assertEquals(Ip.parse("10.1.1.1"), inflate(trie, Ip.parse("2.0.0.0")));
  }

  @Test
  public void testSimple() {
    PrefixTrieMultiMap<Prefix> prefixes = new PrefixTrieMultiMap<>();
    prefixes.put(Prefix.ZERO, Prefix.ZERO);
    prefixes.put(Prefix.parse("10.1.1.0/24"), Prefix.ZERO);
    prefixes.put(Prefix.parse("10.1.1.1/32"), Prefix.ZERO);
    prefixes.put(Prefix.parse("10.1.1.2/32"), Prefix.ZERO);
    prefixes.put(Prefix.parse("8.8.8.8/32"), Prefix.ZERO);

    PrefixTrieMultiMap<IpWildcard> trie = buildDependentBitsTrie(prefixes);
    // 10.1.1.0/24 compresses to 1 bit
    assertEquals(
        IpWildcard.parse("2.0.0.0:125.255.255.255"), compress(trie, Prefix.parse("10.1.1.0/24")));
    assertEquals(Ip.parse("10.1.1.1"), inflate(trie, Ip.parse("2.0.0.0")));
    // 10.1.1.1 compresses to 3 bits
    assertEquals(
        IpWildcard.parse("2.0.0.0:125.255.255.125"), compress(trie, Prefix.parse("10.1.1.1/32")));
    assertEquals(Ip.parse("10.1.1.1"), inflate(trie, Ip.parse("2.0.0.0")));

    // 10.1.1.2 compresses to 3 bits
    assertEquals(
        IpWildcard.parse("2.0.0.2:125.255.255.125"), compress(trie, Prefix.parse("10.1.1.2/32")));
    assertEquals(Ip.parse("10.1.1.2"), inflate(trie, Ip.parse("2.0.0.2")));

    // 10.1.1.1.3 compresses to 3 bits
    BDDPacket pkt = new BDDPacket();
    BDD bdd0_24 = pkt.getDstIp().toBDD(compress(trie, Prefix.parse("10.1.1.0/24")));
    BDD bdd1_32 = pkt.getDstIp().toBDD(compress(trie, Prefix.parse("10.1.1.1/32")));
    BDD bdd2_32 = pkt.getDstIp().toBDD(compress(trie, Prefix.parse("10.1.1.2/32")));

    assertEquals("<106:1>", bdd0_24.toReprString());
    assertEquals("<106:1, 124:0, 130:0>", bdd1_32.toReprString());
    assertEquals("<106:1, 124:0, 130:1>", bdd2_32.toReprString());

    /* Note: don't always get minimum value representatives. We can improve that in the inflator, just need some
     * new prefix trie APIs.
     */

    {
      BDD bdd = bdd0_24.diff(bdd1_32);
      assertEquals("<106:1, 124:0, 130:1><106:1, 124:1>", bdd.toReprString());

      Ip compressedIp = Ip.create(pkt.getDstIp().getValueSatisfying(bdd).get());

      assertEquals(Ip.parse("2.0.0.2"), compressedIp);
      assertEquals(Ip.parse("10.1.1.2"), inflate(trie, compressedIp));
    }

    {
      BDD bdd = bdd0_24.diff(bdd2_32);
      assertEquals("<106:1, 124:0, 130:0><106:1, 124:1>", bdd.toReprString());

      Ip compressedIp = Ip.create(pkt.getDstIp().getValueSatisfying(bdd).get());

      assertEquals(Ip.parse("2.0.0.0"), compressedIp);
      assertEquals(Ip.parse("10.1.1.1"), inflate(trie, compressedIp));
    }

    {
      BDD bdd = bdd0_24.diff(bdd1_32).diff(bdd2_32);
      assertEquals("<106:1, 124:1>", bdd.toReprString());

      Ip compressedIp = Ip.create(pkt.getDstIp().getValueSatisfying(bdd).get());

      assertEquals(Ip.parse("2.0.0.128"), compressedIp);
      Ip inflatedIp = inflate(trie, compressedIp);
      assertEquals(Ip.parse("10.1.1.128"), inflatedIp);
      assertEquals(Ip.parse("10.1.1.0"), minEquivalentIp(prefixes, inflatedIp, pkt.getDstIp()));
    }
  }

  @Test
  public void testSimple2() {
    PrefixTrieMultiMap<Prefix> prefixes = new PrefixTrieMultiMap<>();
    prefixes.put(Prefix.ZERO, Prefix.ZERO);
    prefixes.put(Prefix.parse("10.1.1.0/24"), Prefix.ZERO);
    prefixes.put(Prefix.parse("10.1.1.1/32"), Prefix.ZERO);
    prefixes.put(Prefix.parse("10.1.1.2/32"), Prefix.ZERO);
    prefixes.put(Prefix.parse("8.8.8.8/32"), Prefix.ZERO);

    PrefixTrieMultiMap<IpWildcard> trie = buildDependentBitsTrie(prefixes);
    // 10.1.1.0/24 compresses to 1 bit
    assertEquals(
        IpWildcard.parse("2.0.0.0:253.255.255.255"), compress(trie, Prefix.parse("10.1.1.0/24")));
    assertEquals(Ip.parse("10.1.1.1"), inflate(trie, Ip.parse("2.0.0.0")));
    // 10.1.1.1 compresses to 3 bits
    assertEquals(
        IpWildcard.parse("2.0.0.0:253.255.255.125"), compress(trie, Prefix.parse("10.1.1.1/32")));
    assertEquals(Ip.parse("10.1.1.1"), inflate(trie, Ip.parse("2.0.0.0")));

    // 10.1.1.2 compresses to 3 bits
    assertEquals(
        IpWildcard.parse("2.0.0.2:253.255.255.125"), compress(trie, Prefix.parse("10.1.1.2/32")));
    assertEquals(Ip.parse("10.1.1.2"), inflate(trie, Ip.parse("2.0.0.2")));

    // 10.1.1.1.3 compresses to 3 bits
    BDDPacket pkt = new BDDPacket();
    BDD bdd0_24 = pkt.getDstIp().toBDD(compress(trie, Prefix.parse("10.1.1.0/24")));
    BDD bdd1_32 = pkt.getDstIp().toBDD(compress(trie, Prefix.parse("10.1.1.1/32")));
    BDD bdd2_32 = pkt.getDstIp().toBDD(compress(trie, Prefix.parse("10.1.1.2/32")));

    assertEquals("<106:1>", bdd0_24.toReprString());
    assertEquals("<106:1, 124:0, 130:0>", bdd1_32.toReprString());
    assertEquals("<106:1, 124:0, 130:1>", bdd2_32.toReprString());

    /* Note: don't always get minimum value representatives. We can improve that in the inflator, just need some
     * new prefix trie APIs.
     */

    {
      BDD bdd = bdd0_24.diff(bdd1_32);
      assertEquals("<106:1, 124:0, 130:1><106:1, 124:1>", bdd.toReprString());

      Ip compressedIp = Ip.create(pkt.getDstIp().getValueSatisfying(bdd).get());

      assertEquals(Ip.parse("2.0.0.2"), compressedIp);
      assertEquals(Ip.parse("10.1.1.2"), inflate(trie, compressedIp));
    }

    {
      BDD bdd = bdd0_24.diff(bdd2_32);
      assertEquals("<106:1, 124:0, 130:0><106:1, 124:1>", bdd.toReprString());

      Ip compressedIp = Ip.create(pkt.getDstIp().getValueSatisfying(bdd).get());

      assertEquals(Ip.parse("2.0.0.0"), compressedIp);
      assertEquals(Ip.parse("10.1.1.1"), inflate(trie, compressedIp));
    }

    {
      BDD bdd = bdd0_24.diff(bdd1_32).diff(bdd2_32);
      assertEquals("<106:1, 124:1>", bdd.toReprString());

      Ip compressedIp = Ip.create(pkt.getDstIp().getValueSatisfying(bdd).get());

      assertEquals(Ip.parse("2.0.0.128"), compressedIp);
      Ip inflatedIp = inflate(trie, compressedIp);
      assertEquals(Ip.parse("10.1.1.128"), inflatedIp);
      assertEquals(Ip.parse("10.1.1.0"), minEquivalentIp(prefixes, inflatedIp, pkt.getDstIp()));
    }
  }
}
