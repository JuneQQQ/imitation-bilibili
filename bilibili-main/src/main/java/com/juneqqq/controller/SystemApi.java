package com.juneqqq.controller;

import com.juneqqq.entity.dao.R;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
public class SystemApi {

//    @Resource
//    private ElasticSearchService elasticSearchService;

    @GetMapping("/contents")
    public R<List<Map<String, Object>>> getContents(@RequestParam String keyword,
                                                    @RequestParam Integer pageNo,
                                                    @RequestParam Integer pageSize) throws IOException {
//        List<Map<String, Object>> list = elasticSearchService.getContents(keyword,pageNo,pageSize);
        return null;
//        return new JsonResponse<>(list);
    }
}
