package com.tiny.okhttp.internal.connection;

import com.tiny.okhttp.*;
import com.tiny.okhttp.internal.http.ExchangeCodec;
import com.tiny.okhttp.internal.http1.Http1ExchangeCodec;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;

import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.net.*;
import java.util.List;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public final class RealConnection implements Connection {

    public final RealConnectionPool connectionPool;
    private final Route route;



    /** The low-level TCP socket. */
    private Socket rawSocket;

    /**
     * The application layer socket. Either an {@link SSLSocket} layered over {@link #rawSocket}, or
     * {@link #rawSocket} itself if this connection does not use SSL.
     */
    private Socket socket;
//    private Handshake handshake;
    private Protocol protocol;
    private BufferedSource source;
    private BufferedSink sink;

    public RealConnection(RealConnectionPool connectionPool, Route route) {
        this.connectionPool = connectionPool;
        this.route = route;
    }

    ExchangeCodec newCodec(OkHttpClient client, Interceptor.Chain chain) throws SocketException {
        /*socket.setSoTimeout(chain.readTimeoutMillis());
        source.timeout().timeout(chain.readTimeoutMillis(), MILLISECONDS);
        sink.timeout().timeout(chain.writeTimeoutMillis(), MILLISECONDS);
        return new Http1ExchangeCodec(client, this, source, sink);*/
        return new Http1ExchangeCodec();
    }

    public void connect(int connectTimeout, int readTimeout, int writeTimeout,
                        int pingIntervalMillis, boolean connectionRetryEnabled, Call call) throws IOException {
        if (protocol != null) throw new IllegalStateException("already connected");
        while (true) {
            try {
                connectSocket(connectTimeout, readTimeout, call);
                establishProtocol();
                break;
            } catch (IOException e) {
                throw e;
            }
        }
    }

    private void connectSocket(int connectTimeout, int readTimeout, Call call) throws IOException {
        Proxy proxy = route.proxy();
        Address address = route.address();

        rawSocket = proxy.type() == Proxy.Type.DIRECT || proxy.type() == Proxy.Type.HTTP
                ? address.socketFactory().createSocket()
                : new Socket(proxy);

        rawSocket.setSoTimeout(readTimeout);
        try {
            connectSocket(rawSocket, route.socketAddress(), connectTimeout);
        } catch (ConnectException e) {
            ConnectException ce = new ConnectException("Failed to connect to " + route.socketAddress());
            ce.initCause(e);
            throw ce;
        }

        /*try {
            source = Okio.buffer(Okio.source(rawSocket));
            sink = Okio.buffer(Okio.sink(rawSocket));
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        }*/
    }

    public void connectSocket(Socket socket, InetSocketAddress address, int connectTimeout)
            throws IOException {
        socket.connect(address, connectTimeout);
    }

    private void establishProtocol() {
        socket = rawSocket;
        protocol = Protocol.HTTP_1_1;
    }
}
