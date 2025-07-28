package com.secure.prints.database;

import com.secure.prints.database.entity.InvoiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface InvoiceRepository extends JpaRepository<InvoiceEntity, Long> {

}
