package com.juneqqq.controller;

import com.juneqqq.service.common.DemoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@Slf4j
public class DemoApi {

    @Resource
    private DemoService demoService;

    @RequestMapping("demo")
    public String demo(){
        Long query = demoService.query(1L);

        return String.valueOf(query);
    }

//    @Resource
//    private ElasticSearchService elasticSearchService;

//    @Resource
//    private MsDeclareService msDeclareService;

//    @GetMapping("/query")
//    public Map<Integer,Long> query(Long id) {
//        log.debug("id is:"+id);
//        HashMap<Integer,Long> map = new HashMap<>();
//        map.put(11111,demoService.query(id));
//        return map;
//    }
//
//    @GetMapping("/es-videos")
//    public R<Video> getEsVideos(@RequestParam String keyword) {
////       Video video = elasticSearchService.getVideos(keyword);
//        Video video = new Video();
//        return new R<>(video);
//    }
//
//    @GetMapping("/demos")
//    public Long msget(@RequestParam Long id) {
//        return msDeclareService.msget(id);
//    }
//
//    @PostMapping("/demos")
//    public Map<String, Object> mspost(@RequestBody Map<String, Object> params) {
//        return msDeclareService.mspost(params);
//    }

//    @HystrixCommand(fallbackMethod = "error",
//            commandProperties = {
//                    @HystrixProperty(
//                            name = "execution.isolation.thread.timeoutInMilliseconds",
//                            value = "2000"
//                    )
//            }
//    )
//    @GetMapping("/timeout")
//    public String circuitBreakerWithHystrix(@RequestParam Long time) {
//        return msDeclareService.timeout(time);
//    }

    public String error(Long time) {
        return "超时出错！";
    }

}
