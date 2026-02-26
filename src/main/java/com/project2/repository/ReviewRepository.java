package com.project2.repository;

import com.project2.model.Review;
import com.project2.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByReviewee(User reviewee);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.reviewee = :user")
    Double findAverageRatingByReviewee(User user);
}
