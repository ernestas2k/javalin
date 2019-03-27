/*
 * Javalin - https://javalin.io
 * Copyright 2017 David Åse
 * Licensed under Apache 2.0: https://github.com/tipsy/javalin/blob/master/LICENSE
 */

package io.javalin.websocket

import io.javalin.core.PathParser
import org.eclipse.jetty.websocket.api.Session
import org.eclipse.jetty.websocket.api.UpgradeRequest
import org.eclipse.jetty.websocket.api.annotations.*
import java.util.*
import java.util.concurrent.ConcurrentHashMap

data class WsEntry(val path: String, val handler: WsHandler, val caseSensitiveUrls: Boolean) {
    private val pathParser = PathParser(path, caseSensitiveUrls)
    fun matches(requestUri: String) = pathParser.matches(requestUri)
    fun extractPathParams(requestUri: String) = pathParser.extractPathParams(requestUri)
}

/**
 * Every WebSocket request passes through a single instance of this class.
 * Session IDs are generated and tracked here, and path-parameters are cached for performance.
 */
@WebSocket
class WsPathMatcher {

    val wsEntries = mutableListOf<WsEntry>()
    var wsLogger: WsHandler? = null
    private val sessionIds = ConcurrentHashMap<Session, String>()
    private val sessionPathParams = ConcurrentHashMap<Session, Map<String, String>>()

    fun add(wsEntry: WsEntry) {
        if (!wsEntry.caseSensitiveUrls && wsEntry.path != wsEntry.path.toLowerCase()) {
            throw IllegalArgumentException("By default URLs must be lowercase. Change casing or call `app.enableCaseSensitiveUrls()` to allow mixed casing.")
        }
        wsEntries.add(wsEntry)
    }

    @OnWebSocketConnect
    fun webSocketConnect(session: Session) {
        findEntry(session)?.let {
            val ctx = OnConnect(wrap(session, it))
            it.handler.connectHandler?.handle(ctx)
            wsLogger?.connectHandler?.handle(ctx)
        }

    }

    @OnWebSocketMessage
    fun webSocketMessage(session: Session, message: String) {
        findEntry(session)?.let {
            val ctx = OnMessage(wrap(session, it), message)
            it.handler.messageHandler?.handle(ctx)
            wsLogger?.messageHandler?.handle(ctx)
        }
    }

    @OnWebSocketMessage
    fun webSocketBinaryMessage(session: Session, buffer: ByteArray, offset: Int, length: Int) {
        findEntry(session)?.let {
            val ctx = OnBinaryMessage(wrap(session, it), buffer.toTypedArray(), offset, length)
            it.handler.binaryMessageHandler?.handle(ctx)
            wsLogger?.binaryMessageHandler?.handle(ctx)
        }
    }

    @OnWebSocketError
    fun webSocketError(session: Session, throwable: Throwable?) {
        findEntry(session)?.let {
            val ctx = OnException(wrap(session, it), throwable)
            it.handler.errorHandler?.handle(ctx)
            wsLogger?.errorHandler?.handle(ctx)
        }
    }

    @OnWebSocketClose
    fun webSocketClose(session: Session, statusCode: Int, reason: String?) {
        findEntry(session)?.let {
            val ctx = OnClose(wrap(session, it), statusCode, reason)
            it.handler.closeHandler?.handle(ctx)
            wsLogger?.closeHandler?.handle(ctx)
        }
        destroy(session)
    }

    private fun findEntry(session: Session) = findEntry(session.upgradeRequest)

    fun findEntry(req: UpgradeRequest) = wsEntries.find { it.matches(req.requestURI.path) }

    private fun wrap(session: Session, wsEntry: WsEntry): WsContextBase {
        sessionIds.putIfAbsent(session, UUID.randomUUID().toString())
        sessionPathParams.putIfAbsent(session, wsEntry.extractPathParams(session.upgradeRequest.requestURI.path))
        return WsContextBase(sessionIds[session]!!, session, sessionPathParams[session]!!, wsEntry.path)
    }

    private fun destroy(session: Session) {
        sessionIds.remove(session)
        sessionPathParams.remove(session)
    }

}
