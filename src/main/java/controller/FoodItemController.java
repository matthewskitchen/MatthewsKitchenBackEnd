package controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dbmodel.FoodItem;
import repository.FoodItemRepository;

import java.util.List;

@RestController
@RequestMapping("/api")
public class FoodItemController {

    @Autowired
    private FoodItemRepository foodItemRepository;
    
    @GetMapping("/food-items")
    public List<FoodItem> getAllFoodItems() {
        return foodItemRepository.findAll();
    }
    
    @PostMapping("/admin/food-items")
    public FoodItem createFoodItem(@RequestBody FoodItem foodItem) {
        return foodItemRepository.save(foodItem);
    }
    
    @DeleteMapping("/admin/food-items/{id}")
    public ResponseEntity<Void> deleteFoodItem(@PathVariable Integer id) {
        if (!foodItemRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        foodItemRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
    
    @PutMapping("/admin/food-items/{id}")
    public ResponseEntity<FoodItem> updateFoodItem(@PathVariable Integer id, @RequestBody FoodItem foodItemDetails) {
        return foodItemRepository.findById(id)
            .map(existingItem -> {
                existingItem.setName(foodItemDetails.getName());
                existingItem.setDescription(foodItemDetails.getDescription());
                existingItem.setPrice(foodItemDetails.getPrice());
                existingItem.setImageUrl(foodItemDetails.getImageUrl());
                existingItem.setVeg(foodItemDetails.getVeg());
                existingItem.setInventory(foodItemDetails.getInventory());
                FoodItem updatedItem = foodItemRepository.save(existingItem);
                return ResponseEntity.ok(updatedItem);
            })
            .orElse(ResponseEntity.notFound().build());
    }
}