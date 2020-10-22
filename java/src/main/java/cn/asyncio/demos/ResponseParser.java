package cn.asyncio.demos;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpMessage;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.impl.entity.EntityDeserializer;
import org.apache.http.impl.entity.LaxContentLengthStrategy;
import org.apache.http.impl.io.AbstractSessionInputBuffer;
import org.apache.http.impl.io.HttpResponseParser;
import org.apache.http.io.HttpMessageParser;
import org.apache.http.io.SessionInputBuffer;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicLineParser;
import org.apache.http.params.BasicHttpParams;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class ResponseParser {
    public static HttpResponse parse(String responseText) {
        try {
            SessionInputBuffer inputBuffer = new AbstractSessionInputBuffer() {
                {
                    init(new ByteArrayInputStream(responseText.getBytes()), 10, new BasicHttpParams());
                }

                @Override
                public boolean isDataAvailable(int timeout) throws IOException {
                    return true;
                }
            };
            HttpMessageParser parser = new HttpResponseParser(inputBuffer, new BasicLineParser(new ProtocolVersion("HTTP", 1, 1)), new DefaultHttpResponseFactory(), new BasicHttpParams());
            HttpMessage message = parser.parse();
            if (message instanceof BasicHttpResponse) {
                BasicHttpResponse response = (BasicHttpResponse) message;
                EntityDeserializer entityDeserializer = new EntityDeserializer(new LaxContentLengthStrategy());
                HttpEntity entity = entityDeserializer.deserialize(inputBuffer, message);
                response.setEntity(entity);
            }
            return (HttpResponse) message;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (HttpException e) {
            throw new RuntimeException(e);
        }
    }
}
