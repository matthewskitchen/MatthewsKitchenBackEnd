package repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import dbmodel.FoodItem;

@Repository
public interface FoodItemRepository extends JpaRepository<FoodItem, Integer> {
    FoodItem findByName(String name);
}