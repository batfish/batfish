package jdd.bdd;

/**
 * some BDD helper function that we could/want not put in the BDD class directly
 */

public class BDDUtil {


	/**
	 * encode the number as a BDD.
	 * must use the same encoding as numberToMinterm!
	 * @see #numberToMinterm
	 */
	public static int numberToBDD(BDD jdd, int [] vars, int num) {
		int ret = 1;

		for(int i = 0; i < vars.length; i++) {
			int next = (num & (1L << i)) == 0 ? jdd.not(vars[i]) : vars[i];
			jdd.ref(next);
			ret = jdd.andTo(ret, next);
			jdd.deref(next);

		}
		return ret;
	}

	/**
	 * encode the number as a boolean vector, starting from the given index.
	 * must use the same encoding as numberToBDD!
	 *
	 * @see #numberToBDD
	 */
	public static void numberToMinterm(int num, int length, int index, boolean [] output) {
		for(int i = 0; i < length; i++)
			output[index++] = ((num & (1L << i)) != 0) ;
	}
}
