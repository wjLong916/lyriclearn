package com.lyriclearn.ledger.model;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/** 平台内权益目录（PRD 表10）。第三方音乐权益以平台补贴优惠券形式发放，积分仅作兑换门槛。 */
@Component
public class RewardCatalog {

    private final List<RewardItem> items = List.of(
            new RewardItem("pron-pack", "高级发音批改包（10 次）", "逐音素纠偏与连读专项评分",
                    300, "30 元", "AI 学习能力", false),
            new RewardItem("rag-qa", "RAG 上下文答疑（30 天）", "绑定当前歌曲的智能答疑额度",
                    200, "20 元", "AI 学习能力", false),
            new RewardItem("archive-pdf", "学习档案 PDF 导出", "含音标、释义、乐谱与学情报告",
                    120, "12 元", "学习资料", false),
            new RewardItem("monthly-member", "月度学习会员", "解锁高阶歌曲与无限复习",
                    390, "39 元", "会员权益", false),
            new RewardItem("sbt-mint", "SBT 学习凭证铸造", "绑定账户的不可转让成就凭证（服务项）",
                    20, "服务项", "学习资料", true),
            new RewardItem("music-coupon", "第三方音乐优惠券", "平台补贴发放，跳转官方收银台使用",
                    80, "8 元", "音乐福利", false));

    public List<RewardItem> all() { return items; }

    public Optional<RewardItem> byId(String itemId) {
        return items.stream().filter(i -> i.itemId().equals(itemId)).findFirst();
    }
}
