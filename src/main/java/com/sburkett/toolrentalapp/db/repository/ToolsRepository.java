package com.sburkett.toolrentalapp.db.repository;

import com.sburkett.toolrentalapp.db.entity.ToolsDao;
import org.springframework.data.repository.Repository;

public interface ToolsRepository extends Repository<ToolsDao, String> {
    ToolsDao findByToolCode(String toolCode);
}