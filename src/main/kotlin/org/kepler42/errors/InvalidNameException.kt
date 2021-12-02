package org.kepler42.errors

data class InvalidNameException(
    override val message: String = "Invalid name"
): Exception(message)
