
package jdd.examples;


import jdd.bdd.BDD;

/**
 * This is the most simple BDD example. Created for those of you doing
 * BDD coding for the first time.
 *
 * <p>
 * The comments in the code will guide you through creating a very simple
 * BDD Application
 */

// This is our class "Simple1", where we will put all the code for this simple example:
public class Simple1 {

	// since the example is so simple, we will put everything in the main function
	public static void main(String [] args) {

		// 1. The first thing we need to do is to create _one_ BDD object.
		// In JDD, the BDD object is some kind of "manager" for all BDD operations
		// (don't confuse this with a single BDD tree!)
		BDD bdd = new BDD(1000,100);
		// The two arguments we passed were the initial size of the node-table and
		// operation cache. If you work with large example you might need to increase
		// the numbers but 1000 and 100 are good for most simple applications.
		// If not enough, the JDD library will grow them automatically.



		// 2. Now, the next thing you need to do is to create some BDD variables.
		// remember that a BDD was a functions f: 2^V --> {0, 1} where V was a set
		// of boolean variables? Well, these are variables we are going to create now.
		// Lets have four variables, v1 to v4.
		int v1 = bdd.createVar();
		int v2 = bdd.createVar();
		int v3 = bdd.createVar();
		int v4 = bdd.createVar();

		// Notice that the variables are created as Java "integers". The JDD package uses
		// numbers to internally identify BDD trees. Please not that if you pass a bad
		// number, the JDD library may terminate. So do NOT do play or do any arithmetic
		// with these numbers!
		//
		// IMPORTANT NOTE: There are however two numbers that have special meaning in JDD.
		// The logical true and false are represented as the numbers "1" and "0" respectively

		// 3. These variables are used to build the function f. In fact, you can create any
		// number of functions you want with a single BDD object. Lets create three functions:
		// f1 = (v1 AND v2) OR v3
		// f2 = (v1 OR v2 OR v3 OR v4)
		// f3 = v1 XOR (NOT v4)

		// This part is a bit tricky. All BDDs have "reference-counts", meaning that you
		// call BDD.ref() on them when you get them (compare this to alloc() in C) and when
		// you are done with them you free them by calling BDD.deref() (this is like calling
		// free() in C). These two functions will help the internal memory management in JDD to
		// work properly.
		//
		// For this reason, we will need intermediate variables like "tmp", to keep track of
		// intermediate BDDs.

		int tmp1, tmp2;

		// Now, lets start with the first function.
		// Since JDD only provides binary functions, we will need to create it in steps:
		tmp1 = bdd.and( v1, v2);	// first we create (v1 AND v2)
		bdd.ref(tmp1);								// Now, we must call BDD.ref() as soon as we have one of these.

		int f1 = bdd.or( tmp1, v3);  // This is equal to (v1 AND v2) OR v3.
		bdd.ref(f1);								// Yes, we must "ref" this BDD too!
		bdd.deref(tmp1);							// We don't need tmp anymore, so lets free it.


		// Now, lets start with "f2".
		// Observe that you can combine BDD and ref operations in this way:
		tmp1 = bdd.ref ( bdd.or(v1, v2) );  //  compute the intermediate BDD and "ref"-it
		tmp2 = bdd.ref ( bdd.or(v3, v4) );	// dito...
		int f2 = bdd.ref ( bdd.or(tmp1, tmp2) );
		bdd.deref(tmp1);			// We wont need these two anymore
		bdd.deref(tmp2);


		// Now, its time for the third function
		tmp1 = bdd.ref( bdd.not(v4) ); 		// computes and ref:s (NOT v4)
		int f3 = bdd.ref( bdd.xor(tmp1, v1) );	// and then [ (NOT v4)	 OR v1];
		bdd.deref(tmp1); // ...and free memory.


		// 4. This ref() and deref() madness may seem very complicated in the beginning,
		// but you will learn to use it after just a few minutes. If you like, you can
		// write your own high-level wrapper around this "integer" representation that
		// takes care of the ref() and deref() stuff.
		//
		// Now, if you use the DebugBDD classes from the jdd.bdd.debug packages instead of
		// the BDD class, it will warn you if you have forgotten to call ref() in the
		// right places.


		// 5. Ok, so we have our functions. Lets take a look at them and see how they look like.
		// In JDD, there are multiple ways to "visualize" your BDDs:

		// 5.a The print() functions prints the internal representation of the BDD which is
		// a table with four columns (internal ID, variable, high child, low child).
		// You probably DONT want to print a BDD this was in normal situations.
		bdd.print(f1);

		// 5.b The set representation is much more useful.
		// It will print all the assignments to variables in V that will make f1 true.
		// Each line looks like this
		//     "0-1-"
		// which in this case means that v1 must be FALSE, v3 must be TRUE  while v2 and v4
		// can be anything. Put this in (v1 AND v2) OR v3 and you will get
		// (0 AND ?) OR 1 which equals 1, as promised.
		bdd.printSet(f1);

		// 5.c Cubes works same as above except that it writes the name of variables that if
		// the are TRUE, the function will have the value TRUE.
		// In this case, we will probably get something like
		// "v3"
 		// "v1 v3"
 		// "v1 v2"
 		// verifying these is an exercise left to the reader :)
		bdd.printCubes(f1);

		// 5.d Perhaps the best was to see a BDD is to actually _see_ the BDD.
		// printDot() will create a file with a picture of your BDD. With the default
		// options, assuming that you have installed the AT&T tool "DOT, the following
		// line will create "f1.png" in you current directory.
		//
		// If you don't like PNG images, you can make JDD to give you others types of
		// images, but that is a bit advanced and wont be discussed here.
		bdd.printDot("f1", f1);




		// 6.
		// OK, we have reached the end of this example.
		// But first, please remember that you must "free" you BDD objects when you are done with it.
		// The BDD objects will use a lot of memory and you don't want then staying resident in memory.
		//
		// As any other objects in JDD that must be freed, the BDD objects has a cleanup() functions
		// that returns all the allocated memory.
		//
		// Note that after calling this functions, you BDD object will be invalid for further use!
		bdd.cleanup();

		// 7.
		// Of course, you would normally need to free your f1, f2 and f3 BDDs too.
		// But when you free the parent BDD object, all BDDs are freed so you don't need to
		// worry about them anymore.
		//
		// But in general, we should have done something like "bdd.deref(f1)" just before the end.



		// END:
		// ok, this was the end of the simple example.
		// More advanced BDD operations are explained in the Simple2 example

	}
}
