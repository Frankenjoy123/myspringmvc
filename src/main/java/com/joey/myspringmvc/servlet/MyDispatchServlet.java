package com.joey.myspringmvc.servlet;

import com.joey.myspringmvc.annotation.MyAutowired;
import com.joey.myspringmvc.annotation.MyController;
import com.joey.myspringmvc.annotation.MyRequestMapping;
import com.joey.myspringmvc.annotation.MyService;
import com.joey.myspringmvc.demo.StudentController;
import com.joey.myspringmvc.demo.StudentService;

import javax.lang.model.element.AnnotationMirror;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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

    private Map<String , Object> beanMap = new ConcurrentHashMap<>();

    private Map<String , Method> methodMap = new ConcurrentHashMap<>();

    @Override
    public void init(ServletConfig config) throws ServletException {

        //1.加载配置文件
        doLoadConfig(config.getInitParameter("contextConfigLocation"));

        //2.初始化所有相关联的类,扫描用户设定的包下面所有的类
        doScanner(properties.getProperty("scanPackage"));

        System.out.println("classNames :" + classNames.toString());

        //3. IOC 拿到扫描到的类,通过反射机制,实例化,并且放到ioc容器中(k-v  beanName-bean) beanName默认是首字母小写
        doInstance();

        //4 DI 依赖注入
        try {
            doDependInject();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        StudentController studentController = (StudentController) beanMap.get("studentController");
        studentController.getDetail(10);

        //5.初始化HandlerMapping(将url和method对应上)
        initHandlerMapping();

    }

    private void doDependInject() throws IllegalAccessException {

        if (beanMap == null || beanMap.size() == 0 ){
            return;
        }


        for (Map.Entry<String , Object> entry : beanMap.entrySet()){

            System.out.println(entry);

            Object object = entry.getValue();

            System.out.println(object.getClass());

            if (containsTarget(object.getClass().getDeclaredAnnotations(),
                    MyController.class)){

                Field[]  fields =  object.getClass().getDeclaredFields();

                if (fields != null && fields.length>0){

                    for (Field field : fields){

                        Annotation[] annotations = field.getDeclaredAnnotations();

                        //MyAutowire注解需要注入
                        if (containsTarget(annotations , MyAutowired.class)){

                            field.setAccessible(true);

                            String beanName = tranFirstLower(field.getType().getSimpleName());

                            Object bean = beanMap.get(beanName);

                            field.set(object , bean);

                        }


                    }

                }


            }

        }

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


            try {
                for (String className : classNames){

                    Class<?> c = Class.forName(properties.getProperty("scanPackage") +"."+ className);

                    Annotation[] annotations =  c.getAnnotations();

                    if (annotations == null || annotations.length == 0){
                        continue;
                    }

                    if (containsTarget(annotations , MyService.class)){
                        beanMap.put(tranFirstLower(c.getSimpleName()) , c.newInstance());

                    }else if (containsTarget(annotations , MyController.class)){

                        beanMap.put(tranFirstLower(c.getSimpleName()) , c.newInstance());
                    }


                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }

        }

    }

    private boolean containsTarget(Annotation[] annotations , Class<?> target){

        if (annotations == null || annotations.length == 0){
            return false;
        }

        for (Annotation a : annotations){

            if (a.annotationType().equals(target)){
                return true;
            }

        }

        return false;
    }

    private void initHandlerMapping() {

        if (beanMap == null || beanMap.size()==0){
            return;
        }



        for (Map.Entry<String,Object> entry : beanMap.entrySet()){

            if (entry.getKey() == null || entry.getValue() == null){
                continue;
            }

            Object object = entry.getValue();
            Class c  = object.getClass();

            Annotation[] annotations =  c.getDeclaredAnnotations();

            if (containsTarget(annotations , MyController.class)) {

                String baseUrl;

                MyRequestMapping a  = (MyRequestMapping) c.getAnnotation(MyRequestMapping.class);

                baseUrl = a.value();

                Method[] methods = c.getDeclaredMethods();

                if (methods != null && methods.length>0){


                    for (Method method : methods){

                        MyRequestMapping myRequestMapping = method.getAnnotation(MyRequestMapping.class);
                        String s = myRequestMapping.value();

                        String url = baseUrl + s;

                        url = url.replace("/+","/");

                        methodMap.put(url , method);
                    }
                }

            }



        }


    }




    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        PrintWriter writer = resp.getWriter();
        writer.print("hello myspringmvc : dear joey");
//        super.doGet(req, resp);
    }


    private static String tranFirstLower(String s){


        char[] chars = s.toCharArray();

        chars[0] += 32;
        return String.valueOf(chars);
    }


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        try {
            doDispatchServlet(req , resp);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private void doDispatchServlet(HttpServletRequest req, HttpServletResponse resp) throws InvocationTargetException, IllegalAccessException {


        String url = req.getRequestURI();

        url = url.replaceAll("//","/");

        if (!this.methodMap.containsKey(url)){
            try {
                resp.getWriter().print("NOT FOUND method");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        Method method = methodMap.get(url);



        //方法的参数列表
        Class[] parameterTypes = method.getParameterTypes();

        Object[] paramValues = new Object[parameterTypes.length];

        Map<String , String[]> requestMap =  req.getParameterMap();

        for (int i=0 ; i<parameterTypes.length ; i++){

            Class paramType = parameterTypes[i];

            if (paramType.equals(HttpServletRequest.class)){
                paramValues[i] = req;
                continue;
            }

            if (paramType.equals(HttpServletResponse.class)){
                paramValues[i] = resp;
                continue;
            }

            if (paramType.equals(String.class)){

                String value = null;

                paramValues[i]=value;
            }
        }

        Class methodDeclaringClass  = method.getDeclaringClass();

        Object bean = beanMap.get(tranFirstLower(methodDeclaringClass.getSimpleName()));

        method.invoke(bean , paramValues);

    }


}
