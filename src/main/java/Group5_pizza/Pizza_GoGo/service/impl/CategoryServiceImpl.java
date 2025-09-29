package Group5_pizza.Pizza_GoGo.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import Group5_pizza.Pizza_GoGo.model.Category;
import Group5_pizza.Pizza_GoGo.repository.CategoryRepository;
import Group5_pizza.Pizza_GoGo.service.CategoryService;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository repository;

    public CategoryServiceImpl(CategoryRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Category> getAllCategories() {
        return repository.findAll();
    }

    @Override
    public Category getCategoryById(Integer id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public Category saveCategory(Category category) {
        return repository.save(category);
    }

    @Override
    public void deleteCategory(Integer id) {
        repository.deleteById(id);
    }
}