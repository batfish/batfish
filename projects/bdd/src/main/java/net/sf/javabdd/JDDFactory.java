// JDDFactory.java, created Aug 1, 2003 7:06:47 PM by joewhaley
// Copyright (C) 2003 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package net.sf.javabdd;

import java.util.Collection;
import java.math.BigInteger;

/**
 * JDDFactory
 * 
 * @author John Whaley
 * @version $Id: JDDFactory.java,v 1.6 2005/05/21 08:47:10 joewhaley Exp $
 */
public class JDDFactory extends BDDFactory {

    private final jdd.bdd.BDD bdd;
    private int[] vars; // indexed by EXTERNAL
    private int[] level2var; // internal -> external
    private int[] var2level; // external -> internal
    
    private JDDFactory(int nodenum, int cachesize) {
        bdd = new jdd.bdd.BDD(nodenum, cachesize);
        vars = new int[256];
        jdd.util.Options.verbose = true;
    }
    
    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#init(int, int)
     */
    public static BDDFactory init(int nodenum, int cachesize) {
        BDDFactory f = new JDDFactory(nodenum, cachesize);
        return f;
    }

    /**
     * Wrapper for the BDD index number used internally in the representation.
     */
    private class bdd extends BDD {
        int _index;

        static final int INVALID_BDD = -1;

        bdd(int index) {
            this._index = index;
            bdd.ref(_index);
        }

        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#getFactory()
         */
        public BDDFactory getFactory() {
            return JDDFactory.this;
        }

        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#isZero()
         */
        public boolean isZero() {
            return _index == bdd.getZero();
        }

        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#isOne()
         */
        public boolean isOne() {
            return _index == bdd.getOne();
        }

        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#var()
         */
        public int var() {
            int v = bdd.getVar(_index);
            return level2var != null ? level2var[v] : v;
        }

        public int level() {
            int v = bdd.getVar(_index);
            return v;
        }
        
        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#high()
         */
        public BDD high() {
            return new bdd(bdd.getHigh(_index));
        }

        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#low()
         */
        public BDD low() {
            return new bdd(bdd.getLow(_index));
        }

        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#id()
         */
        public BDD id() {
            return new bdd(_index);
        }

        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#not()
         */
        public BDD not() {
            return new bdd(bdd.not(_index));
        }

        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#ite(net.sf.javabdd.BDD, net.sf.javabdd.BDD)
         */
        public BDD ite(BDD thenBDD, BDD elseBDD) {
            int x = _index;
            int y = ((bdd) thenBDD)._index;
            int z = ((bdd) elseBDD)._index;
            return new bdd(bdd.ite(x, y, z));
        }

        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#relprod(net.sf.javabdd.BDD, net.sf.javabdd.BDD)
         */
        public BDD relprod(BDD that, BDD var) {
            int x = _index;
            int y = ((bdd) that)._index;
            int z = ((bdd) var)._index;
            return new bdd(bdd.relProd(x, y, z));
        }

        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#compose(net.sf.javabdd.BDD, int)
         */
        public BDD compose(BDD g, int var) {
            int x = _index;
            int y = ((bdd) g)._index;
            return null; // todo.
        }

        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#veccompose(net.sf.javabdd.BDDPairing)
         */
        public BDD veccompose(BDDPairing pair) {
            int x = _index;
            return null; // todo.
        }

        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#constrain(net.sf.javabdd.BDD)
         */
        public BDD constrain(BDD that) {
            int x = _index;
            int y = ((bdd) that)._index;
            return null; // todo.
        }

        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#exist(net.sf.javabdd.BDD)
         */
        public BDD exist(BDD var) {
            int x = _index;
            int y = ((bdd) var)._index;
            return new bdd(bdd.exists(x, y));
        }

        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#forAll(net.sf.javabdd.BDD)
         */
        public BDD forAll(BDD var) {
            int x = _index;
            int y = ((bdd) var)._index;
            return new bdd(bdd.forall(x, y));
        }

        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#unique(net.sf.javabdd.BDD)
         */
        public BDD unique(BDD var) {
            int x = _index;
            int y = ((bdd) var)._index;
            return null; // todo.
        }

        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#restrict(net.sf.javabdd.BDD)
         */
        public BDD restrict(BDD var) {
            int x = _index;
            int y = ((bdd) var)._index;
            return new bdd(bdd.restrict(x, y));
        }

        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#restrictWith(net.sf.javabdd.BDD)
         */
        public BDD restrictWith(BDD that) {
            int x = _index;
            int y = ((bdd) that)._index;
            int a = bdd.restrict(x, y);
            //System.out.println("restrictWith("+System.identityHashCode(this)+") "+x+" -> "+a);
            bdd.deref(x);
            if (this != that)
                that.free();
            bdd.deref(a);
            this._index = a;
            return this;
        }
        
        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#simplify(net.sf.javabdd.BDD)
         */
        public BDD simplify(BDD d) {
            int x = _index;
            int y = ((bdd) d)._index;
            return new bdd(bdd.simplify(x, y));
        }

        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#support()
         */
        public BDD support() {
            int x = _index;
            return new bdd(bdd.support(x));
        }

        private int apply0(int x, int y, int z) {
            int r;
            switch (z) {
                case 0: r = bdd.and(x, y); break;
                case 1: r = bdd.xor(x, y); break;
                case 2: r = bdd.or(x, y); break;
                case 3: r = bdd.nand(x, y); break;
                case 4: r = bdd.nor(x, y); break;
                case 5: r = bdd.imp(x, y); break;
                case 6: r = bdd.biimp(x, y); break;
                case 7: r = bdd.and(x, bdd.not(y)); break; // diff
                default:
                    throw new BDDException(); // TODO.
            }
            return r;
        }
        
        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#apply(net.sf.javabdd.BDD, net.sf.javabdd.BDDFactory.BDDOp)
         */
        public BDD apply(BDD that, BDDOp opr) {
            int x = _index;
            int y = ((bdd) that)._index;
            int z = opr.id;
            return new bdd(apply0(x, y, z));
        }

        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#applyWith(net.sf.javabdd.BDD, net.sf.javabdd.BDDFactory.BDDOp)
         */
        public BDD applyWith(BDD that, BDDOp opr) {
            int x = _index;
            int y = ((bdd) that)._index;
            int z = opr.id;
            int a = apply0(x, y, z);
            bdd.deref(x);
            if (this != that)
                that.free();
            bdd.ref(a);
            this._index = a;
            return this;
        }

        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#applyAll(net.sf.javabdd.BDD, net.sf.javabdd.BDDFactory.BDDOp, net.sf.javabdd.BDD)
         */
        public BDD applyAll(BDD that, BDDOp opr, BDD var) {
            int x = _index;
            int y = ((bdd) that)._index;
            int z = opr.id;
            int a = ((bdd) var)._index;
            // todo: combine.
            int r = apply0(x, y, z);
            bdd.ref(r);
            int r2 = bdd.forall(r, a);
            bdd.deref(r);
            return new bdd(r2);
        }

        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#applyEx(net.sf.javabdd.BDD, net.sf.javabdd.BDDFactory.BDDOp, net.sf.javabdd.BDD)
         */
        public BDD applyEx(BDD that, BDDOp opr, BDD var) {
            int x = _index;
            int y = ((bdd) that)._index;
            int z = opr.id;
            int a = ((bdd) var)._index;
            // todo: combine.
            int r = apply0(x, y, z);
            bdd.ref(r);
            int r2 = bdd.exists(r, a);
            bdd.deref(r);
            return new bdd(r2);
        }

        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#applyUni(net.sf.javabdd.BDD, net.sf.javabdd.BDDFactory.BDDOp, net.sf.javabdd.BDD)
         */
        public BDD applyUni(BDD that, BDDOp opr, BDD var) {
            int x = _index;
            int y = ((bdd) that)._index;
            int z = opr.id;
            int a = ((bdd) var)._index;
            throw new BDDException(); // todo.
        }

        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#satOne()
         */
        public BDD satOne() {
            int x = _index;
            return new bdd(bdd.oneSat(x));
        }

        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#replace(net.sf.javabdd.BDDPairing)
         */
        public BDD replace(BDDPairing pair) {
            int x = _index;
            return new bdd(bdd.replace(x, ((bddPairing) pair).pairing));
        }

        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#replaceWith(net.sf.javabdd.BDDPairing)
         */
        public BDD replaceWith(BDDPairing pair) {
            int x = _index;
            int y = bdd.replace(x, ((bddPairing) pair).pairing);
            //System.out.println("replaceWith("+System.identityHashCode(this)+") "+x+" -> "+y);
            bdd.deref(x);
            bdd.ref(y);
            _index = y;
            return this;
        }

        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#nodeCount()
         */
        public int nodeCount() {
            return bdd.nodeCount(_index);
        }

        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#satCount()
         */
        public double satCount() {
            return bdd.satCount(_index);
        }

        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#equals(net.sf.javabdd.BDD)
         */
        public boolean equals(BDD that) {
            return this._index == ((bdd) that)._index;
        }

        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#hashCode()
         */
        public int hashCode() {
            return _index;
        }

        static final boolean USE_FINALIZER = false;
        
        /**
         * @see java.lang.Object#finalize()
         */
        /*
        protected void finalize() throws Throwable {
            super.finalize();
            if (USE_FINALIZER) {
                if (false && _index >= 0) {
                    System.out.println("BDD not freed! "+System.identityHashCode(this));
                }
                this.free();
            }
        }
        */
        
        /**
         * @see net.sf.javabdd.BDD#free()
         */
        public void free() {
            bdd.deref(_index);
            _index = INVALID_BDD;
        }

        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#fullSatOne()
         */
        public BDD fullSatOne() {
            throw new BDDException();
        }

        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#satOne(net.sf.javabdd.BDD, boolean)
         */
        public BDD satOne(BDD var, boolean pol) {
            // TODO Implement this.
            throw new UnsupportedOperationException();
        }

        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#pathCount()
         */
        public double pathCount() {
            throw new BDDException();
        }

        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#varProfile()
         */
        public int[] varProfile() {
            throw new BDDException();
        }
        
    }

    private class bddPairing extends BDDPairing {
        
        private int[] from;
        private int[] to;
        private jdd.bdd.Permutation pairing;
        
        private bddPairing() {
            reset();
        }
        
        /* (non-Javadoc)
         * @see net.sf.javabdd.BDDPairing#set(int, int)
         */
        public void set(int oldvar, int newvar) {
            for (int i = 0; i < from.length; ++i) {
                if (from[i] == vars[oldvar]) {
                    to[i] = vars[newvar];
                    pairing = bdd.createPermutation(from, to);
                    return;
                }
            }
            int[] oldfrom = from;
            from = new int[from.length + 1];
            System.arraycopy(oldfrom, 0, from, 0, oldfrom.length);
            from[oldfrom.length] = vars[oldvar];
            int[] oldto = to;
            to = new int[to.length + 1];
            System.arraycopy(oldto, 0, to, 0, oldto.length);
            to[oldto.length] = vars[newvar];
            pairing = bdd.createPermutation(from, to);
        }
        
        /* (non-Javadoc)
         * @see net.sf.javabdd.BDDPairing#set(int, net.sf.javabdd.BDD)
         */
        public void set(int oldvar, BDD newvar) {
            throw new BDDException();
        }
        
        public void set(int[] oldvar, int[] newvar) {
            int[] oldfrom = from;
            from = new int[from.length + oldvar.length];
            System.arraycopy(oldfrom, 0, from, 0, oldfrom.length);
            for (int i = 0; i < oldvar.length; ++i) {
                from[i + oldfrom.length] = vars[oldvar[i]];
            }
            int[] oldto = to;
            to = new int[to.length + newvar.length];
            System.arraycopy(oldto, 0, to, 0, oldto.length);
            for (int i = 0; i < newvar.length; ++i) {
                to[i + oldto.length] = vars[newvar[i]];
            }
            //debug();
            pairing = bdd.createPermutation(from, to);
        }
        
        void debug() {
            for (int i = 0; i < from.length; ++i) {
                System.out.println(bdd.getVar(from[i])+" -> "+bdd.getVar(to[i]));
            }
        }
        
        /* (non-Javadoc)
         * @see net.sf.javabdd.BDDPairing#reset()
         */
        public void reset() {
            from = to = new int[] { };
            pairing = null;
        }
        
    }

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#zero()
     */
    public BDD zero() {
        return new bdd(bdd.getZero());
    }

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#one()
     */
    public BDD one() {
        return new bdd(bdd.getOne());
    }

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#initialize(int, int)
     */
    protected void initialize(int nodenum, int cachesize) {
        throw new BDDException();
    }

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#isInitialized()
     */
    public boolean isInitialized() {
        // TODO Auto-generated method stub
        return true;
    }

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#done()
     */
    public void done() {
        bdd.cleanup();
    }

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#setError(int)
     */
    public void setError(int code) {
        // TODO Implement this.
    }
    
    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#clearError()
     */
    public void clearError() {
        // TODO Implement this.
    }
    
    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#setMaxNodeNum(int)
     */
    public int setMaxNodeNum(int size) {
        // TODO Auto-generated method stub
        //throw new BDDException();
        return 0;
    }

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#setMinFreeNodes(double)
     */
    public double setMinFreeNodes(double x) {
        int old = jdd.util.Configuration.minFreeNodesProcent;
        jdd.util.Configuration.minFreeNodesProcent = (int)(x * 100);
        return (double) old / 100.;
    }

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#setIncreaseFactor(double)
     */
    public double setIncreaseFactor(double x) {
        // TODO.
        return 0.;
    }

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#setMaxIncrease(int)
     */
    public int setMaxIncrease(int x) {
        int old = jdd.util.Configuration.maxNodeIncrease;
        jdd.util.Configuration.maxNodeIncrease = x;
        return old;
    }

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#setNodeTableSize(int)
     */
    public int setNodeTableSize(int x) {
        // TODO.
        return getNodeTableSize();
    }
    
    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#setCacheSize(int)
     */
    public int setCacheSize(int x) {
        // TODO.
        return 0;
    }
    
    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#getCacheSize()
     */
    public int getCacheSize() {
        // TODO Implement this.
        throw new UnsupportedOperationException();
    }
    
    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#setCacheRatio(int)
     */
    public double setCacheRatio(double x) {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#varNum()
     */
    public int varNum() {
        return bdd.numberOfVariables();
    }

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#setVarNum(int)
     */
    public int setVarNum(int num) {
        if (num > Integer.MAX_VALUE / 2)
            throw new BDDException();
        int old = bdd.numberOfVariables();
        int oldSize = vars.length;
        int newSize = oldSize;
        while (num > newSize) {
            newSize *= 2;
        }
        if (oldSize != newSize) {
            int[] oldVars = vars;
            vars = new int[newSize];
            System.arraycopy(oldVars, 0, vars, 0, old);
            
            if (level2var != null) {
                int[] oldlevel2var = level2var;
                level2var = new int[newSize];
                System.arraycopy(oldlevel2var, 0, level2var, 0, old);
                
                int[] oldvar2level = var2level;
                var2level = new int[newSize];
                System.arraycopy(oldvar2level, 0, var2level, 0, old);
            }
        }
        while (bdd.numberOfVariables() < num) {
            int k = bdd.numberOfVariables();
            vars[k] = bdd.createVar();
            bdd.ref(vars[k]);
            if (level2var != null) {
                level2var[k] = k;
                var2level[k] = k;
            }
        }
        return old;
    }

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#duplicateVar(int)
     */
    public int duplicateVar(int var) {
        // TODO Implement this.
        throw new UnsupportedOperationException();
    }
    
    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#ithVar(int)
     */
    public BDD ithVar(int var) {
        if (var >= bdd.numberOfVariables())
            throw new BDDException();
        //int v = var2level != null ? var2level[var] : var;
        int v = var;
        return new bdd(vars[v]);
    }

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#nithVar(int)
     */
    public BDD nithVar(int var) {
        if (var >= bdd.numberOfVariables())
            throw new BDDException();
        //int v = var2level != null ? var2level[var] : var;
        int v = var;
        return new bdd(bdd.not(vars[v]));
    }

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#printAll()
     */
    public void printAll() {
        throw new BDDException();
    }

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#printTable(net.sf.javabdd.BDD)
     */
    public void printTable(BDD b) {
        throw new BDDException();
    }

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#level2Var(int)
     */
    public int level2Var(int level) {
        return level2var != null ? level2var[level] : level;
    }

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#var2Level(int)
     */
    public int var2Level(int var) {
        return var2level != null ? var2level[var] : var;
    }

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#reorder(net.sf.javabdd.BDDFactory.ReorderMethod)
     */
    public void reorder(ReorderMethod m) {
        throw new BDDException();
    }

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#autoReorder(net.sf.javabdd.BDDFactory.ReorderMethod)
     */
    public void autoReorder(ReorderMethod method) {
        throw new BDDException();
    }

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#autoReorder(net.sf.javabdd.BDDFactory.ReorderMethod, int)
     */
    public void autoReorder(ReorderMethod method, int max) {
        throw new BDDException();
    }

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#getReorderMethod()
     */
    public ReorderMethod getReorderMethod() {
        throw new BDDException();
    }

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#getReorderTimes()
     */
    public int getReorderTimes() {
        throw new BDDException();
    }

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#disableReorder()
     */
    public void disableReorder() {
        throw new BDDException();
    }

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#enableReorder()
     */
    public void enableReorder() {
        throw new BDDException();
    }

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#reorderVerbose(int)
     */
    public int reorderVerbose(int v) {
        throw new BDDException();
    }

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#setVarOrder(int[])
     */
    public void setVarOrder(int[] neworder) {
        // todo: setting var order corrupts all existing BDD's!
        if (var2level != null)
            throw new BDDException();
        
        if (bdd.numberOfVariables() != neworder.length)
            throw new BDDException();
        
        int[] newvars = new int[vars.length];
        var2level = new int[vars.length];
        level2var = new int[vars.length];
        for (int i = 0; i < bdd.numberOfVariables(); ++i) {
            int k = neworder[i];
            //System.out.println("Var "+k+" (node "+vars[k]+") in original order -> var "+i+" (node "+vars[i]+") in new order");
            newvars[k] = vars[i];
            var2level[k] = i;
            level2var[i] = k;
        }
        vars = newvars;
        
        //System.out.println("Number of domains: "+numberOfDomains());
        for (int i = 0; i < numberOfDomains(); ++i) {
            BDDDomain d = getDomain(i);
            d.var = makeSet(d.ivar);
            //System.out.println("Set for domain "+d+": "+d.var.toStringWithDomains());
        }
    }

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#addVarBlock(net.sf.javabdd.BDD, boolean)
     */
    public void addVarBlock(BDD var, boolean fixed) {
        throw new BDDException();
    }

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#addVarBlock(int, int, boolean)
     */
    public void addVarBlock(int first, int last, boolean fixed) {
        throw new BDDException();
    }

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#varBlockAll()
     */
    public void varBlockAll() {
        throw new BDDException();
    }

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#clearVarBlocks()
     */
    public void clearVarBlocks() {
        throw new BDDException();
    }

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#printOrder()
     */
    public void printOrder() {
        throw new BDDException();
    }

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#nodeCount(java.util.Collection)
     */
    public int nodeCount(Collection r) {
        throw new BDDException();
    }

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#getNodeTableSize()
     */
    public int getNodeTableSize() {
        // todo.
        return bdd.countRootNodes();
    }

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#getNodeNum()
     */
    public int getNodeNum() {
        // todo.
        return bdd.countRootNodes();
    }

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#reorderGain()
     */
    public int reorderGain() {
        throw new BDDException();
    }

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#printStat()
     */
    public void printStat() {
        bdd.showStats();
    }

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#makePair()
     */
    public BDDPairing makePair() {
        return new bddPairing();
    }

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#swapVar(int, int)
     */
    public void swapVar(int v1, int v2) {
        throw new BDDException();
    }

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#createDomain(int, BigInteger)
     */
    protected BDDDomain createDomain(int a, BigInteger b) {
        return new bddDomain(a, b);
    }

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#createBitVector(int)
     */
    protected BDDBitVector createBitVector(int a) {
        return new bddBitVector(a);
    }
    
    private class bddDomain extends BDDDomain {

        private bddDomain(int a, BigInteger b) {
            super(a, b);
        }

        /* (non-Javadoc)
         * @see net.sf.javabdd.BDDBitVector#getFactory()
         */
        public BDDFactory getFactory() { return JDDFactory.this; }

    }
    
    private class bddBitVector extends BDDBitVector {

        private bddBitVector(int a) {
            super(a);
        }

        /* (non-Javadoc)
         * @see net.sf.javabdd.BDDBitVector#getFactory()
         */
        public BDDFactory getFactory() { return JDDFactory.this; }

    }
    
    public static final String REVISION = "$Revision: 1.6 $";
    
    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#getVersion()
     */
    public String getVersion() {
        return "JDD "+REVISION.substring(11, REVISION.length()-2);
    }
}
