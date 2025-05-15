
package jdd.examples;


import jdd.bdd.BDD;

/**
 * This is the second simple BDD example. Created for those of you who just
 * learned to do BDD operations and are hungry to learn more.
 *
 * <p>
 * The comments in the code will guide you through creating a very simple
 * BDD Application
 */

// This is our class "Simple2", where we will put all the code for this simple example:
public class Simple2 {

	// since the example is so simple, we will put everything in the main function
	public static void main(String [] args) {

		// OK, the object of this example is to explain the quantifiers, forall and exists.
		// More specifically, we will compute
		//    "a = (Exists v1, v2) f"
		// and
		//    "b = (Forall v1, v2) f"
		// given that
		//    "f = v1 OR v2 OR v3"

		// 1. These parts are explained in the first example (Simple1.java), so we wont
		// explain them again
		BDD bdd = new BDD(1000,100);
		int v1 = bdd.createVar();
		int v2 = bdd.createVar();
		int v3 = bdd.createVar();

		int tmp = bdd.ref( bdd.or(v1, v2) );
		int f = bdd.ref( bdd.or( tmp, v3) );
		bdd.deref( tmp);



		// 2. Now, we must have some way to identify the quantified variables.
		// In JDD, this term is identifies as the logical "cube" of the involved variables.
		// in simple words, a cube is a conjunction (AND) of a list of variables.
		// in our case, cube = v1 AND v2.
			int cube = bdd.ref ( bdd.and(v1, v2) );

		// in the calls to the BDD functions forall() and exists(), we will send this
		// new BDD "cube" instead of v1 and v2. Notice that a cube is nothing but a
		// simple BDD. Like any other BDDs, it must be freed when we are done with it.




		// 3. Now, we will first try the universal quantifier, forall:
		int b = bdd.ref ( bdd.forall(f, cube) ); // Don't forget to ref()!

		// This will yield the value FALSE, since in logic "(forall v1,v2) (v1 OR v2 OR v3)" equals
		// "[v1 OR ( (v2 OR v3) AND (NOT v2 OR v3))] AND [NOT v1 OR ( (v2 OR v3) AND (NOT v2 OR v3))]"
		// which after some heavy thinking equals "v3" :)
		//
		// But don't take may word for it. Look for yourself:
		System.out.print("'b = ");
		bdd.printCubes(b);

		// 4. The existential quantifier work very much in the same way:
		int a = bdd.ref ( bdd.exists(f, cube) ); // Don't forget to ref()!

		// Now, this one will be equal to logical true, since no matter what value "v3"
		// has, the "exists" some way for the function "f" to become true:
		// "(exists v1,v2)(v1 or v2 or v3)" equals to
		// "[v1 OR ( (v2 OR v3) OR (NOT v2 OR v3))] OR [NOT v1 OR ( (v2 OR v3) OR (NOT v2 OR v3))]"
		// which equals to the logical TRUE. See for yourself:
		System.out.print("'a' = ");
		bdd.printCubes(a);



		// 5. In practice, we are often interested two now if a quantification returned a constant
		// value (one of the logical TRUE and FALSE). With JDD, checking this as easy as follows:
		if( a == 0) {
			System.out.println("sorry man, 'a' is FALSE");
		} else if(a == 1) {
			System.out.println("hurray, 'a' is TRUE!");
		}

		// Since in JDD, the logical TRUE and FALSE are mapped to "1" and "0" respectively.


		// 6. That's it folks!
		// But before you go out to celebrate, don't forget to clean up the mess:
		bdd.deref(a);
		bdd.deref(b);
		bdd.deref(f);
		bdd.ref(cube);

		// and finally
		bdd.cleanup();


		// 7. The next lesson, Simple3.java will explain more about things like
		// permutations and relational products, see you!

	}
}
