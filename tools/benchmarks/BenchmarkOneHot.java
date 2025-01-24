package tools.benchmarks;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.JFactory;
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
public class BenchmarkOneHot {
  private static final int NUM_VARIANTS = 1 << 16;

  private BDDFactory _factory;
  private Random _rng;

  @Param({"8", "32", "128", "512"})
  public int _numDisjuncts;

  private List<BDD[]> _trials;

  @Setup(Level.Trial)
  public void setUp() {
    BDDFactory factory = JFactory.init(1_000_000, (1_000_000 + 8 - 1) / 8);
    factory.setCacheRatio(8);
    _factory = factory;
    int varNum = _numDisjuncts * 64;
    _factory.setVarNum(varNum);
    _rng = new Random();
    _trials = new ArrayList<>(NUM_VARIANTS);
    for (int i = 0; i < NUM_VARIANTS; i++) {
      SortedSet<Integer> used = new TreeSet<>();
      for (int j = 0; j < _numDisjuncts; j++) {
        int var;
        do {
          var = _rng.nextInt(varNum);
        } while (!used.add(var));
      }
      _trials.add(used.stream().map(_factory::ithVar).toArray(BDD[]::new));
    }
  }

  @TearDown(Level.Trial)
  public void tearDown() {
    System.out.println(_factory.getCacheStats().toString());
  }

  @Benchmark
  public void benchOneHot() {
    BDD[] trial = _trials.get(_rng.nextInt(_trials.size()));
    _factory.onehot(trial).free();
  }
}
