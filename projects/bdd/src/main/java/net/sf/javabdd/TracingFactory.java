/*
 * Note: We obtained permission from the author of Javabdd, John Whaley, to use
 * the library with Batfish under the MIT license. The email exchange is included
 * in LICENSE.email file.
 *
 * MIT License
 *
 * Copyright (c) 2013-2017 John Whaley
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package net.sf.javabdd;

import com.carrotsearch.hppc.IntIntHashMap;
import com.carrotsearch.hppc.IntIntMap;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringJoiner;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import javax.annotation.Nullable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This is a 100% Java implementation of the BDD factory.
 *
 * <p>It was originally authored by John Whaley, and has since been heavily modified and improved by
 * the Batfish Authors.
 */
public final class TracingFactory extends JFactory {
  private static final Logger LOGGER = LogManager.getLogger(TracingFactory.class);

  /** The actual trace. */
  private final List<TracedOperation> _trace;

  /**
   * Every time a new BDD is assigned (in {@code bdd_makenode}) we track it here. This is used to
   * disambiguate different BDDs returned by the inner factory that might have the same IDs after
   * freeing.
   */
  private final IntIntMap _indexToSequence;

  /** Returns the current sequence number assigned to the given {@link BDDImpl}. */
  private int tracingSequence(BDDImpl bdd) {
    return _indexToSequence.get(bdd._index);
  }

  /**
   * Implements the trace operation. The result is passed via {@link Supplier} in case of mutating
   * arguments.
   */
  private TracedBDDImpl trace(Supplier<BDD> result, Operation op, BDDImpl... args) {
    int[] uniqueArgs = Arrays.stream(args).mapToInt(this::tracingSequence).toArray();
    BDDImpl resultImpl = (BDDImpl) result.get();
    _trace.add(new TracedOperation(tracingSequence(resultImpl), null, op, uniqueArgs));
    return new TracedBDDImpl(resultImpl);
  }

  /** Implements the trace operation. */
  private TracedBDDImpl traceNoArgs(BDD result, Operation op) {
    BDDImpl resultImpl = (BDDImpl) result;
    _trace.add(new TracedOperation(tracingSequence(resultImpl), null, op));
    return new TracedBDDImpl(resultImpl);
  }

  /**
   * Traces an operation. The result is passed via {@link Supplier} in case of mutating arguments.
   */
  private TracedBDDImpl traceWithInt(
      Supplier<BDD> getResult, int val, Operation op, BDDImpl... args) {
    int[] uniqueArgs = Arrays.stream(args).mapToInt(this::tracingSequence).toArray();
    BDDImpl resultImpl = (BDDImpl) getResult.get();
    _trace.add(new TracedOperation(tracingSequence(resultImpl), val, op, uniqueArgs));
    return new TracedBDDImpl(resultImpl);
  }

  /** Implements the trace operation. */
  private void traceNoResult(Operation op, BDDImpl... args) {
    int[] uniqueArgs = Arrays.stream(args).mapToInt(this::tracingSequence).toArray();
    _trace.add(new TracedOperation(null, null, op, uniqueArgs));
  }

  private void traceIntNoResult(Operation op, int arg) {
    _trace.add(new TracedOperation(null, arg, op));
  }

  private enum Operation {
    /** {@link BDDFactory#andAll}. */
    AND_ALL,
    /** {@link BDDFactory#andAllAndFree}. */
    AND_ALL_FREE,
    /** {@link BDD#andSat(BDD)}. */
    AND_SAT,
    /** Standard binary operation with bdd_apply such as {@link BDD#and(BDD)}. */
    APPLY,
    /** Standard binary operation with bdd_applyEq such as {@link BDD#andEq(BDD)}. */
    APPLY_EQ,
    /** {@link BDD#applyEx(BDD, BDDOp, BDD)}. */
    APPLY_EX,
    /** Standard binary operation with bdd_applyWith such as {@link BDD#andWith(BDD)}. */
    APPLY_WITH,
    /** {@link BDD#diffSat(BDD)}. */
    DIFF_SAT,
    /** {@link BDD#exist(BDD)}. */
    EXIST,
    /** {@link BDD#existEq(BDD)}. */
    EXIST_EQ,
    /** {@link BDD#free()}. */
    FREE,
    /** {@link BDD#fullSatOne()}. */
    FULL_SAT_ONE,
    /** {@link BDD#high()}. */
    HIGH,
    /** {@link BDD#id()}. */
    ID,
    /** {@link BDD#ite(BDD, BDD)}. */
    ITE,
    /** {@link BDDFactory#ithVar(int)}. */
    ITH_VAR,
    /** {@link BDD#low()}. */
    LOW,
    /** {@link BDD#minAssignmentBits()}. */
    MIN_ASSIGNMENT_BITS,
    /** {@link BDDFactory#nithVar(int)}. */
    NITH_VAR,
    /** {@link BDD#not()}. */
    NOT,
    /** {@link BDD#notEq()}. */
    NOT_EQ,
    /** {@link BDDFactory#one}. */
    ONE,
    /** {@link BDDFactory#orAll}. */
    OR_ALL,
    /** {@link BDDFactory#orAllAndFree}. */
    OR_ALL_FREE,
    /** {@link BDD#project(BDD)}. */
    PROJECT,
    /** {@link BDD#randomFullSatOne(int)}. */
    RANDOM_FULL_SAT_ONE,
    /** {@link BDD#satOne()}. */
    SAT_ONE,
    /** {@link BDDFactory#setVarNum(int)}. */
    SET_VAR_NUM,
    /** {@link BDD#support()}. */
    SUPPORT,
    /** {@link BDD#testsVars(BDD)}. */
    TESTS_VARS,
    /** {@link BDDFactory#zero}. */
    ZERO,
  }

  /** Used to log and reply all BDD operations on a factory. */
  private static class TracedOperation implements Serializable {
    private static final long serialVersionUID = -490376373193321074L;
    private final Operation _op;
    private final @Nullable Integer _resultSeq;
    private final @Nullable Integer _intArg;
    private final int[] _argSeqs;

    public TracedOperation(
        @Nullable Integer resultSeq, @Nullable Integer intArg, Operation op, int... argSeqs) {
      _resultSeq = resultSeq;
      _intArg = intArg;
      _op = op;
      _argSeqs = argSeqs;
    }

    @Override
    public String toString() {
      return new StringJoiner(", ", TracedOperation.class.getSimpleName() + "[", "]")
          .add("op=" + _op)
          .add("intArg=" + _intArg)
          .add("resultSeq=" + _resultSeq)
          .add("argSeqs=" + _argSeqs.length)
          .toString();
    }
  }

  /** Saves the trace created by this {@link TracingFactory} at the given {@link Path}. */
  public void saveTrace(Path path) throws IOException {
    try (FileOutputStream fos = new FileOutputStream(path.toFile())) {
      try (GZIPOutputStream gzip = new GZIPOutputStream(fos)) {
        try (ObjectOutputStream oos = new ObjectOutputStream(gzip)) {
          oos.writeObject(_trace);
        }
      }
    }
  }

  /** Replays the trace at the given {@link Path}. */
  public static void replayTrace(BDDFactory factory, Path path)
      throws IOException, ClassNotFoundException {
    System.err.println("Reading trace from " + path);
    LOGGER.info("Reading trace from {}", path);
    List<TracedOperation> trace;
    try (FileInputStream fis = new FileInputStream(path.toFile())) {
      try (GZIPInputStream gis = new GZIPInputStream(fis)) {
        try (ObjectInputStream ois = new ObjectInputStream(gis)) {
          trace = (List<TracedOperation>) ois.readObject();
        }
      }
    }
    replay(factory, trace);
  }

  private static void replay(BDDFactory factory, List<TracedOperation> trace) {
    Replayer replayer = new Replayer((JFactory) factory);
    int count = 0;
    for (TracedOperation operation : trace) {
      if (++count % 10000 == 0) {
        System.err.println("Replaying a " + operation._op + ": " + count + " / " + trace.size());
        LOGGER.info("Replaying a {}", operation._op);
      }
      replayer.replayTracedOperation(operation);
    }
    Map<String, Long> opCounts =
        trace.stream()
            .map(to -> to._op)
            .collect(Collectors.groupingBy(Operation::toString, Collectors.counting()));
    System.err.println(
        "Operator counts: "
            + opCounts.entrySet().stream()
                .sorted(Comparator.comparingLong(e -> opCounts.get(e.getKey())))
                .map(Entry::toString)
                .collect(Collectors.joining(", ")));
  }

  private static class Replayer {
    private final IntIntMap _sequenceToReplayIndex;
    private final JFactory _factory;

    public Replayer(JFactory factory) {
      _factory = factory;
      _sequenceToReplayIndex = new IntIntHashMap();
    }

    /** Creates a BDD without incrementing its reference count. */
    private BDD replayBdd(int seq) {
      int index = _sequenceToReplayIndex.getOrDefault(seq, -1);
      if (index == -1) {
        throw new IllegalStateException("No mapping for seq " + seq + " found ");
      }
      BDDImpl ret = (BDDImpl) _factory.zero();
      ret._index = index;
      return ret;
    }

    private void recordSequence(int seq, BDD result) {
      BDDImpl res = (BDDImpl) result;
      int existingIndex = _sequenceToReplayIndex.getOrDefault(seq, -1);
      if (existingIndex == -1) {
        System.err.println("Assigning seq " + seq + " as index " + res._index);
        _sequenceToReplayIndex.put(seq, res._index);
      } else if (res._index != existingIndex) {
        throw new IllegalStateException("Reassigned index for sequence " + seq);
      }
    }

    private void replayTracedOperation(TracedOperation operation) {
      System.err.println("Replaying a " + operation._op);
      switch (operation._op) {
        case AND_ALL:
          {
            assert operation._resultSeq != null;
            assert operation._intArg == null;
            BDD[] ops =
                Arrays.stream(operation._argSeqs).mapToObj(this::replayBdd).toArray(BDD[]::new);
            recordSequence(operation._resultSeq, _factory.andAll(ops));
          }
          break;
        case AND_ALL_FREE:
          {
            assert operation._resultSeq != null;
            assert operation._intArg == null;
            BDD[] ops =
                Arrays.stream(operation._argSeqs).mapToObj(this::replayBdd).toArray(BDD[]::new);
            recordSequence(operation._resultSeq, _factory.andAllAndFree(ops));
          }
          break;
        case AND_SAT:
          assert operation._resultSeq == null;
          assert operation._intArg == null;
          replayBdd(operation._argSeqs[0]).andSat(replayBdd(operation._argSeqs[1]));
          break;
        case APPLY:
          assert operation._resultSeq != null;
          assert operation._intArg != null;
          recordSequence(
              operation._resultSeq,
              replayBdd(operation._argSeqs[0])
                  .apply(replayBdd(operation._argSeqs[1]), getOp(operation._intArg), true));
          break;
        case APPLY_EQ:
          assert operation._resultSeq != null;
          assert operation._intArg != null;
          recordSequence(
              operation._resultSeq,
              replayBdd(operation._argSeqs[0])
                  .apply(replayBdd(operation._argSeqs[1]), getOp(operation._intArg), false));
          break;
        case APPLY_EX:
          assert operation._resultSeq != null;
          assert operation._intArg != null;
          recordSequence(
              operation._resultSeq,
              replayBdd(operation._argSeqs[0])
                  .applyEx(
                      replayBdd(operation._argSeqs[1]),
                      getOp(operation._intArg),
                      replayBdd(operation._argSeqs[2])));
          break;
        case APPLY_WITH:
          assert operation._resultSeq != null;
          assert operation._intArg != null;
          recordSequence(
              operation._resultSeq,
              replayBdd(operation._argSeqs[0])
                  .applyWith(replayBdd(operation._argSeqs[1]), getOp(operation._intArg)));
          break;
        case DIFF_SAT:
          assert operation._resultSeq == null;
          assert operation._intArg == null;
          replayBdd(operation._argSeqs[0]).diffSat(replayBdd(operation._argSeqs[1]));
          break;
        case EXIST:
          assert operation._resultSeq != null;
          recordSequence(
              operation._resultSeq,
              replayBdd(operation._argSeqs[0]).exist(replayBdd(operation._argSeqs[1])));
          break;
        case EXIST_EQ:
          assert operation._resultSeq != null;
          recordSequence(
              operation._resultSeq,
              replayBdd(operation._argSeqs[0]).existEq(replayBdd(operation._argSeqs[1])));
          break;
        case FREE:
          replayBdd(operation._argSeqs[0]).free();
          break;
        case FULL_SAT_ONE:
          replayBdd(operation._argSeqs[0]).fullSatOne();
          break;
        case ID:
          replayBdd(operation._argSeqs[0]).id();
          break;
        case ITE:
          assert operation._resultSeq != null;
          recordSequence(
              operation._resultSeq,
              replayBdd(operation._argSeqs[0])
                  .ite(replayBdd(operation._argSeqs[1]), replayBdd(operation._argSeqs[2])));
          break;
        case ITH_VAR:
          assert operation._resultSeq != null;
          assert operation._intArg != null;
          recordSequence(operation._resultSeq, _factory.ithVar(operation._intArg));
          break;
        case MIN_ASSIGNMENT_BITS:
          replayBdd(operation._argSeqs[0]).minAssignmentBits();
          break;
        case NITH_VAR:
          assert operation._resultSeq != null;
          assert operation._intArg != null;
          recordSequence(operation._resultSeq, _factory.nithVar(operation._intArg));
          break;
        case NOT:
          assert operation._resultSeq != null;
          recordSequence(operation._resultSeq, replayBdd(operation._argSeqs[0]).not());
          break;
        case ONE:
          assert operation._resultSeq != null;
          recordSequence(operation._resultSeq, _factory.one());
          break;
        case OR_ALL:
          {
            assert operation._resultSeq != null;
            BDD[] ops =
                Arrays.stream(operation._argSeqs).mapToObj(this::replayBdd).toArray(BDD[]::new);
            recordSequence(operation._resultSeq, _factory.orAll(ops));
          }
          break;
        case OR_ALL_FREE:
          {
            assert operation._resultSeq != null;
            BDD[] ops =
                Arrays.stream(operation._argSeqs).mapToObj(this::replayBdd).toArray(BDD[]::new);
            recordSequence(operation._resultSeq, _factory.orAllAndFree(ops));
          }
          break;
        case PROJECT:
          assert operation._resultSeq != null;
          recordSequence(
              operation._resultSeq,
              replayBdd(operation._argSeqs[0]).project(replayBdd(operation._argSeqs[1])));
          break;
        case RANDOM_FULL_SAT_ONE:
          assert operation._resultSeq != null;
          recordSequence(
              operation._resultSeq,
              replayBdd(operation._argSeqs[0]).randomFullSatOne(operation._argSeqs[1]));
          break;
        case SAT_ONE:
          assert operation._resultSeq != null;
          recordSequence(operation._resultSeq, replayBdd(operation._argSeqs[0]).satOne());
          break;
        case SET_VAR_NUM:
          assert operation._intArg != null;
          _factory.setVarNum(operation._intArg);
          break;
        case SUPPORT:
          assert operation._resultSeq != null;
          recordSequence(operation._resultSeq, replayBdd(operation._argSeqs[0]).support());
          break;
        case TESTS_VARS:
          assert operation._resultSeq == null;
          assert operation._intArg == null;
          replayBdd(operation._argSeqs[0]).testsVars(replayBdd(operation._argSeqs[1]));
          break;
        case ZERO:
          assert operation._resultSeq != null;
          recordSequence(operation._resultSeq, _factory.zero());
          break;
        default:
          throw new IllegalStateException("Unsupported replay of " + operation._op);
      }
    }
  }

  private TracingFactory() {
    _trace = new ArrayList<>();
    _indexToSequence = new IntIntHashMap();
    // These do not go through #newNodeIndex.
    BDDImpl zero = (BDDImpl) super.zero();
    BDDImpl one = (BDDImpl) super.one();
    newNodeIndex(zero._index);
    newNodeIndex(one._index);
    traceNoArgs(zero, Operation.ZERO);
    traceNoArgs(one, Operation.ONE);
  }

  public static BDDFactory init(int nodenum, int cachesize) {
    BDDFactory f = new TracingFactory();
    f.initialize(nodenum, cachesize);
    return f;
  }

  /** Wrapper for the BDD index number used internally in the representation. */
  private class TracedBDDImpl extends BDD {
    BDDImpl _bdd;

    TracedBDDImpl(BDDImpl bdd) {
      _bdd = bdd;
    }

    @Override
    public BDDFactory getFactory() {
      return TracingFactory.this;
    }

    @Override
    public boolean isZero() {
      return _bdd.isZero();
    }

    @Override
    public boolean isOne() {
      return _bdd.isOne();
    }

    @Override
    public boolean isAssignment() {
      return _bdd.isAssignment();
    }

    @Override
    public int var() {
      return _bdd.var();
    }

    @Override
    public boolean isAnd() {
      return _bdd.isAnd();
    }

    @Override
    public boolean isNor() {
      return _bdd.isNor();
    }

    @Override
    public boolean isVar() {
      return _bdd.isVar();
    }

    @Override
    public TracedBDDImpl high() {
      return trace(() -> _bdd.high(), Operation.HIGH, _bdd);
    }

    @Override
    public TracedBDDImpl low() {
      return trace(() -> _bdd.low(), Operation.LOW, _bdd);
    }

    @Override
    public TracedBDDImpl id() {
      return trace(() -> _bdd.id(), Operation.ID, _bdd);
    }

    @Override
    public TracedBDDImpl not() {
      return trace(() -> _bdd.not(), Operation.NOT, _bdd);
    }

    public TracedBDDImpl notEq() {
      return trace(() -> _bdd.notEq(), Operation.NOT_EQ, _bdd);
    }

    @Override
    public TracedBDDImpl ite(BDD thenBDD, BDD elseBDD) {
      TracedBDDImpl tracedThen = (TracedBDDImpl) thenBDD;
      TracedBDDImpl tracedElse = (TracedBDDImpl) elseBDD;
      return trace(
          () -> _bdd.ite(tracedThen._bdd, tracedElse._bdd),
          Operation.ITE,
          _bdd,
          tracedThen._bdd,
          tracedElse._bdd);
    }

    @Override
    public TracedBDDImpl relprod(BDD that, BDD var) {
      throw new UnsupportedOperationException();
    }

    @Override
    public TracedBDDImpl compose(BDD g, int var) {
      throw new UnsupportedOperationException();
    }

    @Override
    public TracedBDDImpl veccompose(BDDPairing pair) {
      throw new UnsupportedOperationException();
    }

    @Override
    public TracedBDDImpl constrain(BDD that) {
      throw new UnsupportedOperationException();
    }

    @Override
    TracedBDDImpl exist(BDD var, boolean makeNew) {
      TracedBDDImpl tracedVar = (TracedBDDImpl) var;
      return trace(
          () -> _bdd.exist(tracedVar._bdd, makeNew),
          makeNew ? Operation.EXIST : Operation.EXIST_EQ,
          _bdd,
          tracedVar._bdd);
    }

    @Override
    public boolean testsVars(BDD var) {
      TracedBDDImpl tracedVar = (TracedBDDImpl) var;
      traceNoResult(Operation.TESTS_VARS, _bdd, tracedVar._bdd);
      return _bdd.testsVars(tracedVar._bdd);
    }

    @Override
    public TracedBDDImpl project(BDD var) {
      TracedBDDImpl tracedVar = (TracedBDDImpl) var;
      return trace(() -> _bdd.project(tracedVar._bdd), Operation.PROJECT, _bdd, tracedVar._bdd);
    }

    @Override
    public TracedBDDImpl forAll(BDD var) {
      throw new UnsupportedOperationException();
    }

    @Override
    public TracedBDDImpl unique(BDD var) {
      throw new UnsupportedOperationException();
    }

    @Override
    public TracedBDDImpl restrict(BDD var) {
      throw new UnsupportedOperationException();
    }

    @Override
    public TracedBDDImpl restrictWith(BDD that) {
      throw new UnsupportedOperationException();
    }

    @Override
    public TracedBDDImpl simplify(BDD d) {
      throw new UnsupportedOperationException();
    }

    @Override
    public TracedBDDImpl support() {
      return trace(() -> _bdd.support(), Operation.SUPPORT, _bdd);
    }

    @Override
    public boolean andSat(BDD that) {
      TracedBDDImpl tThat = (TracedBDDImpl) that;
      traceNoResult(Operation.AND_SAT, _bdd, tThat._bdd);
      return _bdd.andSat(tThat._bdd);
    }

    @Override
    public boolean diffSat(BDD that) {
      TracedBDDImpl tThat = (TracedBDDImpl) that;
      traceNoResult(Operation.DIFF_SAT, _bdd, tThat._bdd);
      return _bdd.diffSat(tThat._bdd);
    }

    @Override
    TracedBDDImpl apply(BDD that, BDDOp opr, boolean makeNew) {
      TracedBDDImpl tThat = (TracedBDDImpl) that;
      return traceWithInt(
          () -> _bdd.apply(tThat._bdd, opr, makeNew),
          opr.id,
          makeNew ? Operation.APPLY : Operation.APPLY_EQ,
          _bdd,
          tThat._bdd);
    }

    @Override
    public TracedBDDImpl applyWith(BDD that, BDDOp opr) {
      TracedBDDImpl tThat = (TracedBDDImpl) that;
      return traceWithInt(
          () -> _bdd.applyWith(tThat._bdd, opr), opr.id, Operation.APPLY_WITH, _bdd, tThat._bdd);
    }

    @Override
    public TracedBDDImpl applyAll(BDD that, BDDOp opr, BDD var) {
      throw new UnsupportedOperationException();
    }

    @Override
    public TracedBDDImpl applyEx(BDD that, BDDOp opr, BDD var) {
      TracedBDDImpl tThat = (TracedBDDImpl) that;
      TracedBDDImpl tVar = (TracedBDDImpl) var;
      return traceWithInt(
          () -> _bdd.applyEx(tThat._bdd, opr, tVar._bdd),
          opr.id,
          Operation.APPLY_EX,
          _bdd,
          tThat._bdd,
          tVar._bdd);
    }

    @Override
    public BDD transform(BDD rel, BDDPairing pair) {
      throw new UnsupportedOperationException();
    }

    @Override
    public TracedBDDImpl applyUni(BDD that, BDDOp opr, BDD var) {
      throw new UnsupportedOperationException();
    }

    @Override
    public TracedBDDImpl satOne() {
      return trace(() -> _bdd.satOne(), Operation.SAT_ONE, _bdd);
    }

    @Override
    public TracedBDDImpl fullSatOne() {
      return trace(() -> _bdd.fullSatOne(), Operation.FULL_SAT_ONE, _bdd);
    }

    @Override
    public BitSet minAssignmentBits() {
      traceNoResult(Operation.MIN_ASSIGNMENT_BITS, _bdd);
      return _bdd.minAssignmentBits();
    }

    @Override
    public TracedBDDImpl randomFullSatOne(int seed) {
      return traceWithInt(
          () -> _bdd.randomFullSatOne(seed), seed, Operation.RANDOM_FULL_SAT_ONE, _bdd);
    }

    @Override
    public TracedBDDImpl satOne(BDD var, boolean pol) {
      throw new UnsupportedOperationException();
    }

    @Override
    public TracedBDDImpl replace(BDDPairing pair) {
      throw new UnsupportedOperationException();
    }

    @Override
    public TracedBDDImpl replaceWith(BDDPairing pair) {
      throw new UnsupportedOperationException();
    }

    @Override
    public int nodeCount() {
      return _bdd.nodeCount();
    }

    @Override
    public double pathCount() {
      return _bdd.pathCount();
    }

    @Override
    public double satCount() {
      return _bdd.satCount();
    }

    @Override
    public int[] varProfile() {
      return _bdd.varProfile();
    }

    @Override
    public boolean equals(@Nullable Object o) {
      if (!(o instanceof TracedBDDImpl)) {
        return false;
      }
      TracedBDDImpl that = (TracedBDDImpl) o;
      return _bdd.equals(that._bdd);
    }

    @Override
    public int hashCode() {
      return _bdd.hashCode();
    }

    @Override
    public void free() {
      traceNoResult(Operation.FREE, _bdd);
      _bdd.free();
    }
  }

  private int bddproduced; /* Number of new nodes ever produced */

  @Override
  protected void newNodeIndex(int index) {
    _indexToSequence.put(index, ++bddproduced);
  }

  @Override
  public BDD zero() {
    BDDImpl ret = (BDDImpl) super.zero();
    // Deliberately not tracing "trivial" operation.
    return new TracedBDDImpl(ret);
  }

  @Override
  public BDD one() {
    BDDImpl ret = (BDDImpl) super.one();
    // Deliberately not tracing "trivial" operation.
    return new TracedBDDImpl(ret);
  }

  @Override
  public BDD ithVar(int var) {
    return traceWithInt(() -> super.ithVar(var), var, Operation.ITH_VAR);
  }

  @Override
  public BDD nithVar(int var) {
    return traceWithInt(() -> super.nithVar(var), var, Operation.NITH_VAR);
  }

  @Override
  public BDD andAll(Collection<BDD> bddOperands, boolean free) {
    List<BDD> implOperands = new LinkedList<>();
    bddOperands.forEach(
        bdd -> {
          BDDImpl impl = ((TracedBDDImpl) bdd)._bdd;
          implOperands.add(impl);
        });
    return trace(
        () -> super.andAll(implOperands, free),
        free ? Operation.AND_ALL_FREE : Operation.AND_ALL,
        implOperands.stream().map(bdd -> (BDDImpl) bdd).toArray(BDDImpl[]::new));
  }

  @Override
  protected BDD orAll(Collection<BDD> bddOperands, boolean free) {
    List<BDD> implOperands = new LinkedList<>();
    bddOperands.forEach(
        bdd -> {
          BDDImpl impl = ((TracedBDDImpl) bdd)._bdd;
          implOperands.add(impl);
        });
    return trace(
        () -> super.orAll(implOperands, free),
        free ? Operation.OR_ALL_FREE : Operation.OR_ALL,
        implOperands.stream().map(bdd -> (BDDImpl) bdd).toArray(BDDImpl[]::new));
  }

  @Override
  public int setVarNum(int num) {
    traceIntNoResult(Operation.SET_VAR_NUM, num);
    return super.setVarNum(num);
  }

  /// Unsupported operations below here

  @Override
  public BDD buildCube(int value, List<BDD> variables) {
    throw new UnsupportedOperationException();
  }

  @Override
  public BDD buildCube(int value, int[] variables) {
    throw new UnsupportedOperationException();
  }

  @Override
  public BDD makeSet(int[] varset) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int extVarNum(int num) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int[] getVarOrder() {
    return super.getVarOrder();
  }

  @Override
  public BDDPairing makePair(int oldvar, int newvar) {
    throw new UnsupportedOperationException();
  }

  @Override
  public BDDPairing makePair(int oldvar, BDD newvar) {
    throw new UnsupportedOperationException();
  }

  @Override
  public BDDPairing makePair(BDDDomain oldvar, BDDDomain newvar) {
    throw new UnsupportedOperationException();
  }

  @Override
  public BDDDomain extDomain(long domainSize) {
    throw new UnsupportedOperationException();
  }

  @Override
  public BDDDomain extDomain(BigInteger domainSize) {
    throw new UnsupportedOperationException();
  }

  @Override
  public BDDDomain[] extDomain(int[] dom) {
    throw new UnsupportedOperationException();
  }

  @Override
  public BDDDomain[] extDomain(long[] dom) {
    throw new UnsupportedOperationException();
  }

  @Override
  public BDDDomain[] extDomain(BigInteger[] domainSizes) {
    throw new UnsupportedOperationException();
  }

  @Override
  public BDDDomain overlapDomain(BDDDomain d1, BDDDomain d2) {
    throw new UnsupportedOperationException();
  }

  @Override
  public BDD makeSet(BDDDomain[] v) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void clearAllDomains() {
    throw new UnsupportedOperationException();
  }

  @Override
  public int numberOfDomains() {
    throw new UnsupportedOperationException();
  }

  @Override
  public BDDDomain getDomain(int i) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int[] makeVarOrdering(boolean reverseLocal, String ordering) {
    throw new UnsupportedOperationException();
  }

  @Override
  public BDDBitVector buildVector(int bitnum, boolean b) {
    throw new UnsupportedOperationException();
  }

  @Override
  public BDDBitVector constantVector(int bitnum, long val) {
    throw new UnsupportedOperationException();
  }

  @Override
  public BDDBitVector constantVector(int bitnum, BigInteger val) {
    throw new UnsupportedOperationException();
  }

  @Override
  public BDDBitVector buildVector(int bitnum, int offset, int step) {
    throw new UnsupportedOperationException();
  }

  @Override
  public BDDBitVector buildVector(BDDDomain d) {
    throw new UnsupportedOperationException();
  }

  @Override
  public BDDBitVector buildVector(int[] var) {
    throw new UnsupportedOperationException();
  }
}
