package com.creatix.domain.dao;

import com.creatix.domain.entity.store.Property;
import com.creatix.domain.entity.store.MaintenanceSlotSchedule;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.creatix.domain.entity.store.QMaintenanceSlotSchedule.maintenanceSlotSchedule;

@Repository
@Transactional
public class MaintenanceSlotScheduleDao extends DaoBase<MaintenanceSlotSchedule, Long> {

    public List<MaintenanceSlotSchedule> findAll() {
        return queryFactory.selectFrom(maintenanceSlotSchedule).fetch();
    }

    public List<MaintenanceSlotSchedule> findByProperty(Property property) {
        return queryFactory.selectFrom(maintenanceSlotSchedule)
                .where(maintenanceSlotSchedule.property.eq(property))
                .fetch();
    }

}
