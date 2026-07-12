package com.surplusfood.marketplace.repository;

import com.surplusfood.marketplace.entity.Wishlist;
import com.surplusfood.marketplace.entity.WishlistId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, WishlistId> {
    List<Wishlist> findByUserId(Long userId);
    List<Wishlist> findByBusinessId(Long businessId);
    boolean existsByIdUserIdAndIdBusinessId(Long userId, Long businessId);
}
