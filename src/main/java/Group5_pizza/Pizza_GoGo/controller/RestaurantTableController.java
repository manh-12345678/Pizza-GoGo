package Group5_pizza.Pizza_GoGo.controller;

import Group5_pizza.Pizza_GoGo.model.RestaurantTable;
import Group5_pizza.Pizza_GoGo.service.RestaurantTableService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/tables")
public class RestaurantTableController {
    private final RestaurantTableService restaurantTableService;

    public RestaurantTableController(RestaurantTableService _restaurantTableService) {
        this.restaurantTableService = _restaurantTableService;
    }

    @GetMapping
    public String getAllTables(Model model) {
        try {
            model.addAttribute("tables", restaurantTableService.getAllTables());
        } catch (Exception e) {
            model.addAttribute("error", "Error loading tables");
        }
        return "tables/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("table", new RestaurantTable());
        return "tables/form";
    }

    @GetMapping("/{id}")
    public String getTableById(@PathVariable Integer id, Model model) {
        try {
            model.addAttribute("table", restaurantTableService.getTableById(id));
            return "tables/view";
        } catch (Exception e) {
            return "redirect:/tables";
        }
    }

    @PostMapping
    public String createTable(@ModelAttribute RestaurantTable table, RedirectAttributes redirectAttributes) {
        try {
            restaurantTableService.saveTable(table);
            redirectAttributes.addFlashAttribute("success", "Table created successfully!");
            return "redirect:/tables";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error creating table");
            return "redirect:/tables/new";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model) {
        try {
            model.addAttribute("table", restaurantTableService.getTableById(id));
            return "tables/form";
        } catch (Exception e) {
            return "redirect:/tables";
        }
    }

    @PostMapping("/{id}")
    public String updateTable(@PathVariable Integer id, @ModelAttribute RestaurantTable table, RedirectAttributes redirectAttributes) {
        try {
            table.setTableId(id);
            restaurantTableService.saveTable(table);
            redirectAttributes.addFlashAttribute("success", "Table updated successfully!");
            return "redirect:/tables";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating table");
            return "redirect:/tables/edit/" + id;
        }
    }

    @GetMapping("/delete/{id}")
    public String deleteTable(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            restaurantTableService.deleteTable(id);
            redirectAttributes.addFlashAttribute("success", "Table deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting table");
        }
        return "redirect:/tables";
    }
}
