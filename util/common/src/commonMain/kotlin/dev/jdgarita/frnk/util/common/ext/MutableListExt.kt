package dev.jdgarita.frnk.util.common.ext

/**
 * Truncates a list to the specified size
 */
fun MutableList<*>.truncate(size: Int) {
    while (size < count()) {
        // remove any screens above
        removeAt(size)
    }
}