package com.sburkett.toolrentalapp.db.repository;

import com.sburkett.toolrentalapp.db.entity.ToolPricesDao;
import org.springframework.data.repository.Repository;

public interface ToolPricesRepository extends Repository<ToolPricesDao, String> {
    ToolPricesDao findByToolType(String toolName);
}
