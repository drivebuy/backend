package utils

import java.nio.charset.StandardCharsets
import java.security.MessageDigest

object Hashing {

  def hash(key: String, salt: String): String = {
    val keyBytes = (salt + key).getBytes("UTF-8")
    val sha      = MessageDigest.getInstance("SHA-256")
    new String(sha.digest(keyBytes), StandardCharsets.UTF_8)
  }
}