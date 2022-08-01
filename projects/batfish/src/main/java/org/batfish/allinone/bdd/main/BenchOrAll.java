package org.batfish.allinone.bdd.main;

import java.util.Random;
import java.util.stream.IntStream;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.datamodel.Ip;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

@State(Scope.Benchmark)
public class BenchOrAll {
  private static final int NUM_IP_BDDS = 1000;

  private BDDPacket _pkt;
  private Random _rng;
  private BDD[] _ipBdds;

  @Param({"OrigJFactory", "JFactory"})
  public String _factoryName;

  @Param({"2", "4", "8", "16", "32", "64"})
  public String _numDisjunctsStr;

  private int _numDisjuncts;

  private Ip randomIp() {
    return Ip.create(Math.abs(_rng.nextInt()));
  }

  @Setup(Level.Trial)
  public void setUp() {
    _pkt = new BDDPacket(BDDFactory.init(_factoryName, 10_000_000, 1_000_000));
    _rng = new Random();
    _ipBdds =
        IntStream.range(0, NUM_IP_BDDS)
            .mapToObj(i -> _pkt.getDstIp().toBDD(randomIp()))
            .toArray(BDD[]::new);
    _numDisjuncts = Integer.parseInt(_numDisjunctsStr);
  }

  private BDD randomIpBdd() {
    return _ipBdds[_rng.nextInt(_ipBdds.length)];
  }

  @Benchmark
  public void benchOr() {
    BDD res = _pkt.getFactory().zero();
    for (int i = 0; i < _numDisjuncts; i++) {
      res.orEq(randomIpBdd());
    }
  }

  @Benchmark
  public void benchOrAll() {
    BDD[] disjuncts = new BDD[_numDisjuncts];
    for (int i = 0; i < _numDisjuncts; i++) {
      disjuncts[i] = randomIpBdd();
    }
    _pkt.getFactory().orAll(disjuncts);
  }
}
