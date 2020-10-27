package cn.asyncio.demos;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.stream.LongStream;

import static com.google.common.base.Strings.isNullOrEmpty;

@RestController
@RequestMapping(value = "/")
public class ManagerCtrl {

    private final String remoteAddr;

    public ManagerCtrl() {
        String remoteAddr = System.getenv("REMOTE_ADDR");
        if (isNullOrEmpty(remoteAddr)) {
            this.remoteAddr = "http://localhost:8000/worker";
        } else {
            this.remoteAddr = remoteAddr;
        }
    }

    @GetMapping("/manager")
    public Flux<String> get() throws IOException {
        WebClient client = WebClient.create(remoteAddr);
        return Flux.fromStream(LongStream.range(0, 100).boxed())
                .flatMap(i -> client.get().retrieve().bodyToMono(String.class));
    }
}
