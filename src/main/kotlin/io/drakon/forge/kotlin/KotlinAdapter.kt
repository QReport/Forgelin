package io.drakon.forge.kotlin

import cpw.mods.fml.common.FMLModContainer
import cpw.mods.fml.common.ILanguageAdapter
import cpw.mods.fml.common.ModContainer
import org.apache.logging.log4j.LogManager

import java.lang.reflect.Field
import java.lang.reflect.Method

/**
 * Kotlin implementation of FML's ILanguageAdapter.
 *
 * Use by setting <pre>modLanguageAdapter = "io.drakon.forge.kotlin.KotlinAdapter"</pre> in the Mod annotation.
 * Your Kotlin @Mod implementation <b>must</b> be an <pre>object</pre> type.
 *
 * @author Arkan <arkan@drakon.io>
 * @author Carrot <git@bunnies.io>
 */
@Suppress("UNUSED")
class KotlinAdapter : ILanguageAdapter {
    override fun supportsStatics(): Boolean = false

    override fun setInternalProxies(p0: ModContainer?, p1: cpw.mods.fml.relauncher.Side?, p2: ClassLoader?) = Unit

    private val logger = LogManager.getLogger("ILanguageAdapter/Kotlin")

    override fun setProxy(target: Field, proxyTarget: Class<*>, proxy: Any) {
        logger.debug("Setting proxy on target: {}.{} -> {}", target.declaringClass.simpleName, target.name, proxy)

        val instanceField = findInstanceFieldOrThrow(proxyTarget)
        val modObject = findModObjectOrThrow(instanceField)

        target.set(modObject, proxy)
    }

    override fun getNewInstance(container: FMLModContainer?, objectClass: Class<*>, classLoader: ClassLoader, factoryMarkedAnnotation: Method?): Any? {
        logger.debug("Constructing new instance of {}", objectClass.simpleName)

        val instanceField = findInstanceFieldOrThrow(objectClass)
        val modObject = findModObjectOrThrow(instanceField)

        return modObject
    }

    private fun findInstanceFieldOrThrow(targetClass: Class<*>): Field {
        val instanceField: Field = try {
            targetClass.getField("INSTANCE")
        } catch (exception: NoSuchFieldException) {
            throw noInstanceFieldException(exception)
        } catch (exception: SecurityException) {
            throw instanceSecurityException(exception)
        }

        return instanceField
    }

    private fun findModObjectOrThrow(instanceField: Field): Any {
        val modObject = try {
            instanceField.get(null)
        } catch (exception: IllegalArgumentException) {
            throw unexpectedInitializerSignatureException(exception)
        } catch (exception: IllegalAccessException) {
            throw wrongVisibilityOnInitializerException(exception)
        }

        return modObject
    }

    private fun noInstanceFieldException(exception: Exception) = KotlinAdapterException("Couldn't find INSTANCE singleton on Kotlin @Mod container", exception)
    private fun instanceSecurityException(exception: Exception) = KotlinAdapterException("Security violation accessing INSTANCE singleton on Kotlin @Mod container", exception)
    private fun unexpectedInitializerSignatureException(exception: Exception) = KotlinAdapterException("Kotlin @Mod object has an unexpected initializer signature, somehow?", exception)
    private fun wrongVisibilityOnInitializerException(exception: Exception) = KotlinAdapterException("Initializer on Kotlin @Mod object isn't `public`", exception)

    private class KotlinAdapterException(message: String, exception: Exception): RuntimeException("Kotlin adapter error - do not report to Forge! " + message, exception)
}