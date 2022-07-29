package org.batfish.allinone.bdd.main;

import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.common.bdd.IpSpaceToBDD;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.AclIpSpaceLine;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.UniverseIpSpace;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

@State(Scope.Benchmark)
public class BDDParallelismStressTest {
  public static class Config {
    public final String factory;
    public final int threads;

    public Config(String factory, int threads) {
      this.factory = factory;
      this.threads = threads;
    }
  }

  public static Config parseConfig(String spec) {
    String[] params = spec.split(":");
    String factory = params[0];
    int threads = Integer.parseInt(params[1]);
    return new Config(factory, threads);
  }

  @Param({"OrigJFactory:1", "JFactory:1"})
  public String _configSpec;

  private static long bench(String factory, int numThreads, boolean gcTime)
      throws InterruptedException {
    int total_iters = 10;
    System.gc();
    BDDPacket pkt = new BDDPacket(BDDFactory.init(factory, 10_000_000, 1_000_000));
    Random rng = new Random();
    Thread[] threads = new Thread[numThreads];

    IpSpaceToBDD toBdd =
        new IpSpaceToBDD(
            pkt.getDstIpSpaceToBDD(), ImmutableMap.of("foo", UniverseIpSpace.INSTANCE));

    long t = System.currentTimeMillis();
    for (int i = 0; i < numThreads; i++) {
      final int iFinal = i;
      Runnable job =
          () -> {
            for (int j = iFinal; j < total_iters; j += numThreads) {
              BDD bdd = pkt.getFactory().zero();
              BDD bddNot = pkt.getFactory().one();
              for (int n = 0; n < 1000; n++) {
                Ip ip1 = Ip.create(Math.abs(rng.nextInt()));
                Ip ip2 = Ip.create(Math.abs(rng.nextInt()));
                AclIpSpace aclIpSpace =
                    (AclIpSpace)
                        AclIpSpace.builder()
                            .then(AclIpSpaceLine.permit(ip1.toIpSpace()))
                            .then(AclIpSpaceLine.permit(ip2.toIpSpace()))
                            .build();
                BDD randBdd = toBdd.visit(aclIpSpace);

                bdd.orEq(randBdd);
                bddNot.diffWith(randBdd);
              }
              BDD bddNot2 = bdd.not();
              bdd.free();
              if (!bddNot2.equals(bddNot)) {
                throw new RuntimeException("yikes!");
              }
              bddNot.free();
              bddNot2.free();
            }
          };
      threads[i] = new Thread(job);
      threads[i].start();
    }

    for (int i = 0; i < numThreads; i++) {
      threads[i].join();
    }

    t = System.currentTimeMillis() - t;

    return gcTime ? pkt.getFactory().getGCStats().time : t;
  }

  private static boolean parseBoolean(String s) {
    if ("true".equalsIgnoreCase(s)) {
      return true;
    }
    if ("false".equalsIgnoreCase(s)) {
      return false;
    }
    throw new IllegalArgumentException("not a boolean: " + s);
  }

  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.SECONDS)
  public void bench() throws InterruptedException {
    Config cfg = parseConfig(_configSpec);
    bench(cfg.factory, cfg.threads, false);
  }

  public static void main(String[] args)
      throws IOException, ClassNotFoundException, InterruptedException {
    int minNumThreads = Integer.parseInt(args[0]);
    int maxNumThreads = Integer.parseInt(args[1]);
    int iters = Integer.parseInt(args[2]);
    boolean gcTime = parseBoolean(args[3]);
    // warm up
    for (int i = 0; i < 10; i++) {
      System.out.println("Warm up iteration " + i);
      bench("JFactory", 1, gcTime);
    }
    List<String> log = new ArrayList<>();
    for (int numThreads = minNumThreads; numThreads <= maxNumThreads; numThreads++) {
      int numThreadsFinal = numThreads;
      String times =
          IntStream.range(0, iters)
              .mapToObj(
                  i -> {
                    try {
                      return String.valueOf(bench("JFactory", numThreadsFinal, gcTime));
                    } catch (InterruptedException e) {
                      throw new RuntimeException(e);
                    }
                  })
              .collect(Collectors.joining(","));
      log.add(String.format("%s,%s", numThreads, times));
      log.forEach(System.out::println);
      System.out.println("--------------");
    }
    log.forEach(System.out::println);
  }
}
