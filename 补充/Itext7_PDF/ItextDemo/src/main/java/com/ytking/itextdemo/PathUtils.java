package com.ytking.itextdemo;
import java.io.File;
import java.net.URL;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

/**
 * 路径获取类
 * */
public class PathUtils {
    /**
     * 获取项目根目录的绝对路径
     *
     * @return 如:F:\TongJianpeng\J2EEUtil
     * */
    public static String getAbsolutePathWithProject() {
        return System.getProperty("user.dir");
    }

    /**
     * 获取项目所在盘符
     * */
    public static String getDriverPathWithProject() {
        return new File("/").getAbsolutePath();
    }

    /**
     * 获取项目根目录的绝对路径
     *
     * @return 项目根目.例如<br/> F:\tomcat\webapps\J2EEUtil\
     * */
    public static String getAbsolutePathWithWebProject(
            HttpServletRequest request) {
        return request.getSession().getServletContext().getRealPath("/");
    }

    /**
     * 获取项目根目录下的指定目录的绝对路径
     *
     *  项目根目下的指定目录
     *            .例如:/login/
     * @return 项目根目下的指定目录.例如:<br/> F:\tomcat\webapps\J2EEUtil\login\
     * */
    public static String getAbsolutePathWithWebProject(
            HttpServletRequest request, String path) {
        return request.getSession().getServletContext().getRealPath(path);
    }

    /**
     * 获取项目根目录的绝对路径
     *
     * @return 项目根目.例如<br/> F:\tomcat\webapps\J2EEUtil\
     * */
    public static String getAbsolutePathWithWebProject(ServletContext context) {
        return context.getRealPath("/");
    }

    /**
     * 获取项目根目录下的指定目录的绝对路径
     *
     *  项目根目下的指定目录
     *            .例如:/login/
     * @return 项目根目下的指定目录.例如:<br/> F:\tomcat\webapps\J2EEUtil\login\
     * */
    public static String getAbsolutePathWithWebProject(ServletContext context,
                                                       String path) {
        return context.getRealPath(path);
    }

    /**
     * 获取项目classpath目录的绝对路径
     *
     * @return classes目录的绝对路径<br/>
     *         file:/F:/tomcat/webapps/J2EEUtil/WEB-INF/classes/
     * */
    public static URL getAbsolutePathWithClass() {
        return PathUtils.class.getResource("/");
    }

    /**
     * 获取项目classPath目录下的指定目录的绝对路径
     *
     * @param path
     *            classes目录下的指定目录.比如:/com/
     * @return file:/F:/tomcat/webapps/J2EEUtil/WEB-INF/classes/com/
     * */
    public static URL getAbsolutePathWithClass(String path) {
        return PathUtils.class.getResource(path);
    }

    /**
     * 获取指定类文件的所在目录的绝对路径
     *
     * @param clazz
     *            类
     * @return 类文件的绝对路径.例如:<br/> 包com.Aries.Util.Web下的Main.java类.<br/>
     *         路径为:file:/
     *         F:/tomcat/webapps/J2EEUtil/WEB-INF/classes/com/Aries/Util/Web/
     * */
    public static URL getAbsolutePathWithClass(Class clazz) {
        return clazz.getResource("");
    }
}