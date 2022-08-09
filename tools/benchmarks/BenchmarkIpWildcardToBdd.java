package tools.benchmarks;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.common.bdd.ImmutableBDDInteger;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.Prefix;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class BenchmarkIpWildcardToBdd {
  private BDDPacket _pkt;
  private ImmutableBDDInteger _dstIp;
  private Random _rng;

  @Setup(Level.Trial)
  public void setUp() {
    _pkt = new BDDPacket();
    _dstIp = _pkt.getDstIp();
    _rng = new Random();
  }

  private Ip randomIp() {
    return Ip.create(Math.abs(_rng.nextInt()));
  }

  private IpWildcard randomIpWildcard() {
    return IpWildcard.ipWithWildcardMask(randomIp(), 0x00FF0000L);
  }

  private IpWildcard randomIpAsWildcard() {
    return IpWildcard.create(randomIp());
  }

  private IpWildcard randomPrefix24AsWildcard() {
    return IpWildcard.create(Prefix.create(randomIp(), 24));
  }

  @Benchmark
  public void benchRandomWildcard() {
    _dstIp.toBDD(randomIpWildcard()).free();
  }

  @Benchmark
  public void benchRandomIpAsWildcard() {
    _dstIp.toBDD(randomIpAsWildcard()).free();
  }

  @Benchmark
  public void benchRandomPrefix24AsWildcard() {
    _dstIp.toBDD(randomPrefix24AsWildcard()).free();
  }
}
