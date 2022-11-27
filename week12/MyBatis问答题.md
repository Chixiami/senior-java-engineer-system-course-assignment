# MyBatis问答题
## 题目01-MyBatis通常是一个XML映射文件配合一个Dao接口，请问Dao接口的工作原理是什么？

MyBatis运行时会使用JDK动态代理为Dao接口生产代理proxy对象，代理对象会拦截接口方法，转而执行MappedStatement所代表的sql，然后将sql执行结果返回。

## 题目02-请你用自己的理解，简单说说MyBatis内SQL语句的执行原理

MyBatis sql执行有两种方式

第一种 基于Dao层实现类开发

由于sqlSessionFactory默认生成的SqlSession是
DefaultSqlSession类型，先执行DefaultSqlSession中的selectOne()，

selectOne()方法中会执行selectList()

selectList()方法中通过 Excetor 执行器 Executor 中执行的语句 MappedStatement 是从 configuration这个封装类用入参statement字符串转化而来

Executor 有两个实现类 BaseExecutor 和 CachingExecutor 取决于是否使用二级缓存
 
如果启用了二级缓存，会先去缓存中查询，然后执行 SimpleExecutor的方法

生成StatementHandler代理对象

使用 StatementHandler 创建 Statement 并执行，调用ParameterHandler设置参数， 调用ResultSetHandler处理结果

第二种 基于Mapper动态代理自动生成Dao层实现类

入口是先执行DefaultSqlSession中的getMapper()方法 

然后进入configuration.getMapper

然后进入mapperRegistry.getMapper

然后通过代理工厂 MapperProxyFactory.newInstance() 创建了代理对象

代理以后，所有的Mapper方法调用时，都会调用MapperProxy类中的invoke方法

在invoke方法中，先判断是否是Object方法，如果是Object方法，则放行，如果不是，

获取缓存中MapperMethodInvoker，如果没有则创建一个（避免重复创建对象），

然后判断是否是接口的默认方法，如果不是默认方法，创建一个PlainMethodInvoker并放入缓存

然后进入MapperMethod.execute 判断当前执行的SQL语句的类型，根据不同的类型进行分流，最后还是调用到了sqlSession.selectOne


## 题目03 MyBatis 分页的原理是什么

MyBatis框架允许用户通过自定义拦截器的方式改变SQL的执行行为，在SQL执行时追加SQL分页语法，从而达到简化分页查询的目的。

自定义PageInterceptor实现现Interceptor接口，在拦截方法中，把执行的SQL语句替换成带有limit的MySQL分页语句来实现分页