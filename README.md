# 农业无人机智能作业调度平台

前端使用 Vue3 + Vite，后端使用 Spring Boot 3 + SpringMVC + MyBatis-Plus，数据库使用 MySQL。系统覆盖登录鉴权、无人机资源、农田地块、作业任务、调度方案生成、作业执行记录、调度结果看板等核心业务。

## 技术亮点

- 多因素调度评分：综合任务优先级、虫害风险、截止时间、电量、载荷和喷幅生成调度方案。
- 前后端分离：Vue3 工作台通过 REST API 调用 Spring Boot 服务。
- SpringMVC：使用 Controller、RESTful 映射、全局异常处理、跨域配置和接口访问拦截器完成 Web 层处理。
- MyBatis-Plus：使用 BaseMapper 完成核心 CRUD，减少模板代码。
- 可解释决策：每条调度计划保留评分和分配原因，便于答辩和简历描述。
- 地图态势：维护地块边界点、无人机经纬度和作业轨迹点，前端展示地块多边形、无人机位置和轨迹线，后续可替换为高德地图/Leaflet 底图。
- 操作留痕：记录登录、调度方案生成、确认、取消、开始作业、完成作业等关键动作。

## 本地运行

1. 创建数据库并导入样例数据：

```sql
source database/schema.sql;
```

已有数据库只想升级新增登录和作业执行表时，执行：

```sql
source database/upgrade_auth_operation.sql;
```

默认登录账号：

```text
admin / admin123
```

2. 修改后端数据库账号：

```yaml
backend/src/main/resources/application.yml
spring.datasource.username: root
spring.datasource.password: 你的数据库密码
```

3. 启动后端：

```bash
cd backend
mvn spring-boot:run
```

4. 启动前端：

```bash
cd frontend
npm install
npm run dev
```

如需启用高德地图，在 `frontend` 目录创建 `.env`：

```text
VITE_AMAP_KEY=你的高德Web端JSAPI Key
VITE_AMAP_SECURITY_CODE=你的高德安全密钥
```

重启前端后进入“地图态势”，可查看地块边界、无人机位置、作业轨迹，并点击地图读取经纬度。

5. 浏览器访问：

```text
http://localhost:5173
```

## 系统简要总结

农业无人机智能作业调度平台：基于 Spring Boot、SpringMVC、MyBatis-Plus、MySQL、Vue3 实现农田作业任务管理、无人机资源管理和调度方案生成。设计多因素调度评分策略，结合任务优先级、作业窗口、虫害风险、电量、距离、载荷与喷幅进行资源匹配，并输出可解释调度原因；前端实现作业驾驶舱、任务录入、地图态势和调度结果可视化。
