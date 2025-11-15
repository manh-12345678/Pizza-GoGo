package Group5_pizza.Pizza_GoGo.service;

import Group5_pizza.Pizza_GoGo.model.Order;

/**
 * Interface quản lý logic kho hàng
 */
public interface InventoryService {

    /**
     * Trừ kho nguyên vật liệu dựa trên các sản phẩm trong một đơn hàng.
     * Ném Exception nếu không đủ hàng.
     * @param order Đơn hàng đã hoàn thành (cần eager load OrderDetails)
     */
    void deductIngredientsForOrder(Order order);

    /**
     * Hoàn lại kho (ví dụ: đơn hàng bị hủy)
     * @param order Đơn hàng bị hủy (cần eager load OrderDetails)
     */
    void restockIngredientsForCancelledOrder(Order order);
}