package com.joey.myspringmvc.servlet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by xiaowu.zhou@tongdun.cn on 2018/5/8.
 */
public class MyDispatchServlet extends HttpServlet {

    private Properties properties = new Properties();

    private List<String> classNames = new ArrayList<>();

    @Override
    public void init(ServletConfig config) throws ServletException {

        //1.加载配置文件
        doLoadConfig(config.getInitParameter("contextConfigLocation"));

        //2.初始化所有相关联的类,扫描用户设定的包下面所有的类
        doScanner(properties.getProperty("scanPackage"));

        System.out.println("classNames :" + classNames.toString());

        //3.拿到扫描到的类,通过反射机制,实例化,并且放到ioc容器中(k-v  beanName-bean) beanName默认是首字母小写
        doInstance();

        //4.初始化HandlerMapping(将url和method对应上)
        initHandlerMapping();

    }

    private void doLoadConfig(String contextConfigLocation){

        System.out.println("contextConfigLocation : " + contextConfigLocation);

        InputStream inputStream =this.getClass().getClassLoader()
                .getResourceAsStream(contextConfigLocation);

        try {
            properties.load(inputStream);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null){
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void doScanner(String scanPackage) {

        scanPackage = scanPackage.replace(".","/");

          URL url = this.getClass().getClassLoader()
                  .getResource("/" + scanPackage);

          System.out.println("path : " + url.getPath());


        File base = new File(url.getFile());

        for (File file : base.listFiles()){

            if (file.isDirectory()){

                doScanner(file.getPath());

            }else {

                classNames.add(file.getName().replace(".class",""));

            }

        }

    }

    private void doInstance() {

        if (classNames.isEmpty()){
            return;
        }else {

        }

    }

    private void initHandlerMapping() {


    }







    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        PrintWriter writer = resp.getWriter();
        writer.print("hello myspringmvc : dear joey");
//        super.doGet(req, resp);
    }
}
