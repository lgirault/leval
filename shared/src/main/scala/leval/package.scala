package object leval {

  type Error = String
  type Try[T] = Either[Error, T]

  def ignore[A](a : A) : Unit = {
    val _ = a
  }
  def error(msg : String = "Should not happen") : Nothing = sys.error(msg)



}
