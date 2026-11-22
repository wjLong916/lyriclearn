package com.lyriclearn.gateway;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 第三方音乐网关（mock 实现，架构 5.4）。
 * 生产环境在此抽象内适配不同平台的字段差异、错误码归一、限流重试；
 * 本阶段返回内置模拟数据。原版歌词仅保存在本组件的请求生命周期内存中，
 * 不写入数据库、缓存或日志（合规约束：不持久化歌词）。
 */
@Component
public class MockMusicGateway {

    /** 仅内存持有、随请求销毁的模拟歌词（句级时间轴，秒）。严禁落库。 */
    private static final Map<String, List<LyricLine>> MEMORY_LYRICS = Map.of(
            "qq:twinkle", List.of(
                    new LyricLine(0.0, "Twinkle, twinkle, little star,", "闪烁吧，闪烁吧，小星星，"),
                    new LyricLine(2.0, "How I wonder what you are.", "我多想知道你是什么。"),
                    new LyricLine(4.0, "Up above the world so high,", "在世界的上方那么高，"),
                    new LyricLine(6.0, "Like a diamond in the sky.", "像天空中的一颗钻石。")),
            "qq:lemon", List.of(
                    new LyricLine(0.0, "I'm sitting here in the boring room,", "我坐在这无聊的房间里，"),
                    new LyricLine(2.5, "It's just another rainy Sunday afternoon.", "这又是一个下雨的周日下午。"),
                    new LyricLine(5.0, "I'm wasting my time, I got nothing to do,", "我在虚度时光，无事可做，"),
                    new LyricLine(7.5, "I'm hanging around, I'm waiting for you.", "我四处闲逛，等你到来。")),
            "qq:letitbe", List.of(
                    new LyricLine(0.0, "When I find myself in times of trouble,", "当我发现自己身处困境，"),
                    new LyricLine(2.5, "Mother Mary comes to me,", "圣母玛利亚来到我身边，"),
                    new LyricLine(5.0, "Speaking words of wisdom, let it be.", "说出智慧之语，顺其自然。"),
                    new LyricLine(7.5, "And in my hour of darkness,", "在我黑暗的时刻，"),
                    new LyricLine(10.0, "She is standing right in front of me,", "她就站在我面前，"),
                    new LyricLine(12.5, "Speaking words of wisdom, let it be.", "说出智慧之语，顺其自然。")),
            "qq:yesterday", List.of(
                    new LyricLine(0.0, "Yesterday, all my troubles seemed so far away,", "昨天，我所有的烦恼似乎都远去，"),
                    new LyricLine(3.0, "Now it looks as though they're here to stay,", "如今它们却仿佛留了下来，"),
                    new LyricLine(6.0, "Oh, I believe in yesterday.", "哦，我相信昨天。"),
                    new LyricLine(9.0, "Suddenly, I'm not half the man I used to be,", "突然间，我不再是从前的自己，"),
                    new LyricLine(12.0, "There's a shadow hanging over me,", "一片阴影笼罩着我，"),
                    new LyricLine(15.0, "Oh, yesterday came suddenly.", "哦，昨天突然降临。"))
    );

    /** 模拟第三方检索结果（仅公开元信息）。 */
    public List<SongMeta> search(String keyword, String level) {
        return List.of(
                new SongMeta("qqmusic", "twinkle", "Twinkle Twinkle Little Star", "Nursery Rhyme",
                        "https://cover.example.com/twinkle.png", "A1", "L0", true),
                new SongMeta("qqmusic", "lemon", "Lemon Tree", "Fool's Garden",
                        "https://cover.example.com/lemon.png", "A2", "L1", true),
                new SongMeta("qqmusic", "letitbe", "Let It Be", "The Beatles",
                        "https://cover.example.com/letitbe.png", "B1", "L2", true),
                new SongMeta("qqmusic", "yesterday", "Yesterday", "The Beatles",
                        "https://cover.example.com/yesterday.png", "B2", "L3", true)
        ).stream()
                .filter(s -> keyword == null || keyword.isBlank()
                        || s.title().toLowerCase().contains(keyword.toLowerCase())
                        || s.artist().toLowerCase().contains(keyword.toLowerCase()))
                .filter(s -> level == null || level.isBlank() || s.cefr().equals(level))
                .toList();
    }

    /**
     * 实时拉取内存歌词（架构 3.3：仅打开学习页时拉取，页面关闭即销毁）。
     * 返回的对象仅供本次请求对齐使用，调用方不得持久化。
     */
    public List<LyricLine> fetchLyric(String platform, String songId) {
        return MEMORY_LYRICS.getOrDefault(platform + ":" + songId, List.of());
    }

    /** 第三方官方播放器跳转地址（仅透传，不存储音频）。 */
    public String playUrl(String platform, String songId) {
        return "https://music.example.com/" + platform + "/play/" + songId;
    }

    public record SongMeta(String platform, String songId, String title, String artist,
                           String cover, String cefr, String level, boolean learnable) {}

    public record LyricLine(double start, String original, String translation) {}
}
