// CUDDFactory.java, created Jan 29, 2003 9:50:57 PM by jwhaley
// Copyright (C) 2003 John Whaley
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package net.sf.javabdd;

import java.util.Collection;
import java.math.BigInteger;

/**
 * <p>An implementation of BDDFactory that relies on the CUDD library through a
 * native interface.  You can use this by calling the "CUDDFactory.init()"
 * method with the desired arguments.  This will return you an instance of the
 * BDDFactory class that you can use.  Call "done()" on that instance when you
 * are finished.</p>
 * 
 * <p>CUDD does not have much of the functionality that BuDDy has, and it has
 * not been well-tested.  Furthermore, it is slower than BuDDy.  Therefore, it
 * is recommended that you use the BuDDy library instead.</p>
 * 
 * <p>This class (and the CUDD library) do NOT support multithreading.
 * Furthermore, there can be only one instance active at a time.  You can only
 * call "init()" again after you have called "done()" on the original instance.
 * It is not recommended to call "init()" again after calling "done()" unless
 * you are _completely_ sure that all BDD objects that reference the old
 * factory have been freed.</p>
 * 
 * <p>If you really need multiple BDD factories, consider using the JavaFactory
 * class for the additional BDD factories --- JavaFactory can have multiple
 * factory instances active at a time.</p>
 * 
 * @see net.sf.javabdd.BDDFactory
 * @see net.sf.javabdd.BuDDyFactory
 * 
 * @author John Whaley
 * @version $Id: CUDDFactory.java,v 1.8 2005/06/29 07:52:06 joewhaley Exp $
 */
public class CUDDFactory extends BDDFactory {

    public static BDDFactory init(int nodenum, int cachesize) {
        CUDDFactory f = new CUDDFactory();
        f.initialize(nodenum/256, cachesize);
        return f;
    }
    
    private static CUDDFactory INSTANCE;
    
    static {
        String libname = "cudd";
        try {
            System.loadLibrary(libname);
        } catch (java.lang.UnsatisfiedLinkError x) {
            // Cannot find library, try loading it from the current directory...
            libname = System.mapLibraryName(libname);
            String currentdir = getProperty("user.dir", ".");
            String sep = getProperty("file.separator", "/");
            System.load(currentdir+sep+libname);
        }
        registerNatives();
    }
    
    private static native void registerNatives();
    
    private CUDDFactory() {}
    
    private static long zero;
    private static long one;
    
    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#zero()
     */
    public BDD zero() {
        return new CUDDBDD(zero);
    }

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#one()
     */
    public BDD one() {
        return new CUDDBDD(one);
    }

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#initialize(int, int)
     */
    protected void initialize(int nodenum, int cachesize) {
        if (INSTANCE != null) {
            throw new InternalError("Error: CUDDFactory already initialized.");
        }
        INSTANCE = this;
        initialize0(nodenum, cachesize);
    }
    private static native void initialize0(int nodenum, int cachesize);

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#isInitialized()
     */
    public boolean isInitialized() {
        return isInitialized0();
    }
    private static native boolean isInitialized0();

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#done()
     */
    public void done() {
        INSTANCE = null;
        done0();
    }
    private static native void done0();

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
        // TODO Implement this.
        System.err.println("Warning: setMaxNodeNum() not yet implemented");
        return 1000000;
    }

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#setNodeTableSize(int)
     */
    public int setNodeTableSize(int size) {
        // TODO Implement this.
        System.err.println("Warning: setNodeTableSize() not yet implemented");
        return getNodeTableSize();
    }
    
    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#setCacheSize(int)
     */
    public int setCacheSize(int size) {
        // TODO Implement this.
        System.err.println("Warning: setCacheSize() not yet implemented");
        return 0;
    }
    
    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#setMinFreeNodes(double)
     */
    public double setMinFreeNodes(double x) {
        // TODO Implement this.
        System.err.println("Warning: setMinFreeNodes() not yet implemented");
        return 0;
    }

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#setMaxIncrease(int)
     */
    public int setMaxIncrease(int x) {
        // TODO Implement this.
        System.err.println("Warning: setMaxIncrease() not yet implemented");
        return 50000;
    }

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#setCacheRatio(double)
     */
    public double setCacheRatio(double x) {
        // TODO Implement this.
        System.err.println("Warning: setCacheRatio() not yet implemented");
        return 0;
    }

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#setIncreaseFactor(double)
     */
    public double setIncreaseFactor(double x) {
        // TODO Implement this.
        System.err.println("Warning: setIncreaseFactor() not yet implemented");
        return 0;
    }
    
    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#varNum()
     */
    public int varNum() {
        return varNum0();
    }
    private static native int varNum0();

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#setVarNum(int)
     */
    public int setVarNum(int num) {
        return setVarNum0(num);
    }
    private static native int setVarNum0(int num);

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
        long id = ithVar0(var);
        return new CUDDBDD(id);
    }
    private static native long ithVar0(int var);

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#nithVar(int)
     */
    public BDD nithVar(int var) {
        BDD b = ithVar(var);
        BDD c = b.not(); b.free();
        return c;
    }

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#swapVar(int, int)
     */
    public void swapVar(int v1, int v2) {
        // TODO Implement this.
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#makePair()
     */
    public BDDPairing makePair() {
        return new CUDDBDDPairing();
    }

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#printAll()
     */
    public void printAll() {
        // TODO Implement this.
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#printTable(net.sf.javabdd.BDD)
     */
    public void printTable(BDD b) {
        // TODO Implement this.
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#level2Var(int)
     */
    public int level2Var(int level) {
        return level2Var0(level);
    }
    private static native int level2Var0(int level);

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#var2Level(int)
     */
    public int var2Level(int var) {
        return var2Level0(var);
    }
    private static native int var2Level0(int var);

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#reorder(net.sf.javabdd.BDDFactory.ReorderMethod)
     */
    public void reorder(ReorderMethod m) {
        // TODO Implement this.
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#autoReorder(net.sf.javabdd.BDDFactory.ReorderMethod)
     */
    public void autoReorder(ReorderMethod method) {
        // TODO Implement this.
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#autoReorder(net.sf.javabdd.BDDFactory.ReorderMethod, int)
     */
    public void autoReorder(ReorderMethod method, int max) {
        // TODO Implement this.
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#getReorderMethod()
     */
    public ReorderMethod getReorderMethod() {
        // TODO Implement this.
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#getReorderTimes()
     */
    public int getReorderTimes() {
        // TODO Implement this.
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#disableReorder()
     */
    public void disableReorder() {
        // TODO Implement this.
        System.err.println("Warning: disableReorder() not yet implemented");
    }

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#enableReorder()
     */
    public void enableReorder() {
        // TODO Implement this.
        System.err.println("Warning: enableReorder() not yet implemented");
    }

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#reorderVerbose(int)
     */
    public int reorderVerbose(int v) {
        // TODO Implement this.
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#setVarOrder(int[])
     */
    public void setVarOrder(int[] neworder) {
        setVarOrder0(neworder);
    }
    private static native void setVarOrder0(int[] neworder);

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#addVarBlock(net.sf.javabdd.BDD, boolean)
     */
    public void addVarBlock(BDD var, boolean fixed) {
        // TODO Implement this.
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#addVarBlock(int, int, boolean)
     */
    public void addVarBlock(int first, int last, boolean fixed) {
        // TODO Implement this.
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#varBlockAll()
     */
    public void varBlockAll() {
        // TODO Implement this.
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#clearVarBlocks()
     */
    public void clearVarBlocks() {
        // TODO Implement this.
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#printOrder()
     */
    public void printOrder() {
        // TODO Implement this.
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#nodeCount(java.util.Collection)
     */
    public int nodeCount(Collection r) {
        // TODO Implement this.
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#getNodeTableSize()
     */
    public int getNodeTableSize() {
        return getAllocNum0();
    }
    private static native int getAllocNum0();

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#getNodeNum()
     */
    public int getNodeNum() {
        return getNodeNum0();
    }
    private static native int getNodeNum0();

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#getCacheSize()
     */
    public int getCacheSize() {
        return getCacheSize0();
    }
    private static native int getCacheSize0();
    
    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#reorderGain()
     */
    public int reorderGain() {
        // TODO Implement this.
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#printStat()
     */
    public void printStat() {
        // TODO Implement this.
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#createDomain(int, BigInteger)
     */
    protected BDDDomain createDomain(int a, BigInteger b) {
        return new CUDDBDDDomain(a, b);
    }

    /* (non-Javadoc)
     * An implementation of a BDD class, used by the CUDD interface.
     */
    private static class CUDDBDD extends BDD {

        /** The pointer used by the BDD library. */
        private long _ddnode_ptr;
        
        /** An invalid id, for use in invalidating BDDs. */
        static final long INVALID_BDD = -1;
        
        private CUDDBDD(long ddnode) {
            this._ddnode_ptr = ddnode;
            addRef(ddnode);
        }

        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#getFactory()
         */
        public BDDFactory getFactory() {
            return INSTANCE;
        }
        
        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#isZero()
         */
        public boolean isZero() {
            return this._ddnode_ptr == zero;
        }

        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#isOne()
         */
        public boolean isOne() {
            return this._ddnode_ptr == one;
        }

        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#var()
         */
        public int var() {
            if (isZero() || isOne())
                throw new BDDException("cannot get var of terminal");
            return var0(_ddnode_ptr);
        }
        private static native int var0(long b);
        
        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#high()
         */
        public BDD high() {
            long b = high0(_ddnode_ptr);
            return new CUDDBDD(b);
        }
        private static native long high0(long b);
        
        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#low()
         */
        public BDD low() {
            long b = low0(_ddnode_ptr);
            return new CUDDBDD(b);
        }
        private static native long low0(long b);
        
        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#id()
         */
        public BDD id() {
            return new CUDDBDD(_ddnode_ptr);
        }

        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#not()
         */
        public BDD not() {
            long b = not0(_ddnode_ptr);
            return new CUDDBDD(b);
        }
        private static native long not0(long b);
        
        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#ite(net.sf.javabdd.BDD, net.sf.javabdd.BDD)
         */
        public BDD ite(BDD thenBDD, BDD elseBDD) {
            CUDDBDD c = (CUDDBDD) thenBDD;
            CUDDBDD d = (CUDDBDD) elseBDD;
            long b = ite0(_ddnode_ptr, c._ddnode_ptr, d._ddnode_ptr);
            return new CUDDBDD(b);
        }
        private static native long ite0(long b, long c, long d);
        
        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#relprod(net.sf.javabdd.BDD, net.sf.javabdd.BDD)
         */
        public BDD relprod(BDD that, BDD var) {
            CUDDBDD c = (CUDDBDD) that;
            CUDDBDD d = (CUDDBDD) var;
            long b = relprod0(_ddnode_ptr, c._ddnode_ptr, d._ddnode_ptr);
            return new CUDDBDD(b);
        }
        private static native long relprod0(long b, long c, long d);
        
        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#compose(net.sf.javabdd.BDD, int)
         */
        public BDD compose(BDD that, int var) {
            CUDDBDD c = (CUDDBDD) that;
            long b = compose0(_ddnode_ptr, c._ddnode_ptr, var);
            return new CUDDBDD(b);
        }
        private static native long compose0(long b, long c, int var);

        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#constrain(net.sf.javabdd.BDD)
         */
        public BDD constrain(BDD that) {
            // TODO Implement this.
            throw new UnsupportedOperationException();
        }

        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#exist(net.sf.javabdd.BDD)
         */
        public BDD exist(BDD var) {
            CUDDBDD c = (CUDDBDD) var;
            long b = exist0(_ddnode_ptr, c._ddnode_ptr);
            return new CUDDBDD(b);
        }
        private static native long exist0(long b, long c);

        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#forAll(net.sf.javabdd.BDD)
         */
        public BDD forAll(BDD var) {
            CUDDBDD c = (CUDDBDD) var;
            long b = forAll0(_ddnode_ptr, c._ddnode_ptr);
            return new CUDDBDD(b);
        }
        private static native long forAll0(long b, long c);

        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#unique(net.sf.javabdd.BDD)
         */
        public BDD unique(BDD var) {
            // TODO Implement this.
            throw new UnsupportedOperationException();
        }

        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#restrict(net.sf.javabdd.BDD)
         */
        public BDD restrict(BDD var) {
            CUDDBDD c = (CUDDBDD) var;
            long b = restrict0(_ddnode_ptr, c._ddnode_ptr);
            return new CUDDBDD(b);
        }
        private static native long restrict0(long b, long var);
        
        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#restrictWith(net.sf.javabdd.BDD)
         */
        public BDD restrictWith(BDD var) {
            CUDDBDD c = (CUDDBDD) var;
            long b = restrict0(_ddnode_ptr, c._ddnode_ptr);
            addRef(b);
            delRef(_ddnode_ptr);
            if (this != c) {
                delRef(c._ddnode_ptr);
                c._ddnode_ptr = INVALID_BDD;
            }
            _ddnode_ptr = b;
            return this;
        }
        
        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#simplify(net.sf.javabdd.BDD)
         */
        public BDD simplify(BDD d) {
            // TODO Implement this.
            throw new UnsupportedOperationException();
        }

        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#support()
         */
        public BDD support() {
            long b = support0(_ddnode_ptr);
            return new CUDDBDD(b);
        }
        private static native long support0(long b);
        
        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#apply(net.sf.javabdd.BDD, net.sf.javabdd.BDDFactory.BDDOp)
         */
        public BDD apply(BDD that, BDDFactory.BDDOp opr) {
            CUDDBDD c = (CUDDBDD) that;
            long b = apply0(_ddnode_ptr, c._ddnode_ptr, opr.id);
            return new CUDDBDD(b);
        }
        private static native long apply0(long b, long c, int opr);
        
        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#applyWith(net.sf.javabdd.BDD, net.sf.javabdd.BDDFactory.BDDOp)
         */
        public BDD applyWith(BDD that, BDDFactory.BDDOp opr) {
            CUDDBDD c = (CUDDBDD) that;
            long b = apply0(_ddnode_ptr, c._ddnode_ptr, opr.id);
            addRef(b);
            delRef(_ddnode_ptr);
            if (this != c) {
                delRef(c._ddnode_ptr);
                c._ddnode_ptr = INVALID_BDD;
            }
            _ddnode_ptr = b;
            return this;
        }
        
        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#applyAll(net.sf.javabdd.BDD, net.sf.javabdd.BDDFactory.BDDOp, net.sf.javabdd.BDD)
         */
        public BDD applyAll(BDD that, BDDOp opr, BDD var) {
            // TODO Implement this.
            throw new UnsupportedOperationException();
        }

        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#applyEx(net.sf.javabdd.BDD, net.sf.javabdd.BDDFactory.BDDOp, net.sf.javabdd.BDD)
         */
        public BDD applyEx(BDD that, BDDOp opr, BDD var) {
            // TODO Implement this.
            throw new UnsupportedOperationException();
        }

        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#applyUni(net.sf.javabdd.BDD, net.sf.javabdd.BDDFactory.BDDOp, net.sf.javabdd.BDD)
         */
        public BDD applyUni(BDD that, BDDOp opr, BDD var) {
            // TODO Implement this.
            throw new UnsupportedOperationException();
        }

        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#satOne()
         */
        public BDD satOne() {
            long b = satOne0(_ddnode_ptr);
            return new CUDDBDD(b);
        }
        private static native long satOne0(long b);
        
        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#fullSatOne()
         */
        public BDD fullSatOne() {
            // TODO Implement this.
            throw new UnsupportedOperationException();
        }

        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#satOne(net.sf.javabdd.BDD, boolean)
         */
        public BDD satOne(BDD var, boolean pol) {
            // TODO Implement this.
            throw new UnsupportedOperationException();
        }

        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#nodeCount()
         */
        public int nodeCount() {
            return nodeCount0(_ddnode_ptr);
        }
        private static native int nodeCount0(long b);
        
        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#pathCount()
         */
        public double pathCount() {
            return pathCount0(_ddnode_ptr);
        }
        private static native double pathCount0(long b);
        
        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#satCount()
         */
        public double satCount() {
            return satCount0(_ddnode_ptr);
        }
        private static native double satCount0(long b);
        
        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#varProfile()
         */
        public int[] varProfile() {
            // TODO Implement this.
            throw new UnsupportedOperationException();
        }

        private static native void addRef(long p);

        private static native void delRef(long p);
        
        static final boolean USE_FINALIZER = false;
        
        /* Finalizer runs in different thread, and CUDD is not thread-safe.
         * Also, the existence of any finalize() method hurts performance
         * considerably.
         */
        /* (non-Javadoc)
         * @see java.lang.Object#finalize()
         */
        /*
        protected void finalize() throws Throwable {
            super.finalize();
            if (USE_FINALIZER) {
                if (false && _ddnode_ptr >= 0) {
                    System.out.println("BDD not freed! "+System.identityHashCode(this));
                }
                this.free();
            }
        }
        */
        
        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#free()
         */
        public void free() {
            delRef(_ddnode_ptr);
            _ddnode_ptr = INVALID_BDD;
        }
        
        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#veccompose(net.sf.javabdd.BDDPairing)
         */
        public BDD veccompose(BDDPairing pair) {
            CUDDBDDPairing p = (CUDDBDDPairing) pair;
            long b = veccompose0(_ddnode_ptr, p._ptr);
            return new CUDDBDD(b);
        }
        private static native long veccompose0(long b, long p);
        
        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#replace(net.sf.javabdd.BDDPairing)
         */
        public BDD replace(BDDPairing pair) {
            CUDDBDDPairing p = (CUDDBDDPairing) pair;
            long b = replace0(_ddnode_ptr, p._ptr);
            return new CUDDBDD(b);
        }
        private static native long replace0(long b, long p);
        
        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#replaceWith(net.sf.javabdd.BDDPairing)
         */
        public BDD replaceWith(BDDPairing pair) {
            CUDDBDDPairing p = (CUDDBDDPairing) pair;
            long b = replace0(_ddnode_ptr, p._ptr);
            addRef(b);
            delRef(_ddnode_ptr);
            _ddnode_ptr = b;
            return this;
        }

        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#equals(net.sf.javabdd.BDD)
         */
        public boolean equals(BDD that) {
            return this._ddnode_ptr == ((CUDDBDD) that)._ddnode_ptr;
        }

        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#hashCode()
         */
        public int hashCode() {
            return (int) this._ddnode_ptr;
        }

    }
    
    /* (non-Javadoc)
     * An implementation of a BDDDomain, used by the CUDD interface.
     */
    private static class CUDDBDDDomain extends BDDDomain {

        private CUDDBDDDomain(int index, BigInteger range) {
            super(index, range);
        }

        /* (non-Javadoc)
         * @see net.sf.javabdd.BDDDomain#getFactory()
         */
        public BDDFactory getFactory() {
            return INSTANCE;
        }
        
    }

    /* (non-Javadoc)
     * An implementation of a BDDPairing, used by the CUDD interface.
     */
    private static class CUDDBDDPairing extends BDDPairing {

        long _ptr;

        private CUDDBDDPairing() {
            _ptr = alloc();
        }

        private static native long alloc();

        /* (non-Javadoc)
         * @see net.sf.javabdd.BDDPairing#set(int, int)
         */
        public void set(int oldvar, int newvar) {
            set0(_ptr, oldvar, newvar);
        }
        private static native void set0(long p, int oldvar, int newvar);

        /* (non-Javadoc)
         * @see net.sf.javabdd.BDDPairing#set(int, net.sf.javabdd.BDD)
         */
        public void set(int oldvar, BDD newvar) {
            CUDDBDD c = (CUDDBDD) newvar;
            set2(_ptr, oldvar, c._ddnode_ptr);
        }
        private static native void set2(long p, int oldvar, long newbdd);
        
        /* (non-Javadoc)
         * @see net.sf.javabdd.BDDPairing#reset()
         */
        public void reset() {
            reset0(_ptr);
        }
        private static native void reset0(long ptr);
        
        /**
         * Free the memory allocated for this pair.
         */
        private static native void free0(long ptr);
    }

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#createBitVector(int)
     */
    protected BDDBitVector createBitVector(int a) {
        return new CUDDBDDBitVector(a);
    }
    
    /* (non-Javadoc)
     * An implementation of a BDDBitVector, used by the CUDD interface.
     */
    private static class CUDDBDDBitVector extends BDDBitVector {

        private CUDDBDDBitVector(int a) {
            super(a);
        }

        public BDDFactory getFactory() { return INSTANCE; }

    }
    
    public static void main(String[] args) {
        BDDFactory bdd = init(1000000, 100000);
        
        System.out.println("One: "+CUDDFactory.one);
        System.out.println("Zero: "+CUDDFactory.zero);
        
        BDDDomain[] doms = bdd.extDomain(new int[] {50, 10, 15, 20, 15});
        
        BDD b = bdd.one();
        for (int i=0; i<doms.length-1; ++i) {
            b.andWith(doms[i].ithVar(i));
        }
        
        for (int i=0; i<bdd.numberOfDomains(); ++i) {
            BDDDomain d = bdd.getDomain(i);
            int[] ivar = d.vars();
            System.out.print("Domain #"+i+":");
            for (int j=0; j<ivar.length; ++j) {
                System.out.print(' ');
                System.out.print(j);
                System.out.print(':');
                System.out.print(ivar[j]);
            }
            System.out.println();
        }
        
        BDDPairing p = bdd.makePair(doms[2], doms[doms.length-1]);
        System.out.println("Pairing: "+p);
        
        System.out.println("Before replace(): "+b);
        BDD c = b.replace(p);
        System.out.println("After replace(): "+c);
        
        c.printDot();
    }

    public static final String REVISION = "$Revision: 1.8 $";
    
    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#getVersion()
     */
    public String getVersion() {
        return "CUDD "+REVISION.substring(11, REVISION.length()-2);
    }
    
}
