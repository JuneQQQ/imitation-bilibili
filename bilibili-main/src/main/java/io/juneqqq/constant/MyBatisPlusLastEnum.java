package io.juneqqq.constant;

import lombok.Getter;

@Getter
    public enum MyBatisPlusLastEnum {

        LIMIT_1("limit 1"),
        LIMIT_2("limit 2"),
        LIMIT_5("limit 5"),
        LIMIT_30("limit 30"),
        LIMIT_500("limit 500");

        private String sql;

        MyBatisPlusLastEnum(String sql) {
            this.sql = sql;
        }

    }