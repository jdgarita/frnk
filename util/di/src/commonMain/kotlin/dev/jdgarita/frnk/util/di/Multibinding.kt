package dev.jdgarita.frnk.util.di

import org.koin.core.definition.Definition
import org.koin.core.module.Module
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.Qualifier
import org.koin.core.scope.Scope

fun <T> Module.set(qualifier: Qualifier) = single(qualifier) {
    mutableSetOf<T>()
}

fun <T> Scope.getSet(qualifier: Qualifier) = get<MutableSet<T>>(qualifier)

inline fun <reified VarType : SetType, reified SetType> Module.intoSetSingle(
    setQualifier: Qualifier,
    valueQualifier: Qualifier? = null,
    crossinline definition: Definition<VarType>
) {
    single(valueQualifier, createdAtStart = true) {
        val value: VarType = definition(this, parametersOf())
        getSet<SetType>(setQualifier).add(value)
        value
    }
}