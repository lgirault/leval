package object leval {

  type Error = String
  type Try[T] = Either[Error, T]

  def ignore[A](a : A) : Unit = {
    val _ = a
  }

}
