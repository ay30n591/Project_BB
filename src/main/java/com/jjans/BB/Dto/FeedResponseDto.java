package com.jjans.BB.Dto;

import com.jjans.BB.Entity.Comment;
import com.jjans.BB.Entity.Feed;
import com.jjans.BB.Entity.HashTag;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class FeedResponseDto{

    private Long id;
    private String contents;
    private int feedLike;
    private String imageFileUrl;
    private Long userId;

    private String musicArtist;
    private String releaseDate;
    private String musicTitle;
    private String albumName;
    private String userName;
    private String vedioId;
    private String tagName;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private List<CommentResponseDto> comments;

    public FeedResponseDto(Feed feed) {
        this.id = feed.getId();
        this.contents = feed.getContent();
        this.feedLike = feed.getFeedLike();
        this.imageFileUrl = feed.getImageUrl();  // 수정된 부분
        this.userId = feed.getUser().getId();
        this.userName = feed.getUser().getUserName();
        this.vedioId = feed.getVideoId();
        this.createdAt = feed.getCreateDate();
        this.modifiedAt = feed.getModifiedDate();
        this.releaseDate = feed.getReleaseDate();
        this.musicTitle = feed.getMusicTitle();
        this.albumName = feed.getAlbumName();
        this.musicArtist = feed.getMusicArtist();
        // Set<HashTag>에서 각 HashTag의 tagName을 추출하여 List<String>으로 변환
        List<String> tagNames = feed.getHashTags().stream()
                .map(HashTag::getTagName)
                .collect(Collectors.toList());
        // List<String>을 쉼표로 구분된 하나의 문자열로 결합
        this.tagName = String.join(", ", tagNames);

        List<Comment> comments = feed.getComments();
        if (comments != null) {
            this.comments = comments.stream().map(CommentResponseDto::new).collect(Collectors.toList());
        } else {
            this.comments = Collections.emptyList();
        }    }
}
