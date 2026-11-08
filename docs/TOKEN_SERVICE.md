# RemoteControl 即构凭证服务

复用现有 Token04 AES-GCM 签发器。`POST /getZegoToken` 接收 JSON `userId`、`loginRoomId`；用户 ID 为 1–64 个字符，协助码为六位 ASCII 数字。成功响应保留 `data`、`error`，增加十进制字符串 `appId`、`roomId`、`userId`、Unix 秒 `expiresAt`。客户端登录和续期必须使用相同身份。

| HTTP | error.code | 含义 |
| --- | --- | --- |
| 200 | SUCCESS | 本地签发成功 |
| 400 | INVALID_REQUEST | 请求无效 |
| 503 | NOT_CONFIGURED | 新账号参数未配置或无效 |
| 500 | SIGNING_FAILED | 签发失败 |

响应禁止缓存，不返回内部异常或密钥。`GET /health` 在本地配置有效时返回 200/ok，缺配置时返回 503/not_configured；不能代表云端账号、余额或服务开通状态。

## 私有配置

服务器 `/etc/remotecontrol-token.env` 必须归 root 所有，权限 0600。参照 `deploy/remotecontrol-token.env.example`，在服务器本地编辑：

- `ZEGO_APP_ID`：新项目的 AppID，范围 1–4294967295。
- `ZEGO_SERVER_SECRET`：同一项目的 32 字节 ASCII ServerSecret。
- `ZEGO_TOKEN_TTL_SECONDS`：60–86400 秒，默认 600。

缺配置时不会回退旧账号。真实参数不得写入源码、命令行、日志或聊天；填入后重启 `remotecontrol-token.service`。服务采用基础 Token04，空 payload，不宣称启用房间或推流权限鉴权。

## 独立启动与部署

JDK 17 或更高版本构建：`./mvnw test package`。默认主类继续为原有 `RemotecontrolApplication`。独立 Token 进程通过 PropertiesLauncher 指定入口：

```sh
java -Dloader.main=com.lumostech.remotecontrol.token.TokenServiceApplication \
  -cp target/remotecontrol-0.0.1-SNAPSHOT.jar \
  org.springframework.boot.loader.launch.PropertiesLauncher \
  --server.address=127.0.0.1 --server.port=8088
```

该入口显式导入 Token 控制器，排除 MySQL/MyBatis，不启动用户 API。`deploy/remotecontrol-token.service` 使用 DynamicUser、只读系统和内存限制；JAR 由 root 安装到 `/opt/remotecontrol-token/releases/<sha256>/app.jar`，`current` 原子指向该目录。

把 `deploy/nginx-remotecontrol.conf.inc` 安装为 `/etc/nginx/snippets/remotecontrol-token.conf`，仅在 opub.cn 现有 HTTPS server 内 include。外部入口：

- `https://opub.cn/remotecontrol/getZegoToken`
- `https://opub.cn/remotecontrol/health`

变更前备份实际 Nginx、service、环境文件及 current 指向；先检查差异和 `nginx -t`，成功后 reload。验证旧首页与 `/health` 均保持 200，8088 仅监听回环地址。不要覆盖已有环境文件。

## 验证与回滚

测试使用动态假密钥，独立解密 Token 验证身份、时间和格式；运行时测试确认没有 DataSource/UserAPI。缺配置部署预期返回明确 503。探针只输出状态、业务码和 Token 是否存在，不打印 Token。

若回归，恢复本次备份的 opub.conf/include，`nginx -t` 后 reload；恢复之前 current、service/env。原先没有 Token 服务时 stop/disable 新服务。保留备份和 Java 运行环境，不改变许可证/支付服务。实际部署批次和备份位置见交付记录。

本地签发成功、HTTP 200 和构建通过均不等于真实即构鉴权通过。新账号私有参数和两台 Android 设备具备后，还需验证登录、首帧、远程操作、重连及续期。

Nginx reload 后轮询目标接口的 JSON 响应，确认新配置就绪。服务器可能把 IPv4 回环监听显示为 IPv6 映射格式。
