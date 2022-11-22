# 基础镜像
FROM  java:8
# author
MAINTAINER kaixin

EXPOSE 8585

# 挂载目录
VOLUME /home/kxmall
# 创建目录
RUN mkdir -p /home/kxmall
# 指定路径
WORKDIR /home/kxmall
# 复制jar文件到路径
COPY kxmall-admin-0.0.1-RELEASE.jar /home/kxmall/kxmall-admin.jar
# 启动认证服务
ENTRYPOINT ["java","-jar","kxmall-admin.jar"]
