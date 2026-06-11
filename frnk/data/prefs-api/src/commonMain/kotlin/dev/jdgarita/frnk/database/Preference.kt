package dev.jdgarita.frnk.database

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * A typed, single-key view over a [KeyValueStore]. Reads fall back to [defaultValue] when the key
 * is absent (and, for encoded types, when the stored value can't be decoded). Created via the
 * extension factories below ([stringPreference], [booleanPreference], [intPreference],
 * [enumPreference]); not meant to be implemented by callers.
 *
 * Usable two ways from a single object:
 * ```
 * val darkMode = store.booleanPreference("ui.dark_mode", default = false)
 * darkMode.value = true            // imperative
 * var dark by darkMode             // delegated
 * ```
 *
 * The [KeyValueStore] contract is unchanged: every typed accessor rides on the existing
 * `getString`/`putString`/`getBoolean`/`putBoolean`/`remove` primitives.
 *
 * Type coverage is intentionally limited to String, Boolean, Int and Enum. `Long`/`Double` and a
 * nullable-string variant are deliberately deferred — there is no consumer today and the AC forbids
 * changing the underlying contract (they would need extra String encoding). [Preference] already
 * supports `T = String?`, so a nullable-string factory is a non-breaking future addition.
 */
sealed interface Preference<T> : ReadWriteProperty<Any?, T> {
    /** The underlying [KeyValueStore] key this preference reads from / writes to. */
    val key: String

    /** Value returned when the key is unset (or, for encoded types, undecodable). */
    val defaultValue: T

    /** Current stored value, or [defaultValue] if unset/undecodable. */
    var value: T

    /** Clears the key. Subsequent reads return [defaultValue]. */
    fun remove()

    // ReadWriteProperty bridge — lets `var x by pref` delegate to the same backing logic.
    override fun getValue(
        thisRef: Any?,
        property: KProperty<*>,
    ): T = value

    override fun setValue(
        thisRef: Any?,
        property: KProperty<*>,
        value: T,
    ) {
        this.value = value
    }
}

/** Backing implementation. Read/write are supplied by the factories so each type owns its encoding. */
internal class KeyValuePreference<T>(
    private val store: KeyValueStore,
    override val key: String,
    override val defaultValue: T,
    private val read: (KeyValueStore, String, T) -> T,
    private val write: (KeyValueStore, String, T) -> Unit,
) : Preference<T> {
    override var value: T
        get() = read(store, key, defaultValue)
        set(value) = write(store, key, value)

    override fun remove() = store.remove(key)
}

/** A typed [String] preference. Reads return [default] when the key is unset. */
fun KeyValueStore.stringPreference(
    key: String,
    default: String,
): Preference<String> =
    KeyValuePreference(
        store = this,
        key = key,
        defaultValue = default,
        read = { store, k, d -> store.getString(k, d) ?: d },
        write = { store, k, v -> store.putString(k, v) },
    )

/** A typed [Boolean] preference. Reads return [default] when the key is unset. */
fun KeyValueStore.booleanPreference(
    key: String,
    default: Boolean,
): Preference<Boolean> =
    KeyValuePreference(
        store = this,
        key = key,
        defaultValue = default,
        read = { store, k, d -> store.getBoolean(k, d) },
        write = { store, k, v -> store.putBoolean(k, v) },
    )

/**
 * A typed [Int] preference, encoded over the String primitive. Reads return [default] when the key
 * is unset **or** the stored value is not a valid integer (e.g. written by an older app version).
 */
fun KeyValueStore.intPreference(
    key: String,
    default: Int,
): Preference<Int> =
    KeyValuePreference(
        store = this,
        key = key,
        defaultValue = default,
        read = { store, k, d -> store.getString(k, null)?.toIntOrNull() ?: d },
        write = { store, k, v -> store.putString(k, v.toString()) },
    )

/**
 * A typed [Enum] preference, encoded as the constant's [Enum.name] over the String primitive. Reads
 * return [default] when the key is unset **or** the stored name no longer matches any constant
 * (e.g. the constant was renamed/removed between app versions) — it never throws.
 */
inline fun <reified E : Enum<E>> KeyValueStore.enumPreference(
    key: String,
    default: E,
): Preference<E> = enumPreferenceImpl(key, default, enumValues<E>())

@PublishedApi
internal fun <E : Enum<E>> KeyValueStore.enumPreferenceImpl(
    key: String,
    default: E,
    values: Array<E>,
): Preference<E> =
    KeyValuePreference(
        store = this,
        key = key,
        defaultValue = default,
        read = { store, k, d -> store.getString(k, null)?.let { name -> values.firstOrNull { it.name == name } } ?: d },
        write = { store, k, v -> store.putString(k, v.name) },
    )
