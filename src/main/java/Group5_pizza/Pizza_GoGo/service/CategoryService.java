package Group5_pizza.Pizza_GoGo.service;

import java.util.List;

import Group5_pizza.Pizza_GoGo.model.Category;

public interface CategoryService {
    List<Category> getAllCategories();

    Category getCategoryById(Integer id);

    Category saveCategory(Category category);
    void deleteCategory(Integer id);
}