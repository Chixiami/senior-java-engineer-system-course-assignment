# 题目01-请谈谈自己对于IOC和AOP的理解

## IOC：
IOC是控制反转，指的是将对象的控制权交给 Spring 的对象容器。


IOC由于把创建和查找依赖对象的控制权交给了容器，由容器进行注入和组合对象，降低了程序的耦合性。

ID依赖注入：IoC解耦只是降低对象之间的依赖关系，但不会消除。 依赖的管理需要以DI来处理。管理依赖有三种方式

1. 构造函数注入
2. set方法注入
3. 使用名称空间p注入

## AOP：
AOP 是面向切面编程，是通过运行期代理的技术，实现程序功能统一维护的技术。

 AOP 在程序运行期间，不修改源码，对已有的方法进行加强。

优点是减少重复代码，提高开发效率，方便维护

AOP中的概念：

1. 切入点（Pointcut）：用于定义通知应该切入到那些连接点上，不同的通知能切入到不同的点，通俗点说就是把通知（也就是事务）加入你想的业务代码上
2. Aspect(切面)：切面由切点和增强组成，切面 = 切点 + 增强，也可以= 切点 + 方位信息 + 横切逻
   辑，还可以= 连接点 + 横切逻辑
3. JoinPoint(连接点)：横切程序执行的特定位置，比如：类中某个方法调用前、调用后，方法抛出异
   常后等
4. Target(目标对象)：增强逻辑的目标类
5. Weaving(织入)：织入是将增强逻辑/横切逻辑添加到目标类具体连接点上的过程
6. Proxy(代理)：一个类被AOP织入增强后，就产出了一个代理类，它是融合了原类和增强逻辑的代
   理类。
7. Advice(通知/增强)：增强的第一层意思就是你的横切逻辑代码（增强逻辑代码）


# 题目02-请问Spring如何解决循环依赖问题

循环依赖指的是N个Bean互相引用对方，最终形成闭环

Spring 中使用了三级缓存的设计，来解决单例模式下的属性循环依赖问题。解决的只是单例模式下的set属性赋值的Bean属性循环依赖问题，对于多例Bean的和构造方法注
入参数的循环依赖问题，并不能使用三级缓存设计解决。

三级缓存处理过程：

1. A实例化后、注入前放到3级缓存
2. B放到3级缓存后，A在注入属性时，发现有循环依赖，因此需要先getBean(B)，实例化B，并将B也
   从入3级缓存
3. B放到3级缓存后，这时B要开始注入属性A，于是B找到了循环依赖A后，再从头执行getBean(A)方
   法，getSingleton方法本次从缓存中取，然后将A设置到2级缓存，并且从3级缓存移除。
4. B如愿以偿的拿到了A完成注入，然后B执行到 DefaultSingletonBeanRegistry#addSingleton方
   法，将B从3级缓存移出，放入1级缓存，到此B完成。B的完成是被动的，A需要它，才会先去创建
   它
5. A 还要继续自己的流程，然后populateBean方法将B注入。然后，A移出2级缓存，进入1级缓存，
   整个流程完成！

**不能解决构造方法的循环依赖原因:**

对象构造函数会在实例化阶段调用

**不能解决多例的循环依赖原因:**

IoC容器只会管理单例Bean，并将单例Bean存入缓存。作用域为prototype时，每次getBean
都会创建新的对象，并不存入缓存，因此不可以解决循环依赖问题。

**需要三级缓存原因：**
使用三级而非二级缓存并非出于 IOC 的考虑，而是出于 AOP 的考虑，即若使用二级缓存，在 AOP 情形
注入到其他 Bean的，不是最终的代理对象，而是原始对象。