// BuDDyFactory.java, created Jan 29, 2003 9:50:57 PM by jwhaley
// Copyright (C) 2003 John Whaley
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package net.sf.javabdd;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;

/**
 * <p>An implementation of BDDFactory that relies on the BuDDy library through a
 * native interface.  You can use this by calling the "BuDDyFactory.init()"
 * method with the desired arguments.  This will return you an instance of the
 * BDDFactory class that you can use.  Call "done()" on that instance when you
 * are finished.</p>
 * 
 * <p>This class (and the BuDDy library) do NOT support multithreading.
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
 * 
 * @author John Whaley
 * @version $Id: BuDDyFactory.java,v 1.13 2005/06/29 07:49:39 joewhaley Exp $
 */
public class BuDDyFactory extends BDDFactory {

    public static BDDFactory init(int nodenum, int cachesize) {
        BuDDyFactory f;
        if (USE_FINALIZER)
            f = new BuDDyFactoryWithFinalizer();
        else
            f = new BuDDyFactory();
        f.initialize(nodenum, cachesize);
        return f;
    }
    
    /**
     * Single factory instance.  Only one factory object is enabled at a time.
     */
    private static BuDDyFactory INSTANCE;
    
    static {
        String libname = getProperty("buddylib", "buddy");
        try {
            System.loadLibrary(libname);
        } catch (java.lang.UnsatisfiedLinkError x) {
            // Cannot find library, try loading it from the current directory...
            libname = System.mapLibraryName(libname);
            String currentdir = getProperty("user.dir", ".");
            String sep = getProperty("file.separator", "/");
            String filename = currentdir+sep+libname;
            try {
                System.load(filename);
            } catch (java.lang.UnsatisfiedLinkError y) {
                File f = new File(filename);
                if (!f.exists()) throw y;
                // Try to make a copy and use that.
                try {
                    File f2 = File.createTempFile("buddy", ".dll");
                    copyFile(f, f2);
                    f2.deleteOnExit();
                    System.out.println("buddy.dll is in use, linking temporary copy "+f2);
                    System.load(f2.getAbsolutePath());
                } catch (IOException z) {
                    throw y;
                }
            }
        }
        registerNatives();
    }
    
    private static void copyFile(File in, File out) throws IOException {
        FileInputStream fis = new FileInputStream(in);
        FileOutputStream fos = new FileOutputStream(out);
        byte[] buf = new byte[1024];
        int i = 0;
        while ((i = fis.read(buf)) != -1) {
            fos.write(buf, 0, i);
        }
        fis.close();
        fos.close();
    }
    
    private static native void registerNatives();
    
    private BuDDyFactory() {}

    private static final boolean USE_FINALIZER = false;
    
    private static class BuDDyFactoryWithFinalizer extends BuDDyFactory {
        
        /**
         * @see java.lang.Object#finalize()
         */
        protected void finalize() throws Throwable {
            super.finalize();
            this.done();
        }
        
    }
    
    private static BuDDyBDD makeBDD(int id) {
        BuDDyBDD b;
        if (USE_FINALIZER) {
            b = new BuDDyBDDWithFinalizer(id);
            if (false) { // can check for specific id's here.
                System.out.println("Created "+System.identityHashCode(b)+" id "+id);
                new Exception().printStackTrace(System.out);
            }
        } else {
            b = new BuDDyBDD(id);
        }
        return b;
    }
    
    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#zero()
     */
    public BDD zero() { return makeBDD(0); }

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#one()
     */
    public BDD one() { return makeBDD(1); }
    
    /**
     * Converts collection of BuDDyBDD's into an int array, for passing to
     * native code.
     * 
     * @param c  collection of BuDDyBDD's
     * @return  int array of indices
     */
    private static int[] toBuDDyArray(Collection c) {
        int[] a = new int[c.size()];
        int k = 0;
        for (Iterator i = c.iterator(); k < a.length; ++k) {
            BuDDyBDD b = (BuDDyBDD) i.next();
            a[k] = b._id;
        }
        return a;
    }
    
    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#buildCube(int, java.util.List)
     */
    public BDD buildCube(int value, List var) {
        int[] a = toBuDDyArray(var);
        int id = buildCube0(value, a);
        return makeBDD(id);
    }
    private static native int buildCube0(int value, int[] var);

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#buildCube(int, int[])
     */
    public BDD buildCube(int value, int[] var) {
        int id = buildCube1(value, var);
        return makeBDD(id);
    }
    private static native int buildCube1(int value, int[] var);

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#makeSet(int[])
     */
    public BDD makeSet(int[] v) {
        int id = makeSet0(v);
        return makeBDD(id);
    }
    private static native int makeSet0(int[] var);
    
    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#initialize(int, int)
     */
    protected void initialize(int nodenum, int cachesize) {
        if (INSTANCE != null)
            throw new InternalError("Error: BDDFactory already initialized.");
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
        if (USE_FINALIZER) {
            System.gc();
            System.runFinalization();
        }
        INSTANCE = null;
        done0();
    }
    private static native void done0();

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#reset()
     */
    public void reset() {
        if (USE_FINALIZER) {
            System.gc();
            System.runFinalization();
        }
        super.reset();
    }
    
    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#setError(int)
     */
    public void setError(int code) {
        setError0(code);
    }
    private static native void setError0(int code);
    
    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#clearError()
     */
    public void clearError() {
        clearError0();
    }
    private static native void clearError0();
    
    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#setMaxNodeNum(int)
     */
    public int setMaxNodeNum(int size) {
        return setMaxNodeNum0(size);
    }
    private static native int setMaxNodeNum0(int size);

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#setMinFreeNodes(double)
     */
    public double setMinFreeNodes(double x) {
        return setMinFreeNodes0((int)(x * 100.)) / 100.;
    }
    private static native int setMinFreeNodes0(int x);
    
    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#setMaxIncrease(int)
     */
    public int setMaxIncrease(int x) {
        return setMaxIncrease0(x);
    }
    private static native int setMaxIncrease0(int x);
    
    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#setIncreaseFactor(double)
     */
    public double setIncreaseFactor(double x) {
        return setIncreaseFactor0(x);
    }
    private static native double setIncreaseFactor0(double x);
    
    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#setCacheRatio(int)
     */
    public double setCacheRatio(double x) {
        return setCacheRatio0((int)(x * 100.)) / 100.;
    }
    private static native int setCacheRatio0(int x);
    
    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#setNodeTableSize(int)
     */
    public int setNodeTableSize(int x) {
        return setNodeTableSize0(x);
    }
    private static native int setNodeTableSize0(int x);
    
    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#setCacheSize(int)
     */
    public int setCacheSize(int x) {
        return setCacheSize0(x);
    }
    private static native int setCacheSize0(int x);
    
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
        return duplicateVar0(var);
    }
    private static native int duplicateVar0(int var);
    
    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#extVarNum(int)
     */
    public int extVarNum(int num) {
        return extVarNum0(num);
    }
    private static native int extVarNum0(int num);
    
    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#ithVar(int)
     */
    public BDD ithVar(int var) {
        int id = ithVar0(var);
        return makeBDD(id);
    }
    private static native int ithVar0(int var);

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#nithVar(int)
     */
    public BDD nithVar(int var) {
        int id = nithVar0(var);
        return makeBDD(id);
    }
    private static native int nithVar0(int var);

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#swapVar(int, int)
     */
    public void swapVar(int v1, int v2) {
        swapVar0(v1, v2);
    }
    private static native void swapVar0(int v1, int v2);

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#makePair()
     */
    public BDDPairing makePair() {
        long ptr = makePair0();
        if (USE_FINALIZER) {
            return new BuDDyBDDPairingWithFinalizer(ptr);
        } else {
            return new BuDDyBDDPairing(ptr);
        }
    }
    private static native long makePair0();
    
    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#printAll()
     */
    public void printAll() {
        printAll0();
    }
    private static native void printAll0();

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#printTable(net.sf.javabdd.BDD)
     */
    public void printTable(BDD b) {
        BuDDyBDD bb = (BuDDyBDD) b;
        printTable0(bb._id);
    }
    private static native void printTable0(int b);

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#load(java.lang.String)
     */
    public BDD load(String filename) {
        int id = load0(filename);
        return makeBDD(id);
    }
    private static native int load0(String filename);

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#save(java.lang.String, net.sf.javabdd.BDD)
     */
    public void save(String filename, BDD b) {
        BuDDyBDD bb = (BuDDyBDD) b;
        save0(filename, bb._id);
    }
    private static native void save0(String filename, int b);

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
    public void reorder(BDDFactory.ReorderMethod method) {
        reorder0(method.id);
    }
    private static native void reorder0(int method);

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#autoReorder(net.sf.javabdd.BDDFactory.ReorderMethod)
     */
    public void autoReorder(BDDFactory.ReorderMethod method) {
        autoReorder0(method.id);
    }
    private static native void autoReorder0(int method);

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#autoReorder(net.sf.javabdd.BDDFactory.ReorderMethod, int)
     */
    public void autoReorder(BDDFactory.ReorderMethod method, int max) {
        autoReorder1(method.id, max);
    }
    private static native void autoReorder1(int method, int max);

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#getReorderMethod()
     */
    public BDDFactory.ReorderMethod getReorderMethod() {
        int method = getReorderMethod0();
        switch (method) {
            case 0: return REORDER_NONE;
            case 1: return REORDER_WIN2;
            case 2: return REORDER_WIN2ITE;
            case 3: return REORDER_WIN3;
            case 4: return REORDER_WIN3ITE;
            case 5: return REORDER_SIFT;
            case 6: return REORDER_SIFTITE;
            case 7: return REORDER_RANDOM;
            default: throw new BDDException();
        }
    }
    private static native int getReorderMethod0();

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#getReorderTimes()
     */
    public int getReorderTimes() {
        return getReorderTimes0();
    }
    private static native int getReorderTimes0();

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#disableReorder()
     */
    public void disableReorder() {
        disableReorder0();
    }
    private static native void disableReorder0();

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#enableReorder()
     */
    public void enableReorder() {
        enableReorder0();
    }
    private static native void enableReorder0();

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#reorderVerbose(int)
     */
    public int reorderVerbose(int v) {
        return reorderVerbose0(v);
    }
    private static native int reorderVerbose0(int v);

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
        BuDDyBDD bb = (BuDDyBDD) var;
        addVarBlock0(bb._id, fixed);
    }
    private static native void addVarBlock0(int var, boolean fixed);

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#addVarBlock(int, int, boolean)
     */
    public void addVarBlock(int first, int last, boolean fixed) {
        addVarBlock1(first, last, fixed);
    }
    private static native void addVarBlock1(int first, int last, boolean fixed);

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#varBlockAll()
     */
    public void varBlockAll() {
        varBlockAll0();
    }
    private static native void varBlockAll0();

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#clearVarBlocks()
     */
    public void clearVarBlocks() {
        clearVarBlocks0();
    }
    private static native void clearVarBlocks0();

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#printOrder()
     */
    public void printOrder() {
        printOrder0();
    }
    private static native void printOrder0();

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#nodeCount(java.util.Collection)
     */
    public int nodeCount(Collection r) {
        int[] a = toBuDDyArray(r);
        return nodeCount0(a);
    }
    private static native int nodeCount0(int[] a);

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#getNodeTableSize()
     */
    public int getNodeTableSize() {
        return getAllocNum0();
    }
    private static native int getAllocNum0();

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#getCacheSize()
     */
    public int getCacheSize() {
        return getCacheSize0();
    }
    private static native int getCacheSize0();
    
    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#getNodeNum()
     */
    public int getNodeNum() {
        return getNodeNum0();
    }
    private static native int getNodeNum0();

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#reorderGain()
     */
    public int reorderGain() {
        return reorderGain0();
    }
    private static native int reorderGain0();

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#printStat()
     */
    public void printStat() {
        printStat0();
    }
    private static native void printStat0();

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#createDomain(int, java.math.BigInteger)
     */
    protected BDDDomain createDomain(int a, BigInteger b) {
        return new BuDDyBDDDomain(a, b);
    }

    /* (non-Javadoc)
     * An implementation of a BDD class, used by the BuDDy interface.
     */
    private static class BuDDyBDD extends BDD {
    
        /** The value used by the BDD library. */
        protected int _id;
        
        /** An invalid id, for use in invalidating BDDs. */
        static final int INVALID_BDD = -1;
        
        protected BuDDyBDD(int id) {
            _id = id;
            addRef(_id);
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
            return _id == 0;
        }
        
        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#isOne()
         */
        public boolean isOne() {
            return _id == 1;
        }
        
        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#var()
         */
        public int var() {
            return var0(_id);
        }
        private static native int var0(int b);
        
        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#high()
         */
        public BDD high() {
            int b = high0(_id);
            return makeBDD(b);
        }
        private static native int high0(int b);
        
        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#low()
         */
        public BDD low() {
            int b = low0(_id);
            return makeBDD(b);
        }
        private static native int low0(int b);
        
        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#id()
         */
        public BDD id() {
            return makeBDD(_id);
        }
        
        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#not()
         */
        public BDD not() {
            int b = not0(_id);
            return makeBDD(b);
        }
        private static native int not0(int b);
        
        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#ite(net.sf.javabdd.BDD, net.sf.javabdd.BDD)
         */
        public BDD ite(BDD thenBDD, BDD elseBDD) {
            BuDDyBDD c = (BuDDyBDD) thenBDD;
            BuDDyBDD d = (BuDDyBDD) elseBDD;
            int b = ite0(_id, c._id, d._id);
            return makeBDD(b);
        }
        private static native int ite0(int b, int c, int d);
        
        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#relprod(net.sf.javabdd.BDD, net.sf.javabdd.BDD)
         */
        public BDD relprod(BDD that, BDD var) {
            BuDDyBDD c = (BuDDyBDD) that;
            BuDDyBDD d = (BuDDyBDD) var;
            int b = relprod0(_id, c._id, d._id);
            return makeBDD(b);
        }
        private static native int relprod0(int b, int c, int d);
        
        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#compose(net.sf.javabdd.BDD, int)
         */
        public BDD compose(BDD that, int var) {
            BuDDyBDD c = (BuDDyBDD) that;
            int b = compose0(_id, c._id, var);
            return makeBDD(b);
        }
        private static native int compose0(int b, int c, int var);
        
        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#constrain(net.sf.javabdd.BDD)
         */
        public BDD constrain(BDD that) {
            BuDDyBDD c = (BuDDyBDD) that;
            int b = constrain0(_id, c._id);
            return makeBDD(b);
        }
        private static native int constrain0(int b, int c);
        
        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#exist(net.sf.javabdd.BDD)
         */
        public BDD exist(BDD var) {
            BuDDyBDD c = (BuDDyBDD) var;
            int b = exist0(_id, c._id);
            return makeBDD(b);
        }
        private static native int exist0(int b, int var);
        
        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#forAll(net.sf.javabdd.BDD)
         */
        public BDD forAll(BDD var) {
            BuDDyBDD c = (BuDDyBDD) var;
            int b = forAll0(_id, c._id);
            return makeBDD(b);
        }
        private static native int forAll0(int b, int var);
        
        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#unique(net.sf.javabdd.BDD)
         */
        public BDD unique(BDD var) {
            BuDDyBDD c = (BuDDyBDD) var;
            int b = unique0(_id, c._id);
            return makeBDD(b);
        }
        private static native int unique0(int b, int var);
        
        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#restrict(net.sf.javabdd.BDD)
         */
        public BDD restrict(BDD var) {
            BuDDyBDD c = (BuDDyBDD) var;
            int b = restrict0(_id, c._id);
            return makeBDD(b);
        }
        private static native int restrict0(int b, int var);
        
        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#restrictWith(net.sf.javabdd.BDD)
         */
        public BDD restrictWith(BDD var) {
            BuDDyBDD c = (BuDDyBDD) var;
            int b = restrict0(_id, c._id);
            addRef(b);
            delRef(_id);
            if (this != c) {
                delRef(c._id);
                c._id = INVALID_BDD;
            }
            _id = b;
            return this;
        }
        
        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#simplify(net.sf.javabdd.BDD)
         */
        public BDD simplify(BDD d) {
            BuDDyBDD c = (BuDDyBDD) d;
            int b = simplify0(_id, c._id);
            return makeBDD(b);
        }
        private static native int simplify0(int b, int d);
        
        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#support()
         */
        public BDD support() {
            int b = support0(_id);
            return makeBDD(b);
        }
        private static native int support0(int b);
        
        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#apply(net.sf.javabdd.BDD, net.sf.javabdd.BDDFactory.BDDOp)
         */
        public BDD apply(BDD that, BDDFactory.BDDOp opr) {
            BuDDyBDD c = (BuDDyBDD) that;
            int b = apply0(_id, c._id, opr.id);
            return makeBDD(b);
        }
        private static native int apply0(int b, int c, int opr);
        
        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#applyWith(net.sf.javabdd.BDD, net.sf.javabdd.BDDFactory.BDDOp)
         */
        public BDD applyWith(BDD that, BDDFactory.BDDOp opr) {
            BuDDyBDD c = (BuDDyBDD) that;
            int b = apply0(_id, c._id, opr.id);
            addRef(b);
            delRef(_id);
            if (this != c) {
                delRef(c._id);
                c._id = INVALID_BDD;
            }
            _id = b;
            return this;
        }
        
        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#applyAll(net.sf.javabdd.BDD, net.sf.javabdd.BDDFactory.BDDOp, net.sf.javabdd.BDD)
         */
        public BDD applyAll(BDD that, BDDFactory.BDDOp opr, BDD var) {
            BuDDyBDD c = (BuDDyBDD) that;
            BuDDyBDD d = (BuDDyBDD) var;
            int b = applyAll0(_id, c._id, opr.id, d._id);
            return makeBDD(b);
        }
        private static native int applyAll0(int b, int c, int opr, int d);
        
        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#applyEx(net.sf.javabdd.BDD, net.sf.javabdd.BDDFactory.BDDOp, net.sf.javabdd.BDD)
         */
        public BDD applyEx(BDD that, BDDFactory.BDDOp opr, BDD var) {
            BuDDyBDD c = (BuDDyBDD) that;
            BuDDyBDD d = (BuDDyBDD) var;
            int b = applyEx0(_id, c._id, opr.id, d._id);
            return makeBDD(b);
        }
        private static native int applyEx0(int b, int c, int opr, int d);
        
        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#applyUni(net.sf.javabdd.BDD, net.sf.javabdd.BDDFactory.BDDOp, net.sf.javabdd.BDD)
         */
        public BDD applyUni(BDD that, BDDFactory.BDDOp opr, BDD var) {
            BuDDyBDD c = (BuDDyBDD) that;
            BuDDyBDD d = (BuDDyBDD) var;
            int b = applyUni0(_id, c._id, opr.id, d._id);
            return makeBDD(b);
        }
        private static native int applyUni0(int b, int c, int opr, int d);
        
        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#satOne()
         */
        public BDD satOne() {
            int b = satOne0(_id);
            return makeBDD(b);
        }
        private static native int satOne0(int b);
        
        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#fullSatOne()
         */
        public BDD fullSatOne() {
            int b = fullSatOne0(_id);
            return makeBDD(b);
        }
        private static native int fullSatOne0(int b);
        
        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#satOne(net.sf.javabdd.BDD, boolean)
         */
        public BDD satOne(BDD var, boolean pol) {
            BuDDyBDD c = (BuDDyBDD) var;
            int b = satOne1(_id, c._id, pol?1:0);
            return makeBDD(b);
        }
        private static native int satOne1(int b, int c, int d);
        
        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#printSet()
         */
        public void printSet() {
            printSet0(_id);
        }
        private static native void printSet0(int b);
        
        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#printDot()
         */
        public void printDot() {
            printDot0(_id);
        }
        private static native void printDot0(int b);

        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#nodeCount()
         */
        public int nodeCount() {
            return nodeCount0(_id);
        }
        private static native int nodeCount0(int b);
        
        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#pathCount()
         */
        public double pathCount() {
            return pathCount0(_id);
        }
        private static native double pathCount0(int b);
        
        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#satCount()
         */
        public double satCount() {
            return satCount0(_id);
        }
        private static native double satCount0(int b);
        
        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#satCount(net.sf.javabdd.BDD)
         */
        public double satCount(BDD varset) {
            BuDDyBDD c = (BuDDyBDD) varset;
            return satCount1(_id, c._id);
        }
        private static native double satCount1(int b, int c);
        
        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#logSatCount()
         */
        public double logSatCount() {
            return logSatCount0(_id);
        }
        private static native double logSatCount0(int b);
        
        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#logSatCount(net.sf.javabdd.BDD)
         */
        public double logSatCount(BDD varset) {
            BuDDyBDD c = (BuDDyBDD) varset;
            return logSatCount1(_id, c._id);
        }
        private static native double logSatCount1(int b, int c);
        
        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#varProfile()
         */
        public int[] varProfile() {
            return varProfile0(_id);
        }
        private static native int[] varProfile0(int b);
        
        private static native void addRef(int b);
        
        private static native void delRef(int b);
        
        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#free()
         */
        public void free() {
            if (INSTANCE != null) {
                delRef(_id);
            }
            _id = INVALID_BDD;
        }
        
        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#veccompose(net.sf.javabdd.BDDPairing)
         */
        public BDD veccompose(BDDPairing pair) {
            BuDDyBDDPairing p = (BuDDyBDDPairing) pair;
            int b = veccompose0(_id, p._ptr);
            return makeBDD(b);
        }
        private static native int veccompose0(int b, long p);
        
        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#replace(net.sf.javabdd.BDDPairing)
         */
        public BDD replace(BDDPairing pair) {
            BuDDyBDDPairing p = (BuDDyBDDPairing) pair;
            int b = replace0(_id, p._ptr);
            return makeBDD(b);
        }
        private static native int replace0(int b, long p);
        
        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#replaceWith(net.sf.javabdd.BDDPairing)
         */
        public BDD replaceWith(BDDPairing pair) {
            BuDDyBDDPairing p = (BuDDyBDDPairing) pair;
            int b = replace0(_id, p._ptr);
            addRef(b);
            delRef(_id);
            _id = b;
            return this;
        }
        
        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#equals(net.sf.javabdd.BDD)
         */
        public boolean equals(BDD that) {
            return this._id == ((BuDDyBDD) that)._id;
        }
        
        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#hashCode()
         */
        public int hashCode() {
            return this._id;
        }

    }
    
    private static class BuDDyBDDWithFinalizer extends BuDDyBDD {
        
        protected BuDDyBDDWithFinalizer(int id) {
            super(id);
        }
        
        /* Finalizer runs in different thread, and BuDDy is not thread-safe.
         * Also, the existence of any finalize() method hurts performance
         * considerably.
         */
        /* (non-Javadoc)
         * @see java.lang.Object#finalize()
         */
        protected void finalize() throws Throwable {
            super.finalize();
            if (_id >= 0) {
                System.out.println("BDD not freed! "+System.identityHashCode(this)+" _id "+_id+" nodes: "+nodeCount());
            }
            //this.free();
        }
        static {
            //System.runFinalizersOnExit(true);
        }
    }
    
    /* (non-Javadoc)
     * An implementation of a BDDDomain, used by the BuDDy interface.
     */
    private static class BuDDyBDDDomain extends BDDDomain {

        private BuDDyBDDDomain(int a, BigInteger b) {
            super(a, b);
        }

        /* (non-Javadoc)
         * @see net.sf.javabdd.BDDDomain#getFactory()
         */
        public BDDFactory getFactory() { return INSTANCE; }

    }
    
    /* (non-Javadoc)
     * An implementation of a BDDPairing, used by the BuDDy interface.
     */
    private static class BuDDyBDDPairing extends BDDPairing {
        
        private long _ptr;
        
        private BuDDyBDDPairing(long ptr) {
                this._ptr = ptr;
        }
        
        /* (non-Javadoc)
         * @see net.sf.javabdd.BDDPairing#set(int, int)
         */
        public void set(int oldvar, int newvar) {
            set0(_ptr, oldvar, newvar);
        }
        private static native void set0(long p, int oldvar, int newvar);
        
        /* (non-Javadoc)
         * @see net.sf.javabdd.BDDPairing#set(int[], int[])
         */
        public void set(int[] oldvar, int[] newvar) {
            set1(_ptr, oldvar, newvar);
        }
        private static native void set1(long p, int[] oldvar, int[] newvar);
        
        /* (non-Javadoc)
         * @see net.sf.javabdd.BDDPairing#set(int, net.sf.javabdd.BDD)
         */
        public void set(int oldvar, BDD newvar) {
            BuDDyBDD c = (BuDDyBDD) newvar;
            set2(_ptr, oldvar, c._id);
        }
        private static native void set2(long p, int oldvar, int newbdd);
        
        /* (non-Javadoc)
         * @see net.sf.javabdd.BDDPairing#set(int[], net.sf.javabdd.BDD[])
         */
        public void set(int[] oldvar, BDD[] newvar) {
            int[] a = toBuDDyArray(Arrays.asList(newvar));
            set3(_ptr, oldvar, a);
        }
        private static native void set3(long p, int[] oldvar, int[] newbdds);
        
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
        public void free() {
            if (_ptr != 0) free0(_ptr);
            _ptr = 0;
        }
        private static native void free0(long p);
        
    }
    
    private static class BuDDyBDDPairingWithFinalizer extends BuDDyBDDPairing {
        
        private BuDDyBDDPairingWithFinalizer(long ptr) {
            super(ptr);
        }

        /* (non-Javadoc)
         * @see java.lang.Object#finalize()
         */
        protected void finalize() throws Throwable {
            super.finalize();
            free();
        }

    }
    
    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#createBitVector(int)
     */
    protected BDDBitVector createBitVector(int a) {
        return new BuDDyBDDBitVector(a);
    }
    
    /* (non-Javadoc)
     * An implementation of a BDDBitVector, used by the BuDDy interface.
     */
    private static class BuDDyBDDBitVector extends BDDBitVector {

        private BuDDyBDDBitVector(int a) {
            super(a);
        }

        /* (non-Javadoc)
         * @see net.sf.javabdd.BDDBitVector#getFactory()
         */
        public BDDFactory getFactory() { return INSTANCE; }

    }
    
    public static final String REVISION = "$Revision: 1.13 $";
    
    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#getVersion()
     */
    public String getVersion() {
        return getVersion0()+" rev"+REVISION.substring(11, REVISION.length()-2);
    }
    private static native String getVersion0();
    
    // Called by native code.
    private static void gc_callback(int i) {
        INSTANCE.gbc_handler(i!=0, INSTANCE.gcstats);
    }
    
    // Called by native code.
    private static void reorder_callback(int i) {
        INSTANCE.reorder_handler(i!=0, INSTANCE.reorderstats);
    }
    
    // Called by native code.
    private static void resize_callback(int i, int j) {
        INSTANCE.resize_handler(i, j);
    }
}
