package com.xxwn.pitchfeed.domain.feed.repository;

import com.xxwn.pitchfeed.domain.feed.entity.Feed;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeedRepository extends JpaRepository<Feed, Long> {

    List<Feed> findAllByActiveTrue();
}
