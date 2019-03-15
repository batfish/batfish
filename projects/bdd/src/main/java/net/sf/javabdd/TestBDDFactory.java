// TestBDDFactory.java, created Aug 2, 2003 10:02:48 PM by joewhaley
// Copyright (C) 2003 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package net.sf.javabdd;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.io.IOException;
import java.math.BigInteger;

/**
 * <p>This BDD factory is used to test other BDD factories.  It is a wrapper around
 * two BDD factories, and all operations are performed on both factories.  It
 * throws an exception if the results from the two implementations do not match.</p>
 * 
 * @see net.sf.javabdd.BDDFactory
 * 
 * @author John Whaley
 * @version $Id: TestBDDFactory.java,v 1.8 2005/05/21 08:47:09 joewhaley Exp $
 */
public class TestBDDFactory extends BDDFactory {

    BDDFactory f1, f2;

    public TestBDDFactory(BDDFactory a, BDDFactory b) {
        f1 = a; f2 = b;
    }

    public static BDDFactory init(int nodenum, int cachesize) {
        String bdd1 = getProperty("bdd1", "j");
        String bdd2 = getProperty("bdd2", "micro");
        BDDFactory a = BDDFactory.init(bdd1, nodenum, cachesize);
        BDDFactory b = BDDFactory.init(bdd2, nodenum, cachesize);
        return new TestBDDFactory(a, b);
    }

    public static final void assertSame(boolean b, String s) {
        if (!b) {
            throw new InternalError(s);
        }
    }
    
    public static final void assertSame(BDD b1, BDD b2, String s) {
        if (!b1.toString().equals(b2.toString())) {
        //if (b1.nodeCount() != b2.nodeCount()) {
            System.out.println("b1 = "+b1.nodeCount());
            System.out.println("b2 = "+b2.nodeCount());
            System.out.println("b1 = "+b1.toString());
            System.out.println("b2 = "+b2.toString());
            throw new InternalError(s);
        }
    }

    public static final void assertSame(boolean b, BDD b1, BDD b2, String s) {
        if (!b) {
            System.err.println("b1 = "+b1);
            System.err.println("b2 = "+b2);
            throw new InternalError(s);
        }
    }
    
    private class TestBDD extends BDD {

        BDD b1, b2;

        TestBDD(BDD a, BDD b) {
            this.b1 = a;
            this.b2 = b;
            assertSame(a, b, "constructor");
        }

        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#getFactory()
         */
        public BDDFactory getFactory() {
            return TestBDDFactory.this;
        }

        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#isZero()
         */
        public boolean isZero() {
            boolean r1 = b1.isZero();
            boolean r2 = b2.isZero();
            assertSame(r1 == r2, b1, b2, "isZero");
            return r1;
        }

        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#isOne()
         */
        public boolean isOne() {
            boolean r1 = b1.isOne();
            boolean r2 = b2.isOne();
            assertSame(r1 == r2, b1, b2, "isOne");
            return r1;
        }

        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#var()
         */
        public int var() {
            int r1 = b1.var();
            int r2 = b2.var();
            assertSame(r1 == r2, b1, b2, "var");
            return r1;
        }

        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#high()
         */
        public BDD high() {
            BDD r1 = b1.high();
            BDD r2 = b2.high();
            return new TestBDD(r1, r2);
        }

        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#low()
         */
        public BDD low() {
            BDD r1 = b1.low();
            BDD r2 = b2.low();
            return new TestBDD(r1, r2);
        }

        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#id()
         */
        public BDD id() {
            BDD r1 = b1.id();
            BDD r2 = b2.id();
            return new TestBDD(r1, r2);
        }

        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#not()
         */
        public BDD not() {
            BDD r1 = b1.not();
            BDD r2 = b2.not();
            return new TestBDD(r1, r2);
        }

        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#ite(net.sf.javabdd.BDD, net.sf.javabdd.BDD)
         */
        public BDD ite(BDD thenBDD, BDD elseBDD) {
            BDD c1 = ((TestBDD)thenBDD).b1;
            BDD c2 = ((TestBDD)thenBDD).b2;
            BDD d1 = ((TestBDD)elseBDD).b1;
            BDD d2 = ((TestBDD)elseBDD).b2;
            BDD r1 = b1.ite(c1, d1);
            BDD r2 = b2.ite(c2, d2);
            return new TestBDD(r1, r2);
        }

        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#relprod(net.sf.javabdd.BDD, net.sf.javabdd.BDD)
         */
        public BDD relprod(BDD that, BDD var) {
            BDD c1 = ((TestBDD)that).b1;
            BDD c2 = ((TestBDD)that).b2;
            BDD d1 = ((TestBDD)var).b1;
            BDD d2 = ((TestBDD)var).b2;
            BDD r1 = b1.relprod(c1, d1);
            BDD r2 = b2.relprod(c2, d2);
            return new TestBDD(r1, r2);
        }

        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#compose(net.sf.javabdd.BDD, int)
         */
        public BDD compose(BDD g, int var) {
            BDD c1 = ((TestBDD)g).b1;
            BDD c2 = ((TestBDD)g).b2;
            BDD r1 = b1.compose(c1, var);
            BDD r2 = b2.compose(c2, var);
            return new TestBDD(r1, r2);
        }

        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#veccompose(net.sf.javabdd.BDDPairing)
         */
        public BDD veccompose(BDDPairing pair) {
            BDDPairing c1 = ((TestBDDPairing)pair).b1;
            BDDPairing c2 = ((TestBDDPairing)pair).b2;
            BDD r1 = b1.veccompose(c1);
            BDD r2 = b2.veccompose(c2);
            return new TestBDD(r1, r2);
        }

        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#constrain(net.sf.javabdd.BDD)
         */
        public BDD constrain(BDD that) {
            BDD c1 = ((TestBDD)that).b1;
            BDD c2 = ((TestBDD)that).b2;
            BDD r1 = b1.constrain(c1);
            BDD r2 = b2.constrain(c2);
            return new TestBDD(r1, r2);
        }

        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#exist(net.sf.javabdd.BDD)
         */
        public BDD exist(BDD var) {
            BDD c1 = ((TestBDD)var).b1;
            BDD c2 = ((TestBDD)var).b2;
            BDD r1 = b1.exist(c1);
            BDD r2 = b2.exist(c2);
            return new TestBDD(r1, r2);
        }

        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#forAll(net.sf.javabdd.BDD)
         */
        public BDD forAll(BDD var) {
            BDD c1 = ((TestBDD)var).b1;
            BDD c2 = ((TestBDD)var).b2;
            BDD r1 = b1.forAll(c1);
            BDD r2 = b2.forAll(c2);
            return new TestBDD(r1, r2);
        }

        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#unique(net.sf.javabdd.BDD)
         */
        public BDD unique(BDD var) {
            BDD c1 = ((TestBDD)var).b1;
            BDD c2 = ((TestBDD)var).b2;
            BDD r1 = b1.unique(c1);
            BDD r2 = b2.unique(c2);
            return new TestBDD(r1, r2);
        }

        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#restrict(net.sf.javabdd.BDD)
         */
        public BDD restrict(BDD var) {
            BDD c1 = ((TestBDD)var).b1;
            BDD c2 = ((TestBDD)var).b2;
            BDD r1 = b1.restrict(c1);
            BDD r2 = b2.restrict(c2);
            return new TestBDD(r1, r2);
        }

        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#restrictWith(net.sf.javabdd.BDD)
         */
        public BDD restrictWith(BDD var) {
            BDD c1 = ((TestBDD)var).b1;
            BDD c2 = ((TestBDD)var).b2;
            b1.restrictWith(c1);
            b2.restrictWith(c2);
            assertSame(b1, b2, "restrict");
            return this;
        }
        
        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#simplify(net.sf.javabdd.BDD)
         */
        public BDD simplify(BDD d) {
            BDD c1 = ((TestBDD)d).b1;
            BDD c2 = ((TestBDD)d).b2;
            BDD r1 = b1.simplify(c1);
            BDD r2 = b2.simplify(c2);
            return new TestBDD(r1, r2);
        }

        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#support()
         */
        public BDD support() {
            BDD r1 = b1.support();
            BDD r2 = b2.support();
            return new TestBDD(r1, r2);
        }

        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#apply(net.sf.javabdd.BDD, net.sf.javabdd.BDDFactory.BDDOp)
         */
        public BDD apply(BDD that, BDDOp opr) {
            BDD c1 = ((TestBDD)that).b1;
            BDD c2 = ((TestBDD)that).b2;
            BDD r1 = b1.apply(c1, opr);
            BDD r2 = b2.apply(c2, opr);
            return new TestBDD(r1, r2);
        }

        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#applyWith(net.sf.javabdd.BDD, net.sf.javabdd.BDDFactory.BDDOp)
         */
        public BDD applyWith(BDD that, BDDOp opr) {
            BDD c1 = ((TestBDD)that).b1;
            BDD c2 = ((TestBDD)that).b2;
            b1.applyWith(c1, opr);
            b2.applyWith(c2, opr);
            assertSame(b1, b2, "applyWith "+opr);
            return this;
        }

        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#applyAll(net.sf.javabdd.BDD, net.sf.javabdd.BDDFactory.BDDOp, net.sf.javabdd.BDD)
         */
        public BDD applyAll(BDD that, BDDOp opr, BDD var) {
            BDD c1 = ((TestBDD)that).b1;
            BDD c2 = ((TestBDD)that).b2;
            BDD e1 = ((TestBDD)var).b1;
            BDD e2 = ((TestBDD)var).b2;
            BDD r1 = b1.applyAll(c1, opr, e1);
            BDD r2 = b2.applyAll(c2, opr, e2);
            return new TestBDD(r1, r2);
        }

        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#applyEx(net.sf.javabdd.BDD, net.sf.javabdd.BDDFactory.BDDOp, net.sf.javabdd.BDD)
         */
        public BDD applyEx(BDD that, BDDOp opr, BDD var) {
            BDD c1 = ((TestBDD)that).b1;
            BDD c2 = ((TestBDD)that).b2;
            BDD e1 = ((TestBDD)var).b1;
            BDD e2 = ((TestBDD)var).b2;
            BDD r1 = b1.applyEx(c1, opr, e1);
            BDD r2 = b2.applyEx(c2, opr, e2);
            return new TestBDD(r1, r2);
        }

        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#applyUni(net.sf.javabdd.BDD, net.sf.javabdd.BDDFactory.BDDOp, net.sf.javabdd.BDD)
         */
        public BDD applyUni(BDD that, BDDOp opr, BDD var) {
            BDD c1 = ((TestBDD)that).b1;
            BDD c2 = ((TestBDD)that).b2;
            BDD e1 = ((TestBDD)var).b1;
            BDD e2 = ((TestBDD)var).b2;
            BDD r1 = b1.applyUni(c1, opr, e1);
            BDD r2 = b2.applyUni(c2, opr, e2);
            return new TestBDD(r1, r2);
        }

        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#satOne()
         */
        public BDD satOne() {
            BDD r1 = b1.satOne();
            BDD r2 = b2.satOne();
            return new TestBDD(r1, r2);
        }

        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#fullSatOne()
         */
        public BDD fullSatOne() {
            BDD r1 = b1.fullSatOne();
            BDD r2 = b2.fullSatOne();
            return new TestBDD(r1, r2);
        }

        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#satOne(net.sf.javabdd.BDD, net.sf.javabdd.BDD)
         */
        public BDD satOne(BDD var, boolean pol) {
            BDD c1 = ((TestBDD)var).b1;
            BDD c2 = ((TestBDD)var).b2;
            BDD r1 = b1.satOne(c1, pol);
            BDD r2 = b2.satOne(c2, pol);
            return new TestBDD(r1, r2);
        }

        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#replace(net.sf.javabdd.BDDPairing)
         */
        public BDD replace(BDDPairing pair) {
            BDDPairing c1 = ((TestBDDPairing)pair).b1;
            BDDPairing c2 = ((TestBDDPairing)pair).b2;
            BDD r1 = b1.replace(c1);
            BDD r2 = b2.replace(c2);
            return new TestBDD(r1, r2);
        }

        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#replaceWith(net.sf.javabdd.BDDPairing)
         */
        public BDD replaceWith(BDDPairing pair) {
            BDDPairing c1 = ((TestBDDPairing)pair).b1;
            BDDPairing c2 = ((TestBDDPairing)pair).b2;
            b1.replaceWith(c1);
            b2.replaceWith(c2);
            assertSame(b1, b2, "replaceWith");
            return this;
        }

        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#printDot()
         */
        public void printDot() {
            // TODO Compare!
            b1.printDot();
        }

        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#nodeCount()
         */
        public int nodeCount() {
            int r1 = b1.nodeCount();
            int r2 = b2.nodeCount();
            assertSame(r1 == r2, b1, b2, "nodeCount");
            return r1;
        }

        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#pathCount()
         */
        public double pathCount() {
            double r1 = b1.pathCount();
            double r2 = b2.pathCount();
            assertSame(r1 == r2, b1, b2, "pathCount");
            return r1;
        }

        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#satCount()
         */
        public double satCount() {
            double r1 = b1.satCount();
            double r2 = b2.satCount();
            assertSame(r1 == r2, b1, b2, "satCount");
            return r1;
        }

        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#varProfile()
         */
        public int[] varProfile() {
            int[] r1 = b1.varProfile();
            int[] r2 = b2.varProfile();
            assertSame(r1.length == r2.length, "varProfile");
            for (int i=0; i<r1.length; ++i) {
                assertSame(r1[i] == r2[i], "varProfile");
            }
            return r1;
        }

        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#equals(net.sf.javabdd.BDD)
         */
        public boolean equals(BDD that) {
            BDD c1 = ((TestBDD)that).b1;
            BDD c2 = ((TestBDD)that).b2;
            boolean r1 = b1.equals(c1);
            boolean r2 = b2.equals(c2);
            assertSame(r1 == r2, b1, b2, "equals");
            return r1;
        }

        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#hashCode()
         */
        public int hashCode() {
            // TODO Compare!
            b1.hashCode();
            return b2.hashCode();
        }

        /* (non-Javadoc)
         * @see net.sf.javabdd.BDD#free()
         */
        public void free() {
            b1.free();
            b2.free();
        }
    }

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#zero()
     */
    public BDD zero() {
        return new TestBDD(f1.zero(), f2.zero());
    }

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#one()
     */
    public BDD one() {
        return new TestBDD(f1.one(), f2.one());
    }

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#initialize(int, int)
     */
    protected void initialize(int nodenum, int cachesize) {
        f1.initialize(nodenum, cachesize);
        f2.initialize(nodenum, cachesize);
    }

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#isInitialized()
     */
    public boolean isInitialized() {
        boolean r1 = f1.isInitialized();
        boolean r2 = f2.isInitialized();
        assertSame(r1 == r2, "isInitialized");
        return r1;
    }

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#done()
     */
    public void done() {
        f1.done();
        f2.done();
    }

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#setError(int)
     */
    public void setError(int code) {
        f1.setError(code);
        f2.setError(code);
    }
    
    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#clearError()
     */
    public void clearError() {
        f1.clearError();
        f2.clearError();
    }
    
    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#setMaxNodeNum(int)
     */
    public int setMaxNodeNum(int size) {
        int r1 = f1.setMaxNodeNum(size);
        int r2 = f2.setMaxNodeNum(size);
        assertSame(r1 == r2, "setMaxNodeNum");
        return r1;
    }

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#setMinFreeNodes(double)
     */
    public double setMinFreeNodes(double x) {
        double r1 = f1.setMinFreeNodes(x);
        double r2 = f2.setMinFreeNodes(x);
        assertSame(r1 == r2, "setMinFreeNodes");
        return r1;
    }

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#setIncreaseFactor(double)
     */
    public double setIncreaseFactor(double x) {
        double r1 = f1.setIncreaseFactor(x);
        double r2 = f2.setIncreaseFactor(x);
        assertSame(r1 == r2, "setIncreaseFactor");
        return r1;
    }
    
    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#setMaxIncrease(int)
     */
    public int setMaxIncrease(int x) {
        int r1 = f1.setMaxIncrease(x);
        int r2 = f2.setMaxIncrease(x);
        assertSame(r1 == r2, "setMaxIncrease");
        return r1;
    }

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#setCacheRatio(double)
     */
    public double setCacheRatio(double x) {
        double r1 = f1.setCacheRatio(x);
        double r2 = f2.setCacheRatio(x);
        assertSame(r1 == r2, "setCacheRatio");
        return r1;
    }

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#setNodeTableSize(int)
     */
    public int setNodeTableSize(int size) {
        int r1 = f1.setNodeTableSize(size);
        int r2 = f2.setNodeTableSize(size);
        assertSame(r1 == r2, "setNodeTableSize");
        return r1;
    }
    
    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#setCacheSize(int)
     */
    public int setCacheSize(int size) {
        int r1 = f1.setCacheSize(size);
        int r2 = f2.setCacheSize(size);
        assertSame(r1 == r2, "setCacheSize");
        return r1;
    }
    
    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#varNum()
     */
    public int varNum() {
        int r1 = f1.varNum();
        int r2 = f2.varNum();
        assertSame(r1 == r2, "varNum");
        return r1;
    }

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#setVarNum(int)
     */
    public int setVarNum(int num) {
        int r1 = f1.setVarNum(num);
        int r2 = f2.setVarNum(num);
        //assertSame(r1 == r2, "setVarNum");
        return r1;
    }

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#duplicateVar(int)
     */
    public int duplicateVar(int var) {
        int r1 = f1.duplicateVar(var);
        int r2 = f2.duplicateVar(var);
        assertSame(r1 == r2, "duplicateVar");
        return r1;
    }
    
    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#ithVar(int)
     */
    public BDD ithVar(int var) {
        return new TestBDD(f1.ithVar(var), f2.ithVar(var));
    }

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#nithVar(int)
     */
    public BDD nithVar(int var) {
        return new TestBDD(f1.nithVar(var), f2.nithVar(var));
    }

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#printAll()
     */
    public void printAll() {
        // TODO Compare!
        f1.printAll();
    }

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#printTable(net.sf.javabdd.BDD)
     */
    public void printTable(BDD b) {
        // TODO Compare!
        BDD b1 = ((TestBDD)b).b1;
        f1.printTable(b1);
    }

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#load(java.lang.String)
     */
    public BDD load(String filename) throws IOException {
        return new TestBDD(f1.load(filename), f2.load(filename));
    }

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#save(java.lang.String, net.sf.javabdd.BDD)
     */
    public void save(String filename, BDD var) throws IOException {
        // TODO Compare!
        BDD b1 = ((TestBDD)var).b1;
        f1.save(filename, b1);
    }

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#level2Var(int)
     */
    public int level2Var(int level) {
        int r1 = f1.level2Var(level);
        int r2 = f2.level2Var(level);
        assertSame(r1 == r2, "level2Var");
        return r1;
    }

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#var2Level(int)
     */
    public int var2Level(int var) {
        int r1 = f1.var2Level(var);
        int r2 = f2.var2Level(var);
        assertSame(r1 == r2, "var2Level");
        return r1;
    }

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#reorder(net.sf.javabdd.BDDFactory.ReorderMethod)
     */
    public void reorder(ReorderMethod m) {
        f1.reorder(m);
        f2.reorder(m);
    }

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#autoReorder(net.sf.javabdd.BDDFactory.ReorderMethod)
     */
    public void autoReorder(ReorderMethod method) {
        f1.autoReorder(method);
        f2.autoReorder(method);
    }

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#autoReorder(net.sf.javabdd.BDDFactory.ReorderMethod, int)
     */
    public void autoReorder(ReorderMethod method, int max) {
        f1.autoReorder(method, max);
        f2.autoReorder(method, max);
    }

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#getReorderMethod()
     */
    public ReorderMethod getReorderMethod() {
        ReorderMethod r1 = f1.getReorderMethod();
        ReorderMethod r2 = f2.getReorderMethod();
        assertSame(r1.equals(r2), "getReorderMethod");
        return r1;
    }

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#getReorderTimes()
     */
    public int getReorderTimes() {
        int r1 = f1.getReorderTimes();
        int r2 = f2.getReorderTimes();
        assertSame(r1 == r2, "getReorderTimes");
        return r1;
    }

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#disableReorder()
     */
    public void disableReorder() {
        f1.disableReorder();
        f2.disableReorder();
    }

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#enableReorder()
     */
    public void enableReorder() {
        f1.enableReorder();
        f2.enableReorder();
    }

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#reorderVerbose(int)
     */
    public int reorderVerbose(int v) {
        int r1 = f1.reorderVerbose(v);
        int r2 = f2.reorderVerbose(v);
        assertSame(r1 == r2, "reorderVerbose");
        return r1;
    }

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#setVarOrder(int[])
     */
    public void setVarOrder(int[] neworder) {
        f1.setVarOrder(neworder);
        f2.setVarOrder(neworder);
    }

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#addVarBlock(net.sf.javabdd.BDD, boolean)
     */
    public void addVarBlock(BDD var, boolean fixed) {
        BDD c1 = ((TestBDD)var).b1;
        BDD c2 = ((TestBDD)var).b2;
        f1.addVarBlock(c1, fixed);
        f2.addVarBlock(c2, fixed);
    }

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#addVarBlock(int, int, boolean)
     */
    public void addVarBlock(int first, int last, boolean fixed) {
        f1.addVarBlock(first, last, fixed);
        f2.addVarBlock(first, last, fixed);
    }

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#varBlockAll()
     */
    public void varBlockAll() {
        f1.varBlockAll();
        f2.varBlockAll();
    }

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#clearVarBlocks()
     */
    public void clearVarBlocks() {
        f1.clearVarBlocks();
        f2.clearVarBlocks();
    }

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#printOrder()
     */
    public void printOrder() {
        // TODO Compare!
        f1.printOrder();
    }

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#nodeCount(java.util.Collection)
     */
    public int nodeCount(Collection r) {
        LinkedList a1 = new LinkedList();
        LinkedList a2 = new LinkedList();
        for (Iterator i=r.iterator(); i.hasNext();) {
            TestBDD b = (TestBDD)i.next();
            a1.add(b.b1);
            a2.add(b.b2);
        }
        int r1 = f1.nodeCount(a1);
        int r2 = f2.nodeCount(a2);
        assertSame(r1 == r2, "nodeCount");
        return r1;
    }

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#getNodeTableSize()
     */
    public int getNodeTableSize() {
        int r1 = f1.getNodeTableSize();
        int r2 = f2.getNodeTableSize();
        assertSame(r1 == r2, "getNodeTableSize");
        return r1;
    }

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#getNodeNum()
     */
    public int getNodeNum() {
        int r1 = f1.getNodeNum();
        int r2 = f2.getNodeNum();
        assertSame(r1 == r2, "getNodeNum");
        return r1;
    }

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#getCacheSize()
     */
    public int getCacheSize() {
        int r1 = f1.getCacheSize();
        int r2 = f2.getCacheSize();
        assertSame(r1 == r2, "getCacheSize");
        return r1;
    }
    
    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#reorderGain()
     */
    public int reorderGain() {
        int r1 = f1.reorderGain();
        int r2 = f2.reorderGain();
        assertSame(r1 == r2, "reorderGain");
        return r1;
    }

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#printStat()
     */
    public void printStat() {
        // TODO Compare!
        f1.printStat();
    }

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#makePair()
     */
    public BDDPairing makePair() {
        BDDPairing p1 = f1.makePair();
        BDDPairing p2 = f2.makePair();
        return new TestBDDPairing(p1, p2);
    }

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#swapVar(int, int)
     */
    public void swapVar(int v1, int v2) {
        f1.swapVar(v1, v2);
        f2.swapVar(v1, v2);
    }

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#createDomain(int, BigInteger)
     */
    protected BDDDomain createDomain(int a, BigInteger b) {
        return new TestBDDDomain(a, b);
    }

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#createBitVector(int)
     */
    protected BDDBitVector createBitVector(int a) {
        return new TestBDDBitVector(a);
    }

    private static class TestBDDPairing extends BDDPairing {
        
        BDDPairing b1, b2;
        
        TestBDDPairing(BDDPairing p1, BDDPairing p2) {
            this.b1 = p1;
            this.b2 = p2;
        }

        /* (non-Javadoc)
         * @see net.sf.javabdd.BDDPairing#set(int, int)
         */
        public void set(int oldvar, int newvar) {
            b1.set(oldvar, newvar);
            b2.set(oldvar, newvar);
        }

        /* (non-Javadoc)
         * @see net.sf.javabdd.BDDPairing#set(int, net.sf.javabdd.BDD)
         */
        public void set(int oldvar, BDD newvar) {
            b1.set(oldvar, newvar);
            b2.set(oldvar, newvar);
        }

        /* (non-Javadoc)
         * @see net.sf.javabdd.BDDPairing#reset()
         */
        public void reset() {
            b1.reset();
            b2.reset();
        }
        
    }
        
    private class TestBDDDomain extends BDDDomain {

        private TestBDDDomain(int a, BigInteger b) {
            super(a, b);
        }

        /* (non-Javadoc)
         * @see net.sf.javabdd.BDDDomain#getFactory()
         */
        public BDDFactory getFactory() {
            return TestBDDFactory.this;
        }
        
    }

    private class TestBDDBitVector extends BDDBitVector {

        TestBDDBitVector(int a) {
            super(a);
        }
        
        /* (non-Javadoc)
         * @see net.sf.javabdd.BDDBitVector#getFactory()
         */
        public BDDFactory getFactory() {
            return TestBDDFactory.this;
        }
        
    }
    
    public static final String REVISION = "$Revision: 1.8 $";

    /* (non-Javadoc)
     * @see net.sf.javabdd.BDDFactory#getVersion()
     */
    public String getVersion() {
        return "TestBDD "+REVISION.substring(11, REVISION.length()-2)+
               " of ("+f1.getVersion()+","+f2.getVersion()+")";
    }
}
