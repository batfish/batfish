package org.batfish.allinone.bdd.main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import net.sf.javabdd.BDD;
import org.batfish.common.bdd.BDDPacket;

/** Replays a BDD trace. */
public final class BDDParallelismStressTest {
  private static long bench(int numThreads, boolean gcTime) throws InterruptedException {
    int total_iters = 100;
    System.gc();
    BDDPacket pkt = new BDDPacket();
    pkt.getFactory().setNodeTableSize(10_000_000);
    Random rng = new Random();
    Thread[] threads = new Thread[numThreads];
    long t = System.currentTimeMillis();
    for (int i = 0; i < numThreads; i++) {
      final int iFinal = i;
      Runnable job =
          () -> {
            for (int j = iFinal; j < total_iters; j += numThreads) {
              BDD bdd = pkt.getFactory().zero();
              for (int n = 0; n < 10000; n++) {
                bdd.orWith(pkt.getDstIp().value(Math.abs(rng.nextInt())));
              }
              bdd.free();
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

  public static void main(String[] args)
      throws IOException, ClassNotFoundException, InterruptedException {
    int minNumThreads = Integer.parseInt(args[0]);
    int maxNumThreads = Integer.parseInt(args[1]);
    int iters = Integer.parseInt(args[2]);
    boolean gcTime = parseBoolean(args[3]);
    BDDParallelismStressTest test = new BDDParallelismStressTest();
    // warm up
    for (int i = 0; i < 4; i++) {
      test.bench(1, gcTime);
    }
    List<String> log = new ArrayList<>();
    for (int numThreads = minNumThreads; numThreads <= maxNumThreads; numThreads++) {
      int numThreadsFinal = numThreads;
      String times =
          IntStream.range(0, iters)
              .mapToObj(
                  i -> {
                    try {
                      return String.valueOf(test.bench(numThreadsFinal, gcTime));
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
