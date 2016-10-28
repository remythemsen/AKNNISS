package LSH.hashFunctions

import java.io.{FileInputStream, FileOutputStream, ObjectInputStream, ObjectOutputStream}

import org.scalatest.{FlatSpec, Matchers}

/**
  * Created by remeeh on 28-10-2016.
  */
class HyperplaneTest extends FlatSpec with Matchers {
  "Hyperplane Function" should "produce the same value on same input" in {
    val hp = new Hyperplane(30)
    val testVector = Vector(0.0, 5.2, 1.3, 3.6, 9.5, 10.0, 11.2, 9.1, 2.3)
    hp.apply(testVector).equals(hp.apply(testVector)) should be (true)
  }


  "Hyperplane Function" should "produce the same output even after being reloaded from disk" in {
    val hp = new Hyperplane(30)
    val testVector = Vector(0.0, 5.2, 1.3, 3.6, 9.5, 10.0, 11.2, 9.1, 2.3)
    val resBefore = hp(testVector)
    val dir = "src/test/tmp/HyperplaneSerializeTest.tmp"

    val fis = new FileOutputStream(dir)
    val oos = new ObjectOutputStream(fis)
    oos.writeObject(hp)
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
    val hpl = ois.readObject.asInstanceOf[Hyperplane]
    ois.close()

    hp(testVector).equals(hpl(testVector)) should be(true)
  }
}
