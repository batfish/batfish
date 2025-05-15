/**
 * NDD implementation in JavaBDD version (compatible and extendable).
 * Based on JavaBDD package in {@see <a href="https://github.com/batfish/batfish">Batfish</a>}.
 * @author Zechun Li & Yichi Zhang - XJTU ANTS NetVerify Lab
 * @version 1.0
 */
package net.sf.javabdd;

import jdd.util.Configuration;
import jdd.util.Options;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.BitSet;
import java.util.ArrayList;
import java.util.Queue;
import java.util.Set;
import java.util.Collection;
import java.util.LinkedList;

public class NDDFactory extends BDDFactory {

    public static int mkCount = 0;

    public void free() {
        bddEngine = null;
        nodeTable = null;
    }

    private int BDD_TABLE_SIZE;
    private int BDD_CACHE_SIZE;
    private int NDD_TABLE_SIZE;
    private int CACHE_SIZE;
    private int CACHE_RATIO = 8;    // default 8

    // node table
    private jdd.bdd.BDD bddEngine;
    private NodeTable nodeTable;
    private int fieldNum;

    // per field
    private ArrayList<Integer> maxVariablePerField;
    private ArrayList<Double> satCountDiv;
    private ArrayList<int[]> bddVarsPerField;
    private ArrayList<int[]> bddNotVarsPerField;
    private ArrayList<NDD[]> nddVarsPerField;
    private ArrayList<NDD[]> nddNotVarsPerField;
    // protect temporary NDD nodes during garbage collection
    private HashSet<NDD> temporarilyProtect;

    // operation caches
    // note that: the usage of operation caches must be based on lazy gc
    private OperationCache<NDD> notCache;
    private OperationCache<NDD> andCache;
    private OperationCache<NDD> orCache;

    // terminal node true and false
    private final NDD TRUE = new NDD();
    private final NDD FALSE = new NDD();

    /**
     * default init for NDDFactory
     * NDD cache size will be computed based on {@link CACHE_RATIO}
     * BDD cache size will be setted as {@link bddCacheSize}
     * @param nddTableSize the size of NDD table. {@link CACHE_SIZE} will be computed based on it
     * @param bddTableSize
     * @param bddCacheSize
     */
    public NDDFactory(int nddTableSize, int bddTableSize, int bddCacheSize) {
        NDD_TABLE_SIZE = nddTableSize;
        CACHE_SIZE = NDD_TABLE_SIZE / CACHE_RATIO;
        BDD_TABLE_SIZE = bddTableSize;
        BDD_CACHE_SIZE = bddCacheSize;
        nodeTable = new NodeTable<>(NDD_TABLE_SIZE, BDD_TABLE_SIZE, BDD_CACHE_SIZE);
        bddEngine = nodeTable.getBddEngine();

        fieldNum = -1;
        maxVariablePerField = new ArrayList<>();
        satCountDiv = new ArrayList<>();
        bddVarsPerField = new ArrayList<>();
        bddNotVarsPerField = new ArrayList<>();
        nddVarsPerField = new ArrayList<>();
        nddNotVarsPerField = new ArrayList<>();
        temporarilyProtect = new HashSet<>();

        notCache = new OperationCache<>(CACHE_SIZE, 2);
        andCache = new OperationCache<>(CACHE_SIZE, 3);
        orCache = new OperationCache<>(CACHE_SIZE, 3);

        Options.verbose = true;
    }

    /**
     * without initialization of ndd(and node table) and operation cache
     * @param bddTableSize
     * @param bddCacheSize
     */
    public NDDFactory(int bddTableSize, int bddCacheSize) {
        BDD_TABLE_SIZE = bddTableSize;
        BDD_CACHE_SIZE = bddCacheSize;

        fieldNum = -1;
        maxVariablePerField = new ArrayList<>();
        satCountDiv = new ArrayList<>();
        bddVarsPerField = new ArrayList<>();
        bddNotVarsPerField = new ArrayList<>();
        nddVarsPerField = new ArrayList<>();
        nddNotVarsPerField = new ArrayList<>();
        temporarilyProtect = new HashSet<>();

        Options.verbose = true;
    }


    /**
     * init for batfish cuz should not modify the parameters pass to factory
     * will use {@link setVarNum} lazily to initialize nodetable and declare fields
     * @param bddTableSize
     * @param bddCacheSize
     * @return default factory with only constrain setted
     */
    public static BDDFactory init(int bddTableSize, int bddCacheSize) {
        return new NDDFactory(bddTableSize, bddCacheSize);
    }

    /**
     * default init for NDDFactory
     * @param fields ArrayList<Integer>
     * @param nddTableSize
     * @param bddTableSize
     * @param bddCacheSize
     * @return default factory with all parameters setted
     */
    public static BDDFactory init(ArrayList<Integer> fields, int nddTableSize, int bddTableSize, int bddCacheSize) {
        NDDFactory f = new NDDFactory(nddTableSize, bddTableSize, bddCacheSize);
        for (int i = 0; i < fields.size(); i++) {
            f.declareField(fields.get(i));
        }
        return f;
    }

    /**
     * default init for NDDFactory
     * @param fields int[]
     * @param nddTableSize
     * @param bddTableSize
     * @param bddCacheSize
     * @return default factory with all parameters setted
     */
    public static BDDFactory init(int[] fields, int nddTableSize, int bddTableSize, int bddCacheSize) {
        NDDFactory f = new NDDFactory(nddTableSize, bddTableSize, bddCacheSize);
        for (int i = 0; i < fields.length; i++) {
            f.declareField(fields[i]);
        }
        return f;
    }

    /**
     * declare a field with bitNum bits
     * @param bitNum the number of bits in the field
     * @return current field number
     */
    public int declareField(int bitNum) {
        // 1. update the number of fields
        fieldNum++;
        // 2. update the boundary of each field
        if (maxVariablePerField.isEmpty()) {
            maxVariablePerField.add(bitNum - 1);
        } else {
            maxVariablePerField.add(maxVariablePerField.get(maxVariablePerField.size() - 1) + bitNum);
        }
        // 3. update satCountDiv, which will be used in satCount operation of NDD
        double factor = Math.pow(2.0, bitNum);
        for (int i = 0; i < satCountDiv.size(); i++) {
            satCountDiv.set(i, satCountDiv.get(i) * factor);
        }
        int totalBitBefore = 0;
        if (maxVariablePerField.size() > 1) {
            totalBitBefore = maxVariablePerField.get(maxVariablePerField.size() - 2) + 1;
        }
        satCountDiv.add(Math.pow(2.0, totalBitBefore));
        // 4. add node table
        nodeTable.declareField();
        // 5. declare vars
        int[] bddVars = new int[bitNum];
        int[] bddNotVars = new int[bitNum];
        NDD[] nddVars = new NDD[bitNum];
        NDD[] nddNotVars = new NDD[bitNum];

        for (int i = 0; i < bitNum; i++) {
            bddVars[i] = bddEngine.ref(bddEngine.createVar());
            bddNotVars[i] = bddEngine.ref(bddEngine.not(bddVars[i]));
            HashMap<NDD, Integer> edges = new HashMap<>();
            edges.put(TRUE, bddEngine.ref(bddVars[i]));
            nddVars[i] = mk(fieldNum, edges);
            nodeTable.fixNDDNodeRefCount(nddVars[i]);
            edges = new HashMap<>();
            edges.put(TRUE, bddEngine.ref(bddNotVars[i]));
            nddNotVars[i] = mk(fieldNum, edges);
            nodeTable.fixNDDNodeRefCount(nddNotVars[i]);
        }
        bddVarsPerField.add(bddVars);
        bddNotVarsPerField.add(bddNotVars);
        nddVarsPerField.add(nddVars);
        nddNotVarsPerField.add(nddNotVars);

        return fieldNum;
    }

    public BDD getTrue() {
        return new bdd(TRUE);
    }

    public boolean isTrue(BDD b) {
        return b.isOne();
    }

    public BDD getFalse() {
        return new bdd(FALSE);
    }

    public boolean isFalse(BDD b) {
        return b.isZero();
    }

    public void clearCaches() {
        notCache.clearCache();
        andCache.clearCache();
        orCache.clearCache();
    }

    /**
     * user should call this method to ref(protect) NDD nodes
     * call {@link nodeTable#ref()} in Factory source file
     * @param ndd
     * @return for call chain
     */
    public BDD ref(BDD ndd) {
        ((NDDFactory.bdd) ndd).ref();
        return ndd;
    }

    /**
     * user should call this method to deref NDD nodes
     * call {@link nodeTable#deref()} in Factory source file
     * @param ndd
     */
    public BDD deref(BDD ndd) {
        ((NDDFactory.bdd) ndd).deref();
        return ndd;
    }

    /**
     * create or reuse a new NDD node
     * @param field
     * @param edges
     * @return the new NDD node or the reused one
     */
    public NDD mk(int field, HashMap<NDD, Integer> edges) {
        if (edges.size() == 0) {
            // Since NDD omits all edges pointing to FALSE, the empty edge represents FALSE.
            return FALSE;
        } else if (edges.size() == 1 && edges.values().iterator().next() == 1) {
            // Omit nodes with the only edge labeled by BDD TRUE.
            return edges.keySet().iterator().next();
        } else {
            return nodeTable.mk(field, edges);
        }
    }

    /**
     * Returns the total number of {@link BDD BDDs} allocated from this {@link BDDFactory factory}
     * that were never {@link BDD#free() freed}.
     */
    @Override
    public long numOutstandingBDDs() {
        return nodeTable.referenceCount.size();
    }

    /**
     * lazy declare fields in NDD
     * @param {fields} can be int[] or ArrayList<Integer>
     */
    public void setVarNum(int[] fields, int nddTableSize) {
        NDD_TABLE_SIZE = nddTableSize;
        CACHE_SIZE = NDD_TABLE_SIZE / CACHE_RATIO;
        nodeTable = new NodeTable<>(NDD_TABLE_SIZE, BDD_TABLE_SIZE, BDD_CACHE_SIZE);
        bddEngine = nodeTable.getBddEngine();

        notCache = new OperationCache<>(CACHE_SIZE, 2);
        andCache = new OperationCache<>(CACHE_SIZE, 3);
        orCache = new OperationCache<>(CACHE_SIZE, 3);

        for (int field : fields) {
            declareField(field);
        }
    }

    public void setVarNum(ArrayList<Integer> fields, int nddTableSize) {
        NDD_TABLE_SIZE = nddTableSize;
        CACHE_SIZE = NDD_TABLE_SIZE / CACHE_RATIO;
        nodeTable = new NodeTable<>(NDD_TABLE_SIZE, BDD_TABLE_SIZE, BDD_CACHE_SIZE);
        bddEngine = nodeTable.getBddEngine();

        notCache = new OperationCache<>(CACHE_SIZE, 2);
        andCache = new OperationCache<>(CACHE_SIZE, 3);
        orCache = new OperationCache<>(CACHE_SIZE, 3);

        for (Integer field : fields) {
            declareField(field);
        }
    }

    /**
     * Same with {@link #getVar(int field, int index)}
     * {@bddVarsPerField} stores {field[i]} number of BDD variables for each field
     * {@nddVarsPerField} stores {field[i]} number of NDD variables for each field
     * 
     * @return the {@param var}th ndd variable with its bdd pointed to BDDTrue on edge
     */
    @Override
    public BDD ithVar(int var) {
        // find var in field i
        int i = 0;
        for ( ; i < maxVariablePerField.size(); i++) {
            if (maxVariablePerField.get(i) >= var) {
                break;
            }
        }
        int lastIdx = i == 0 ? 0 : maxVariablePerField.get(i - 1) + 1;
        return new bdd(nddVarsPerField.get(i)[var - lastIdx]);
    }

    @Override
    public BDD nithVar(int var) {
        return new bdd(((bdd) ithVar(var))._index.not());
    }

    /**
     * Get the ndd variable of a specific bit.
     * @param field The id of the field.
     * @param index The id of the bit in the field.
     * @return The ndd variable.
     */
    public BDD getVar(int field, int index) {
        return new bdd(nddVarsPerField.get(field)[index]);
    }

    /**
     * Get the negation the variable for a specific bit.
     * @param field The id of the field.
     * @param index The id of the bit in the field.
     * @return The negation of the ndd variable.
     */
    public BDD getNotVar(int field, int index) {
        return new bdd(nddNotVarsPerField.get(field)[index]);
    }

    public BDD createBDD(NDD n) {
        return new bdd(n);
    }

    public void print(BDD ndd) {
        ((NDDFactory.bdd) ndd)._index.printRec();
    }

    /**
     * wrap NDD node as BDD for factory
     */
    public class bdd extends BDD {
        public NDD _index;

        public bdd() {
            _index = new NDD();
        }

        public bdd(NDD n) {
            _index = n;
            nodeTable.ref(_index);
        }

        public bdd(BDD b) {
            _index = ((bdd) b)._index;
            nodeTable.ref(_index);
        }

        public bdd(int f, HashMap<NDD, Integer> e) {
            _index = mk(f, e);
            nodeTable.ref(_index);
        }

        @Override
        public BDDFactory getFactory() {
            return NDDFactory.this;
        }

        public void ref() {
            nodeTable.ref(_index);
        }

        public void deref() {
            nodeTable.deref(_index);
        }

        /**
         * Returns true if this BDD is a satsifiable assignment.
         *
         * <p>A BDD is an assignment if there is exactly a single path to the {@link BDDFactory#one()}
         * BDD.
         *
         * <p>Note that being an assignment does not mean that there is a value assigned to every
         * variable. See {@link #satOne()} and {@link #fullSatOne()}.
         */
        @Override
        public boolean isAssignment() {
            if (this.isOne()) {
                return true;
            } else if (this.isZero()) {
                return false;
            }
            int flag = 0;
            Iterator iter = this._index.edges.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<NDD, Integer> edge = (Map.Entry<NDD, Integer>) iter.next();
                BDD child = new bdd(edge.getKey());
                if (child.isZero()) {
                    continue;
                } else if (child.isOne()) {
                    return ((bdd) child)._index.field == fieldNum - 1 ? true : false;
                } else {
                    if (!child.isAssignment())
                        return false;
                    flag++;
                }
            }
            if (flag == 1) {
                return true;
            }
            return false;
        }

        @Override
        public boolean isZero() {
            return _index.isFalse();
        }

        @Override
        public boolean isOne() {
            return _index.isTrue();
        }

        // TODO: check
        @Override
        public int var() {
            if (_index.isTerminal()) {
                return 0;
            }
            BitSet bit = null;
            for (Map.Entry<NDD, Integer> entry : this._index.edges.entrySet()) {
                NDD child = entry.getKey();
                if (child.isFalse())
                    return 0;
                BitSet bitset = bddEngine.minAssignment(entry.getValue());
                if (bit == null) {
                    bit = bitset;
                } else {
                    // compare and set the min
                    int i = 0;
                    for ( ; i < bitset.length(); i++) {
                        if (bitset.get(i) != bit.get(i))
                            break;
                    }
                    if (bit.get(i)) {
                        bit = bitset;
                    }
                }
            }
            int start = bit.nextSetBit(0);
            int offset = maxVariablePerField.get(this._index.field) - maxVariablePerField.get(this._index.field - 1);
            if (this._index.field == 0) {
                return start;
            } else {
                return start + maxVariablePerField.get(this._index.field - 1);
            }
        }

        /**
         * should NOT use this method
         * @return the first child node
         */
        @Override
        public BDD high() {
            BDD ret = null;
            Iterator iter = this._index.edges.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<NDD, Integer> edge = (Map.Entry<NDD, Integer>) iter.next();
                BDD child = new bdd(edge.getKey());
                if (child.isOne()) {
                    return child;
                }
                ret = child;
            }
            return ret;
        }

        /**
         * should NOT use this method
         */
        @Override
        public BDD low() {
            BDD ret = null;
            Iterator iter = this._index.edges.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<NDD, Integer> edge = (Map.Entry<NDD, Integer>) iter.next();
                BDD child = new bdd(edge.getKey());
                if (child.isZero()) {
                    return child;
                }
                ret = child;
            }
            if (ret.isOne()) {
                return new bdd(FALSE);
            }
            return ret;
        }

        @Override
        public BDD id() {
            return new bdd(_index);
        }

        @Override
        public BDD not() {
            return new bdd(_index.not());
        }

        /**
         * {@link #not()}, but {@code this} {@link BDD} is changed to the result rather than creating a
         * new BDD object.
         */
        @Override
        public BDD notEq() {
            NDD result = this._index.not();
            nodeTable.ref(result);
            nodeTable.deref(this._index);
            this._index = result;
            return this;
        }

        /**
         * Return true iff the {@code and} of the two BDDs is satisfiable. Equivalent to {@code
         * !this.and(that).isZero()}.
         *
         * @param that BDD to 'and' with
         * @return whether the 'and' is satisfiable
         */
        @Override
        public boolean andSat(BDD that) {
            return !this.and(that).isZero();
        }

        /**
         * Return true iff the {@code diff} of the two BDDs is satisfiable. Equivalent to {@code
         * !this.diff(that).isZero()}.
         *
         * @param that BDD to 'diff' with
         * @return whether the 'diff' is satisfiable
         */
        @Override
        public boolean diffSat(BDD that) {
            return !this.diff(that).isZero();
        }

        @Override
        public BDD ite(BDD thenBDD, BDD elseBDD) {
            if (this.isOne()) {
                return thenBDD;
            } else if (this.isZero()) {
                return elseBDD;
            } else {
                throw new BDDException();
            }
        }

        @Override
        public BDD relprod(BDD that, BDD var) { throw new BDDException(); }

        @Override
        public BDD compose(BDD g, int var) { throw new BDDException(); }

        @Override
        public BDD veccompose(BDDPairing pair) { throw new BDDException(); }

        @Override
        public BDD constrain(BDD that) { throw new BDDException(); }

        @Override
        public BDD exist(BDD var) {
            return new bdd(_index.exist(((bdd) var)._index.field));
        }

        @Override
        BDD exist(BDD var, boolean makeNew) {
            throw new BDDException();
        }

        /**
         * Return true if this BDD tests any of the variables set in the given {@code var} BDD. Equivalent
         * to {@code !this.exist(var).equals(this)}, but doesn't create BDDs.
         *
         * @param var BDD specifying the variables to test
         */
        @Override
        public boolean testsVars(BDD var) {
            return !this.exist(var).equals(this);
        }

        /**
         * Project this BDD onto the variables in the set. i.e. existentially quantify all other
         * variables.
         *
         * <p>Compare to bdd_project.
         *
         * @param var BDD containing the variables to be projected onto
         * @return the result of the projection
         * @see BDDDomain#set()
         */
        @Override
        public BDD project(BDD var) { throw new BDDException(); }

        @Override
        public BDD forAll(BDD var) { throw new BDDException(); }

        @Override
        public BDD unique(BDD var) { throw new BDDException(); }

        @Override
        public BDD restrict(BDD var) { throw new BDDException(); }

        @Override
        public BDD restrictWith(BDD var) { throw new BDDException(); }

        @Override
        public BDD simplify(BDD d) { throw new BDDException(); }

        @Override
        public BDD support() { throw new BDDException(); }

        /**
         * Returns the result of applying the binary operator <tt>opr</tt> to the two BDDs.
         *
         * @param that    the BDD to apply the operator on
         * @param opr     the operator to apply
         * @param makeNew whether a new BDD is created ({@code true}) or {@code this} BDD is modified.
         *                Note that {@code that} is never changed.
         * @return the result of applying the operator
         */
        @Override
        BDD apply(BDD that, BDDOp opr, boolean makeNew) {
            temporarilyProtect.clear();
            // initialize assurance for return in default case
            NDD r = null;
            NDD x = this._index;
            NDD y = ((bdd) that)._index;
            switch (opr.id) {
                case 0: r = x.and(y); break;
                case 1: r = x.and(y.not()).or(y.and(x.not())); break;  // r = bdd.xor(x, y);
                case 2: r = x.or(y); break;
                case 3: r = x.and(y).not(); break;
                case 4: r = x.or(y).not(); break;
                case 5: r = x.not().or(y); break;  // r = bdd.imp(x, y);
                case 6: r = x.biimp(y); break;     // r = bdd.biimp(x, y);
                case 7: r = x.diff(y); break;
                case 9: r = y.not().or(x); break;  // inverse imp
                default:
                    throw new BDDException();
            }
            if (makeNew) {
                return new bdd(r);
            }
            if (!x.equals(r)) {
                nodeTable.ref(r);
                nodeTable.deref(x);
                this._index = r;
            }
            return this;
        }

        public BDD apply(BDD that, BDDOp opr) {
            return apply(that, opr, true);
        }

        @Override
        public BDD applyWith(BDD that, BDDOp opr) {
            bdd tmp = (NDDFactory.bdd) this.apply(that, opr);
            // ref new node and deref this and that
            nodeTable.ref(tmp._index);
            nodeTable.deref(this._index);
            if (!this.equals(that)) {
                // deep copy
                that.free();
            }
            this._index = tmp._index;
            return this;
        }

        @Override
        public BDD applyAll(BDD that, BDDOp opr, BDD var) { throw new BDDException(); }

        @Override
        public BDD applyEx(BDD that, BDDOp opr, BDD var) { throw new BDDException(); }

        /**
         * Shorthand for {@code this.applyEx(rel, BDDFactory.and, vars).replace(pair)}, where
         *
         * <ol>
         *   <li>vars is a varset BDD representation of the codomain of pair
         *   <li>if pair maps variable V1 to V2, then LEVEL(V1) == LEVEL(V2)+1
         * </ol>
         *
         * <p>Use case: {@code rel} represents a relation (multi-valued or nondeterministic function) as a
         * constraint over unprimed and and primed variables (unprimed variables represent inputs and
         * primed variables represent outputs), {@code x} represents a set of values as a constraint over
         * unprimed variables, and {@code pair} maps the primed variables to their corresponding unprimed
         * variables. {@code x.transform(rel, pair)} returns the image of {@code x} under {@code rel},
         * i.e. the set containing all possible results of apply {@code rel} to a value in {@code x},
         * represented as a constraint over unprimed variables.
         *
         * @param rel
         * @param pair
         */
        @Override
        public BDD transform(BDD rel, BDDPairing pair) { throw new BDDException(); }

        @Override
        public BDD applyUni(BDD that, BDDOp opr, BDD var) { throw new BDDException(); }

        @Override
        public BDD satOne() {
            NDD ret = this._index.satOne_rec();
            return new bdd(ret);
        }

        @Override
        public BDD fullSatOne() { throw new BDDException(); }

        /**
         * Returns a {@link BitSet} containing the smallest possible assignment to this BDD, using
         * variable order.
         *
         * <p>Note that the returned {@link BitSet} is in little-Endian order. That is, the least
         * significant value in the BitSet is the first BDD variable.
         */
        @Override
        public BitSet minAssignmentBits() {
            BitSet set = new BitSet(maxVariablePerField.get(fieldNum) + 1);
            minassignmentbits_rec(set, this._index);
            return set;
        }

        private void minassignmentbits_rec(BitSet set, NDD ndd) {
            if (ndd.isFalse() || ndd.isTrue()) {
                return;
            }
            NDD child = null;
            BitSet bitset = null;
            for (Map.Entry<NDD, Integer> entry : ndd.edges.entrySet()) {
                NDD c = entry.getKey();
                if (c.isFalse())
                    continue;
                int e = entry.getValue();
                BitSet bit = bddEngine.minAssignment(e);
                if (bitset == null) {
                    bitset = bit;
                    child = c;
                } else {
                    // compare to get min
                    int i = 0;
                    for ( ; i < bit.length(); i++) {
                        if (bitset.get(i) != bit.get(i))
                            break;
                    }
                    if (bitset.get(i)) {
                        bitset = bit;
                        child = c;
                    }
                }
            }
            for (int b = bitset.length(); (b = bitset.previousSetBit(b - 1)) >= 0; ) {
                if (ndd.field == 0) {
                    set.set(b);
                } else {
                    set.set(b + maxVariablePerField.get(ndd.field - 1));
                }
            }
            // set.or(bitset);
            minassignmentbits_rec(set, child);
        }

        /**
         * Finds one satisfying variable assignment, deterministically produced as a function of the seed.
         * Finds a BDD with exactly one variable at all levels. The new BDD implies this BDD and is not
         * false unless this BDD is false.
         *
         * @param seed
         * @return one satisfying variable assignment
         */
        @Override
        public BDD randomFullSatOne(int seed) { throw new BDDException(); }

        @Override
        public BDD satOne(BDD var, boolean pol) { throw new BDDException(); }

        // TODO: need all satify
        @Override
        public AllSatIterator allsat() { throw new BDDException(); }

        // TODO: batfish needs?
        @Override
        public BDD replace(BDDPairing pair) { throw new BDDException(); }

        @Override
        public BDD replaceWith(BDDPairing pair) { throw new BDDException(); }

        @Override
        public int nodeCount() { return mkCount; }

        @Override
        public double pathCount() { throw new BDDException(); }

        @Override
        public double satCount() {
            return this._index.satCount();
        }

        @Override
        public int[] varProfile() { return new int[0]; }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof bdd)) {
                return false;
            }
            return _index == ((bdd) o)._index;
        }

        public boolean equals(BDD that) {
            return this._index == ((bdd) that)._index;
        }

        @Override
        public int hashCode() { return 0; }

        @Override
        public void free() {
            nodeTable.deref(_index);
            // create new NDD to point to
            // _index = null;
        }

        @Override
        public boolean isConstant() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'isConstant'");
        }

        @Override
        public boolean isVar() {
            return _index.edges.size() == 1 && _index.edges.containsKey(TRUE);
        }

        @Override
        public boolean isAnd() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'isAnd'");
        }

        @Override
        public boolean isNor() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'isNor'");
        }
    }

    public class NDD {
        // per node content
        private int field;
        private HashMap<NDD, Integer> edges;

        // empty constructor for True and False NDD node
        public NDD() {
            // field start from -1
            field = fieldNum;
        }

        public NDD(int field, HashMap<NDD, Integer> edges) {
            this.field = field;
            this.edges = edges;
        }

        public NDD(NDD a) {
            this.field = a.field;
            this.edges = a.edges;
        }

        public int getField() {
            return field;
        }

        public HashMap<NDD, Integer> getEdges() {
            return edges;
        }

        public boolean isTrue() {
            return this == TRUE;
        }

        public boolean isFalse() {
            return this == FALSE;
        }

        public boolean isTerminal() {
            return this == TRUE || this == FALSE;
        }

        private void addEdge(HashMap<NDD, Integer> edges, NDD descendant, int labelBDD) {
            // omit the edge pointing to terminal node FALSE
            if (descendant.isFalse()) {
                bddEngine.deref(labelBDD);
                return;
            }
            // try to find the edge pointing to the same descendant
            Integer oldLabel = edges.get(descendant);
            if (oldLabel == null) {
                oldLabel = 0;
            }
            // merge the bdd label
            int newLabel = bddEngine.orTo(oldLabel, labelBDD);
            bddEngine.deref(labelBDD);
            edges.put(descendant, newLabel);
        }

        public NDD and(NDD a) {
            temporarilyProtect.clear();
            return andRec(a);
        }

        private NDD andRec(NDD b) {
            // terminal condition
            if (isFalse() || b.isTrue()) {
                return this;
            } else if (isTrue() || b.isFalse() || this == b) {
                return b;
            }

            // check the cache
            if (andCache.getEntry(this, b))
                return andCache.result;

            NDD result = null;
            HashMap<NDD, Integer> newEdges = new HashMap<>();
            if (b.field == field) {
                for (Map.Entry<NDD, Integer> entryA : edges.entrySet()) {
                    for (Map.Entry<NDD, Integer> entryB : b.edges.entrySet()) {
                        // the bdd label on the new edge
                        int intersect = bddEngine.ref(bddEngine.and(entryA.getValue(), entryB.getValue()));
                        if (intersect != 0) {
                            // the descendant of the new edge
                            NDD subResult = entryA.getKey().andRec(entryB.getKey());
                            // try to merge edges
                            addEdge(newEdges, subResult, intersect);
                        }
                    }
                }
            } else {
                if (field > b.field) {
                    return b.andRec(this);
                }
                for (Map.Entry<NDD, Integer> entryA : edges.entrySet()) {
                    /*
                     * if A branches on a higher field than B,
                     * we can let A operate with a pseudo node
                     * with only edge labelled by true and pointing to B
                     */
                    NDD subRet = entryA.getKey().andRec(b);
                    addEdge(newEdges, subRet, bddEngine.ref(entryA.getValue()));
                }
            }
            // try to create or reuse node
            result = mk(field, newEdges);
            // protect the node during the operation
            temporarilyProtect.add(result);
            // store the result into cache
            andCache.setEntry(andCache.hashValue, this, b, result);
            return result;
        }

        public NDD or(NDD b) {
            temporarilyProtect.clear();
            return orRec(b);
        }

        private NDD orRec(NDD b) {
            // terminal condition
            if (isTrue() || b.isFalse()) {
                return this;
            } else if (isFalse() || b.isTrue() || this == b) {
                return b;
            }

            //check the cache
            if (orCache.getEntry(this, b))
                return orCache.result;

            NDD result = null;
            HashMap<NDD, Integer> newEdges = new HashMap<>();
            if (field == b.field) {
                // record edges of each node, which will 'or' with the edge pointing to FALSE of another node
                HashMap<NDD, Integer> residualA = new HashMap<>(edges);
                HashMap<NDD, Integer> residualB = new HashMap<>(b.edges);
                for (int oneBDD : edges.values()) {
                    bddEngine.ref(oneBDD);
                }
                for (int oneBDD : b.edges.values()) {
                    bddEngine.ref(oneBDD);
                }
                for (Map.Entry<NDD, Integer> entryA : edges.entrySet()) {
                    for (Map.Entry<NDD, Integer> entryB : b.edges.entrySet()) {
                        // the bdd label on the new edge
                        int intersect = bddEngine.ref(bddEngine.and(entryA.getValue(), entryB.getValue()));
                        if (intersect != 0) {
                            // update residual
                            int oldResidual = residualA.get(entryA.getKey());
                            int notIntersect = bddEngine.ref(bddEngine.not(intersect));
                            residualA.put(entryA.getKey(), bddEngine.andTo(oldResidual, notIntersect));
                            oldResidual = residualB.get(entryB.getKey());
                            residualB.put(entryB.getKey(), bddEngine.andTo(oldResidual, notIntersect));
                            bddEngine.deref(notIntersect);
                            // the descendant of the new edge
                            NDD subResult = entryA.getKey().orRec(entryB.getKey());
                            // try to merge edges
                            addEdge(newEdges, subResult, intersect);
                        }
                    }
                }
                /*
                 * Each residual of A doesn't match with any explicit edge of B,
                 * and will match with the edge pointing to FALSE of B, which is omitted.
                 * The situation is the same for B.
                 */
                for (Map.Entry<NDD, Integer> entryA : residualA.entrySet()) {
                    if (entryA.getValue() != 0) {
                        addEdge(newEdges, entryA.getKey(), bddEngine.ref(entryA.getValue()));
                    }
                }
                for (Map.Entry<NDD, Integer> entry_b : residualB.entrySet()) {
                    if (entry_b.getValue() != 0) {
                        addEdge(newEdges, entry_b.getKey(), bddEngine.ref(entry_b.getValue()));
                    }
                }
            } else {
                if (field > b.field) {
                    return b.orRec(this);
                }
                int residualB = 1;
                for (Map.Entry<NDD, Integer> entryA : edges.entrySet()) {
                    /*
                     * if A branches on a higher field than B,
                     * we can let A operate with a pseudo node
                     * with only edge labelled by true and pointing to B
                     */
                    int notIntersect = bddEngine.ref(bddEngine.not(entryA.getValue()));
                    residualB = bddEngine.andTo(residualB, notIntersect);
                    bddEngine.deref(notIntersect);
                    NDD subResult = entryA.getKey().orRec(b);
                    addEdge(newEdges, subResult, bddEngine.ref(entryA.getValue()));
                }
                if (residualB != 0) {
                    addEdge(newEdges, b, residualB);
                }
            }
            // try to create or reuse node
            result = mk(field, newEdges);
            // protect the node during the operation
            temporarilyProtect.add(result);
            // store the result into cache
            orCache.setEntry(orCache.hashValue, this, b, result);
            return result;
        }

        public NDD not() {
            temporarilyProtect.clear();
            return notRec();
        }

        private NDD notRec() {
            if (isTrue()) {
                return FALSE;
            } else if (isFalse()) {
                return TRUE;
            }


            if (notCache.getEntry(this))
                return notCache.result;

            HashMap<NDD, Integer> newEdges = new HashMap<>();
            Integer residual = 1;
            for (Map.Entry<NDD, Integer> entryA : edges.entrySet()) {
                int notIntersect = bddEngine.ref(bddEngine.not(entryA.getValue()));
                residual = bddEngine.andTo(residual, notIntersect);
                bddEngine.deref(notIntersect);
                NDD subResult = entryA.getKey().notRec();
                addEdge(newEdges, subResult, bddEngine.ref(entryA.getValue()));
            }
            if (residual != 0) {
                addEdge(newEdges, TRUE, residual);
            }
            NDD result = mk(field, newEdges);
            temporarilyProtect.add(result);
            notCache.setEntry(notCache.hashValue, this, result);
            return result;
        }

        // a / b <==> a ∩ (not b)
        public NDD diff(NDD b) {
            temporarilyProtect.clear();
            NDD n = b.notRec();
            temporarilyProtect.add(n);
            NDD result = this.andRec(n);
            return result;
        }

        public NDD exist(int field) {
            temporarilyProtect.clear();
            return existRec(field);
        }

        // existential quantification
        // not updated with lazyGC
        private NDD existRec(int field) {
            if (isTerminal() || this.field > field) {
                return this;
            }

            NDD result = FALSE;
            if (this.field == field) {
                for (NDD next : edges.keySet()) {
                    result = result.orRec(next);
                }
            } else {
                HashMap<NDD, Integer> newEdges = new HashMap<>();
                for (Map.Entry<NDD, Integer> entryA : edges.entrySet()) {
                    NDD subResult = entryA.getKey().existRec(field);
                    addEdge(newEdges, subResult, bddEngine.ref(entryA.getValue()));
                }
                result = mk(field, newEdges);
            }
            temporarilyProtect.add(result);
            return result;
        }

        // a => b <==> (not a) ∪ b
        public NDD imp(NDD b) {
            temporarilyProtect.clear();
            NDD n = this.notRec();
            temporarilyProtect.add(n);
            return n.orRec(b);
        }

        public NDD biimp(NDD b) {
            if (this == b) {
                return TRUE;
            } else if (isFalse()) {
                return b.not();
            } else if (isTrue()) {
                return b;
            } else if (b.isFalse()) {
                return not();
            } else if (b.isTrue()) {
                return this;
            }
            throw new BDDException();
        }

        // calculate the number of solutions of an NDD
        public double satCount() {
            return bddEngine.satCount(bddEngine.deref(toBDD()));
            // return satCountRec(0);
        }

        public int toBDD() {
            if (isTrue()) {
                return 1;
            } else if (isFalse()) {
                return 0;
            } else {
                int result = 0;
                for (Map.Entry<NDD, Integer> entry : edges.entrySet()) {
                    int temp = bddEngine.andTo(entry.getKey().toBDD(), entry.getValue());
                    result = bddEngine.orTo(result, temp);
                    bddEngine.deref(temp);
                }
                return result;
            }
        }

        private double satCountRec(int field) {
            if (isFalse()) {
                return 0;
            } else if (isTrue()) {
                if (field > fieldNum) {
                    return 1;
                } else {
                    int len = maxVariablePerField.get(maxVariablePerField.size() - 1);
                    if (field == 0) {
                        len++;
                    } else {
                        len -= maxVariablePerField.get(field - 1);
                    }
                    return Math.pow(2.0, len);
                }
            } else {
                double result = 0;
                if (field == this.field) {
                    for (Map.Entry<NDD, Integer> entry : edges.entrySet()) {
                        double bddSat = bddEngine.satCount(entry.getValue()) / satCountDiv.get(this.field);
                        double nddSat = entry.getKey().satCountRec(field + 1);
                        result += bddSat * nddSat;
                    }
                } else {
                    int len = maxVariablePerField.get(field);
                    if (field == 0) {
                        len++;
                    } else {
                        len -= maxVariablePerField.get(field - 1);
                    }
                    result = Math.pow(2.0, len) * this.satCountRec(field + 1);
                }
                return result;
            }
        }

        private NDD satOne_rec() {
            if (isTerminal())
                return this;
            NDD child = null;
            int edge = 0;
            for (Map.Entry<NDD, Integer> entry : edges.entrySet()) {
                if (entry.getKey().isFalse())
                    continue;
                child = entry.getKey();
                edge = entry.getValue();
                break;
            }
            if (child == null)
                return FALSE;   // TODO
            int jddSat = bddEngine.oneSat(edge);
            NDD result = child.satOne_rec();
            HashMap<NDD, Integer> newEdge = new HashMap<>();
            newEdge.put(result, jddSat);
            return new NDD(this.field, newEdge);
        }

        public void printRec() {
            if (isTrue()) {
                System.out.println("TRUE\n");
            } else if (isFalse()) {
                System.out.println("FALSE\n");
            } else {
                System.out.println("field: " + field + " node: " + this);
                for (Map.Entry<NDD, Integer> entry: edges.entrySet()) {
                    System.out.println("next: " + entry.getKey() + " label: " + entry.getValue());
                    bddEngine.printDot("/Users/augists/Downloads/NDD-main/" + entry.getValue(), entry.getValue());
                }
                System.out.println();
                for (NDD next : edges.keySet()) {
                    next.printRec();
                }
            }
        }
    }

    public class NodeTable <E> {
        // node table
        long currentSize;
        long maxSize;
        // each element is a node table for a field
        ArrayList<HashMap<HashMap<NDD, Integer>, NDD>> nodeTable;
        // bdd engine
        jdd.bdd.BDD bddEngine;
        // garbage collection
        final double QUICK_GROW_THRESHOLD = 0.1;
        HashMap<NDD, Integer> referenceCount;

        public NodeTable(long maxSize, int bddTableSize, int bddCacheSize) {
            this.currentSize = 0L;
            this.maxSize = maxSize;
            this.nodeTable = new ArrayList<>();
            bddEngine = new jdd.bdd.BDD(bddTableSize, bddCacheSize);
            this.referenceCount = new HashMap<>();
        }

        public void setMaxSize(long maxSize) {
            this.maxSize = maxSize;
        }

        public jdd.bdd.BDD getBddEngine() {
            return bddEngine;
        }

        // declare a new node table for a new field
        public void declareField() {
            nodeTable.add(new HashMap<>());
        }

        // create or reuse a new node
        public NDD mk(int field, HashMap<NDD, Integer> edges) {
            if (edges.size() == 0) {
                // Since NDD omits all edges pointing to FALSE, the empty edge represents FALSE.
                return FALSE;
            } else if (edges.size() == 1 && edges.values().iterator().next() == 1) {
                // Omit nodes with the only edge labeled by BDD TRUE.
                return edges.keySet().iterator().next();
            }
            NDD node = nodeTable.get(field).get(edges);
            if (node == null) {
                // update mkCount to show status
                mkCount++;

                // create a new node
                // 1. add ref count of all descendants
                Iterator<NDD> iterator = edges.keySet().iterator();
                while (iterator.hasNext()) {
                    NDD descendant = iterator.next();
                    if (!descendant.isTerminal()) {
                        referenceCount.put(descendant, referenceCount.get(descendant) + 1);
                    }
                }

                // 2. check if there should be a gc or grow
                if (currentSize >= maxSize) {
                    gcOrGrow();
                }

                // 3. create node
                NDD newNode = new NDD(field, edges);
                nodeTable.get(field).put(edges, newNode);
                referenceCount.put(newNode, 0);
                currentSize++;
                return newNode;
            } else {
                // reuse node
                for (Integer bdd : edges.values()) {
                    bddEngine.deref(bdd);
                }
                return node;
            }
        }

        private void gcOrGrow() {
            gc();
            if (maxSize - currentSize <= maxSize * QUICK_GROW_THRESHOLD) {
                grow();
            }
            clearCaches();
        }

        private void gc() {
            // protect temporary nodes during NDD operations
            for (NDD ndd : temporarilyProtect) {
                ref(ndd);
            }

            // remove unused nodes by topological sorting
            Queue<NDD> deadNodesQueue = new LinkedList<>();
            for (Map.Entry<NDD, Integer> entry : referenceCount.entrySet()) {
                if (entry.getValue() == 0) {
                    deadNodesQueue.offer(entry.getKey());
                }
            }
            while (!deadNodesQueue.isEmpty()) {
                NDD deadNode = deadNodesQueue.poll();
                for (NDD descendant : deadNode.getEdges().keySet()) {
                    if (descendant.isTerminal()) continue;
                    int newReferenceCount = referenceCount.get(descendant) - 1;
                    referenceCount.put(descendant, newReferenceCount);
                    if (newReferenceCount == 0) {
                        deadNodesQueue.offer(descendant);
                    }
                }
                // delete current dead node
                for (int bddLabel : deadNode.getEdges().values()) {
                    bddEngine.deref(bddLabel);
                }
                referenceCount.remove(deadNode);
                nodeTable.get(deadNode.getField()).remove(deadNode.getEdges());
                currentSize--;
            }

            for (NDD ndd : temporarilyProtect) {
                deref(ndd);
            }
        }

        private void grow() {
            maxSize *= 2;
        }

        /**
         * Protect a root node from garbage collection.
         * @param ndd The root to be protected.
         * @return The ndd node.
         */
        public NDD ref(NDD ndd) {
            if (!ndd.isTerminal() && referenceCount.get(ndd) != Integer.MAX_VALUE) {
                referenceCount.put(ndd, referenceCount.get(ndd) + 1);
            }
            return ndd;
        }

        /**
         * Ref the initialized NDD node with Integer.MAX_VALUE (special label)
         * @param ndd
         */
        public void fixNDDNodeRefCount(NDD ndd) {
            referenceCount.put(ndd, Integer.MAX_VALUE);
        }

        /**
         * Unprotect a root node, such that the node can be cleared during garbage collection.
         * @param ndd The ndd node to be unprotected.
         */
        public void deref(NDD ndd) {
            if (!ndd.isTerminal() && referenceCount.get(ndd) != Integer.MAX_VALUE) {
                referenceCount.put(ndd, referenceCount.get(ndd) - 1);
            }
        }

        public long getCurrentSize() {
            return currentSize;
        }
    }

    public class OperationCache<T> {
        // The max number of entries in the cache.
        int cacheSize;
        // The length of each entry. 3 for binary operations and 2 for unary operations.
        int entrySize;
        Object[] cache;
        // Store the result of getEntry() temporarily
        public T result;
        public int hashValue;

        /**
         * Construct function of operation cache.
         * @param cacheSize The max number of entries in the cache
         * @param entrySize The length of each entry. 3 for binary operations and 2 for unary operations.
         */
        public OperationCache(int cacheSize, int entrySize) {
            this.cacheSize = cacheSize;
            this.entrySize = entrySize;
            cache = new Object [cacheSize * entrySize];
            result = null;
        }

        /**
         * Grow up function of operation cache.
         * @param new_cache_size assert larger than old_cache_size
         */
        public void growUpSize(int new_cache_size) {
            cacheSize = new_cache_size;
            Object[] old_cache = cache;
            cache = new Object [cacheSize * entrySize];
            System.arraycopy(old_cache, 0, cache, 0, old_cache.length);
        }

        /**
         * Set the result of an entry.
         * @param index The index of the entry to be modified.
         * @param result The result to be cached.
         */
        private void setResult(int index, T result) {
            cache[index * entrySize] = result;
        }

        /**
         * Get the result of an entry.
         * @param index The index of the entry.
         * @return The cached result.
         */
        private T getResult(int index) {
            return (T) cache[index * entrySize];
        }

        /**
         * Set one of the operands of an entry.
         * @param index The index of the entry.
         * @param operandIndex The index of the operand in the entry.
         * @param operand The operand to be stored.
         */
        private void setOperand(int index, int operandIndex, T operand) {
            cache[index * entrySize + operandIndex] = operand;
        }

        /**
         * Get one of the operands of an entry.
         * @param index The index of the entry.
         * @param operandIndex The index of the operand in the entry.
         * @return The cached operand.
         */
        private  T getOperand(int index, int operandIndex) {
            return (T) cache[index * entrySize + operandIndex];
        }

        /**
         * Insert new entry of (operand1, result) into cache.
         * Directly overwrite the old value if there exist a hash collision.
         * @param index The index of the entry to be inserted, which is actually a hash value.
         * @param operand1 The only operand of a unary operation.
         * @param result The result of the operation.
         */
        public void setEntry(int index, T operand1, T result) {
            setOperand(index, 1, operand1);
            setResult(index, result);
        }

        /**
         * Insert new entry of (operand1, operand2, result) into cache.
         * Directly overwrite the old value if there exist a hash collision.
         * @param index The index of the entry to be inserted, which is actually a hash value.
         * @param operand1 The first operand of a binary operation.
         * @param operand2 The second operand of a binary operation.
         * @param result The result of the operation.
         */
        public void setEntry(int index, T operand1, T operand2, T result) {
            setOperand(index, 1, operand1);
            setOperand(index, 2, operand2);
            setResult(index, result);
        }

        /**
         * Get the result of operation(operand1).
         * @param operand1 The only operand of a unary operation.
         * @return TRUE if the entry found (the result will be stored in this.result), FALSE if the entry not found (the hashValue will be stored in this.hashValue).
         */
        public boolean getEntry(T operand1) {
            int hash = goodHash(operand1);
            if (getOperand(hash, 1) == operand1) {
                result = getResult(hash);
                return true;
            } else {
                hashValue = hash;
                return false;
            }
        }

        /**
         * Get the result of operation(operand1, operand2).
         * @param operand1 The first operand of a binary operation.
         * @param operand2 The second operand of a binary operation.
         * @return TRUE if the entry found (the result will be stored in this.result), FALSE if the entry not found (the hashValue will be stored in this.hashValue).
         */
        public boolean getEntry(T operand1, T operand2) {
            int hash = goodHash(operand1, operand2);
            if ((getOperand(hash, 1) == operand1 && getOperand(hash, 2) == operand2)
                    || (getOperand(hash, 1) == operand2 && getOperand(hash, 2) == operand1)) {
                result = getResult(hash);
                return true;
            } else {
                hashValue = hash;
                return false;
            }
        }

        /**
         * Calculate the hash value of the operand, which will be the index in the cache.
         * @param operand1 The only operand of a unary operation.
         * @return The hash value.
         */
        private int goodHash(T operand1) {
            return Math.abs(operand1.hashCode()) % cacheSize;
        }

        /**
         * Calculate the hash value of operands, which will be the index in the cache.
         * @param operand1 The first operand of a binary operation.
         * @param operand2 The second operand of a binary operation.
         * @return The hash value.
         */
        private int goodHash(T operand1, T operand2) {
            return (int) (Math.abs((long) operand1.hashCode() + (long) operand2.hashCode()) % cacheSize);
        }

        /**
         * Invalidate an entry in the cache.
         * @param index The index of the entry to be invalidated.
         */
        private void invalidateEntry(int index) {
            setOperand(index, 1, null);
        }

        /**
         * Check if the entry is valid.
         * @param index The index of the entry.
         * @return If the entry stores valid content.
         */
        private boolean isValid(int index) {
            return getOperand(index, 1) != null;
        }

        /**
         * Invalidate all the entries in the cache.
         */
        // invalidate all entries in the cache during garbage collections of the node table
        public void clearCache() {
            // for (int i = 0; i < cacheSize; i++) {
            //     invalidateEntry(i);
            // }
            cache = new Object[cacheSize * entrySize];
        }
    }

    /**
     * dynamically set var num (not recommended)
     */
    @Override
    public int setVarNum(int num) {
        if (num > Integer.MAX_VALUE / 2)
            throw new BDDException();

        // add a new field
        declareField(num);

        return maxVariablePerField.get(maxVariablePerField.size() - 1);
    }

    public NDD toNDD(int a) {
        HashMap<Integer, HashMap<Integer, Integer>> decomposed = decompose(a);
        HashMap<Integer, NDD> converted = new HashMap<>();
        converted.put(1, TRUE);
        while(decomposed.size() != 0) {
            Set<Integer> finished = converted.keySet();
            for(Map.Entry<Integer, HashMap<Integer, Integer>> entry : decomposed.entrySet()) {
                if(finished.containsAll(entry.getValue().keySet())) {
                    int field = bdd_getField(entry.getKey());
                    HashMap<NDD, Integer> map = new HashMap<>();
                    for(Map.Entry<Integer, Integer> entry1 : entry.getValue().entrySet())
                        map.put(converted.get(entry1.getKey()), bddEngine.ref(entry1.getValue()));
                    NDD n = mk(field, map);
                    converted.put(entry.getKey(), n);
                    decomposed.remove(entry.getKey());
                    break;
                }
            }
        }
        for(HashMap<Integer, Integer> map : decomposed.values())
            for(Integer pred : map.values())
                bddEngine.deref(pred);
        return converted.get(a);
    }

    private HashMap<Integer, HashMap<Integer, Integer>> decompose(int a) {
        HashMap<Integer, HashMap<Integer, Integer>> decomposed_bdd =
                new HashMap<Integer, HashMap<Integer, Integer>>();
        if (a == 0)
            return decomposed_bdd;
        if (a == 1) {
            HashMap<Integer, Integer> map = new HashMap<>();
            map.put(1, 1);
            decomposed_bdd.put(1, map);
            return decomposed_bdd;
        }
        HashMap<Integer, HashSet<Integer>> boundary_tree = new HashMap<Integer, HashSet<Integer>>();
        ArrayList<HashSet<Integer>> boundary_points = new ArrayList<HashSet<Integer>>();

        get_boundary_tree(a, boundary_tree, boundary_points);

        for (int curr_level = 0; curr_level < fieldNum - 1; curr_level++) {
            for (int root : boundary_points.get(curr_level)) {
                for (int end_point : boundary_tree.get(root)) {
                    int res = construct_decomposed_bdd(root, end_point, root);
                    bddEngine.ref(res);
                    if (!decomposed_bdd.containsKey(root)) {
                        decomposed_bdd.put(root, new HashMap<Integer, Integer>());
                    }
                    decomposed_bdd.get(root).put(end_point, res);
                }
            }
        }

        for (int abdd : boundary_points.get(fieldNum - 1)) {
            if (!decomposed_bdd.containsKey(abdd)) {
                decomposed_bdd.put(abdd, new HashMap<Integer, Integer>());
            }
            decomposed_bdd.get(abdd).put(1, bddEngine.ref(abdd));
        }

        return decomposed_bdd;
    }

    private void detect_boundary_point(int root, int curr, HashMap<Integer, HashSet<Integer>> boundary_tree,
            ArrayList<HashSet<Integer>> boundary_points) {
        if (curr == 0)
            return;
        if (curr == 1) {
            if (!boundary_tree.containsKey(root)) {
                boundary_tree.put(root, new HashSet<Integer>());
            }
            boundary_tree.get(root).add(1);
            return;
        }
        if (bdd_getField(root) != bdd_getField(curr)) {
            if (!boundary_tree.containsKey(root)) {
                boundary_tree.put(root, new HashSet<Integer>());
            }
            boundary_tree.get(root).add(curr);
            boundary_points.get(bdd_getField(curr)).add(curr);
            return;
        }
        detect_boundary_point(root, bddEngine.getLow(curr), boundary_tree, boundary_points);
        detect_boundary_point(root, bddEngine.getHigh(curr), boundary_tree, boundary_points);
    }

    private int construct_decomposed_bdd(int root, int end_point, int curr) {
        if (curr == 0)
            return curr;
        if (curr == 1) {
            if (end_point == 1)
                return 1;
            else
                return 0;
        } else if (bdd_getField(root) != bdd_getField(curr)) {
            if (end_point == curr)
                return 1;
            else
                return 0;
        }

        int low = bddEngine.getLow(curr);
        int high = bddEngine.getHigh(curr);

        int new_low = construct_decomposed_bdd(root, end_point, low);
        bddEngine.ref(new_low);
        int new_high = construct_decomposed_bdd(root, end_point, high);
        bddEngine.ref(new_high);

        int result = bddEngine.mk(bddEngine.getVar(curr), new_low, new_high);
        bddEngine.deref(new_low);
        bddEngine.deref(new_high);
        return result;
    }

    private void get_boundary_tree(int a, HashMap<Integer, HashSet<Integer>> boundary_tree,
            ArrayList<HashSet<Integer>> boundary_points) {
        int start_level;
        start_level = bdd_getField(a);
        for(int curr = 0; curr < fieldNum; curr++)
        {
            boundary_points.add(new HashSet<Integer>());
        }
        boundary_points.get(start_level).add(a);
        if (start_level == fieldNum - 1) {
            boundary_tree.put(a, new HashSet<Integer>());
            boundary_tree.get(a).add(1);
            return;
        }

        for (int curr_level = start_level; curr_level < fieldNum; curr_level++) {
            for (int abdd : boundary_points.get(curr_level)) {
                detect_boundary_point(abdd, abdd, boundary_tree, boundary_points);
            }
        }
    }

    private int bdd_getField(int a) {
        int va = bddEngine.getVar(a);
        if (a == 1 || a == 0)
            return fieldNum;
        int curr = 0;
        while(curr < fieldNum) {
            if(va <= maxVariablePerField.get(curr))
                break;
            curr++;
        }
        return curr;
    }

    @Override
    public BDD zero() {
        return new bdd(FALSE);
    }

    @Override
    public BDD one() {
        return new bdd(TRUE);
    }

    @Override
    public int getNodeTableSize() {
        return (int) nodeTable.getCurrentSize();
    }

    /**
     * Run garbage collection. Returns the number of freed nodes.
     */
    @Override
    public long runGC() {
        return 0;
    }

    @Override
    public double setMinFreeNodes(double x) {
        int old = Configuration.minFreeNodesProcent;
        Configuration.minFreeNodesProcent = (int) (x * 100);
        return (double) old / 100.;
    }

    @Override
    public int varNum() {
        return maxVariablePerField.get(fieldNum) + 1;
    }

    @Override
    protected void initialize(int nodenum, int cachesize) {}

    @Override
    public boolean isInitialized() {
        return true;
    }

    @Override
    public double setIncreaseFactor(double x) {
        return 0;
    }

    /**
     * Sets the cache ratio for the operator caches. When the node table grows, operator caches will
     * also grow to maintain the ratio.
     *
     * <p>Compare to bdd_setcacheratio.
     *
     * @param x cache ratio
     * @return the previous cache ratio
     */
    @Override
    public int setCacheRatio(int x) {
        int old_cache_ratio = CACHE_RATIO;
        if (x <= 0) {
            return old_cache_ratio;
        }
        // default factory without initialization
        // set new cache ratio and return
        if (fieldNum < 0) {
            CACHE_RATIO = x;
            return old_cache_ratio;
        }
        int new_cache_size = NDD_TABLE_SIZE / x;
        // cache only grow up
        if (new_cache_size <= CACHE_SIZE) {
            return old_cache_ratio;
        }
        CACHE_RATIO = x;
        CACHE_SIZE = new_cache_size;
        // grow operation cache
        andCache.growUpSize(CACHE_SIZE);
        notCache.growUpSize(CACHE_SIZE);
        orCache.growUpSize(CACHE_SIZE);
        return old_cache_ratio;
    }

    /**
     * Returns the logical 'and' of zero or more BDD literals (constraints on exactly one variable --
     * i.e. the variable must be true or must be false). Does not consume any inputs, and returns a
     * fresh BDD.
     *
     * <p>Precondition: The variables' levels must be strictly increasing.
     *
     * @param literals
     */
    @Override
    public BDD andLiterals(BDD... literals) {
        if (literals.length == 0) {
            return new bdd(TRUE);
        }
        if (literals.length == 1) {
            return literals[0].id();
        }
        /*
        int result = 1;
        for (int i = 0; i < literals.length; i++) {
            Map<NDDFactory.NDD, Integer> e = ((NDDFactory.bdd) literals[i])._index.edges;
            Iterator<Map.Entry<NDDFactory.NDD, Integer>> iter = e.entrySet().iterator();
            int tmp = result;
            result = bdd.and(result, iter.next().getValue());
            bdd.ref(result);
            bdd.deref(tmp);
        }
        HashMap<NDDFactory.NDD, Integer> m = new HashMap<>();
        m.put(NDDTrue, result);
        NDDFactory.NDD ndd = table.mk(((NDDFactory.bdd) literals[0])._index.field, m);
        return new bdd(ndd);
         */
        BDD ret = new bdd(TRUE);
        for (BDD bdd : literals) {
            BDD tmp = ret.and(bdd);
            // ref in create new bdd
            nodeTable.deref(((bdd) ret)._index);
            ret = tmp;
        }
        return ret;
    }

    public BDD andLiterals(boolean ip, BDD... literals) {
        int length = literals.length;
        if (length == 0) {
            return new bdd(TRUE);
        }
        if (length == 1) {
            return literals[0].id();
        }
        int lastFieldNum = length % 8;
        int fieldCount = length / 8;
        NDD ret = TRUE;
        while (fieldCount > 0) {
            int result = 1;
            int start = fieldCount * 8;
            int end = start + lastFieldNum;
            while (start < end) {
                Map<NDD, Integer> e = ((bdd) literals[start])._index.edges;
                Iterator<Map.Entry<NDD, Integer>> iter = e.entrySet().iterator();
                int tmp = result;
                result = bddEngine.and(result, iter.next().getValue());
                bddEngine.ref(result);
                bddEngine.deref(tmp);
                start++;
            }
            HashMap<NDD, Integer> m = new HashMap<>();
            m.put(ret, result);
            ret = mk(((bdd) literals[start - 1])._index.field, m);
            fieldCount--;
        }
        return new bdd(ret);
    }

    /**
     * Implementation of {@link #andAll(Collection)} and {@link #andAllAndFree(Collection)}.
     *
     * @param bdds
     * @param free
     */
    @Override
    protected BDD andAll(Collection<BDD> bdds, boolean free) {
        if (bdds.isEmpty()) {
            return new bdd(TRUE);
        }
        if (bdds.size() == 1) {
            BDD bdd = bdds.iterator().next();
            return free ? bdd : bdd.id();
        }
        if (bdds.size() == 2) {
            Iterator<BDD> iter = bdds.iterator();
            BDD bdd1 = iter.next();
            BDD bdd2 = iter.next();
            return free ? bdd1.andWith(bdd2) : bdd1.and(bdd2);
        }
        BDD ret = new bdd(TRUE);
        for (BDD bdd : bdds) {
            ret.andWith(bdd);
        }
        /* free when in with method
         * if (free) {
         *     bdds.forEach(BDD::free);
         * }
         */
        return new bdd(ret);
    }

    /**
     * Implementation of {@link #orAll(Collection)} and {@link #orAllAndFree(Collection)}.
     *
     * @param bdds
     * @param free
     */
    @Override
    protected BDD orAll(Collection<BDD> bdds, boolean free) {
        if (bdds.isEmpty()) {
            return new bdd(TRUE);
        }
        if (bdds.size() == 1) {
            BDD bdd = bdds.iterator().next();
            return free ? bdd : bdd.id();
        }
        if (bdds.size() == 2) {
            Iterator<BDD> iter = bdds.iterator();
            BDD bdd1 = iter.next();
            BDD bdd2 = iter.next();
            return free ? bdd1.orWith(bdd2) : bdd1.or(bdd2);
        }
        BDD ret = new bdd(FALSE);
        for (BDD bdd : bdds) {
            ret = ret.or(bdd);
        }
        if (free) {
            bdds.forEach(BDD::free);
        }
        return new bdd(ret);
    }

    @Override
    public int setNodeTableSize(int n) {
        // ndd node table only grow up
        if (n <= NDD_TABLE_SIZE) {
            return NDD_TABLE_SIZE;
        }
        // node table grow up
        int oldNDDTableSize = NDD_TABLE_SIZE;
        NDD_TABLE_SIZE = n;
        nodeTable.setMaxSize(NDD_TABLE_SIZE);
        // cache grow up
        int oldCacheSize = CACHE_SIZE;
        CACHE_SIZE = NDD_TABLE_SIZE / CACHE_RATIO;
        andCache.growUpSize(CACHE_SIZE);
        notCache.growUpSize(CACHE_SIZE);
        orCache.growUpSize(CACHE_SIZE);
        return oldNDDTableSize;
    }

    @Override
    public int setCacheSize(int n) {
        // only grow up
        if (n <= CACHE_SIZE) {
            return CACHE_SIZE;
        }
        int oldCacheSize = CACHE_SIZE;
        // make sure the lower cache ratio for larger cache size
        int new_cache_ratio = NDD_TABLE_SIZE / n;
        if (new_cache_ratio >= CACHE_RATIO) {
            return oldCacheSize;
        }
        CACHE_RATIO = new_cache_ratio;
        CACHE_SIZE = NDD_TABLE_SIZE / CACHE_RATIO;
        andCache.growUpSize(CACHE_SIZE);
        notCache.growUpSize(CACHE_SIZE);
        orCache.growUpSize(CACHE_SIZE);
        return oldCacheSize;
    }

    @Override
    public void printAll() {}

    @Override
    public void printTable(BDD b) {}

    @Override
    public int level2Var(int level) { return 0; }

    @Override
    public int var2Level(int var) {
        return var;
    }

    @Override
    public void setVarOrder(int[] neworder) {}

    @Override
    public BDDPairing makePair() { return null; }

    @Override
    public int duplicateVar(int var) { return 0; }

    @Override
    public int nodeCount(Collection r) { return 0; }

    @Override
    public int getNodeNum() { return 0; }

    @Override
    public int getCacheSize() { return 0; }
    @Override
    public void printStat() {}

    @Override
    protected BDDDomain createDomain(int a, BigInteger b) { return null; }

    @Override
    protected BDDBitVector createBitVector(int a) { return null; }
    
    /**
     * NDD node count
     * BDD node count
     * BDD operation cache status
     */
    public void showStatus() {
        System.out.println("=================================");
        System.out.println("NDD node count: " + mkCount);
        System.out.println("BDD node count: " + bddEngine.mkCount);
        System.out.println("BDD show status: ");
        bddEngine.showStats();
        System.out.println("=================================");
    }

    @Override
    public BDD onehotVars(BDD... variables) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'onehotVars'");
    }
}