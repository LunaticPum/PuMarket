FROM pgvector/pgvector:pg17

# 换成阿里云 Debian 源（加速国内下载）
RUN sed -i 's/deb.debian.org/mirrors.aliyun.com/g' /etc/apt/sources.list.d/debian.sources

# 安装 zhparser 中文分词扩展的构建依赖
RUN apt-get update && apt-get install -y \
    build-essential \
    postgresql-server-dev-17 \
    git \
    && git clone https://github.com/amutu/zhparser.git /tmp/zhparser \
    && cd /tmp/zhparser \
    && make && make install \
    && cd / && rm -rf /tmp/zhparser \
    && apt-get purge -y build-essential postgresql-server-dev-17 git \
    && apt-get autoremove -y \
    && rm -rf /var/lib/apt/lists/*
