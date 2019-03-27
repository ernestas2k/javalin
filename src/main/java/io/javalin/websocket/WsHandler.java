/*
 * Javalin - https://javalin.io
 * Copyright 2017 David Åse
 * Licensed under Apache 2.0: https://github.com/tipsy/javalin/blob/master/LICENSE
 */

package io.javalin.websocket;

import org.jetbrains.annotations.NotNull;

public class WsHandler {

    Handler<WsContextOnConnect> connectHandler = null;
    Handler<WsContextOnMessage> messageHandler = null;
    Handler<WsContextOnBinaryMessage> binaryMessageHandler = null;
    Handler<WsContextOnClose> closeHandler = null;
    Handler<WsContextOnException> errorHandler = null;

    /**
     * Add a ConnectHandler to the WsHandler.
     * The handler is called when a WebSocket client connects.
     */
    public void onConnect(@NotNull Handler<WsContextOnConnect> connectHandler) {
        this.connectHandler = connectHandler;
    }

    /**
     * Add a MessageHandler to the WsHandler.
     * The handler is called when a WebSocket client sends
     * a String message.
     */
    public void onMessage(@NotNull Handler<WsContextOnMessage> messageHandler) {
        this.messageHandler = messageHandler;
    }

    /**
     * Add a {@link Handler<WsContextOnBinaryMessage>} to the WsHandler.
     * The handler is called when a WebSocket client sends
     * a binary message.
     */
    public void onBinaryMessage(@NotNull Handler<WsContextOnBinaryMessage> binaryMessageHandler) {
        this.binaryMessageHandler = binaryMessageHandler;
    }

    /**
     * Add a CloseHandler to the WsHandler.
     * The handler is called when a WebSocket client closes
     * the connection. The handler is not called in case of
     * network issues, only when the client actively closes the
     * connection (or times out).
     */
    public void onClose(@NotNull Handler<WsContextOnClose> closeHandler) {
        this.closeHandler = closeHandler;
    }

    /**
     * Add a errorHandler to the WsHandler.
     * The handler is called when an error is detected.
     */
    public void onError(@NotNull Handler<WsContextOnException> errorHandler) {
        this.errorHandler = errorHandler;
    }

}
