/*
 * Javalin - https://javalin.io
 * Copyright 2017 David Åse
 * Licensed under Apache 2.0: https://github.com/tipsy/javalin/blob/master/LICENSE
 */

package io.javalin.websocket;

import org.jetbrains.annotations.NotNull;

public class WsHandler {

    Handler<OnConnect> connectHandler = null;
    Handler<OnMessage> messageHandler = null;
    Handler<OnBinaryMessage> binaryMessageHandler = null;
    Handler<OnClose> closeHandler = null;
    Handler<OnException> errorHandler = null;

    public void onConnect(@NotNull Handler<OnConnect> connectHandler) {
        this.connectHandler = connectHandler;
    }

    public void onMessage(@NotNull Handler<OnMessage> messageHandler) {
        this.messageHandler = messageHandler;
    }

    public void onBinaryMessage(@NotNull Handler<OnBinaryMessage> binaryMessageHandler) {
        this.binaryMessageHandler = binaryMessageHandler;
    }

    public void onClose(@NotNull Handler<OnClose> closeHandler) {
        this.closeHandler = closeHandler;
    }

    public void onError(@NotNull Handler<OnException> errorHandler) {
        this.errorHandler = errorHandler;
    }

}
