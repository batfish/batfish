package jdd.bdd.debug;

import jdd.bdd.BDD;
import jdd.bdd.Permutation;
import jdd.util.Configuration;
import jdd.util.JDDConsole;
import jdd.util.Options;

import java.util.Collection;


/**
 * profiling the BDD by counting each operation.
 *
 * @see ProfiledBDD2
 * @see BDD
 */

public class ProfiledBDD extends BDD {

	private long p_and, p_or, p_xor,p_biimp, p_imp, p_not, p_nand, p_nor;
	private long p_replace, p_exists, p_forall, p_relprod;
	private long p_support, p_restrict, p_simplify, p_ite;
	private long p_satcount;


	public ProfiledBDD(int nodesize) { this(nodesize, Configuration.DEFAULT_BDD_CACHE_SIZE); }
	public ProfiledBDD(int nodesize, int cache_size) {
		super(nodesize, cache_size);
		p_and = p_or = p_xor = p_biimp = p_imp = p_not = 0;
		p_support = p_restrict = p_simplify = p_ite = 0;
		p_replace =  p_exists =  p_forall =  p_relprod  = 0;
		p_satcount = 0;

		if(Options.profile_cache) {
			new BDDDebugFrame(this);
		}
	}



	// ---------------------------------------------------------------
	// Debugging stuff
	public Collection addDebugger(BDDDebuger d) {
		Collection v = super.addDebugger( d );
		v.add( quant_cache );
		v.add( ite_cache );
		v.add( not_cache );
		v.add( op_cache );
		v.add( replace_cache );
		v.add( sat_cache );
		return v;
	}
	// ---------------------------------------------------------------

	public int and(int a, int b) { p_and++; return super.and(a,b); }
	public int or(int a, int b) { p_or++; return super.or(a,b); }
	public int xor(int a, int b) { p_xor++; return super.xor(a,b); }
	public int biimp(int a, int b) { p_biimp++; return super.biimp(a,b); }
	public int imp(int a, int b) { p_imp++; return super.imp(a,b); }
	public int nor(int a, int b) { p_nor++; return super.nor(a,b); }
	public int nand(int a, int b) { p_nand++; return super.nand(a,b); }

	public int not(int a) { p_not++; return super.not(a); }



	public int replace(int a, Permutation b) { p_replace++; return super.replace(a,b); }
	public int exists(int a, int b) { p_exists++; return super.exists(a,b); }
	public int forall(int a, int b) { p_forall++; return super.forall(a,b); }
	public int relProd(int a, int b, int c) { p_relprod++; return super.relProd(a,b,c); }
	public int ite(int a, int b, int c) { p_ite++; return super.ite(a,b,c); }

	public double satCount(int a) { p_satcount++; return super.satCount(a); }


	public int support(int a) { p_support++; return super.support(a); }
	public int restrict(int a, int b) { p_restrict++; return super.restrict(a,b); }
	public int simplify(int a, int b) { p_simplify++; return super.simplify(a,b); }


	public void showStats() {
		if(p_and > 0 || p_or > 0 || p_not > 0)
			JDDConsole.out.printf("# calls to and/or/not:                    %d/%d/%d\n", p_and, p_or, p_not);

		if(p_biimp > 0 || p_imp > 0 ||p_xor > 0)
			JDDConsole.out.printf("# calls to biimp/imp/xor:                 %d/%d/%d\n", p_biimp, p_imp, p_xor);

		if(p_nand > 0 || p_nor > 0 || p_ite > 0)
			JDDConsole.out.printf("# calls to nand/nor/ite:                  %d/%d/%d\n", p_nand, p_nor, p_ite);

		if(p_replace > 0 || p_exists > 0 || p_forall > 0 || p_relprod > 0)
			JDDConsole.out.printf("# calls to replace/exists/forall/relProd: %d/%d/%d/%d\n", p_replace, p_exists, p_forall, p_relprod);

		if(p_support > 0 || p_restrict > 0 || p_simplify > 0)
			JDDConsole.out.printf("# calls to support/restrict/simplify:     %d/%d/%d\n", p_support, p_restrict, p_simplify);

		if(p_satcount > 0)
			JDDConsole.out.printf("# calls to satCount:     %d\n",p_satcount);

		super.showStats();
	}

}
