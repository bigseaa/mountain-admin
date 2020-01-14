1.本项目是一个以springboot为基础的后台管理系统的api，目前已经完成了登录与接口权限控制。
2.登录逻辑：
（1）使用post方式调用localhost:8080/admin/auth/login?username=admin&password=123456，
此接口是排除在权限校验之外的，因此不用带上token也可访问；
（2）在验证账号密码之后，将access_token、jwt_token、refresh_token存入了redis，但是真正认证、授权
不是通过redis进行的，存入redis是方便从reids中通过将access_token获取jwt_token；
（3）另外，在后台中使用方法将access_token存入了cookie中，前端（目前没做）通过这一个token查询后端获得
jwt_token；
（4）在需要认证的接口访问后端时，会进入过滤器，该过滤器会对jwt_token进行解析，将解析获取的数据构造
成为UsernamePasswordAuthenticationToken对象，该对象就表示了当前登录的信息，在一次请求完成后，这个对象
由spring security自动销毁。
（5）在权限控制上，目前权限是暂时写死的权限（以后会补上动态的查询）结合@PreAuthorize注解进行权限控制。