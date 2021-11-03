# utils
常用工具类集合

#### BeanConvertUtil
利用反射进行map和bean的转换。
如果使用Lombok,在bean转换成map的时候,反射通过getWriterMethod获取setter时必须是获取到返回值为void的setter方法,而Lombok的@Accessors(chain = true)的setter方法返回值不是void,所以不能使用Lombok的@Accessors.

#### JsonUtil
JSON序列化/反序列化工具

