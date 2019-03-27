/*
 * Javalin - https://javalin.io
 * Copyright 2017 David Åse
 * Licensed under Apache 2.0: https://github.com/tipsy/javalin/blob/master/LICENSE
 */

package io.javalin.websocket

import io.javalin.core.util.ContextUtil
import io.javalin.json.JavalinJson
import org.eclipse.jetty.websocket.api.Session
import java.nio.ByteBuffer

interface WsContext {
    fun session(): Session
    fun send(message: String) = session().remote.sendString(message)
    fun send(message: ByteBuffer) = session().remote.sendBytes(message)
    fun queryString(): String? = session().upgradeRequest!!.queryString
    fun queryParam(queryParam: String): String? = queryParam(queryParam, default = null)
    fun queryParam(queryParam: String, default: String? = null): String? = queryParams(queryParam).firstOrNull()
            ?: default

    fun queryParams(queryParam: String): List<String> = queryParamMap()[queryParam] ?: emptyList()
    fun queryParamMap(): Map<String, List<String>> = ContextUtil.splitKeyValueStringAndGroupByKey(queryString() ?: "")
    fun mapQueryParams(vararg keys: String): List<String>? = ContextUtil.mapKeysOrReturnNullIfAnyNulls(keys) { queryParam(it) }
    fun anyQueryParamNull(vararg keys: String): Boolean = keys.any { queryParam(it) == null }
    fun pathParam(pathParam: String): String = ContextUtil.pathParamOrThrow(pathParamMap(), pathParam, matchedPath())
    fun pathParamMap(): Map<String, String>
    fun host(): String? = session().upgradeRequest.host
    fun header(header: String): String? = session().upgradeRequest.getHeader(header)
    fun headerMap(): Map<String, String> = session().upgradeRequest.headers.keys.map { it to session().upgradeRequest.getHeader(it) }.toMap()
    fun matchedPath(): String

}

class WsContextBase(val sessionId: String, private val session: Session, private val pathParamMap: Map<String, String>, private val matchedPath: String) : WsContext {
    override fun session() = session
    override fun pathParamMap(): Map<String, String> = pathParamMap
    override fun matchedPath() = matchedPath

    override fun equals(other: Any?) = session() == (other as WsContext).session()
    override fun hashCode() = session().hashCode()
}

class OnConnect(ctx: WsContext) : WsContext by ctx
class OnMessage(ctx: WsContext, private val message: String?) : WsContext by ctx {
    fun message(): String? = message
    inline fun <reified T : Any> message(): T? = if (message() == null) null else JavalinJson.fromJson(message()!!, T::class.java)
}
class OnBinaryMessage(ctx: WsContext, val buffer: Array<Byte>, val offset: Int, val length: Int) : WsContext by ctx
class OnException(ctx: WsContext, val error: Throwable?) : WsContext by ctx
class OnClose(ctx: WsContext, val statusCode: Int, val reason: String?) : WsContext by ctx
