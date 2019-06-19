package org.batfish.minesweeper.abstraction;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.batfish.datamodel.Prefix;
import org.junit.Test;

public class PrefixTrieMapTest {
  @Test
  public void createDestinationMapTest1() {
    PrefixTrieMap prefixTrieMap = new PrefixTrieMap();
    prefixTrieMap.add(Prefix.parse("1.1.1.1/32"), "A");
    prefixTrieMap.add(Prefix.parse("1.1.1.0/30"), "B");
    Map<Set<String>, List<Prefix>> destMap = prefixTrieMap.createDestinationMap();
    Set<String> setA = new TreeSet<>();
    setA.add("A");
    assertThat(destMap.get(setA), hasSize(1));
    assert destMap.get(setA).contains(Prefix.parse("1.1.1.1/32"));

    // B contains all prefixes in 1.1.1.0/30 except for 1.1.1.1/32
    Set<String> setB = new TreeSet<>();
    setB.add("B");
    assertThat(destMap.get(setB), hasSize(2));
    assert destMap.get(setB).contains(Prefix.parse("1.1.1.2/31"));
    assert destMap.get(setB).contains(Prefix.parse("1.1.1.0/32"));
  }

  @Test
  public void createDestinationMapTest2() {
    PrefixTrieMap prefixTrieMap = new PrefixTrieMap();
    prefixTrieMap.add(Prefix.parse("1.1.1.1/32"), "A");
    prefixTrieMap.add(Prefix.parse("1.1.1.1/32"), "B");
    prefixTrieMap.add(Prefix.parse("1.1.1.0/30"), "A");
    Map<Set<String>, List<Prefix>> destMap = prefixTrieMap.createDestinationMap();

    assertThat(destMap.keySet(), hasSize(2));

    Set<String> setA = new TreeSet<>();
    setA.add("A");
    assertThat(destMap.get(setA), hasSize(2));
    assert destMap.get(setA).contains(Prefix.parse("1.1.1.0/32"));
    assert destMap.get(setA).contains(Prefix.parse("1.1.1.2/31"));

    Set<String> setAB = new TreeSet<>();
    setAB.add("A");
    setAB.add("B");
    assertThat(destMap.get(setAB), hasSize(1));
    assert destMap.get(setAB).contains(Prefix.parse("1.1.1.1/32"));
  }
}
