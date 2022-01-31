package org.kepler42.database.repositories.utils

import org.jetbrains.exposed.sql.*

class ILikeOp(expr1: Expression<*>, expr2: Expression<*>) : ComparisonOp(expr1, expr2, "ILIKE")

infix fun <T : String?> ExpressionWithColumnType<T>.insensitiveLike(pattern: String): Op<Boolean> =
    ILikeOp(this, QueryParameter(pattern, columnType))
