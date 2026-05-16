package com.ddaa.apigateway.bff;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/bff")
public class BffController {

    private final BffService bffService;

    public BffController(BffService bffService) {
        this.bffService = bffService;
    }

    @GetMapping("/session")
    public Mono<Map<String, Object>> session(ServerWebExchange exchange) {
        return bffService.getSession(exchange);
    }

    @GetMapping("/ddaa")
    public Mono<ResponseEntity<Object>> listDdaa(ServerWebExchange exchange) {
        return bffService.listDdaa(exchange);
    }

    @GetMapping("/ddaa/form-options")
    public Mono<ResponseEntity<Object>> getDdaaFormOptions(ServerWebExchange exchange) {
        return bffService.getDdaaFormOptions(exchange);
    }

    @GetMapping("/ddaa/{id}")
    public Mono<ResponseEntity<Object>> getDdaa(@PathVariable long id, ServerWebExchange exchange) {
        return bffService.getDdaa(id, exchange);
    }

    @PostMapping("/ddaa")
    public Mono<ResponseEntity<Object>> createDdaa(@RequestBody Mono<Map<String, Object>> payload,
                                                   ServerWebExchange exchange) {
        return bffService.createDdaa(payload, exchange);
    }

    @PutMapping("/ddaa/{id}")
    public Mono<ResponseEntity<Object>> updateDdaa(@PathVariable long id,
                                                   @RequestBody Mono<Map<String, Object>> payload,
                                                   ServerWebExchange exchange) {
        return bffService.updateDdaa(id, payload, exchange);
    }

    @DeleteMapping("/ddaa/{id}")
    public Mono<ResponseEntity<Object>> deleteDdaa(@PathVariable long id, ServerWebExchange exchange) {
        return bffService.deleteDdaa(id, exchange);
    }
}
