package io.juneqqq.core.handler.xxljob;

import com.xxl.job.core.handler.annotation.XxlJob;
import org.springframework.stereotype.Component;

@Component
public class TestHandler {
    @XxlJob("testHandler")
    public void test(){
        System.out.println("nihao");
    }
}
