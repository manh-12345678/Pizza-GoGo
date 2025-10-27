package Group5_pizza.Pizza_GoGo.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import Group5_pizza.Pizza_GoGo.model.Product;

public class ProductValidator {
    public static List<String> validate(Product product) {
        List<String> errors = new ArrayList<>();

        // 🔸 Tên món
        if (product.getName() == null || product.getName().trim().isEmpty()) {
            errors.add("Tên món không được để trống.");
        } else if (product.getName().length() > 100) {
            errors.add("Tên món không được vượt quá 100 ký tự.");
        }

        // 🔸 Mô tả
        if (product.getDescription() != null && product.getDescription().length() > 500) {
            errors.add("Mô tả không được vượt quá 500 ký tự.");
        }

        // 🔸 Giá
        if (product.getPrice() == null) {
            errors.add("Giá món không được để trống.");
        } else if (product.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            errors.add("Giá món phải lớn hơn 0.");
        }

        // 🔸 Ảnh (nếu bạn có field image)
        if (product.getImgUrl() != null && product.getImgUrl().length() > 255) {
            errors.add("Đường dẫn ảnh quá dài (tối đa 255 ký tự).");
        }

        // 🔸 Danh mục
        if (product.getCategory() == null) {
            errors.add("Vui lòng chọn danh mục cho món ăn.");
        }

        return errors;
    }
}