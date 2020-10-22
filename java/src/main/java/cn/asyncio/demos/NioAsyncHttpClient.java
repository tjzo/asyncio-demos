package cn.asyncio.demos;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/*
    https://examples.javacodegeeks.com/core-java/nio/java-nio-async-http-client-example/
 */
public class NioAsyncHttpClient implements AutoCloseable {

    private AsynchronousChannelGroup httpChannelGroup;

    public NioAsyncHttpClient() throws Exception {
        this(2);
    }

    public NioAsyncHttpClient(int threadNum) throws Exception {
        this.httpChannelGroup = AsynchronousChannelGroup.withFixedThreadPool(threadNum, Executors.defaultThreadFactory());
    }

    public void get(String uri, Map<String, Object> headers, Consumer<? super ByteBuffer> success, Consumer<? super Exception> failure)
            throws URISyntaxException, IOException {
        process(new URI(uri), Optional.<ByteBuffer>empty(), "GET", headers, success, failure);
    }

    public void post(String uri, String data, Map<String, Object> headers, Consumer<? super ByteBuffer> success, Consumer<? super Exception> failure)
            throws URISyntaxException, IOException {
        process(new URI(uri), Optional.of(ByteBuffer.wrap(data.getBytes())), "POST", headers, success, failure);
    }

    @Override
    public void close() throws Exception {
        this.httpChannelGroup.shutdown();
    }

    private void process(URI uri, Optional<ByteBuffer> data, String method, Map<String, Object> headers, Consumer<? super ByteBuffer> success,
                         Consumer<? super Exception> failure) throws IOException {
        SocketAddress serverAddress = new InetSocketAddress(uri.getHost(), uri.getPort() == -1 ? 80 : uri.getPort());
        RequestHandler handler = new RequestHandler(AsynchronousSocketChannel.open(this.httpChannelGroup), success, failure);

        doConnect(handler, serverAddress, ByteBuffer.wrap(createRequestHeaders(method, headers, uri).getBytes()), data);
    }

    private String createRequestHeaders(String method, Map<String, Object> headers, URI uri) {
        StringBuilder builder = new StringBuilder();
        builder.append(method + " " + uri.getPath() + " HTTP/1.1\r\n");
        headers.put("Host", uri.getHost());
        for (Map.Entry<String, Object> entry : headers.entrySet()) {
            builder.append(entry.getKey() + ": " + entry.getValue() + "\r\n");
        }
        builder.append("\r\n");
        return builder.toString();
    }

    private void doConnect(RequestHandler handler, SocketAddress address, ByteBuffer headers, Optional<ByteBuffer> body) {

        handler.getChannel().connect(address, null, new CompletionHandler<Void, Void>() {

            @Override
            public void completed(Void result, Void attachment) {
                handler.headers(headers, body);
            }

            @Override
            public void failed(Throwable exc, Void attachment) {
                handler.getFailure().accept(new Exception(exc));
            }
        });
    }
}