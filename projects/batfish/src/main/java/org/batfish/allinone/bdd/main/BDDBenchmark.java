package org.batfish.allinone.bdd.main;

import com.google.common.collect.ImmutableMap;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.IntStream;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.common.bdd.IpSpaceToBDD;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.AclIpSpaceLine;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.UniverseIpSpace;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

/**
 * Example usage:
 *
 * <p>bazel run //projects/batfish/src/main/java/org/batfish/allinone/bdd/main:benchmark \ -- -rf
 * json -jvmArgs -Xmx16G -p _factoryName=JFactory -t 1 benchStream
 *
 * <p>See jmh help (i.e. run with -h) to see what this all means. :)
 */
@State(Scope.Benchmark)
public class BDDBenchmark {
  @Param({"OrigJFactory", "JFactory"})
  public String _factoryName;

  private BDDPacket _pkt;
  private IpSpaceToBDD _toBdd;
  private Random _rng;

  @Setup(Level.Trial)
  public void setUp() {
    _pkt = new BDDPacket(BDDFactory.init(_factoryName, 10_000_000, 1_000_000));
    _toBdd =
        new IpSpaceToBDD(
            _pkt.getDstIpSpaceToBDD(), ImmutableMap.of("foo", UniverseIpSpace.INSTANCE));
    _rng = new Random();
  }

  private IpSpace randomIp() {
    return Ip.create(Math.abs(_rng.nextInt())).toIpSpace();
  }

  private void doOne() {
    BDD bdd =
        _toBdd.visit(
            AclIpSpace.builder()
                .then(AclIpSpaceLine.permit(randomIp()))
                .then(AclIpSpaceLine.permit(randomIp()))
                .then(AclIpSpaceLine.permit(randomIp()))
                .then(AclIpSpaceLine.permit(randomIp()))
                .then(AclIpSpaceLine.permit(randomIp()))
                .build());
    BDD bddNeg = bdd.not();
    BDD bdd2 = bddNeg.not();
    if (!bdd.equals(bdd2)) {
      throw new RuntimeException("sanity check failed");
    }
    bdd.free();
    bddNeg.free();
    bdd2.free();
  }

  @Benchmark
  public void bench() {
    doOne();
  }

  @Benchmark
  public void benchStream() {
    IntStream.range(0, 100)
        .mapToObj(i -> IntStream.range(0, 100).boxed())
        .flatMap(Function.identity())
        .parallel()
        .forEach(j -> doOne());
  }
}
