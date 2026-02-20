package tools.benchmarks;

import java.util.Random;
import java.util.stream.IntStream;
import net.sf.javabdd.BDD;
import net.sf.javabdd.JFactory;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.datamodel.Ip;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;

@BenchmarkMode(Mode.Throughput)
@State(Scope.Benchmark)
public class BenchmarkOrAll {
  private static final int NUM_IP_BDDS = 1000;

  private BDDPacket _pkt;
  private Random _rng;
  private BDD[] _ipBdds;

  @Param({"2", "4", "8", "16", "32", "64", "128", "256", "512"})
  public int _numDisjuncts;

  private BDD[] _disjuncts;
  int _trialOps;

  private Ip randomIp() {
    return Ip.create(Math.abs(_rng.nextInt()));
  }

  @Setup(Level.Trial)
  public void setUp() {
    _pkt = new BDDPacket(JFactory.init(10_000_000, 1_000_000));
    _rng = new Random();
    _ipBdds =
        IntStream.range(0, NUM_IP_BDDS)
            .mapToObj(i -> _pkt.getDstIp().toBDD(randomIp()))
            .toArray(BDD[]::new);
    _disjuncts = new BDD[_numDisjuncts];
  }

  @TearDown(Level.Trial)
  public void tearDown() {
    System.out.println(_pkt.getFactory().getCacheStats().toString());
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
    res.free();
  }

  @Benchmark
  public void benchOrAll() {
    for (int i = 0; i < _numDisjuncts; i++) {
      _disjuncts[i] = randomIpBdd();
    }
    _pkt.getFactory().orAll(_disjuncts).free();
  }
}
