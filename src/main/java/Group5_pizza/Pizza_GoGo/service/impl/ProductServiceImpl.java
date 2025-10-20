package Group5_pizza.Pizza_GoGo.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import Group5_pizza.Pizza_GoGo.model.Product;
import Group5_pizza.Pizza_GoGo.repository.ProductRepository;
import Group5_pizza.Pizza_GoGo.service.ProductService;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository repository;

    public ProductServiceImpl(ProductRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Product> getAllAvailableProducts() {
        return repository.findByIsDeletedFalse();
    }

    @Override
    public List<Product> getAllProducts() {
        return repository.findAll();
    }

    @Override
    public Product getProductById(Integer id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public Product saveProduct(Product product) {
        return repository.save(product);
    }

    @Override
    public void deleteProduct(Integer id) {
        repository.deleteById(id);
    }

    @Override
    public List<Product> searchProductsByName(String name) {
        return repository.findByNameContainingIgnoreCase(name);
    }

    @Override
    public List<Product> searchProductsByCategory(Integer categoryId) {
        return repository.findByCategory_CategoryId(categoryId);
    }

    @Override
    public List<Product> searchProductsByNameAndCategory(String name, Integer categoryId) {
        return repository.findByNameContainingIgnoreCaseAndCategory_CategoryId(name, categoryId);
    }

    @Override
    public List<Product> searchProducts(String name, Integer categoryId) {
        if ((name == null || name.isEmpty()) && categoryId == null) {
            return repository.findAll();
        } else if (name != null && !name.isEmpty() && categoryId != null) {
            return repository.findByNameContainingIgnoreCaseAndCategory_CategoryId(name, categoryId);
        } else if (name != null && !name.isEmpty()) {
            return repository.findByNameContainingIgnoreCase(name);
        } else {
            return repository.findByCategory_CategoryId(categoryId);
        }
    }

}