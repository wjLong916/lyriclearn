package com.lyriclearn.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lyriclearn.article.entity.Article;
import com.lyriclearn.article.repo.ArticleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/** 自建文章库种子：原创短文与公有领域演说节选，不复制受版权保护原文。 */
@Component
public class ArticleSeeder implements CommandLineRunner {

    private final ArticleRepository repository;
    private final ObjectMapper mapper = new ObjectMapper();

    public ArticleSeeder(ArticleRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        if (repository.count() > 0) return;
        seedBee();
        seedCity();
        seedGetty();
        seedDream();
        seedTedDemo();
        seedPoems();
    }

    private void seedBee() {
        save("art-bee", "The Bee and the Flower", "LyricLearn Original", "原创科普短文", "NEWS", "A2", "L1",
                "一只蜜蜂在花丛间采蜜，无意间帮助植物传粉。",
                List.of(
                        Map.of("p", "A bee flies from flower to flower on a warm summer day. It looks for sweet juice called nectar."),
                        Map.of("p", "When the bee lands on a flower, it carries a little yellow dust called pollen from one plant to another. This helps the plant make seeds."),
                        Map.of("p", "The bee does not know it is helping. It is only looking for food. But the world becomes more beautiful because of its work.")),
                List.of(
                        Map.of("word", "nectar", "phonetic", "/ˈnektə/", "meaning", "花蜜"),
                        Map.of("word", "pollen", "phonetic", "/ˈpɒlən/", "meaning", "花粉"),
                        Map.of("word", "seed", "phonetic", "/siːd/", "meaning", "种子")));
    }

    private void seedCity() {
        save("art-city", "Walking in the City", "LyricLearn Original", "原创生活短文", "NEWS", "B1", "L2",
                "越来越多人选择步行出行，城市也在为此改善环境。",
                List.of(
                        Map.of("p", "More people are choosing to walk instead of driving in the city. A short walk is good for the body and clears the mind."),
                        Map.of("p", "Cities are building wider sidewalks and safer crossings. They also add trees and seats so people can rest."),
                        Map.of("p", "Walking does not cost money, and it does not make the air dirty. Many people say they feel happier after a walk.")),
                List.of(
                        Map.of("word", "sidewalk", "phonetic", "/ˈsaɪdwɔːk/", "meaning", "人行道"),
                        Map.of("word", "crossing", "phonetic", "/ˈkrɒsɪŋ/", "meaning", "人行横道；路口"),
                        Map.of("word", "instead", "phonetic", "/ɪnˈsted/", "meaning", "代替；反而")));
    }

    private void seedGetty() {
        save("art-getty", "The Gettysburg Address (Excerpt)", "Abraham Lincoln, 1863", "公有领域演说节选", "SPEECH", "B1", "L2",
                "林肯葛底斯堡演说开篇节选：自由、平等与奉献（公有领域）。",
                List.of(
                        Map.of("p", "Four score and seven years ago our fathers brought forth on this continent a new nation, conceived in liberty, and dedicated to the proposition that all men are created equal."),
                        Map.of("p", "Now we are engaged in a great civil war. We have come to dedicate a portion of that field as a final resting place for those who gave their lives here."),
                        Map.of("p", "It is for us the living to be dedicated to the unfinished work which they have so far nobly advanced.")),
                List.of(
                        Map.of("word", "continent", "phonetic", "/ˈkɒntɪnənt/", "meaning", "大陆；洲"),
                        Map.of("word", "liberty", "phonetic", "/ˈlɪbəti/", "meaning", "自由"),
                        Map.of("word", "dedicate", "phonetic", "/ˈdedɪkeɪt/", "meaning", "奉献；致力于")));
    }

    private void seedDream() {
        save("art-dream", "Hold on to Your Dream", "LyricLearn Original", "原创励志演讲", "SPEECH", "B2", "L3",
                "坚持梦想：缓慢前行、不惧失败，默默耕耘终会发光。",
                List.of(
                        Map.of("p", "Every great journey begins with a single dream. The road may be slow, and the way may be dark, but small steps every day carry you forward."),
                        Map.of("p", "Do not fear failure. Failure is a teacher that shows you what to try next. Those who keep going are the ones who finally arrive."),
                        Map.of("p", "Believe in the work that no one sees yet. One day, your quiet effort will become a story that inspires others.")),
                List.of(
                        Map.of("word", "journey", "phonetic", "/ˈdʒɜːni/", "meaning", "旅程；历程"),
                        Map.of("word", "failure", "phonetic", "/ˈfeɪljə/", "meaning", "失败"),
                        Map.of("word", "inspire", "phonetic", "/ɪnˈspaɪə/", "meaning", "激励；鼓舞")));
    }

    /**
     * 摘要 + 外链跳转示范：受版权保护的第三方演讲不存储原文，
     * 仅由平台撰写导读摘要、标注核心生词，原文通过外链跳转 TED 官方页阅读。
     */
    private void seedTedDemo() {
        save("art-ted", "TED Talk: Ideas Worth Spreading (Demo)", "TED Conferences",
                "第三方演讲 · 摘要导读 + 外链", "SPEECH", "B1", "L2",
                "平台导读：TED 大会汇集各领域讲者分享有影响力的想法。本站仅提供中文导读与核心生词，完整演讲与原文请前往 TED 官方页面阅读（外链示范，接入时替换为具体演讲页）。",
                List.of(),
                List.of(
                        Map.of("word", "inspire", "phonetic", "/ɪnˈspaɪə/", "meaning", "激励；鼓舞"),
                        Map.of("word", "idea", "phonetic", "/aɪˈdɪə/", "meaning", "想法；观点"),
                        Map.of("word", "audience", "phonetic", "/ˈɔːdiəns/", "meaning", "听众；观众")),
                "SUMMARY_LINK", "https://www.ted.com/talks");
    }

    /**
     * 公有领域经典诗歌（L0–L4）：传统童谣与布莱克、莎士比亚作品，
     * 作者均已去世逾版权保护期，文本为公有领域，可自由站内呈现。
     */
    private void seedPoems() {
        // L0 / A1 传统童谣 Hey Diddle Diddle
        save("art-poem-diddle", "Hey Diddle Diddle", "Traditional Nursery Rhyme",
                "公有领域传统童谣 · L0", "POEM", "A1", "L0",
                "一首荒诞有趣的传统童谣：猫拉提琴、母牛跃月，餐具四散跑走。",
                List.of(
                        Map.of("p", "Hey diddle diddle, the cat and the fiddle,"),
                        Map.of("p", "The cow jumped over the moon;"),
                        Map.of("p", "The little dog laughed to see such sport,"),
                        Map.of("p", "And the dish ran away with the spoon.")),
                List.of(
                        Map.of("word", "fiddle", "phonetic", "/ˈfɪdl/", "meaning", "小提琴"),
                        Map.of("word", "moon", "phonetic", "/muːn/", "meaning", "月亮"),
                        Map.of("word", "spoon", "phonetic", "/spuːn/", "meaning", "勺子")));

        // L1 / A2 传统童谣 Little Bo-Peep
        save("art-poem-sheep", "Little Bo-Peep", "Traditional Nursery Rhyme",
                "公有领域传统童谣 · L1", "POEM", "A2", "L1",
                "牧羊女 Bo-Peep 弄丢了小羊，别急着找，它们自己会回家。",
                List.of(
                        Map.of("p", "Little Bo-Peep has lost her sheep,"),
                        Map.of("p", "And doesn't know where to find them;"),
                        Map.of("p", "Leave them alone, and they'll come home,"),
                        Map.of("p", "Bringing their tails behind them.")),
                List.of(
                        Map.of("word", "sheep", "phonetic", "/ʃiːp/", "meaning", "绵羊"),
                        Map.of("word", "lose", "phonetic", "/luːz/", "meaning", "丢失"),
                        Map.of("word", "tail", "phonetic", "/teɪl/", "meaning", "尾巴")));

        // L2 / B1 William Blake, The Lamb（Songs of Innocence, 1789，公有领域）
        save("art-poem-lamb", "The Lamb", "William Blake, 1789",
                "公有领域诗歌 · L2", "POEM", "B1", "L2",
                "布莱克《天真之歌》名篇：追问羔羊是谁创造，温柔而虔诚。",
                List.of(
                        Map.of("p", "Little Lamb, who made thee?"),
                        Map.of("p", "Dost thou know who made thee,"),
                        Map.of("p", "Gave thee life and bid thee feed,"),
                        Map.of("p", "By the stream and o'er the mead;")),
                List.of(
                        Map.of("word", "lamb", "phonetic", "/læm/", "meaning", "羔羊"),
                        Map.of("word", "stream", "phonetic", "/striːm/", "meaning", "小溪；溪流"),
                        Map.of("word", "mead", "phonetic", "/miːd/", "meaning", "草地（诗用语）")));

        // L3 / B2 William Blake, The Tyger（Songs of Experience, 1794，公有领域）
        save("art-poem-tyger", "The Tyger", "William Blake, 1794",
                "公有领域诗歌 · L3", "POEM", "B2", "L3",
                "布莱克《经验之歌》名篇：燃烧般的老虎，惊叹造物的可怕与壮美。",
                List.of(
                        Map.of("p", "Tyger, Tyger, burning bright,"),
                        Map.of("p", "In the forests of the night;"),
                        Map.of("p", "What immortal hand or eye,"),
                        Map.of("p", "Could frame thy fearful symmetry?")),
                List.of(
                        Map.of("word", "tyger", "phonetic", "/ˈtaɪɡə/", "meaning", "老虎（古拼 tyger）"),
                        Map.of("word", "burn", "phonetic", "/bɜːn/", "meaning", "燃烧"),
                        Map.of("word", "symmetry", "phonetic", "/ˈsɪmətri/", "meaning", "对称；匀称")));

        // L4 / C1 Shakespeare, Sonnet 18 节选（公有领域）
        save("art-poem-sonnet", "Sonnet 18 (Excerpt)", "William Shakespeare",
                "公有领域十四行诗 · L4", "POEM", "C1", "L4",
                "莎士比亚第十八首十四行诗开篇：能否把你比作夏日？你比夏日更可爱温婉。",
                List.of(
                        Map.of("p", "Shall I compare thee to a summer's day?"),
                        Map.of("p", "Thou art more lovely and more temperate:"),
                        Map.of("p", "Rough winds do shake the darling buds of May,"),
                        Map.of("p", "And summer's lease hath all too short a date;")),
                List.of(
                        Map.of("word", "compare", "phonetic", "/kəmˈpeə/", "meaning", "比较；比作"),
                        Map.of("word", "temperate", "phonetic", "/ˈtempərət/", "meaning", "温和的；有节制的"),
                        Map.of("word", "lease", "phonetic", "/liːs/", "meaning", "租期；（此处指）持续期")));
    }

    private void save(String articleId, String title, String author, String source, String category,
                      String cefr, String level, String summary,
                      List<Map<String, Object>> paragraphs, List<Map<String, Object>> vocab) {
        save(articleId, title, author, source, category, cefr, level, summary, paragraphs, vocab,
                "FULL_TEXT", null);
    }

    private void save(String articleId, String title, String author, String source, String category,
                      String cefr, String level, String summary,
                      List<Map<String, Object>> paragraphs, List<Map<String, Object>> vocab,
                      String accessMode, String sourceUrl) {
        Article a = new Article();
        a.setArticleId(articleId);
        a.setTitle(title);
        a.setAuthor(author);
        a.setSource(source);
        a.setCategory(category);
        a.setCefr(cefr);
        a.setLevel(level);
        a.setSummary(summary);
        a.setAccessMode(accessMode);
        a.setSourceUrl(sourceUrl);
        try {
            a.setContentJson(mapper.writeValueAsString(paragraphs));
            a.setVocabJson(mapper.writeValueAsString(vocab));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        a.setReviewStatus("ONLINE");
        repository.save(a);
    }
}
