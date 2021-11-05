package com.example.cybs.util;

import com.example.cybs.annotation.CyberSourceProperty;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.core.io.ClassPathResource;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Description bean与map的转换
 * @Author Kim
 * @Date 2021/11/3 14:44
 */

public class BeanConvertUtil {



    public static <T> T properties2Bean(String filename, Class<T> clazz) {
        Properties properties = new Properties();
        // 从classpath中加载properties文件
        try {
            ClassPathResource classPathResource = new ClassPathResource(filename);
            InputStream inputStream = classPathResource.getInputStream();
            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Map<String,String> map = new HashMap<String,String>((Map)properties);
        return map2Bean(map, clazz);
    }

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
                // 如果属性上标注了注解,说明不是正常的驼峰命名,按照注解的值当成map的key
                // 如果属性上没有注解,正常的驼峰命名转下划线
                Field field = getFieldByProperty(bean.getClass(), property);
                CyberSourceProperty annotation = field.getAnnotation(CyberSourceProperty.class);
                property = annotation == null ? humpToUnderline(property) : annotation.value();
                Method readMethod = propertyDescriptor.getReadMethod();
                Object value = readMethod.invoke(bean);
                map.put(property, String.valueOf(value));
            }
        } catch (IntrospectionException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return map;
    }

    public static <T> T map2Bean(Map<String, String> map, Class<T> clazz) {
        T bean = null;
        if (map != null && map.size() > 0) {
            try {
                bean = clazz.newInstance();
                BeanInfo beanInfo = Introspector.getBeanInfo(clazz, Object.class);
                PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
                for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
                    // 获取属性名称
                    String property = propertyDescriptor.getName();
                    Field field = getFieldByProperty(clazz,property);
                    CyberSourceProperty annotation = field.getAnnotation(CyberSourceProperty.class);
                    property = annotation == null ? underlineToHump(property) : annotation.value();
                    // 获取setter方法,不能使用Lombok的Accessors注解,getWriteMethod只能获取到返回值为void的setter方法
                    // 而Accessors(chain=true)的返回值是对象
                    Method writeMethod = propertyDescriptor.getWriteMethod();
                    String value = map.get(property);
                    // setter方法只有一个参数的,直接取数组第一个元素
                    writeMethod.invoke(bean, transType(value, writeMethod.getParameterTypes()[0]));
                }
            } catch (InstantiationException | IllegalAccessException | IntrospectionException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return bean;
    }

    public static Object transType(String value,Class<?> t) {
        if(t == String.class) {
            return String.valueOf(value);
        }else if(t == boolean.class || t == Boolean.class) {
            return Boolean.valueOf(value);
        }else if(t == Byte.class || t == byte.class) {
            return Byte.valueOf(value);
        }else if(t == Short.class || t == short.class) {
            return Short.valueOf(value);
        }else if(t == Float.class || t == float.class) {
            return Float.valueOf(value);
        }else if(t == Double.class || t == double.class) {
            return Double.valueOf(value);
        }else if(t == Integer.class || t == int.class) {
            return Integer.valueOf(value);
        }else if(t == Long.class || t == long.class) {
            return Long.valueOf(value);
        }else if(t == Character.class || t == char.class) {
            return value.charAt(0);
        }else if(t == BigDecimal.class) {
            return new BigDecimal(value);
        }else if(t == BigInteger.class) {
            return new BigInteger(value);
        }else if(t == Date.class) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                return simpleDateFormat.parse(value);
            } catch (ParseException e) {
                e.printStackTrace();
                return null;
            }
        }else if(t.isEnum()) {
            try {
                Class<?> aClass = Class.forName(t.getName());
                Field field = aClass.getDeclaredField(value);
                return field.get(aClass);
            } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return value;
        }
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

    // 通过属性名称获取Field(包含父类)
    private static Field getFieldByProperty(Class<?> clazz,String property) {
        // 获取当前类的Field对象
        Field field = null;
        while (field == null && clazz != null) {
            try {
                // 从当前类中查找到,退出while循环
                field = clazz.getDeclaredField(property);
            } catch (NoSuchFieldException e) {
                // 从当前类中查找不到会抛出异常,从父类中查找
                clazz = clazz.getSuperclass();
            }
        }
        return field;
    }


    public static String[] getNullPropertyNames(Object source) {
        final BeanWrapper src = new BeanWrapperImpl(source);
        PropertyDescriptor[] pds = src.getPropertyDescriptors();

        Set<String> emptyNames = new HashSet<>();
        for (PropertyDescriptor pd : pds) {
            Object srcValue = src.getPropertyValue(pd.getName());
            if (srcValue == null) {
                emptyNames.add(pd.getName());
            }
        }

        String[] result = new String[emptyNames.size()];
        return emptyNames.toArray(result);
    }

    public static void copyPropertiesIgnoreNull(Object src, Object target) {
        BeanUtils.copyProperties(src, target, getNullPropertyNames(src));
    }
}
