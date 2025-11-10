package Group5_pizza.Pizza_GoGo.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.List;

import Group5_pizza.Pizza_GoGo.model.RestaurantTable;
import Group5_pizza.Pizza_GoGo.repository.RestaurantTableRepository;
import Group5_pizza.Pizza_GoGo.service.RestaurantTableService;
import org.springframework.stereotype.Service;

import com.google.zxing.WriterException;

import Group5_pizza.Pizza_GoGo.model.RestaurantTable;
import Group5_pizza.Pizza_GoGo.repository.RestaurantTableRepository;
import Group5_pizza.Pizza_GoGo.service.RestaurantTableService;
import Group5_pizza.Pizza_GoGo.util.QRCodeGenerator;

@Service
public class RestaurantTableServiceImpl implements RestaurantTableService {

    private final RestaurantTableRepository tableRepository;

    public RestaurantTableServiceImpl(RestaurantTableRepository tableRepository) {
        this.tableRepository = tableRepository;
    }

    @Override
    public List<RestaurantTable> getAllTables() {
        return tableRepository.findAll();
    }

    @Override
    public RestaurantTable getTableById(Integer id) {
        return tableRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Table not found with id " + id));
    }

    /**
     * Thêm tham số baseUrl để sinh QR code động từ controller
     */
    @Override
    public RestaurantTable saveTable(RestaurantTable table, String baseUrl) {
        boolean isNew = table.getTableId() == null;

        // Kiểm tra trùng số bàn
        tableRepository.findByTableNumber(table.getTableNumber()).ifPresent(existing -> {
            if (isNew || !existing.getTableId().equals(table.getTableId())) {
                throw new RuntimeException("Số bàn " + table.getTableNumber() + " đã tồn tại!");
            }
        });

        RestaurantTable savedTable = tableRepository.save(table);

        if (isNew) {
            // Đường dẫn folder chứa QR
            String qrFolderPath = "src/main/resources/static/qrcodes";
            File qrFolder = new File(qrFolderPath);
            if (!qrFolder.exists()) {
                qrFolder.mkdirs(); // Tạo folder nếu chưa có
            }

            String qrText;
            if (baseUrl != null) {
                qrText = baseUrl + "/order/table/" + table.getTableNumber();
            } else {
                qrText = "/order/table/" + table.getTableNumber(); // Dùng đường dẫn tương đối nếu baseUrl null
            }

            String qrFilePath = qrFolderPath + "/table-" + table.getTableNumber() + ".png";

            try {
                QRCodeGenerator.generateQRCodeImage(qrText, qrFilePath, 250, 250);
                table.setQrCodeUrl("/qrcodes/table-" + table.getTableNumber() + ".png");
            } catch (WriterException | IOException e) {
                e.printStackTrace();
                table.setQrCodeUrl(null);
            }
        }

        return tableRepository.save(table);
    }

    @Override
    public void deleteTable(Integer id) {
        if (!tableRepository.existsById(id)) {
            throw new RuntimeException("Table not found with id " + id);
        }
        tableRepository.deleteById(id);
    }

}
