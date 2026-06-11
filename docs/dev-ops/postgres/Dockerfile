FROM pgvector/pgvector:pg17

# 换成阿里云 Debian 源（加速国内下载）
RUN sed -i 's/deb.debian.org/mirrors.aliyun.com/g' /etc/apt/sources.list.d/debian.sources

# 安装 zhparser 所需构建工具
RUN apt-get update && apt-get install -y \
    build-essential \
    postgresql-server-dev-17 \
    pkg-config \
    git \
    wget \
    && apt-get clean

# 编译安装 scws（中文分词库，zhparser 依赖）
RUN cd /tmp \
    && wget -q http://www.xunsearch.com/scws/down/scws-1.2.3.tar.bz2 \
    && tar xf scws-1.2.3.tar.bz2 \
    && cd scws-1.2.3 \
    && ./configure \
    && make && make install \
    && ldconfig \
    && cd / && rm -rf /tmp/scws-1.2.3*

# 编译安装 zhparser（PostgreSQL 中文分词扩展）
RUN cd /tmp \
    && wget -q https://github.com/amutu/zhparser/archive/refs/heads/master.zip \
    && unzip -q master.zip \
    && cd zhparser-master \
    && SCWS_HOME=/usr/local make && make install \
    && cd / && rm -rf /tmp/zhparser-master /tmp/master.zip

# 清理构建工具（保留运行时所需的库）
RUN apt-get purge -y build-essential postgresql-server-dev-17 pkg-config git wget \
    && apt-get autoremove -y \
    && rm -rf /var/lib/apt/lists/*
