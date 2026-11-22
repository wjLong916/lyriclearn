package com.lyriclearn.annotation.service;

import com.lyriclearn.annotation.entity.SongAnnotation;
import com.lyriclearn.annotation.repo.SongAnnotationRepository;
import com.lyriclearn.common.BizException;
import com.lyriclearn.gateway.MockMusicGateway;
import com.lyriclearn.omniedu.OmniEduAdapter;
import com.lyriclearn.omniedu.OmniEduDto.ParsePackage;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 学习标注服务（架构 6.1）：标注读取、版本管理、歌词与标注内存对齐。
 * 原版歌词仅在本次请求内存中与标注对齐后下发渲染，页面关闭即销毁，绝不落库。
 */
@Service
public class AnnotationService {

    private final SongAnnotationRepository annotationRepository;
    private final MockMusicGateway musicGateway;
    private final OmniEduAdapter omniEdu;

    public AnnotationService(SongAnnotationRepository annotationRepository,
                             MockMusicGateway musicGateway,
                             OmniEduAdapter omniEdu) {
        this.annotationRepository = annotationRepository;
        this.musicGateway = musicGateway;
        this.omniEdu = omniEdu;
    }

    /** 歌曲详情与标注对齐：并行取元信息、内存歌词与解析包，一次下发渲染结构。 */
    public Map<String, Object> detail(String platform, String songId) {
        List<MockMusicGateway.LyricLine> lyrics = musicGateway.fetchLyric(platform, songId);
        SongAnnotation ann = annotationRepository.findByPlatformAndSongId(platform, songId).orElse(null);

        String title = ann != null ? ann.getTitle() : songId;
        String artist = ann != null ? ann.getArtist() : "Unknown Artist";
        String cefr = ann != null ? ann.getCefrLevel() : "A2";
        boolean learnable = ann != null && ann.isLearnable() && "ONLINE".equals(ann.getReviewStatus());

        ParsePackage pkg = omniEdu.parse(songId, cefr, "v1");

        Map<String, Object> result = new HashMap<>();
        result.put("platform", platform);
        result.put("songId", songId);
        result.put("title", title);
        result.put("artist", artist);
        result.put("cover", ann != null ? ann.getCover() : "");
        result.put("cefr", cefr);
        result.put("level", ann != null ? ann.getDifficultyLevel() : "L1");
        result.put("learnable", learnable);
        result.put("playUrl", musicGateway.playUrl(platform, songId));
        result.put("vocab", pkg.vocab());
        result.put("lines", lyrics.stream().map(l -> Map.of(
                "start", l.start(),
                "original", l.original(),
                "translation", l.translation())).toList());
        return result;
    }

    /** 检索：聚合第三方结果，标注库已上线者打可学习标记。 */
    public List<Map<String, Object>> search(String keyword, String level) {
        return musicGateway.search(keyword, level).stream().map(m -> {
            Map<String, Object> row = new HashMap<>();
            row.put("platform", m.platform());
            row.put("songId", m.songId());
            row.put("title", m.title());
            row.put("artist", m.artist());
            row.put("cover", m.cover());
            row.put("cefr", m.cefr());
            row.put("level", m.level());
            boolean annotated = annotationRepository
                    .findByPlatformAndSongId(m.platform(), m.songId())
                    .map(a -> a.isLearnable() && "ONLINE".equals(a.getReviewStatus()))
                    .orElse(false);
            row.put("learnable", annotated);
            return row;
        }).toList();
    }
}
