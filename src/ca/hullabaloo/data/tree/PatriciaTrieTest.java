package ca.hullabaloo.data.tree;

import junit.framework.TestCase;

import java.math.BigInteger;
import java.util.*;

public class PatriciaTrieTest extends TestCase {
    String[] dat = {"BAC", "AB", "AX", "BACARDI", "BAD"};
    private PatriciaTrie<Integer> trie = new PatriciaTrie<Integer>();

    public void testEmptyWhenCreated() {
        assertTrue(trie.isEmpty());
    }

    @SuppressWarnings({"ConstantIfStatement"})
    public void testAddAndPrint() {
        // disable
        if (true) return;

        for (int i = 0; i < dat.length; i++) {
            trie.put(dat[i], i);
            System.err.printf("[%s,%s]\n", dat[i], i);
            System.err.println(trie.debug());
        }
    }

    public void testNonExistentKeyReturnsNull() {
        assertNull(trie.get("foo"));
        trie.put("foo", 1);
        assertNull(trie.get("d"));
        assertNull(trie.get("fo"));
        assertNull(trie.get("food"));
        assertNull(trie.get("z"));
    }

    public void testOnePutAndGet() {
        trie.put("foo", 1);
        assertEquals(1, trie.get("foo"));
    }

    public void testMultipleChildren() {
        trie.put("abcdefg", 1);
        System.out.println(trie.debug());
        trie.put("mnopqrs", 2);
        System.out.println(trie.debug());
        trie.put("fmopqrs", 3);
        System.out.println(trie.debug());
        trie.put("badffks", 4);
        System.out.println(trie.debug());

        assertEquals(1, trie.get("abcdefg"));
        assertEquals(2, trie.get("mnopqrs"));
        assertEquals(3, trie.get("fmopqrs"));
        assertEquals(4, trie.get("badffks"));
    }

    public void testSimpleDataList() {
        for (int i = 0; i < dat.length; i++) {
            trie.put(dat[i], i);
            for (int j = 0; j <= i; j++)
                assertEquals(j, trie.get(dat[j]));
        }
    }

    public void testShuffledDataList() {
        for (int t = 0; t < dat.length * 2; t++) {
            Collections.shuffle(Arrays.asList(dat));
            for (int i = 0; i < dat.length; i++) {
                trie.put(dat[i], i);
                for (int j = 0; j <= i; j++)
                    assertEquals(j, trie.get(dat[j]));
            }
        }
    }

    public void testLargeStrings() {
        String[] dat = {
                "5jt3nu1ohvai31imils9d0qvqi80br6ne1kbn0k3qvk6an08p40l3o4vsrloll27kgpue4kg4mfjfkmat8egb91dsteau9ko9mnh4n8bi3jrsv05tqi33u8os4tpcb4q583lb8vv0jv8b9r49q9luqia3f3k2n7o3he3akdbv1qbip2rrs1",
                "tcpr9bscnespalc17tv7m8jpf815eum59nt5bnc2ai78418rp2t58jdkfuphmt134o7q0v7d1dojpc46vs4kq7dsalfisv9ik0d48f2m2tf07c96k60ptp6p4r6d58dfg64gpmbrnkj2glmnf3efv8v5kl4b9qno",
                "fjo3gsckua0a7s2a7nhovkp1qrl4hrgia1jv2fjrr469c4rfghu8lcqsvnrpc53f2v5tk5d5440irss3gafq9gdfbcgpfmcf9a2q1mijnlma32grkvtatdg8m9ppq85uahjdm9tod20nng2a4oog0pec6e8k3vt1c26l6euol8g7ji1tr4n",
                "1timbppjl26o48pkjcbs1c5vuifup9ft8usj5uahf44huchr8e01uf26uvuvptj0u0t0oq7dp6rmfgldi4m961rgqdvt44n0qof19jnc87cr62p8h6slafr9i32798rg2coj0p8l0oc4autrkqc1s2rqn4vt22p8m613rorrm3lkdb8hqji5",
                "tu1guese2l41j9lbja5bgodf58081lu9n3a5b9mn3kbcacu8onfbq27nlo502hojsmoqm8fbrv7i5bkljo308mqk9u0jmmuik8csfet2c9g8hv1orp1hdm5095tri4ra",
                "7cj0aomh1ps2o8nu1g14s62vmqhhllcfnum2qsslo6en68ht8cts3qs5fs55jkhqiu9raeibc4eptgbcveuokd9io7dbg770hgrh99jt32khi9febdqppsupicu3jrabn7ll82klro1hbhvk0m29cs7le5",
                "19v3b0q",
                "22co9k06q95jlbrh1liek5um0pgh8088l83s1kvls7t0nt22pue9q2fsbn9i6m23d3v3kqvkk6ma4afkj7c0eut5jitps5v5v8vi96tg71m34guo2ovosbqp91if1067udr9c1f2frqgrbfmvh6t252sccu259a29lg5uc98m70nfl9mqn0t0jq7ec",
                "ej7ujv553l5ams76l8jbl87m2oqtr7n1be6jf7ca2rhc3",
                "qcbclchlrihj8r6p4fj2pig7phusjptjlnipabl6863a9d0i9ohdp8d8l0rtvpqo9d2fkqegp7vaft6i04kinrd9k0lo7mbj92peviascineriaejjoc4s879rhsitj4vg1fj75hb5ei36ctbv6vhssotkbpkiq6"
        };

        for (int i = 0; i < dat.length; i++) {
            trie.put(dat[i], i);
            for (int j = 0; j <= i; j++)
                assertEquals(j, trie.get(dat[j]));
        }
    }

    public void testLargeRandomStrings() {
        String[] dat = randomStrings(100, 30).toArray(new String[0]);

        for (int i = 0; i < dat.length; i++) {
            trie.put(dat[i], i);
            for (int j = 0; j <= i; j++)
                assertEquals(j, trie.get(dat[j]));
        }
    }

    private Set<String> randomStrings(int count, int maxLen) {
        LinkedHashSet<String> result = new LinkedHashSet<String>();
        Random r = new Random();
        for (int i = 0; i < count; i++) {
            int len = r.nextInt(maxLen);
            result.add(new BigInteger(len * 32, r).toString(32));
        }
        return result;
    }
}
