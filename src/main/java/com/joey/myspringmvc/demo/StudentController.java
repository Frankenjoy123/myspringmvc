package com.joey.myspringmvc.demo;

import com.joey.myspringmvc.annotation.MyAutowired;
import com.joey.myspringmvc.annotation.MyController;
import com.joey.myspringmvc.annotation.MyRequestMapping;
import com.joey.myspringmvc.annotation.MyRequestParam;

/**
 * Created by xiaowu.zhou@tongdun.cn on 2018/5/8.
 */
@MyController
@MyRequestMapping("/student")
public class StudentController {


    @MyAutowired
    private StudentService studentSerice;

    @MyRequestMapping("/detail")
    public String getDetail(@MyRequestParam Integer id){

        return studentSerice.getDetail(id);

    }
}
