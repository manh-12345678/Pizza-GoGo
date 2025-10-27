package Group5_pizza.Pizza_GoGo.service.impl;

import Group5_pizza.Pizza_GoGo.model.Topping;
import Group5_pizza.Pizza_GoGo.repository.ToppingRepository;
import Group5_pizza.Pizza_GoGo.service.ToppingService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ToppingServiceImpl implements ToppingService {

    private final ToppingRepository toppingRepository;

    public ToppingServiceImpl(ToppingRepository toppingRepository) {
        this.toppingRepository = toppingRepository;
    }

    @Override
    public List<Topping> getAll() {
        return toppingRepository.findByIsDeletedFalse();
    }

    @Override
    public Topping getById(Integer id) {
        return toppingRepository.findById(id)
                .filter(t -> !Boolean.TRUE.equals(t.getIsDeleted()))
                .orElse(null);
    }

    @Override
    public Topping create(Topping topping) {
        topping.setIsDeleted(false);
        topping.setCreatedAt(LocalDateTime.now());
        topping.setUpdatedAt(LocalDateTime.now());
        return toppingRepository.save(topping);
    }

    @Override
    public Topping update(Integer id, Topping topping) {
        Topping existing = getById(id);
        if (existing == null) return null;

        existing.setName(topping.getName());
        existing.setPrice(topping.getPrice());
        existing.setUpdatedAt(LocalDateTime.now());

        return toppingRepository.save(existing);
    }

    @Override
    public void softDelete(Integer id) {
        Topping existing = getById(id);
        if (existing != null) {
            existing.setIsDeleted(true);
            existing.setUpdatedAt(LocalDateTime.now());
            toppingRepository.save(existing);
        }
    }

    @Override
    public List<Topping> searchByName(String name) {
        return toppingRepository.findByNameContainingIgnoreCaseAndIsDeletedFalse(name);
    }
}