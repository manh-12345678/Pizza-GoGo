package Group5_pizza.Pizza_GoGo.service;

import java.util.List;

import Group5_pizza.Pizza_GoGo.model.Product;

public interface ProductService {
    List<Product> getAllProducts();

    Product getProductById(Integer id);

    Product saveProduct(Product product);

    void deleteProduct(Integer id);

    List<Product> searchProducts(String name, Integer categoryId);
    List<Product> getAllAvailableProducts();


    List<Product> searchProductsByName(String name);

    List<Product> searchProductsByCategory(Integer categoryId);

    List<Product> searchProductsByNameAndCategory(String name, Integer categoryId);

}