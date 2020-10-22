package cn.asyncio.demos;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import org.apache.http.HttpResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static com.google.common.base.Strings.isNullOrEmpty;

@Singleton
public class ManagerServlet extends HttpServlet {

    private static final Logger LOG = LogManager.getLogger(ManagerServlet.class);
    private final String remoteAddr;

    public ManagerServlet() {
        String remoteAddr = System.getenv("REMOTE_ADDR");
        if (isNullOrEmpty(remoteAddr)) {
            this.remoteAddr = "http://localhost:5003/worker";
        } else {
            this.remoteAddr = remoteAddr;
        }
    }

    static {
        System.getenv().entrySet().forEach(k -> System.out.println(k.getKey() + ": " + k.getValue()));
    }

    @Inject
    NioAsyncHttpClient client;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String n = request.getParameter("n");
        int times = 100;
        if (!isNullOrEmpty(n)) {
            try {
                times = Integer.parseInt(n);
            } catch (Exception e) {
                LOG.error(e);
            }
        }

        List<Integer> result = Collections.synchronizedList(Lists.newArrayList());
        try {

            final CountDownLatch latch = new CountDownLatch(times);
            for (int i = 0; i < times; i++) {
                client.get(remoteAddr, Maps.newHashMap(), buffer -> {
                    try {
                        buffer.flip();

                        StringBuilder builder = new StringBuilder();
                        while (buffer.hasRemaining()) {
                            builder.append((char) buffer.get());
                        }
                        HttpResponse resp = ResponseParser.parse(builder.toString());
                        result.add(resp.getStatusLine().getStatusCode());
                    } catch (Exception e) {
                        LOG.error(e);
                    } finally {
                        latch.countDown();
                    }
                }, e -> {
                    LOG.error(e);
                    latch.countDown();
                });
            }
            latch.await();
        } catch (Exception e) {
            LOG.error(e);
        }

        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        out.println(new Gson().toJson(result));
    }
}