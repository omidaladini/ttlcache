package ttlcache

class TTLCache[Key, Value] {
  import scala.collection.mutable.LinkedList
  import scala.collection.mutable.WeakHashMap

  val cache = WeakHashMap.empty[Key, Value]

  @volatile
  var ttlTrain = LinkedList.empty[(Long, Key)]

  def insert(k: Key, v: Value, absTtl: Long) = {
    ttlTrain = ttlTrain :+ ((absTtl, k))
    cache += ((k, v))
  }

  def tick(currentEpoch: Long) : Unit
  = ttlTrain = ttlTrain.span(_._1 > currentEpoch)._1
}


object Main extends App {

  import scala.util.Random.{nextInt => rndInt}

  val c = new TTLCache[Int, Int]

  while(true) {
    //Never run out of memory!
    println("Cache size:" + c.cache.size + " referenced:" + c.ttlTrain.size)
    val currentEpoch = System.currentTimeMillis

    //Add ramdom stuff
    val ttl = currentEpoch + rndInt(2000)
    c.insert(rndInt, rndInt, ttl)

    //Move the reference forward and let GC free up the cache
    c.tick(currentEpoch)
  }
}
