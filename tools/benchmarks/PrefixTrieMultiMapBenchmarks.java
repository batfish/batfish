package tools.benchmarks;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.PrefixTrieMultiMap;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

/** {@link PrefixTrieMultiMap} operations on a real prefix list (one prefix per line). */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(1)
@Warmup(iterations = 3, time = 2)
@Measurement(iterations = 5, time = 2)
public class PrefixTrieMultiMapBenchmarks {
  @Param({"REQUIRED INPUT PARAM"})
  public String prefixFile;

  private Prefix[] _prefixes;
  private Ip[] _queries;
  private Prefix[] _shuffled;
  private PrefixTrieMultiMap<Integer> _trie;
  private int _counter;

  @Setup(Level.Trial)
  public void setUp() throws IOException {
    List<Prefix> ps =
        Files.readAllLines(Path.of(prefixFile)).stream()
            .map(String::trim)
            .filter(l -> !l.isEmpty())
            .distinct()
            .map(Prefix::parse)
            .collect(Collectors.toList());
    Random rng = new Random(1);
    Collections.shuffle(ps, rng);
    _prefixes = ps.toArray(new Prefix[0]);
    List<Prefix> sh = new ArrayList<>(ps);
    Collections.shuffle(sh, rng);
    _shuffled = sh.toArray(new Prefix[0]);
    _queries = new Ip[_prefixes.length];
    for (int i = 0; i < _prefixes.length; ++i) {
      Prefix p = _prefixes[i];
      long span = p.getEndIp().asLong() - p.getStartIp().asLong() + 1;
      _queries[i] = Ip.create(p.getStartIp().asLong() + (Math.abs(rng.nextLong()) % span));
    }
    _trie = new PrefixTrieMultiMap<>();
    for (int i = 0; i < _prefixes.length; ++i) {
      _trie.put(_prefixes[i], i);
    }
    // Some ECMP-like multi-valued nodes.
    for (int i = 0; i < _prefixes.length; i += 10) {
      _trie.put(_prefixes[i], i + 1_000_000);
    }
  }

  @Benchmark
  public void lpm(Blackhole bh) {
    for (Ip ip : _queries) {
      bh.consume(_trie.longestPrefixMatch(ip));
    }
  }

  @Benchmark
  public void get(Blackhole bh) {
    for (Prefix p : _shuffled) {
      bh.consume(_trie.get(p));
    }
  }

  @Benchmark
  public Object build() {
    PrefixTrieMultiMap<Integer> t = new PrefixTrieMultiMap<>();
    for (int i = 0; i < _prefixes.length; ++i) {
      t.put(_prefixes[i], i);
    }
    return t;
  }

  /** RibTree.mergeRoute-like: read the current elements, then replace them. */
  @Benchmark
  public void churn(Blackhole bh) {
    int c = ++_counter;
    for (Prefix p : _shuffled) {
      Set<Integer> cur = _trie.get(p);
      bh.consume(cur);
      _trie.replaceAll(p, c);
    }
  }

  /** The same, through a {@link PrefixTrieMultiMap.Handle}, as RibTree.mergeRoute now does. */
  @Benchmark
  public void churnHandle(Blackhole bh) {
    int c = ++_counter;
    for (Prefix p : _shuffled) {
      PrefixTrieMultiMap<Integer>.Handle h = _trie.handle(p);
      bh.consume(h.get());
      h.replaceAll(c);
    }
  }

  @Benchmark
  public Object allElements() {
    return _trie.getAllElements();
  }

  /** RibResolutionTrie.getAffectedNextHopIps-like traversal. */
  @Benchmark
  public void traverse(Blackhole bh) {
    for (int i = 0; i < 64; ++i) {
      Prefix q = _shuffled[i];
      _trie.traverseEntries(
          (p, s) -> bh.consume(p),
          (p, s) -> p.containsPrefix(q) || (q.containsPrefix(p) && !s.contains(-1)));
    }
  }
}
