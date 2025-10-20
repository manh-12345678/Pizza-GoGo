package Group5_pizza.Pizza_GoGo.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import Group5_pizza.Pizza_GoGo.model.Category;
import Group5_pizza.Pizza_GoGo.repository.CategoryRepository;
import Group5_pizza.Pizza_GoGo.repository.ProductRepository;
import Group5_pizza.Pizza_GoGo.service.CategoryService;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository, ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Override
    public List<Category> searchCategories(String keyword) {
        return categoryRepository.findByCategoryNameContainingIgnoreCase(keyword);
    }

    @Override
    public Category getCategoryById(Integer id) {
        return categoryRepository.findById(id).orElse(null);
    }

    @Override
    public Category saveCategory(Category category) {
        return categoryRepository.save(category);
    }

    @Override
    @Transactional
    public boolean deleteCategory(Integer id) {
        long productCount = productRepository.countByCategory_CategoryId(id);

        if (productCount > 0) {
            return false;
        }

        categoryRepository.deleteById(id);
        return true;
    }
}
