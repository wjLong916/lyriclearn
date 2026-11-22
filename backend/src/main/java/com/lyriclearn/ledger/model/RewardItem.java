package com.lyriclearn.ledger.model;

/** 可兑换权益（PRD 表10）。人民币参考价仅作价值对标，不构成兑付承诺。 */
public record RewardItem(
        String itemId,
        String name,
        String description,
        long costPoints,
        String priceCny,
        String category,
        boolean serviceOnly) {}
