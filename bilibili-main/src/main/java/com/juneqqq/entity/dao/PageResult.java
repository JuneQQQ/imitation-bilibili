package com.juneqqq.entity.dao;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@AllArgsConstructor
@Data
public class PageResult<T> {

    private Long total;

    private List<T> list;

}
