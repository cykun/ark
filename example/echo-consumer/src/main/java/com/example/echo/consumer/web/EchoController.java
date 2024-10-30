package com.example.echo.consumer.web;

import com.example.echo.api.EchoService;
import ink.xikun.ark.consumer.annotation.RpcReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class EchoController {

    @RpcReference(registryAddress = "zookeeper://127.0.0.1:2181")
    private EchoService echoService;

    @GetMapping("echo")
    public String echo(@RequestParam("message") String message) {
        return echoService.echo(message);
    }
}
