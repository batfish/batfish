package org.batfish.minesweeper.bdd;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;
import javax.annotation.Nullable;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;

public class BDDPrimeImplicants implements Iterable<BDD> {

  private BDD _bdd;
  private BDDFactory _factory;
  // the number of variables in the original BDD
  private int _numVars;

  private BDD _primeImplicants;

  BDDPrimeImplicants(BDD bdd) {
    _bdd = bdd;
    _factory = bdd.getFactory();
    _numVars = _factory.varNum();
    int[] oldOrder = _factory.getVarOrder();
    int newSize = _numVars * 3;
    _factory.setVarNum(newSize);
    // each variable has an associated "occurrence" variable and "sign" variable;
    // each such pair of metavariables occurs in the same order as the corresponding
    // original variables
    int[] newOrder = new int[newSize];
    for (int i = 0; i < _numVars; i++) {
      int var = oldOrder[i];
      newOrder[i] = var;
      newOrder[var2OccurrenceVar(i)] = var2OccurrenceVar(var);
      newOrder[var2SignVar(i)] = var2SignVar(var);
    }
    _factory.setVarOrder(newOrder);
    _primeImplicants = computePrimeImplicantBDD(bdd, new HashMap<>());
  }

  public BDD getBDD() {
    return _bdd;
  }

  private int var2OccurrenceVar(int var) {
    return _numVars + 2 * var;
  }

  private int var2SignVar(int var) {
    return _numVars + 2 * var + 1;
  }

  private boolean isOccurrenceVar(int var) {
    return (var - _numVars) % 2 == 0;
  }

  private int metaVar2Var(int ovar) {
    return (ovar - _numVars) / 2;
  }

  /* Creates a BDD that represents all prime implicants of the given BDD.
    Uses the algorithm described in Section 4.4 of
    "Models and Algorithms for Computing Minimum-Size Prime Implicants" by Manquinho et al.,
    International Workshop on Boolean Problems (IWBP 1998)"
    It is based on a "metaproduct" representation (Section 4.3 and cited prior work), where
    each original variable is represented by two new variables: an "occurrence" variable
    that indicates whether the variable is in the prime implicant or not, and a "sign" variable
    that indicates whether the variable is positive or negative in the prime implicant.
  */
  private BDD computePrimeImplicantBDD(BDD bdd, Map<BDD, BDD> memoTable) {

    if (bdd.isOne() || bdd.isZero()) {
      return bdd;
    }

    BDD memoizedPI = memoTable.get(bdd);
    if (memoizedPI != null) {
      return memoizedPI;
    }

    int currVar = bdd.var();
    BDD highPI = computePrimeImplicantBDD(bdd.high(), memoTable);
    BDD lowPI = computePrimeImplicantBDD(bdd.low(), memoTable);
    BDD commonPI = highPI.and(lowPI);
    BDD highPIOnly = highPI.diff(lowPI);
    BDD lowPIOnly = lowPI.diff(highPI);
    int occurrenceVar = var2OccurrenceVar(currVar);
    int signVar = var2SignVar(currVar);
    BDD signPI =
        highPIOnly.and(_factory.ithVar(signVar)).or(lowPIOnly.and(_factory.nithVar(signVar)));
    BDD allPI =
        signPI
            .and(_factory.ithVar(occurrenceVar))
            .or(commonPI.and(_factory.nithVar(occurrenceVar)));
    memoTable.put(bdd, allPI);
    return allPI;
  }

  @Override
  public Iterator<BDD> iterator() {
    return new PrimeImplicantsIterator();
  }

  private static class SearchState {
    BDD _bdd;
    BDD _partialPI;
    // the number of variables in this potential prime implicant so far
    int _size;

    SearchState(BDD bdd, BDD partialPI, int size) {
      _bdd = bdd;
      _partialPI = partialPI;
      _size = size;
    }

    int size() {
      return _size;
    }
  }

  private class PrimeImplicantsIterator implements Iterator<BDD> {

    private PriorityQueue<SearchState> _workQueue;

    private List<BDD> _allPIs;

    @Nullable private BDD _nextPI;

    PrimeImplicantsIterator() {
      _workQueue = new PriorityQueue<>(Comparator.comparingInt(SearchState::size));
      _workQueue.offer(new SearchState(_primeImplicants, _factory.one(), 0));
      _allPIs = new LinkedList<>();
    }

    @Override
    public boolean hasNext() {
      if (_nextPI != null) {
        return true;
      }
      try {
        computeNextPI();
        return true;
      } catch (NoSuchElementException e) {
        return false;
      }
    }

    @Override
    public BDD next() {
      if (_nextPI == null) {
        computeNextPI();
      }
      BDD result = _nextPI;
      _nextPI = null;
      return result;
    }

    private void computeNextPI() {
      while (!_workQueue.isEmpty()) {

        SearchState curr = _workQueue.remove();
        BDD bdd = curr._bdd;
        BDD partialPI = curr._partialPI;

        if (bdd.isZero()) {
          continue;
        }
        if (bdd.isOne()) {
          // make sure that this implicant is not subsumed by an existing one, which seems
          // unavoidable due to the way the metaproduct is constructed
          if (_allPIs.stream().anyMatch(pi -> partialPI.imp(pi).isOne())) {
            continue;
          }
          _nextPI = partialPI;
          _allPIs.add(partialPI);
          return;
        }

        int metaVar = bdd.var();
        BDD low = bdd.low();
        BDD high = bdd.high();
        int var = metaVar2Var(metaVar);
        int size = curr._size;

        if (isOccurrenceVar(metaVar)) {
          _workQueue.offer(new SearchState(low, partialPI, size));
          _workQueue.offer(new SearchState(high, partialPI, size + 1));
        } else {
          _workQueue.offer(new SearchState(low, partialPI.id().and(_factory.nithVar(var)), size));
          _workQueue.offer(new SearchState(high, partialPI.id().and(_factory.ithVar(var)), size));
        }
      }

      throw new NoSuchElementException("there are no more prime implicants");
    }
  }
}
