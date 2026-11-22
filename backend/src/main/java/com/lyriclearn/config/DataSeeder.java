package com.lyriclearn.config;

import com.lyriclearn.annotation.entity.SongAnnotation;
import com.lyriclearn.annotation.repo.SongAnnotationRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/** 种子标注数据：启动时若标注库为空则写入首批可学习歌曲（仅元信息与增强标注，不含歌词）。 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final SongAnnotationRepository repository;

    public DataSeeder(SongAnnotationRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        if (repository.count() > 0) return;
        seed("qqmusic", "twinkle", "Twinkle Twinkle Little Star", "Nursery Rhyme",
                "https://cover.example.com/twinkle.png", "L0", "A1");
        seed("qqmusic", "lemon", "Lemon Tree", "Fool's Garden",
                "https://cover.example.com/lemon.png", "L1", "A2");
        seed("qqmusic", "letitbe", "Let It Be", "The Beatles",
                "https://cover.example.com/letitbe.png", "L2", "B1");
        seed("qqmusic", "yesterday", "Yesterday", "The Beatles",
                "https://cover.example.com/yesterday.png", "L3", "B2");
    }

    private void seed(String platform, String songId, String title, String artist,
                      String cover, String level, String cefr) {
        SongAnnotation a = new SongAnnotation();
        a.setPlatform(platform);
        a.setSongId(songId);
        a.setTitle(title);
        a.setArtist(artist);
        a.setCover(cover);
        a.setDifficultyLevel(level);
        a.setCefrLevel(cefr);
        a.setVersion(1);
        a.setReviewStatus("ONLINE");
        a.setLearnable(true);
        repository.save(a);
    }
}
