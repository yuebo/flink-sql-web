Flink SQL Web 
------------------------
用于提交SQL任务，具体逻辑参考flink sql submit job和flink metries kafka。
## 运行
需要编译flink sql submit job后修改`flink.submit-job-location`属性。
同时可以通过以下配置修改flink服务器地址。
```yaml
flink:
  rest-port: 8081
  job-manager-port: 6123
  host: localhost
```
