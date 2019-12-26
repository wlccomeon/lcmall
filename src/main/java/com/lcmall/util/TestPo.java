package com.lcmall.util;

import com.google.common.collect.Lists;
import lombok.Data;

import java.util.List;

@Data
public class TestPo {
    private String name;
    private Integer id;
    List<TestPo> list = Lists.newArrayList();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}
