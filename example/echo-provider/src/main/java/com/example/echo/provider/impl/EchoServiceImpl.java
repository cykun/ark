package com.example.echo.provider.impl;

import com.example.echo.api.EchoService;
import ink.xikun.ark.povider.annotation.RpcService;

@RpcService(serviceInterface = EchoService.class)
public class EchoServiceImpl implements EchoService {

    @Override
    public String echo(String message) {
        return message;
    }
}
