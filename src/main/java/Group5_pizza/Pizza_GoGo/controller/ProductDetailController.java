package Group5_pizza.Pizza_GoGo.controller;

import Group5_pizza.Pizza_GoGo.DTO.ProductDTO;
import Group5_pizza.Pizza_GoGo.DTO.ReviewView;
import Group5_pizza.Pizza_GoGo.service.ProductService;
import Group5_pizza.Pizza_GoGo.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/product")
@RequiredArgsConstructor
public class ProductDetailController {

    private final ProductService productService;
    private final ReviewService reviewService;

    @GetMapping("/{productId}")
    public String showProductDetail(@PathVariable Integer productId,
                                    Model model,
                                    RedirectAttributes redirectAttributes) {
        ProductDTO product;
        try {
            product = productService.getProductDTOById(productId);
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Sản phẩm không tồn tại hoặc đã bị ẩn.");
            return "redirect:/menu";
        }

        List<ReviewView> reviews = reviewService.getPublishedReviewsForProduct(productId);
        double averageRating = reviews.stream()
                .filter(Objects::nonNull)
                .mapToInt(ReviewView::getRating)
                .average()
                .orElse(0.0);
        int reviewCount = reviews.size();

        Map<Integer, Long> ratingDistribution = IntStream.rangeClosed(1, 5)
                .boxed()
                .sorted((a, b) -> b - a) // descending 5 -> 1
                .collect(Collectors.toMap(
                        rating -> rating,
                        rating -> reviews.stream()
                                .filter(review -> Objects.equals(review.getRating(), rating))
                                .count(),
                        (existing, ignored) -> existing,
                        LinkedHashMap::new
                ));

        model.addAttribute("product", product);
        model.addAttribute("reviews", reviews);
        model.addAttribute("averageRating", averageRating);
        model.addAttribute("reviewCount", reviewCount);
        model.addAttribute("ratingDistribution", ratingDistribution);
        model.addAttribute("hasReviews", !CollectionUtils.isEmpty(reviews));

        return "product/detail";
    }
}


