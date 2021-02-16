# Cva

---
### Cva 是一个JVM语言, 未来的目标是兼容C, 支持C编译到JVM虚拟机上;

+ 选取 Java 的子集，支持简单的面向对象
+ 词法/语法分析手动实现，未借助现有工具
+ 面向 JVM 生成字节码
+ 已实现的编译优化：简易的常量折叠、不可达代码删除，基于到达定义分析的常量/拷贝传播，基于活性分析的死代码删除优化

---
### TODO
- Cva生态的兼容C超级Cva;
- CvaNIO原生库;
- 实现Java的Pkg, 进行Pkg编译, 选取pkg替代Java的package关键字, call取代Java的import关键字;
- CvaDK, 实现HashMap
- CvaVM Cva虚拟机;
- CvaIDE 