package com.sburkett.toolrentalapp.db.repository;

import com.sburkett.toolrentalapp.db.entity.ToolEntity;
import org.springframework.data.repository.Repository;

public interface ToolRepository extends Repository<ToolEntity, String> {
    ToolEntity findByToolCode(String toolCode);
}