package com.juneqqq.feign;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

//@FeignClient("bilibili-api")
public interface MsDeclareService {
    @GetMapping("/demos")
    Long msget(@RequestParam Long id);

    @PostMapping("/demos")
    Map<String, Object> mspost(@RequestBody Map<String, Object> params);

    @GetMapping("/timeout")
    String timeout(@RequestParam Long time);

    @GetMapping("/timeout1")
    String timeout1(@RequestParam Long time);
}
