package cn.asyncio.demos;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Optional;
import java.util.function.Consumer;

public class RequestHandler {

    private AsynchronousSocketChannel channel;
    private Consumer<? super ByteBuffer> success;
    private Consumer<? super Exception> failure;

    RequestHandler(AsynchronousSocketChannel channel, Consumer<? super ByteBuffer> success, Consumer<? super Exception> failure) {
        this.channel = channel;
        this.success = success;
        this.failure = failure;
    }

    AsynchronousSocketChannel getChannel() {
        return this.channel;
    }

    Consumer<? super ByteBuffer> getSuccess() {
        return this.success;
    }

    Consumer<? super Exception> getFailure() {
        return this.failure;
    }

    void closeChannel() {
        try {
            this.channel.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    void headers(ByteBuffer headers, Optional<ByteBuffer> body) {
        this.channel.write(headers, this, new CompletionHandler<Integer, RequestHandler>() {

            @Override
            public void completed(Integer result, RequestHandler handler) {
                if (headers.hasRemaining()) {
                    RequestHandler.this.channel.write(headers, handler, this);
                } else if (body.isPresent()) {
                    RequestHandler.this.body(body.get(), handler);
                } else {
                    RequestHandler.this.response();
                }
            }

            @Override
            public void failed(Throwable exc, RequestHandler handler) {
                handler.getFailure().accept(new Exception(exc));
                RequestHandler.this.closeChannel();
            }
        });
    }

    void body(ByteBuffer body, RequestHandler handler) {
        this.channel.write(body, handler, new CompletionHandler<Integer, RequestHandler>() {

            @Override
            public void completed(Integer result, RequestHandler handler) {
                if (body.hasRemaining()) {
                    RequestHandler.this.channel.write(body, handler, this);
                } else {
                    RequestHandler.this.response();
                }
            }

            @Override
            public void failed(Throwable exc, RequestHandler handler) {
                handler.getFailure().accept(new Exception(exc));
                RequestHandler.this.closeChannel();
            }
        });
    }

    void response() {
        ByteBuffer buffer = ByteBuffer.allocate(2048);
        this.channel.read(buffer, this, new CompletionHandler<Integer, RequestHandler>() {

            @Override
            public void completed(Integer result, RequestHandler handler) {
                if (result > 0) {
                    handler.getSuccess().accept(buffer);
                    buffer.clear();

                    RequestHandler.this.channel.read(buffer, handler, this);
                } else if (result < 0) {
                    RequestHandler.this.closeChannel();
                } else {
                    RequestHandler.this.channel.read(buffer, handler, this);
                }
            }

            @Override
            public void failed(Throwable exc, RequestHandler handler) {
                handler.getFailure().accept(new Exception(exc));
                RequestHandler.this.closeChannel();
            }
        });
    }
}