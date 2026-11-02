package com.drdisagree.iconify.xposed.core.reflect

import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap
import java.util.regex.Pattern

object MemberResolver {

    private val fieldCache = ConcurrentHashMap<String, Field>()
    private val methodCache = ConcurrentHashMap<String, Method>()
    private val missing = ConcurrentHashMap.newKeySet<String>()

    fun findMethods(
        clazz: Class<*>,
        nameOrPattern: String,
        isPattern: Boolean = false,
        parameterTypes: Array<out Any?>? = null
    ): List<Method> {
        val allMethods = (clazz.declaredMethods.asSequence() + clazz.methods.asSequence())
            .distinctBy { method -> method.name + method.parameterTypes.joinToString(",") { it.name } }

        val matched = if (isPattern) {
            val pattern = Pattern.compile(nameOrPattern)
            allMethods.filter { pattern.matcher(it.name).matches() }
        } else {
            allMethods.filter { it.name == nameOrPattern }
        }

        return matched
            .filter { parameterTypes.isNullOrEmpty() || it.parameterTypes.matches(parameterTypes) }
            .onEach { it.isAccessible = true }
            .toList()
    }

    fun findConstructors(
        clazz: Class<*>,
        parameterTypes: Array<out Any?>? = null
    ): List<Constructor<*>> = clazz.declaredConstructors
        .filter { parameterTypes.isNullOrEmpty() || it.parameterTypes.matches(parameterTypes) }
        .onEach { it.isAccessible = true }

    fun findField(clazz: Class<*>, fieldName: String): Field {
        val key = "${clazz.name}@${System.identityHashCode(clazz)}#$fieldName"
        fieldCache[key]?.let { return it }
        if (key in missing) throw NoSuchFieldError(key)

        var current: Class<*>? = clazz
        while (current != null) {
            try {
                return current.getDeclaredField(fieldName).also {
                    it.isAccessible = true
                    fieldCache[key] = it
                }
            } catch (_: NoSuchFieldException) {
                current = current.superclass
            }
        }

        missing.add(key)
        throw NoSuchFieldError(key)
    }

    fun findFieldIfExists(clazz: Class<*>, fieldName: String): Field? = try {
        findField(clazz, fieldName)
    } catch (_: NoSuchFieldError) {
        null
    }

    fun findMethodBestMatch(clazz: Class<*>, methodName: String, args: Array<out Any?>): Method {
        val argTypes = args.map { it?.javaClass }
        val key = "${clazz.name}@${System.identityHashCode(clazz)}#$methodName(${argTypes.joinToString(",") { it?.name ?: "null" }})"
        methodCache[key]?.let { return it }
        if (key in missing) throw NoSuchMethodError(key)

        var best: Method? = null
        var bestScore = Int.MAX_VALUE

        candidateMethods(clazz, methodName).forEach { method ->
            val types = method.parameterTypes
            if (types.size != argTypes.size) return@forEach

            var score = 0
            for (i in types.indices) {
                val distance = assignDistance(types[i], argTypes[i])
                if (distance < 0) return@forEach
                score += distance
            }

            if (score < bestScore) {
                best = method
                bestScore = score
            }
        }

        val method = best ?: run {
            missing.add(key)
            throw NoSuchMethodError(key)
        }

        method.isAccessible = true
        methodCache[key] = method
        return method
    }

    fun findConstructorBestMatch(clazz: Class<*>, args: Array<out Any?>): Constructor<*> {
        val argTypes = args.map { it?.javaClass }
        var best: Constructor<*>? = null
        var bestScore = Int.MAX_VALUE

        clazz.declaredConstructors.forEach { constructor ->
            val types = constructor.parameterTypes
            if (types.size != argTypes.size) return@forEach

            var score = 0
            for (i in types.indices) {
                val distance = assignDistance(types[i], argTypes[i])
                if (distance < 0) return@forEach
                score += distance
            }

            if (score < bestScore) {
                best = constructor
                bestScore = score
            }
        }

        return (best ?: throw NoSuchMethodError(
            "${clazz.name}<init>(${argTypes.joinToString(",") { it?.name ?: "null" }})"
        )).also { it.isAccessible = true }
    }

    private fun candidateMethods(clazz: Class<*>, methodName: String): Sequence<Method> =
        sequence {
            var current: Class<*>? = clazz
            while (current != null) {
                current.declaredMethods.forEach { if (it.name == methodName) yield(it) }
                current = current.superclass
            }
            clazz.methods.forEach { if (it.name == methodName) yield(it) }
        }

    private fun assignDistance(target: Class<*>, source: Class<*>?): Int {
        if (source == null) return if (target.isPrimitive) -1 else 1
        if (target == source) return 0

        val boxedTarget = target.boxed()
        val boxedSource = source.boxed()
        if (boxedTarget == boxedSource) return 1
        if (target.isPrimitive && PRIMITIVE_WIDENING[boxedSource]?.contains(boxedTarget) == true) {
            return 2
        }
        if (!boxedTarget.isAssignableFrom(boxedSource)) return -1

        var depth = 2
        var current: Class<*>? = boxedSource
        while (current != null && current != boxedTarget) {
            depth++
            current = current.superclass
        }
        return if (current == null) depth + 8 else depth
    }

    private fun Array<Class<*>>.matches(expected: Array<out Any?>): Boolean {
        return size == expected.size && indices.all { i ->
            when (val type = expected[i]) {
                null -> true
                is Class<*> -> this[i] == type
                is String -> this[i].name == type
                else -> false
            }
        }
    }

    private fun Class<*>.boxed(): Class<*> = if (isPrimitive) PRIMITIVE_TO_BOXED[this] ?: this else this

    private val PRIMITIVE_TO_BOXED: Map<Class<*>, Class<*>> = mapOf(
        Boolean::class.javaPrimitiveType!! to Boolean::class.javaObjectType,
        Byte::class.javaPrimitiveType!! to Byte::class.javaObjectType,
        Char::class.javaPrimitiveType!! to Char::class.javaObjectType,
        Short::class.javaPrimitiveType!! to Short::class.javaObjectType,
        Int::class.javaPrimitiveType!! to Int::class.javaObjectType,
        Long::class.javaPrimitiveType!! to Long::class.javaObjectType,
        Float::class.javaPrimitiveType!! to Float::class.javaObjectType,
        Double::class.javaPrimitiveType!! to Double::class.javaObjectType
    )

    private val PRIMITIVE_WIDENING: Map<Class<*>, Set<Class<*>>> = mapOf(
        Byte::class.javaObjectType to setOf(
            Short::class.javaObjectType, Int::class.javaObjectType, Long::class.javaObjectType,
            Float::class.javaObjectType, Double::class.javaObjectType
        ),
        Short::class.javaObjectType to setOf(
            Int::class.javaObjectType, Long::class.javaObjectType,
            Float::class.javaObjectType, Double::class.javaObjectType
        ),
        Char::class.javaObjectType to setOf(
            Int::class.javaObjectType, Long::class.javaObjectType,
            Float::class.javaObjectType, Double::class.javaObjectType
        ),
        Int::class.javaObjectType to setOf(
            Long::class.javaObjectType, Float::class.javaObjectType, Double::class.javaObjectType
        ),
        Long::class.javaObjectType to setOf(Float::class.javaObjectType, Double::class.javaObjectType),
        Float::class.javaObjectType to setOf(Double::class.javaObjectType)
    )
}
