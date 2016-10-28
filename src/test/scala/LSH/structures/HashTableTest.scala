package LSH.structures

import java.io.{FileInputStream, FileOutputStream, ObjectInputStream, ObjectOutputStream}

import LSH.hashFunctions.Hyperplane
import org.scalatest.{FlatSpec, Matchers}

/**
  * Created by remeeh on 28-10-2016.
  */
class HashTableTest extends FlatSpec with Matchers {
  "HashTable" should "contain elem x after insert" in {
    val ht = new HashTable(() => new Hyperplane(10))
    val testElem = ("0000001234", Vector(0.0, 5.2, 1.3, 3.6, 2.4, 9.2, 2.3, 3.9, 0.3, 1.2, 1.1, 9.9, 0.0, 0.3013, 0.0))
    ht+=testElem
    val returnedElem = ht.query(testElem._2)
    returnedElem.head.equals(testElem) should be (true)
  }

  "HashTable" should "still contain element after getting reloaded from disk" in {
    val ht = new HashTable(() => new Hyperplane(10))
    val testElem = ("0000001234", Vector(0.0, 5.2, 1.3, 3.6, 2.4, 9.2, 2.3, 3.9, 0.3, 1.2, 1.1, 9.9, 0.0, 0.3013, 0.0))
    ht+=testElem

    val dir = "src/test/tmp/HashTableSerializeTest.tmp"

    val fis = new FileOutputStream(dir)
    val oos = new ObjectOutputStream(fis)
    oos.writeObject(ht)
    oos.close

    // Reload
    val ois = new ObjectInputStream(new FileInputStream(dir)) {
      override def resolveClass(desc: java.io.ObjectStreamClass): Class[_] = {
        try {
          Class.forName(desc.getName, false, getClass.getClassLoader)
        }
        catch {
          case ex: ClassNotFoundException => super.resolveClass(desc)
        }
      }
    }
    val htl = ois.readObject.asInstanceOf[HashTable]
    ois.close()

    val returnedElem = htl.query(testElem._2)
    returnedElem.head.equals(testElem) should be (true)
  }
}
