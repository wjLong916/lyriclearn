package com.lyriclearn.learningpath.service;

import com.lyriclearn.common.BizException;
import com.lyriclearn.learningpath.entity.LearningPathEntity;
import com.lyriclearn.learningpath.entity.LearningPathItemEntity;
import com.lyriclearn.learningpath.model.LearningPath;
import com.lyriclearn.learningpath.model.PathItem;
import com.lyriclearn.learningpath.repo.LearningPathItemRepository;
import com.lyriclearn.learningpath.repo.LearningPathRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 混合学习路径服务：管理员可编排、定价、上下架；学员仅见已发布。
 * 启动时写入三条预置路径（表为空时）。
 */
@Service
public class LearningPathService implements CommandLineRunner {

    private final LearningPathRepository pathRepository;
    private final LearningPathItemRepository itemRepository;

    public LearningPathService(LearningPathRepository pathRepository,
                                LearningPathItemRepository itemRepository) {
        this.pathRepository = pathRepository;
        this.itemRepository = itemRepository;
    }

    @Override
    public void run(String... args) {
        seedIfEmpty();
    }

    public List<LearningPath> listPublished() {
        return pathRepository.findByPublishedOrderBySortOrderAsc("ON").stream()
                .map(this::toDto)
                .toList();
    }

    public List<LearningPath> listAll() {
        return pathRepository.findAllByOrderBySortOrderAsc().stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public LearningPath create(Map<String, Object> req) {
        String pathId = (String) req.getOrDefault("pathId", "lp-" + System.currentTimeMillis());
        if (pathRepository.findByPathId(pathId).isPresent()) {
            throw new BizException(400, "路径 ID 已存在：" + pathId);
        }
        LearningPathEntity e = buildEntity(pathId, req);
        e = pathRepository.save(e);
        saveItems(pathId, (List<Map<String, Object>>) req.getOrDefault("items", List.of()));
        return toDto(e);
    }

    @Transactional
    public LearningPath update(String pathId, Map<String, Object> req) {
        LearningPathEntity e = pathRepository.findByPathId(pathId)
                .orElseThrow(() -> new BizException(404, "路径不存在：" + pathId));
        if (req.get("title") != null) e.setTitle((String) req.get("title"));
        if (req.get("description") != null) e.setDescription((String) req.get("description"));
        if (req.get("cefr") != null) e.setCefr((String) req.get("cefr"));
        if (req.get("level") != null) e.setLevel((String) req.get("level"));
        if (req.get("priceCredits") != null) e.setPriceCredits(((Number) req.get("priceCredits")).intValue());
        if (req.get("published") != null) e.setPublished(Boolean.TRUE.equals(req.get("published")) ? "ON" : "OFF");
        if (req.get("sortOrder") != null) e.setSortOrder(((Number) req.get("sortOrder")).intValue());
        e = pathRepository.save(e);
        if (req.get("items") != null) {
            itemRepository.deleteByPathId(pathId);
            saveItems(pathId, (List<Map<String, Object>>) req.get("items"));
        }
        return toDto(e);
    }

    @Transactional
    public void delete(String pathId) {
        pathRepository.findByPathId(pathId).orElseThrow(() -> new BizException(404, "路径不存在：" + pathId));
        itemRepository.deleteByPathId(pathId);
        pathRepository.deleteByPathId(pathId);
    }

    private LearningPathEntity buildEntity(String pathId, Map<String, Object> req) {
        LearningPathEntity e = new LearningPathEntity();
        e.setPathId(pathId);
        e.setTitle((String) req.get("title"));
        e.setDescription((String) req.get("description"));
        e.setCefr((String) req.get("cefr"));
        e.setLevel((String) req.get("level"));
        e.setPriceCredits(req.get("priceCredits") == null ? 0 : ((Number) req.get("priceCredits")).intValue());
        e.setPublished(Boolean.FALSE.equals(req.get("published")) ? "OFF" : "ON");
        e.setSortOrder(req.get("sortOrder") == null ? 0 : ((Number) req.get("sortOrder")).intValue());
        return e;
    }

    private void saveItems(String pathId, List<Map<String, Object>> items) {
        int seq = 0;
        for (Map<String, Object> it : items) {
            LearningPathItemEntity row = new LearningPathItemEntity();
            row.setPathId(pathId);
            row.setItemType((String) it.get("type"));
            row.setRef((String) it.get("ref"));
            row.setTitle((String) it.get("title"));
            row.setCefr((String) it.get("cefr"));
            row.setSeq(seq++);
            itemRepository.save(row);
        }
    }

    private LearningPath toDto(LearningPathEntity e) {
        List<PathItem> items = itemRepository.findByPathIdOrderBySeqAsc(e.getPathId()).stream()
                .map(i -> new PathItem(i.getItemType(), i.getRef(), i.getTitle(), i.getCefr()))
                .toList();
        return new LearningPath(e.getPathId(), e.getTitle(), e.getDescription(),
                e.getCefr(), e.getLevel(), e.getPriceCredits(), items, "ON".equals(e.getPublished()));
    }

    private void seedIfEmpty() {
        if (pathRepository.count() > 0) return;
        create(Map.of(
                "pathId", "lp-first-steps", "title", "启蒙入门包（歌曲+童谣）",
                "description", "A1 起步：一首金曲配两首传统童谣，跟唱跟读，建立音感与高频词。",
                "cefr", "A1", "level", "L0", "priceCredits", 50, "published", true, "sortOrder", 1,
                "items", List.of(
                        Map.of("type", "SONG", "ref", "qqmusic:twinkle", "title", "Twinkle Twinkle Little Star", "cefr", "A1"),
                        Map.of("type", "ARTICLE", "ref", "art-poem-diddle", "title", "Hey Diddle Diddle", "cefr", "A1"),
                        Map.of("type", "ARTICLE", "ref", "art-poem-sheep", "title", "Little Bo-Peep", "cefr", "A2"))));
        create(Map.of(
                "pathId", "lp-news-speech", "title", "新闻与演说进阶包",
                "description", "B1 进阶：生活新闻短文 + 公有领域演说，配合唱一首歌，训练主旨与观点表达。",
                "cefr", "B1", "level", "L2", "priceCredits", 90, "published", true, "sortOrder", 2,
                "items", List.of(
                        Map.of("type", "ARTICLE", "ref", "art-city", "title", "Walking in the City", "cefr", "B1"),
                        Map.of("type", "ARTICLE", "ref", "art-getty", "title", "The Gettysburg Address (Excerpt)", "cefr", "B1"),
                        Map.of("type", "SONG", "ref", "qqmusic:letitbe", "title", "Let It Be", "cefr", "B1"))));
        create(Map.of(
                "pathId", "lp-literary", "title", "文学抒情提升包",
                "description", "B2 提升：励志短文 + 布莱克名诗 + 一首抒情歌，体会修辞与情感表达。",
                "cefr", "B2", "level", "L3", "priceCredits", 120, "published", true, "sortOrder", 3,
                "items", List.of(
                        Map.of("type", "ARTICLE", "ref", "art-dream", "title", "Hold on to Your Dream", "cefr", "B2"),
                        Map.of("type", "ARTICLE", "ref", "art-poem-tyger", "title", "The Tyger", "cefr", "B2"),
                        Map.of("type", "SONG", "ref", "qqmusic:yesterday", "title", "Yesterday", "cefr", "B2"))));
    }
}
