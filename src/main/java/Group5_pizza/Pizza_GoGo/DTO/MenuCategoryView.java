package Group5_pizza.Pizza_GoGo.DTO;

import java.util.List;

import Group5_pizza.Pizza_GoGo.model.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuCategoryView {
    private String name;
    private String slug;
    private List<Product> products;
}

