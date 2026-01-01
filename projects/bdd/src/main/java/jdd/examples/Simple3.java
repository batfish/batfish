package jdd.examples;

import jdd.bdd.BDD;
import jdd.bdd.Permutation;

/**
 * This is the third simple BDD example. This one discusses sets, functions, permutations and
 * relation products and more
 */
public class Simple3 {

  // since the example is so simple, we will put everything in the main function
  public static void main(String[] args) {

    // 0. In this example, we assume we have a simple universe with 4 elements:
    //    U = { cat, dog, man, mouse }.
    //
    // We use two variables, v0 and v1, to encode our (at least for now) boring universe:
    //
    // v0 | v1 | element
    // ---+----+--------
    //  0 | 0  |   cat
    //  0 | 1  |   dog
    //  1 | 0  |   man
    //  1 | 1  |   mouse
    //
    BDD bdd = new BDD(1000, 100);
    int v0 = bdd.createVar();
    int v1 = bdd.createVar();

    // 1. lets build the members of the set
    int cat = bdd.ref(bdd.minterm("00"));
    int dog = bdd.ref(bdd.minterm("01"));
    int man = bdd.ref(bdd.minterm("10"));
    int mouse = bdd.ref(bdd.minterm("11"));

    // 2. Now something like the set S = { cat, mouse } is represented as a BDD
    //    s = 00 OR 11
    //
    //    Notice that we just used a boolean operation on a set. This is because
    //    the bdd of set S (which is the subset of U) is actually as a set membership
    //    function S: U -> {0,1} - sometimes also called a characteristic function.
    //    Since I am an evil person, I will try to confuse you by mixing the two
    //    in the rest of this example...
    int s = bdd.ref(bdd.or(cat, mouse));
    bdd.printSet(s);

    // 3. now lets do something simple with this, for example check if dog is
    // in the set. the easiest way to do this is to check if the intersection
    // of s INTERSECT { dog } is empty
    int tmp = bdd.and(s, dog);

    if (tmp == 0) System.out.println("As expected, dog is not in { cat, mouse} ");
    else System.out.println("Something is very wrong. Or just another glitch in the Matrix");

    // 4. Now lets create a function that operates on this set.
    //    Lets say we have two functions Friend: S -> S and
    //    Enemy: S -> S with the following values:
    //
    //    Friend(man) = dog
    //    Enemy(mouse) = cat, Enemy(cat)= dog
    //
    //    Again, we use set membership functions to represent these as
    //    Enemy: S x S -> {0,1} and Friend: S x S -> {0,1}. In particular,
    //
    //    Friend = { <man,dog> }
    //    Enemy = { <mouse,cat>, <cat,dog> }
    //
    //    The problem is that we cant represent a pair such as <man,dog> in our
    //    universe. We need to extend U with two additional variables, v0' and v1'.
    //    Since we can't have ' in variables names I normal use p (prime):
    int v0p = bdd.createVar();
    int v1p = bdd.createVar();

    // 5. Now the universe is U: v0 x v1 x v0' x v1'. Something such as
    //    <man, dog> would now be represented as "1001".
    //
    //    To simplify things later, lets create some shortcuts for
    //    the items in v0' x v1' just as we did for v0 x v1 earlier.
    //
    //    After that, we can create our Enemy and Friend functions...
    int catp = bdd.ref(bdd.minterm("--00"));
    int dogp = bdd.ref(bdd.minterm("--01"));
    int manp = bdd.ref(bdd.minterm("--10"));
    int mousep = bdd.ref(bdd.minterm("--11"));

    int friend = bdd.ref(bdd.and(man, dogp));
    int enemy = bdd.ref(bdd.and(mouse, catp));
    enemy = bdd.orTo(enemy, bdd.ref(bdd.and(cat, dogp))); // andTo() takes care of ref() / deref()

    // 5.b lets examine the result...
    System.out.println("Friend = "); // 10001
    bdd.printSet(friend);

    System.out.println("Enemy = "); // 0001, 1100
    bdd.printSet(enemy);

    // 6. now if you want to compute something like X = { friends of man }
    //    you would probably first try something like
    //
    //    X = Friend AND { man }
    int X = bdd.ref(bdd.and(friend, man));

    System.out.println("X = ");
    bdd.printSet(X);

    // 6.b
    //    But this actually yields { <man, dog> } where what we really
    //    want is { dog }. So next you would maybe try
    //
    //    Y = { y | exists x s.t. Friend(x) = y and x = man }
    //
    //    Note that we need a cube of the set used in the exists operations.
    int cube = bdd.cube("11--");
    int Y = bdd.ref(bdd.exists(X, cube));

    System.out.println("Y = ");
    bdd.printSet(Y);

    // 6.c
    //    And in normal maths this would be just enough but since we are using BDDs
    //    you will instead get
    //
    //    Y = { <_, dog> }
    //
    //    So you will need to "move" the result back to v0 x v1 (alt. from y to x)
    //
    //    Z = Y | y -> x
    //
    //    For this we need a permutation which we then use in a call to replace()
    Permutation perm = bdd.createPermutation(new int[] {v0p, v1p}, new int[] {v0, v1}); // vn' -> vn
    int Z = bdd.ref(bdd.replace(Y, perm));

    System.out.println("Z = ");
    bdd.printSet(Z);

    if (Z == dog) System.out.println("Don't worry, dog is still mans best friend!");
    else System.out.println("Dude, your dog just abandoned you...");

    // 7. the combination AND and EXIST is pretty common and it turns out that
    //    it is also often a performance bottleneck. So there is a specialized
    //    function that combines the two into one operation with much better
    //    performance: the Relational Product.
    //
    //    Lets try that on the enemy function, with a statement like this:
    //
    //    W = { y | exists x s.t. Enemy(x) = y and (x != cat) }
    //
    //    which if you haven't figured out yet should yield W = { cat } since
    //    Enemy(mouse, cat)
    //
    int not_cat = bdd.ref(bdd.not(cat));
    int Wp = bdd.ref(bdd.relProd(enemy, not_cat, cube));

    // dont forget to move it back
    int W = bdd.ref(bdd.replace(Wp, perm));

    System.out.println("W = ");
    bdd.printSet(W);

    if (W == cat)
      System.out.println("Good news everyone! We have proof that someone hates the cat!");
    else System.out.println("This is wrong, maybe the cat has hacked your computer");

    // 8. And that is it! you now know everything you need to some do
    //    first order logic calculations with BDDs.
    //
    //    But since they don't hand out Nobel Prizes in mathematics yet
    //    why not stay here for a while and kill some time by cleaning
    //    the BDDs we created while waiting for the Royal Academy to come get to
    //    their senses?

    bdd.deref(W);
    bdd.deref(Wp);
    bdd.deref(not_cat);

    bdd.deref(X);
    bdd.deref(Y);
    bdd.deref(Z);

    bdd.deref(friend);
    bdd.deref(enemy);
    bdd.deref(cube);

    bdd.deref(catp);
    bdd.deref(dogp);
    bdd.deref(manp);
    bdd.deref(mousep);

    bdd.deref(cat);
    bdd.deref(dog);
    bdd.deref(man);
    bdd.deref(mouse);

    // 9. still here?
    //    then I have an exercise for you: compute the set Q
    //
    //    Q = { q | exists x y, Enemy(x) = y and Enemy(y) = q  }
    //

  }
}
