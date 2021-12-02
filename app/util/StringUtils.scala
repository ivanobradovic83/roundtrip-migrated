package util

object StringUtils {

  def isEmpty(value: String): Boolean = value == null || value.isEmpty

  def notEmpty(value: String): Boolean = value != null && value.nonEmpty

  def isAllDigits(value: String): Boolean = value forall Character.isDigit

}
