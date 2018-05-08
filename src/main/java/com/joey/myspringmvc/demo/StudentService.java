package com.joey.myspringmvc.demo;

import com.joey.myspringmvc.annotation.MyService;

/**
 * Created by xiaowu.zhou@tongdun.cn on 2018/5/8.
 */
@MyService
public class StudentService {

    public String getDetail(Integer id){
        return "id : " + id +" name : xiaoming , age :18";
    }

}
