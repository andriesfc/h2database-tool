package h2databasetool.utils

import java.io.IOException
import java.net.URL

class ClassPathResourceNotFound(val resource: String) : IOException(resource)

inline fun <reified T> resource(resource: String): URL =
    T::class.java.getResource(resource) ?: throw ClassPathResourceNotFound(resource)

inline fun <reified T> resourceWithExtension(ext: String) =
    T::class.java.simpleName
        .let { name -> if (ext.isEmpty()) name else "$name.$ext" }
        .let { resource<T>(it) }

