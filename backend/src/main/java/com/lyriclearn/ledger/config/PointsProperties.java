package com.lyriclearn.ledger.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

/** 积分参数（PRD 7.2 奖励系数 / 单日额度），支持运营配置。 */
@Component
@ConfigurationProperties(prefix = "lyriclearn.points")
public class PointsProperties {

    private double baseReward = 10;
    private long dailyCapFree = 30;
    private long dailyCapMember = 100;
    private Map<String, Double> cefr;
    private Quality quality = new Quality();
    private Repeat repeat = new Repeat();

    public double getBaseReward() { return baseReward; }
    public void setBaseReward(double baseReward) { this.baseReward = baseReward; }
    public long getDailyCapFree() { return dailyCapFree; }
    public void setDailyCapFree(long dailyCapFree) { this.dailyCapFree = dailyCapFree; }
    public long getDailyCapMember() { return dailyCapMember; }
    public void setDailyCapMember(long dailyCapMember) { this.dailyCapMember = dailyCapMember; }
    public Map<String, Double> getCefr() { return cefr; }
    public void setCefr(Map<String, Double> cefr) { this.cefr = cefr; }
    public Quality getQuality() { return quality; }
    public void setQuality(Quality quality) { this.quality = quality; }
    public Repeat getRepeat() { return repeat; }
    public void setRepeat(Repeat repeat) { this.repeat = repeat; }

    public static class Quality {
        private double zeroMax = 0.4;
        private double midMax = 0.7;
        private double highMax = 0.9;
        private double zero = 0;
        private double mid = 0.5;
        private double high = 1.0;
        private double top = 1.2;

        public double getZeroMax() { return zeroMax; }
        public void setZeroMax(double zeroMax) { this.zeroMax = zeroMax; }
        public double getMidMax() { return midMax; }
        public void setMidMax(double midMax) { this.midMax = midMax; }
        public double getHighMax() { return highMax; }
        public void setHighMax(double highMax) { this.highMax = highMax; }
        public double getZero() { return zero; }
        public void setZero(double zero) { this.zero = zero; }
        public double getMid() { return mid; }
        public void setMid(double mid) { this.mid = mid; }
        public double getHigh() { return high; }
        public void setHigh(double high) { this.high = high; }
        public double getTop() { return top; }
        public void setTop(double top) { this.top = top; }
    }

    public static class Repeat {
        private double first = 1.0;
        private double review = 0.3;

        public double getFirst() { return first; }
        public void setFirst(double first) { this.first = first; }
        public double getReview() { return review; }
        public void setReview(double review) { this.review = review; }
    }
}
