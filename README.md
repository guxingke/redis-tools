# Redis Tools

Redis 相关的一些命令行工具集合。

## 动机

1. rdb 文件快速分析，其他的一些工具有些慢。
2. redis-cli 的一些命令增强，比如 monitor 支持 pattern 匹配，和命令匹配。
3. 不需要 GUI。

## 特性

1. 快，分析 4g rdb 文件，目前大部分功能都能在数秒内运行结束。
2. 流式分析，内存占用低。
3. 使用 native-image aot 编译，服务器可不配置 `Java` 环境。

## 快速开始

### 可执行文件

支持 linux x86_64, macos arm, x86_64。

```bash
# 下载，赋可执行权限
curl -o rdt -L "https://github.com/guxingke/redis-tools/releases/download/v0.0.1/rdt-$(uname)-$(arch)" && chmod +x ./rdt

# 执行
./rdt
```

### fat-jar

需要 jdk17 运行时。

```bash
# 下载
curl -L "https://github.com/guxingke/redis-tools/releases/download/v0.0.1/rdt.jar"

# 执行
java -jar rdt.jar
```

## 使用

以可执行文件为例，需编译或下载可执行文件。

```bash

# help
λ rdt
Usage: rdt [-hV] [COMMAND]
redis cli tool set
  -h, --help      Show this help message and exit.
  -V, --version   Print version information and exit.
Commands:
  test
  info      show rdb basic information, from aux field.
  bigkeys   search rdb big keys, sort by memory used.
  monitor   like redis-cli monitor command, addition with pattern and cmd
              option.
  rdb-scan  like redis-cli scan command, but for rdb.
  keyspace  overview rdb keyspace, based on glob like key pattern aggregation.
  mem       overview rdb memory, memory used, ttl etc.
  scan      wrapper for redis-cli scan command
  help      Displays help information about the specified command

See 'rdt help <command>' to read about a specific subcommand or concept.

# info
λ rdt info test.rdb
redis-ver : 5.0.7
redis-bits : 64
ctime : 1657189488
used-mem : 6541033248
repl-stream-db : 0
repl-id : ee698884a36b7887e773f3fc64036e638be85977
repl-offset : 60360069123
aof-preamble : 0
total keys: 50650641

# bigkeys
λ rdt bigkeys test.rdb
total: 50650641

Biggest String Keys
┌───────────────────┬───────────────────┬───────────────────┬───────────────────┬──────────────────┐
│ key               │ type              │ mem(byte)         │ size              │ ttl(ms)          │
├───────────────────┼───────────────────┼───────────────────┼───────────────────┼──────────────────┤
│ mykey             │ String            │ 7                 │ 1                 │ -1               │
│ 123               │ String            │ 5                 │ 1                 │ -1               │
└───────────────────┴───────────────────┴───────────────────┴───────────────────┴──────────────────┘

# monitor
λ rdt monitor redis://127.0.0.1:6379 -p foo*  -c set
foo:vv               set        raw: 1657870874.981835 [0 127.0.0.1:65535] "set" "foo:vv" "10"
foo                  set        raw: 1657870908.429316 [0 127.0.0.1:65535] "set" "foo" "10"

# rdb-scan
λ rdt rdb-scan test.rdb -p xc_* bp_* -type string | head -n 1
bp_11111_1

# keyspace
λ rdt keyspace test3.rdb
total: 11111111

┌─────────────────────────────────────────────────┬────────────────────────────────────────────────┐
│ keyspace                                        │ total                                          │
├─────────────────────────────────────────────────┼────────────────────────────────────────────────┤
│ 11:11.*                                         │ 1811                                           │
│ 99:98.*                                         │ 1533                                           │
│ 16:16.*:0                                       │ 2                                              │
└─────────────────────────────────────────────────┴────────────────────────────────────────────────┘

# mem
λ ./rdt mem test.rdb
Memory Overview
┌────────────────────────┬────────────────────────┬────────────────────────┬───────────────────────┐
│ Type                   │ Memory                 │ Number of Keys         │ Top Key               │
├────────────────────────┼────────────────────────┼────────────────────────┼───────────────────────┤
│ Total                  │ 0                      │ 50650641               │                       │
├────────────────────────┼────────────────────────┼────────────────────────┼───────────────────────┤
│ Value                  │ 12 Byte                │ 2                      │ mykey                 │
├────────────────────────┼────────────────────────┼────────────────────────┼───────────────────────┤
│ Zset                   │ 5460 Byte              │ 212                    │ ttttt:tttt:tttttt:172 │
│                        │                        │                        │ 11111                 │
├────────────────────────┼────────────────────────┼────────────────────────┼───────────────────────┤
│ Hash                   │ 1.70 GB                │ 50650427               │ asdfasd.adafdasfasd   │
└────────────────────────┴────────────────────────┴────────────────────────┴───────────────────────┘
TTL Overview
┌────────────────────────────────┬────────────────────────────────┬────────────────────────────────┐
│ Range                          │ Number of Keys                 │ Percentage                     │
├────────────────────────────────┼────────────────────────────────┼────────────────────────────────┤
│ Total                          │ 50650641                       │                                │
├────────────────────────────────┼────────────────────────────────┼────────────────────────────────┤
│ Never Expire                   │ 50650641                       │ ####################           │
└────────────────────────────────┴────────────────────────────────┴────────────────────────────────┘
```


编译或下载的 `fat-jar` 与可执行文件类似。

e.g

```bash
λ java -jar target/rdt.jar
Usage: rdt [-hV] [COMMAND]
redis cli tool set
  -h, --help      Show this help message and exit.
  -V, --version   Print version information and exit.
Commands:
  test
  info      show rdb basic information, from aux field.
  bigkeys   search rdb big keys, sort by memory used.
  monitor   like redis-cli monitor command, addition with pattern and cmd
              option.
  rdb-scan  like redis-cli scan command, but for rdb.
  keyspace  overview rdb keyspace, based on glob like key pattern aggregation.
  mem       overview rdb memory, memory used, ttl etc.
  scan      wrapper for redis-cli scan command
  help      Displays help information about the specified command

See 'rdt help <command>' to read about a specific subcommand or concept.
```

## 开发

### 环境

JDK17
```bash
λ java -version
openjdk version "17.0.2" 2022-01-18 LTS
OpenJDK Runtime Environment (build 17.0.2+9-LTS)
OpenJDK 64-Bit Server VM (build 17.0.2+9-LTS, mixed mode, sharing)
```

GraalVM 可选

```bash
λ native-image --version
GraalVM 22.1.0 Java 17 CE (Java Version 17.0.3+7-jvmci-22.1-b06)
```

### 编译

```bash
mvn clean package
```

### AOT 编译（可选）

在编译之后执行
```bash
native-image -jar target/rdt.jar
```

编译结果为当前目录下 `rdt` 文件。

## 其他

- [自动完成(可点击)](https://picocli.info/autocomplete.html)

```bash
source <(./rdt generate-completion)
```

## 参考

- [Memory analysis](https://docs.redis.com/latest/ri/using-redisinsight/memory-analysis/)
  KEYSPACE 以及 TTL 相关部分。
- [java-rdb-parser](https://github.com/jwhitbeck/java-rdb-parser)
  用来分析 rdb 文件，改动部分代码方便统计。
- [rdr](https://github.com/xueqiu/rdr)
- [redis.io](https://redis.io/)
- [ASCII Table](https://github.com/vdmeer/asciitable)
- [picocli](https://github.com/remkop/picocli)
  命令行程序库
