package org.kepler42.errors

import kotlinx.serialization.Serializable

@Serializable
open class Error(open val code: Int, val error: String)
