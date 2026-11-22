package com.lyriclearn;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * LyricLearn 英文歌曲学习平台 —— 第一阶段模块化单体启动入口。
 * 按架构文档的八服务边界分包（catalog / annotation / session / practice / ledger / evidence / user / gateway / omniedu），
 * 外部依赖（第三方音乐、omniEdu 中台、链节点）在本阶段以 mock / 内存实现替代，预留拆分。
 */
@EnableAsync
@EnableScheduling
@SpringBootApplication
public class LyricLearnApplication {
    public static void main(String[] args) {
        SpringApplication.run(LyricLearnApplication.class, args);
    }
}
