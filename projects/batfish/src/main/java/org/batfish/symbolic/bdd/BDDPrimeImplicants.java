package org.batfish.symbolic.bdd;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;

public class BDDPrimeImplicants implements Iterable<BDD> {

  private BDD _bdd;
  private BDDFactory _factory;
  // the number of variables in the original BDD
  private int _numVars;

  private BDD _primeImplicants;

  public BDDPrimeImplicants(BDD bdd) {
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
    _factory.disableReorder();
    _primeImplicants = computePrimeImplicantBDD(bdd, new HashMap<BDD, BDD>());
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

  private boolean isSignVar(int var) {
    return (var - _numVars) % 2 == 1;
  }

  private int signVar2Var(int svar) {
    return (svar - _numVars) / 2;
  }

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
    BDD highPIOnly = highPI.and(lowPI.not());
    BDD lowPIOnly = lowPI.and(highPI.not());
    int occurrenceVar = var2OccurrenceVar(currVar);
    int signVar = var2SignVar(currVar);
    BDD signPI =
        highPIOnly
            .andWith(_factory.ithVar(signVar))
            .or(lowPIOnly.andWith(_factory.nithVar(signVar)));
    BDD allPI =
        signPI
            .andWith(_factory.ithVar(occurrenceVar))
            .or(commonPI.andWith(_factory.nithVar(occurrenceVar)));
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

    public SearchState(BDD bdd, BDD partialPI) {
      _bdd = bdd;
      _partialPI = partialPI;
    }
  }

  private class PrimeImplicantsIterator implements Iterator<BDD> {

    private List<SearchState> _worklist;

    private List<BDD> _allPIs;

    private BDD _nextPI;

    PrimeImplicantsIterator() {
      _worklist = new LinkedList<>();
      _worklist.add(new SearchState(_primeImplicants, _factory.one()));
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
      while (!_worklist.isEmpty()) {

        SearchState curr = _worklist.remove(0);
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
        BDD high = bdd.high();
        BDD low = bdd.low();

        BDD highPI = partialPI.id();
        BDD lowPI = partialPI.id();
        if (isSignVar(metaVar)) {
          int var = signVar2Var(metaVar);
          highPI = highPI.andWith(_factory.ithVar(var));
          lowPI = lowPI.andWith(_factory.nithVar(var));
        }
        _worklist.add(new SearchState(high, highPI));
        _worklist.add(new SearchState(low, lowPI));
      }

      throw new NoSuchElementException("there are no more prime implicants");
    }
  }
}
