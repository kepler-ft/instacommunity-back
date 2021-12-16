package org.kepler42.errors

class ResourceNotFoundException(message: String = "This resource was not found"): Exception(message)
