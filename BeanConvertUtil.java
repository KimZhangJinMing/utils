package com.example.cybs.util;

import com.example.cybs.annotation.CyberSourceProperty;
import com.example.cybs.domain.request.PayerAuthSetupRequest;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.xpath.operations.Bool;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Description bean与map的转换
 * @Author Kim
 * @Date 2021/11/3 14:44
 */

public class BeanConvertUtil {


    public static Map<String, String> bean2Map(Object bean) {
        if (bean == null) {
            return null;
        }
        Map<String, String> map = new HashMap<>();
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(bean.getClass(), Object.class);
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
            for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
                // 获取属性名称
                String property = propertyDescriptor.getName();
                Field field = bean.getClass().getDeclaredField(property);
                CyberSourceProperty annotation = field.getAnnotation(CyberSourceProperty.class);
                // 如果属性上标注了注解,说明不是正常的驼峰命名,按照注解的值当成map的key
                // 如果属性上没有注解,正常的驼峰命名转下划线
                property = annotation == null ? humpToUnderline(property) : annotation.value();
                Method readMethod = propertyDescriptor.getReadMethod();
                Object value = readMethod.invoke(bean);
                map.put(property, String.valueOf(value));
            }
        } catch (IntrospectionException | InvocationTargetException | IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
        return map;
    }

    public static <T> T mapToBean(Map<String, String> map, Class<T> clazz) {
        T bean = null;
        if (map != null && map.size() > 0) {
            try {
                bean = clazz.newInstance();
                BeanInfo beanInfo = Introspector.getBeanInfo(clazz, Object.class);
                PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
                for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
                    // 获取属性名称
                    String property = propertyDescriptor.getName();
                    Field field = clazz.getDeclaredField(property);
                    CyberSourceProperty annotation = field.getAnnotation(CyberSourceProperty.class);
                    property = annotation == null ? underlineToHump(property) : annotation.value();
                    // 获取setter方法,不能使用Lombok的Accessors注解,getWriteMethod只能获取到返回值为void的setter方法
                    // 而Accessors(chain=true)的返回值是对象
                    Method writeMethod = propertyDescriptor.getWriteMethod();
                    writeMethod.invoke(bean, map.get(property));
                }
            } catch (InstantiationException | IllegalAccessException | IntrospectionException | InvocationTargetException | NoSuchFieldException e) {
                e.printStackTrace();
            }
        }
        return bean;
    }

    // 驼峰转下划线
    private static String humpToUnderline(String str) {
        Pattern pattern = Pattern.compile("[A-Z]");
        Matcher matcher = pattern.matcher(str);
        while (matcher.find()) {
            // 获取到第一个驼峰的字母
            String target = matcher.group();
            str = str.replaceAll(target, "_" + target.toLowerCase());
        }
        return str;
    }

    // 下划线转驼峰
    private static String underlineToHump(String str) {
        Pattern pattern = Pattern.compile("_(.)");
        Matcher matcher = pattern.matcher(str);
        while(matcher.find()) {
            // 获取到第一个_开头的字母
            String target = matcher.group(1);
            str = str.replaceAll("_" + target, target.toUpperCase());
        }
        return str;
    }

}
